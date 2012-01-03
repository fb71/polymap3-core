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

import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * A {@link Feature} implementation based on the {@link IRecordState} API. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordFeature
        extends RecordComplexAttribute
        implements Feature {

    private static Log log = LogFactory.getLog( RecordFeature.class );

    private IRecordState            record;


    public FeatureType getType() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public FeatureId getIdentifier() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
    public BoundingBox getBounds() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    public void setDefaultGeometryProperty( GeometryAttribute geometryAttribute ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
