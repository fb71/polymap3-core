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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * Converts a GeoTools Query into a Lucene Query.  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneQueryBuilder {

    private static Log log = LogFactory.getLog( LuceneQueryBuilder.class );
    
    
    public static Query query( LuceneRecordStore store, org.geotools.data.Query query ) {
        BooleanQuery result = new BooleanQuery();
        
        result.add( store.getValueCoders().searchQuery( 
                new QueryExpression.Equal( "type", "org.polymap.biotop.model.BiotopComposite" ) ), 
                BooleanClause.Occur.MUST );
        
        log.warn( "Lucene Query: " + result );
        return result;
    }
    
}
