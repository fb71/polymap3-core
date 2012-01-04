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

import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.Types;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordAttribute
        extends RecordProperty
        implements Attribute {

    private static Log log = LogFactory.getLog( RecordAttribute.class );

    protected Identifier            id;

    
    public RecordAttribute( RecordProperty parent, String recordKey, AttributeDescriptor descriptor, Identifier id ) {
        super( parent, recordKey, descriptor );
        this.id = id;
        
        log.warn( "No Types check." );
        //Types.validate( this, getValue() );
    }


    public RecordAttribute( RecordProperty parent, String recordKey, AttributeType type, Identifier id ) {
        this( parent, recordKey, new AttributeDescriptorImpl( type, type.getName(), 1, 1, true, null ), id );
    }


    public Object getValue() {
        return state().get( recordKey );
    }
    
    
    public void setValue( Object value ) {
        state().put( recordKey, value );
    }
    

    public Identifier getIdentifier() {
        return id;
    }
    

    public AttributeDescriptor getDescriptor() {
        return (AttributeDescriptor) super.getDescriptor();
    }
    
    
    public AttributeType getType() {
        return (AttributeType) super.getType();
    }


    public void validate() {
        Types.validate(this, this.getValue() );
    }

}
