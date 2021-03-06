/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.qi4j.sample;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;

import org.polymap.core.model.AssocCollection;
import org.polymap.core.model.Entity;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface Person
        extends Labeled, PersonParent { //,Entity {

    //@NotEmpty
    @Optional
    @UseDefaults
    ManyAssociation<Person>         children();

    /** Property declaration - client code must not use this directly. */
    @Optional
    Property<ReferencedEnvelope>    extend();
    
    public abstract void addChild( Person child );

    public abstract AssocCollection<Person> allChildren();


//    public String getLabel();
//    
//    public void setLabel( @NotEmpty String value );
    
//    public String toString();

}
