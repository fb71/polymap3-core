/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.lucenestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import java.io.IOException;

import org.geotools.data.Query;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.runtime.mp.ForEach;
import org.polymap.core.runtime.mp.Parallel;
import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 * <p/>
 * This implementation provides an immutable collection. All modification
 * methods throw an {@link UnsupportedOperationException}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneFeatureCollection
        implements FeatureCollection<FeatureType, Feature>, Iterable<Feature> {

    private static Log log = LogFactory.getLog( LuceneFeatureCollection.class );

    protected LuceneFeatureStore                store;

    protected org.apache.lucene.search.Query    luceneQuery;

    private ScoreDoc[]                          scoreDocs;

    /** The descriptor shared by all collections and features. */
    private AttributeDescriptor                 descriptor;
    
    
    public LuceneFeatureCollection( LuceneFeatureStore store, org.apache.lucene.search.Query luceneQuery, Query query )
    throws IOException {
        this.store = store;
        this.luceneQuery = luceneQuery;
        this.descriptor = new AttributeDescriptorImpl(
                getSchema(), getSchema().getName(), 0, Integer.MAX_VALUE, true, null );
        
        if (query.getStartIndex() != null && query.getStartIndex() > 0) {
            throw new UnsupportedOperationException( "query.getStartIndex() != 0: Paging is not supported yet.");
        }
        
        // execute Lucene query        
        IndexSearcher searcher = store.getRecordStore().getIndexSearcher();
        if (query.getSortBy() != null && query.getSortBy() != SortBy.UNSORTED) {
//            List<SortBy> sortBy = Arrays.asList( query.getSortBy() );
//            Sort sort = new Sort( Iterables.toArray( 
//                    transform( sortBy, new Function<SortBy,SortField>() {
//                        public SortField apply( SortBy from ) {
//                            return new SortField( from.getPropertyName().getPropertyName(), SortField.STRING );
//                        }
//                    }), SortField.class ) );

            // sort fields
            List<SortField> sortFields = new ArrayList();
            for (SortBy sortBy : query.getSortBy()) {
                sortFields.add( new SortField(sortBy.getPropertyName().getPropertyName(), SortField.STRING ) );
            }
            Sort sort = new Sort( Iterables.toArray( sortFields, SortField.class ) );
            // search
            TopDocs topDocs = searcher.search( luceneQuery, query.getMaxFeatures(), sort );
            scoreDocs = topDocs.scoreDocs;
        }
        else {
            TopDocs topDocs = searcher.search( luceneQuery, query.getMaxFeatures() );
            scoreDocs = topDocs.scoreDocs;
        }
    }


    public FeatureType getSchema() {
        return store.getSchema();
    }


    public String getID() {
        throw new RuntimeException( "not yet implemented." );
    }


    public void addListener( CollectionListener listener )
    throws NullPointerException {
        throw new RuntimeException( "not yet implemented." );
    }


    public void removeListener( CollectionListener listener )
    throws NullPointerException {
        throw new RuntimeException( "not yet implemented." );
    }


    public FeatureIterator<Feature> features() {
        return new LuceneFeatureIterator();
    }

    
    public Iterator<Feature> iterator() {
        return (Iterator<Feature>)features();
    }


    /*
     * 
     */
    class LuceneFeatureIterator
            implements FeatureIterator, Iterator {

        private int                 index;
        
        public boolean hasNext() {
            return index + 1 < scoreDocs.length;
        }

        public Feature next() throws NoSuchElementException {
            if (index >= scoreDocs.length) {
                throw new NoSuchElementException( "index=" + index + " >= " + scoreDocs.length );
            }
            try {
                IRecordState record = store.getRecordStore().get( scoreDocs[index++].doc );
                return new RecordFeature( record, descriptor );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }

        public void close() {
        }

        public void remove() {
            throw new UnsupportedOperationException( "Unsupported operation." );
        }
        
    }

    
    public boolean isEmpty() {
        return scoreDocs.length == 0;
    }

    public int size() {
        return scoreDocs.length;
    }

    public void close( FeatureIterator<Feature> close ) {
    }

    public void close( Iterator<Feature> close ) {
    }


    public void accepts( FeatureVisitor visitor, ProgressListener progress )
    throws IOException {
        progress = progress != null ? progress : new NullProgressListener();
        for (Feature feature : this) {
            try {
                visitor.visit( feature );

                progress.progress( 1 );
                if (progress.isCanceled()) {
                    return;
                }
            }
            catch (Throwable e) {
                progress.exceptionOccurred( e );
                if (e instanceof IOException) {
                    throw (IOException)e;
                }
                else if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                else {
                    throw new RuntimeException( e );
                }
            }
        }
    }


    public ReferencedEnvelope getBounds() {
        final GeometryDescriptor geomDescr = getSchema().getGeometryDescriptor();
        if (geomDescr == null) {
            throw new RuntimeException( "No Geometry descriptor in feature type." );
        }
        try {
            return getBounds1P( geomDescr );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    protected ReferencedEnvelope getBounds1P( GeometryDescriptor geomDescr )
    throws Exception {
        ReferencedEnvelope result = new ReferencedEnvelope( geomDescr.getCoordinateReferenceSystem() );
        for (ScoreDoc scoreDoc : scoreDocs) {
            IRecordState record = store.getRecordStore().get( scoreDoc.doc );
            
            Geometry geom = record.get( geomDescr.getLocalName() );
            result.expandToInclude( geom.getEnvelopeInternal() );
        }
        return result;        
    }
    
    
    protected ReferencedEnvelope getBoundsMP( final GeometryDescriptor geomDescr ) {
        final List<ReferencedEnvelope> results = Collections.synchronizedList( new ArrayList<ReferencedEnvelope>() );
        // parallel process partitions
        ForEach.in( Arrays.asList( scoreDocs ) ).doFirst(
                new Parallel<ScoreDoc,ScoreDoc>() {

                    ThreadLocal<ReferencedEnvelope> result = new ThreadLocal();

                    public ScoreDoc process( ScoreDoc input ) throws Exception {
                        if (result.get() == null) {
                            result.set( new ReferencedEnvelope( geomDescr.getCoordinateReferenceSystem() ) );
                            results.add( result.get() );
                        }
                        IRecordState record = store.getRecordStore().get( input.doc );
                        Geometry geom = record.get( geomDescr.getLocalName() );
                        result.get().expandToInclude( geom.getEnvelopeInternal() );
                        return input;
                    }
                }).start();
        // unify partitions
        ReferencedEnvelope result = new ReferencedEnvelope( geomDescr.getCoordinateReferenceSystem() );
        for (ReferencedEnvelope envelop : results) {
            result.expandToInclude( envelop );
        }
        return result;
    }

    
    public Object[] toArray() {
        return Iterables.toArray( this, Object.class );
    }


    public <O> O[] toArray( O[] a ) {
        return Lists.newArrayList( this ).toArray( a );
    }

    
    public boolean contains( Object o ) {
        return Iterables.contains( this, o );
    }


    public boolean containsAll( Collection<?> o ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    // unsupported ****************************************
    
    public void purge() {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public boolean add( Feature obj ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public boolean addAll( Collection<? extends Feature> collection ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public boolean addAll( FeatureCollection<? extends FeatureType, ? extends Feature> resource ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public void clear() {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }


    public boolean remove( Object o ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public boolean removeAll( Collection<?> c ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public boolean retainAll( Collection<?> c ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public FeatureCollection<FeatureType, Feature> subCollection( Filter filter ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }

    public FeatureCollection<FeatureType, Feature> sort( SortBy order ) {
        throw new UnsupportedOperationException( "Unsupported operation.");
    }
}
