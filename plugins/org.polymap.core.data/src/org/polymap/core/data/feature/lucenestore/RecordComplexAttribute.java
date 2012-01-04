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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.identity.Identifier;

import com.google.common.base.Joiner;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordComplexAttribute
        extends RecordAttribute
        implements ComplexAttribute {

    protected Map<String,List<Property>>    properties = new HashMap();
    
    
    public RecordComplexAttribute( RecordProperty parent, String recordKey, AttributeDescriptor descriptor, Identifier id ) {
        super( parent, recordKey, descriptor, id );
    }

    
    public ComplexType getType() {
        return (ComplexType)super.getType();
    }

    
    public Collection<? extends Property> getValue() {
        return getProperties();
    }


    public void setValue( Collection<Property> values ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public Collection<Property> getProperties() {
        List<Property> result = new ArrayList();
        // init all properties
        for (PropertyDescriptor descr : getType().getDescriptors()) {
            result.addAll( getProperties( descr.getName() ) );
        }
        return result;
    }
    
    
    public Collection<Property> getProperties( Name name ) {
        return getProperties( name.getLocalPart() );
    }

    
    public Collection<Property> getProperties( String name ) {
        List<Property> result = properties.get( name );
        if (result == null) {
            PropertyDescriptor childDescriptor = getType().getDescriptor( name );
            // check type
            if (childDescriptor == null) {
                throw new RuntimeException( "No such property: " + name );
            }
            // init properties
            if (childDescriptor.getMaxOccurs() == 1) {
                String propRecordKey = Joiner.on( '/' ).skipNulls().join( recordKey, childDescriptor.getName().getLocalPart() ).intern();
//                String propRecordKey = new StringBuilder( 128 )
//                        .append( recordKey ).append( '/' ).append( childDescriptor.getName().getLocalPart() )
//                        .toString().intern();
                
                RecordAttribute property = childDescriptor.getType() instanceof ComplexType
                        // ComplexType
                        ? new RecordComplexAttribute( this, propRecordKey, (AttributeDescriptor)childDescriptor, null )
                        // Attribute
                        : new RecordAttribute( this, propRecordKey, (AttributeDescriptor)childDescriptor, null );
                        
                result = Collections.singletonList( property );
            }
            else {
                throw new RuntimeException( "Collection attributes are not implemented yet." );
            }
        }
        return result;
    }

    public Property getProperty( Name name ) {
        return getProperty( name.getLocalPart() );
    }

    public Property getProperty( String name ) {
        return ((List<Property>)getProperties( name )).get( 0 );
    }
    
    
    // ****************************************************
    
}
