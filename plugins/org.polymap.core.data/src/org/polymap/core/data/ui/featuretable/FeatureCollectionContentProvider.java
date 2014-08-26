/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.ui.featuretable;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureCollectionContentProvider
        implements IFeatureContentProvider {

    private static Log log = LogFactory.getLog( FeatureCollectionContentProvider.class );

    private FeatureCollection           features;


    public FeatureCollectionContentProvider() {
    }


    public FeatureCollectionContentProvider( FeatureCollection features ) {
        this.features = features;
    }


    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.features = (FeatureCollection)newInput;
    }


    @Override
    public Object[] getElements( Object input ) {
        try {
            final List<IFeatureTableElement> result = new ArrayList();
            features.accepts( new FeatureVisitor() {
                public void visit( Feature feature ) {
                    result.add( new CollectionContentProvider.FeatureTableElement( feature ) );
                }
            }, new NullProgressListener() );
            return result.toArray();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void dispose() {
    }

}
