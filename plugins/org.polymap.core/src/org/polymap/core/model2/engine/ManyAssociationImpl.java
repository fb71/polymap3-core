/* 
 * polymap.org
 * Copyright (C) 2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2.engine;

import java.util.AbstractCollection;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.ManyAssociation;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.store.StoreCollectionProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class ManyAssociationImpl<T extends Entity>
        extends AbstractCollection<T>
        implements ManyAssociation<T> {

    private EntityRuntimeContext            context;

    /** Holding the ids of the associated Entities. */
    private StoreCollectionProperty<Object> storeProp;
    

    public ManyAssociationImpl( EntityRuntimeContext context, StoreCollectionProperty storeProp ) {
        this.context = context;
        this.storeProp = storeProp;
    }

//    @Override
//    public T createElement( ValueInitializer<T> initializer ) {
//        throw new RuntimeException( "not yet..." );
//    }

    @Override
    public PropertyInfo getInfo() {
        return storeProp.getInfo();
    }

    // Collection *****************************************
    
    @Override
    public int size() {
        return storeProp.size();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform( storeProp.iterator(), new Function<Object,T>() {

            UnitOfWork              uow = context.getUnitOfWork();
            
            Class<? extends Entity> entityType = getInfo().getType();
            
            @Override
            public T apply( Object id ) {
                return (T)uow.entity( entityType, id );
            }
        });
    }

    @Override
    public boolean add( T elm ) {
        return storeProp.add( elm.id() );
    }

    @Override
    public String toString() {
        return "ManyAssociation[name:" + getInfo().getName() + ",value=" + super.toString() + "]";
    }

}
