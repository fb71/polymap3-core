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

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RecordProperty
        implements Property {

    protected RecordProperty            parent;
    
    protected String                    recordKey;
    
    protected PropertyDescriptor        descriptor;
    
    protected Map<Object,Object>        userData;


    protected RecordProperty( RecordProperty parent, String recordKey, PropertyDescriptor descriptor ) {
        this.parent = parent;
        this.recordKey = recordKey;
        this.descriptor = descriptor;
        assert descriptor != null : "descriptor == null";
    }
    
    public PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    public Name getName() {
        return getDescriptor().getName();
    }

    public PropertyType getType() {
        return getDescriptor().getType();
    }

    public boolean isNillable() {
        return getDescriptor().isNillable();
    }
    
    
    public Map<Object, Object> getUserData() {
        if (userData == null) {
            userData = new HashMap();
        }
        return userData;
    }
    
    
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof RecordProperty) {
            RecordProperty other = (RecordProperty)obj;
            return descriptor.equals( other.descriptor );
        }    
        return false;
    }
    
    
    public int hashCode() {
        Object value = getValue();
        return 37 * descriptor.hashCode()
            + (37 * (value == null ? 0 : value.hashCode()));
    }
    
    
    public String toString() {
        return new StringBuilder( 128 ) 
                .append( getClass().getSimpleName() ).append( ":" )
                .append( getName().getLocalPart() )
                .append( "<" )
                .append( getType().getName().getLocalPart() )
                .append( ">=" )
                .append( getValue() )
                .toString();
    }

    
    // ****************************************************
    
    protected IRecordState state() {
        assert parent != null : "parent == null -> RecordFeature must implement state()!";
        return parent.state();
    }
    
}
