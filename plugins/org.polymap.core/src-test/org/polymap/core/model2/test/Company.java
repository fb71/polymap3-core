/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2.test;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.MaxOccurs;
import org.polymap.core.model2.Mixins;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.SRS;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
@Mixins( {TrackableMixin.class} )
@SRS( "EPSG:4326" )
public class Company
        extends Entity {

    protected Property<Employee>            chief;
    
    @MaxOccurs(100)
    protected CollectionProperty<Employee>  employees;
    

    public void addEmployee( Employee employee ) {
        methodProlog( "addEmployee", employee );
        employees.add( employee );
        //employee.company().s
    }
}
