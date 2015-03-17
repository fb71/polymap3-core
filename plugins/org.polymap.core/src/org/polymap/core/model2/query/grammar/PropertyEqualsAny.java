/* 
 * polymap.org
 * Copyright (C) 2014, Falko Br�utigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.model2.query.grammar;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.engine.TemplateProperty;

/**
 * The "IN" operator, allows to compare the value of a property with multiple values.
 * True if any of the given values equals the property value.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class PropertyEqualsAny<T>
        extends ComparisonPredicate<T> {

    public T[]                  values;

    
    public PropertyEqualsAny( TemplateProperty<T> prop, T[] values ) {
        super( prop, null );
        this.values = values;
    }


    @Override
    public boolean evaluate( Composite target ) {
        String propName = prop.getInfo().getName();
        Object propValue = ((Property)target.info().getProperty( propName ).get( target )).get();
        
        for (T v : values) {
            if (v.equals( propValue )) {
                return true;
            }
        }
        return false;
    }
    
}
