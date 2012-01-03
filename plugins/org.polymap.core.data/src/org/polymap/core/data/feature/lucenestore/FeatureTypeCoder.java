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

import org.json.JSONObject;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureTypeCoder {

    private static Log log = LogFactory.getLog( FeatureTypeCoder.class );
    
    
    public static String encode( FeatureType schema ) {
            
    }
    
    
    /*
     * 
     */
    class Encoder {
        
        private FeatureType     schema;

        public Encoder( FeatureType schema ) {
            this.schema = schema;
        }
       
        public String encode() {
            
        }
        
        protected JSONObject encodeAttribute( AttributeType type ) {
            
        }
    }
}
