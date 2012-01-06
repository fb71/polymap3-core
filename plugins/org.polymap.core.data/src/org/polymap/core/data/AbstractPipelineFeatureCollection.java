/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
package org.polymap.core.data;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides {@link CollectionEven} and {@link FeatureListener} handling methods.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
abstract class AbstractPipelineFeatureCollection
        extends AbstractFeatureCollection
        implements PipelineFeatureCollection<SimpleFeatureType, SimpleFeature>, FeatureListener {

    private static final Log log = LogFactory.getLog( AbstractPipelineFeatureCollection.class );

    protected ProgressListener        progressListener;


    protected AbstractPipelineFeatureCollection( SimpleFeatureType memberType ) {
        super( memberType );
    }

    public ProgressListener setProgressListener( ProgressListener l ) {
        ProgressListener old = progressListener;
        progressListener = l;
        return old;
    }

    protected void fireChange( CollectionEvent ev ) {
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            (listeners.get(i)).collectionChanged( ev );
        }
    }

    protected void fireChange(SimpleFeature[] features, int type) {
        fireChange( new CollectionEvent( this, features, type ) );
    }

    protected void fireChange(SimpleFeature feature, int type) {
        fireChange( new SimpleFeature[] { feature }, type );
    }

    public void changed( FeatureEvent featureEvent ) {
        log.debug( "changed(): ev=" + featureEvent );
        int type = CollectionEvent.FEATURES_CHANGED;
        switch (featureEvent.getType()) {
            case ADDED:
                type = CollectionEvent.FEATURES_ADDED;
                break;
            case CHANGED:
                type = CollectionEvent.FEATURES_CHANGED;
                break;
            case REMOVED:
                type = CollectionEvent.FEATURES_REMOVED;
                break;
        }
        SimpleFeature[] features = new SimpleFeature[] {};
        fireChange( new CollectionEvent( this, features, type ) );
    }

}
