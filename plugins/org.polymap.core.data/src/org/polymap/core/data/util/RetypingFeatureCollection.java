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
package org.polymap.core.data.util;

import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This decorator can be used to process/modify the features of the target
 * {@link FeatureCollection}. The abstract method {@link #retype(Feature)} is called
 * to do the actual processing.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RetypingFeatureCollection<T extends FeatureType, F extends Feature>
        extends AbstractFeatureCollection {

    private static Log log = LogFactory.getLog( RetypingFeatureCollection.class );

    private FeatureCollection   delegate;
    
    private SimpleFeatureType   targetSchema;
    
    private boolean             breakOnException = true;

    
    public RetypingFeatureCollection( FeatureCollection delegate, T targetSchema ) {
        super( (SimpleFeatureType)targetSchema );
        this.delegate = delegate;
        this.targetSchema = (SimpleFeatureType)targetSchema;
    }
    
    
    public boolean isBreakOnException() {
        return breakOnException;
    }
    
    public RetypingFeatureCollection<T,F> setBreakOnException( boolean breakOnException ) {
        this.breakOnException = breakOnException;
        return this;
    }

    @Override
    public SimpleFeatureType getSchema() {
        return targetSchema;
    }

    @Override
    public void accepts( org.opengis.feature.FeatureVisitor visitor, org.opengis.util.ProgressListener progress ) {
        Iterator<F> it = null;
        progress = progress != null ? progress : new NullProgressListener();
        try {
            progress.started();
            float size = progress instanceof NullProgressListener ? -1 : size();
            float position = 0;            
            for (it=iterator(); !progress.isCanceled() && it.hasNext(); ) {
                if (size > 0) {
                    progress.progress( position++/size );
                }
                try {
                    visitor.visit( it.next() );
                }
                catch( Exception e ){
                    progress.exceptionOccurred( e );
                }
            }            
        }
        finally {
            progress.complete();            
            close( it );
        }
    }

    @Override
    protected Iterator openIterator() {
        return new RetypingIterator( delegate.iterator() );
    }

    @Override
    protected void closeIterator( Iterator close ) {
        RetypingIterator retyping = (RetypingIterator)close;
        delegate.close( retyping.delegateIt );
    }


    @Override
    public int size() {
        return delegate.size();
    }


    protected abstract F retype( F feature );
    
    
    /**
     *
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public class RetypingIterator 
            implements Iterator<F> {
        
        private Iterator<F>         delegateIt;
        
        public RetypingIterator( Iterator<F> delegateIt ) {
            this.delegateIt = delegateIt;
        }

        public boolean hasNext() {
            return delegateIt.hasNext();
        }

        public F next() {
            try {
                return retype( delegateIt.next() );
            } 
            catch (Exception e) {
                if (breakOnException) {
                    throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException( e );
                }
                else {
                    log.warn( "", e );
                    return null;
                }
            }
        }

        public void remove() {
            delegateIt.remove();
        }
    }

}

