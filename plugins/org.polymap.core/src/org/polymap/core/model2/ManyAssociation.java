/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2;

import java.util.Collection;

/**
 * A multi-value association to an {@link Entity}.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface ManyAssociation<T extends Entity>
        extends PropertyBase<T>, Collection<T> {

//    /**
//     * Returns a {@link Expressions#template(Class,EntityRepository) template} for
//     * querying the associated entities.
//     */
//    public T any();
//    
//    /**
//     * Returns a {@link Expressions#template(Class,EntityRepository) template} for
//     * querying the associated entities.
//     */
//    public T all();

}
