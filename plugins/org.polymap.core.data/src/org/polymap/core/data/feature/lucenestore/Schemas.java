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

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import com.google.common.base.Function;

/**
 * Static helper for working with {@link FeatureType} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Schemas {

    public static Function<FeatureType,Name> toName() {
        return new Function<FeatureType,Name>() {
            public Name apply( FeatureType input ) {
                return input.getName();
            }
        };
    }
    
    public static Function<FeatureType,String> toNameString() {
        return new Function<FeatureType,String>() {
            public String apply( FeatureType input ) {
                return input.getName().getLocalPart();
            }
        };
    }
    
}
