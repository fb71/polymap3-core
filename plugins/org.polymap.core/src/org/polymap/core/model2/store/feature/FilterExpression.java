/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.store.feature;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.query.grammar.BooleanExpression;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FilterExpression
        extends BooleanExpression {

    private static Log log = LogFactory.getLog( FilterExpression.class );

    private Filter          filter;
    
    
    public FilterExpression( Filter filter ) {
        assert filter != null;
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    public boolean evaluate( Composite target ) {
        return filter.evaluate( target );
    }

}
