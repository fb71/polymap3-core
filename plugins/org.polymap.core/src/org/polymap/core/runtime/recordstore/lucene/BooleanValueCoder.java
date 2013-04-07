/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.recordstore.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.QueryExpression.Equal;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordState.Document;

/**
 * Encode/decode boolean values. 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
final class BooleanValueCoder
        implements LuceneValueCoder {

    public static final String      SUFFIX = "_bool";
    
    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Boolean) {
            // XXX param indexec ignored
            doc.put( new StringField( key+SUFFIX, value.toString(), Store.YES ) );
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        Field field = doc.getField( key+SUFFIX );
        return field != null ? Boolean.valueOf( field.stringValue() ) : null;
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            if (equalExp.value instanceof Boolean) {
                return new TermQuery( new Term( equalExp.key+SUFFIX, equalExp.value.toString()) );
            }
        }
        return null;
    }
    
}
