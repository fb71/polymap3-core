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

import java.util.List;
import java.util.Set;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.net.URI;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 * @see FeatureStore
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneFeatureStore
        implements FeatureStore<FeatureType,Feature> {

    private static Log log = LogFactory.getLog( LuceneFeatureStore.class );

    private Name                    name;
    
    private FeatureType             schema;
    
    private LuceneRecordStore       store;
    
    
    public LuceneFeatureStore( LuceneRecordStore store, FeatureType schema ) {
        this.schema = schema;
        this.store = store;
        this.name = schema.getName();
    }


    public Name getName() {
        return name;
    }

    
    public FeatureType getSchema() {
        return schema;
    }

    
    public LuceneRecordStore getStore() {
        return store;
    }
    
    
    public ResourceInfo getInfo() {
        return new ResourceInfo() {
            public String getTitle() {
                return name.toString();
            }
            public URI getSchema() {
                throw new RuntimeException( "not yet implemented." );
            }
            public String getName() {
                return name.getLocalPart();
            }
            public Set<String> getKeywords() {
                throw new RuntimeException( "not yet implemented." );
            }
            public String getDescription() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public CoordinateReferenceSystem getCRS() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public ReferencedEnvelope getBounds() {
                try {
                    return LuceneFeatureStore.this.getBounds();
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }
        };
    }


    public QueryCapabilities getQueryCapabilities() {
        return new QueryCapabilities() {
        };
    }


    public FeatureCollection<FeatureType, Feature> getFeatures()
    throws IOException {
        return getFeatures( Filter.INCLUDE );
    }

    
    public FeatureCollection<FeatureType, Feature> getFeatures( Filter filter )
    throws IOException {
        return getFeatures( new Query( schema.getName().getLocalPart(), filter ) ); 
    }


    public FeatureCollection<FeatureType, Feature> getFeatures( Query query )
    throws IOException {
        org.apache.lucene.search.Query luceneQuery = LuceneQueryBuilder.query( store, query );
        return new LuceneFeatureCollection( this, luceneQuery, query ); 
    }

    
    public List<FeatureId> addFeatures( FeatureCollection<FeatureType, Feature> arg0 )
    throws IOException {
        throw new RuntimeException( "not yet implemented." );
    }

    
    @Override
    public Transaction getTransaction() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void modifyFeatures( Name[] arg0, Object[] arg1, Filter arg2 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void modifyFeatures( AttributeDescriptor[] arg0, Object[] arg1, Filter arg2 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void modifyFeatures( Name arg0, Object arg1, Filter arg2 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void modifyFeatures( AttributeDescriptor arg0, Object arg1, Filter arg2 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void removeFeatures( Filter arg0 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setFeatures( FeatureReader<FeatureType, Feature> arg0 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setTransaction( Transaction arg0 ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void addFeatureListener( FeatureListener arg0 ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public ReferencedEnvelope getBounds()
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public ReferencedEnvelope getBounds( Query arg0 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public int getCount( Query arg0 )
            throws IOException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public DataAccess<FeatureType, Feature> getDataStore() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Set<Key> getSupportedHints() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void removeFeatureListener( FeatureListener arg0 ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
