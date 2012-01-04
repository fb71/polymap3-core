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

import org.opengis.feature.Property;
import org.opengis.feature.type.Name;

import com.google.common.base.Predicate;

/**
 * Static helpers for working with {@link Property} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Properties {

    
    public static Predicate<Property> isNamed( final Name name ) {
        return new Predicate<Property>() {
            public boolean apply( Property input ) {
                return name.equals( input.getName() );
            }
        };
    }
    
    
    public static Predicate<Property> isNamed( final String name ) {
        return new Predicate<Property>() {
            public boolean apply( Property input ) {
                return name.equals( input.getName().getLocalPart() );
            }
        };
    }
    
}
