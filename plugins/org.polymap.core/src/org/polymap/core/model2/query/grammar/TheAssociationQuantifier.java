/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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

import org.polymap.core.model2.Association;
import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.TemplateProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class TheAssociationQuantifier<T extends Entity>
        extends Quantifier<Association<T>,T> {

    public TheAssociationQuantifier( Association<T> prop, BooleanExpression subExp ) {
        super( Type.THE_ONLY, prop, subExp );
    }

    
    @Override
    public boolean evaluate( Composite target ) {
        Association<T> targetProp = targetProp( target, (TemplateProperty<T>)prop );
        T composite = targetProp.get();

        return composite != null ? subExp().evaluate( composite ) : false;
    }

}
