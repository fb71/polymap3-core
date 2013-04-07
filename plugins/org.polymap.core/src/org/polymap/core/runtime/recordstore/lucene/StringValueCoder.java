/* 
 * polymap.org
 * Copyright 2011-2013, Polymap GmbH. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.QueryExpression.Equal;
import org.polymap.core.runtime.recordstore.QueryExpression.Match;


/**
 * Interprets each and every field as a String {@link Field}. This *must* be
 * last consulted by {@link ValueCoders}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class StringValueCoder
        implements LuceneValueCoder {

    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof String) {
            Field field = (Field)doc.getField( key );
            if (field != null) {
                field.setStringValue( (String)value );
            }
            else {
                // FIXME indexed is ignore
                doc.add( new StringField( key, (String)value, Field.Store.YES ) );
            }
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        return doc.get( key );
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            
            if (equalExp.value instanceof String) {
                return new TermQuery( new Term( equalExp.key, (String)equalExp.value) );
            }
        }
        // MATCHES
        else if (exp instanceof QueryExpression.Match) {
            Match matchExp = (Match)exp;
            
            if (matchExp.value instanceof String) {
                String value = (String)matchExp.value;
                
                // XXX properly substitute wildcard chars
                if (value.endsWith( "*" )
                        && StringUtils.countMatches( value, "*" ) == 1
                        && StringUtils.countMatches( value, "?" ) == 0) {
                    return new PrefixQuery( new Term( matchExp.key, value.substring( 0, value.length()-1 ) ) );
                }
                else {
                    return new WildcardQuery( new Term( matchExp.key, value ) );
                }
            }
        }
        return null;
    }
    
}
