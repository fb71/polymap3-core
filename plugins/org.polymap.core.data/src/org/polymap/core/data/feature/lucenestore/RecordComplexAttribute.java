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

import java.util.Collection;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.Identifier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RecordComplexAttribute
        extends RecordAttribute
        implements ComplexAttribute {

    public RecordComplexAttribute( RecordProperty parent, AttributeDescriptor descriptor, Identifier id ) {
        super( parent, descriptor, id );
    }

    
    public ComplexType getType() {
        return (ComplexType)super.getType();
    }

    
    public Collection<? extends Property> getValue() {
        throw new RuntimeException( "not yet implemented." );
    }


    public void setValue(Collection<Property> values) {
        throw new RuntimeException( "not yet implemented." );
    }


    public Collection<Property> getProperties() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Collection<Property> getProperties( Name name ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Collection<Property> getProperties( String name ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Property getProperty( Name name ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Property getProperty( String name ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
    
    // ****************************************************
    
    protected void load() {
        
    }
    
    protected String stateKey( RecordProperty child ) {
    }


}
