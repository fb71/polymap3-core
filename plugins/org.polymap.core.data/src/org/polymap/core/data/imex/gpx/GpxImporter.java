/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.imex.gpx;

import java.util.Calendar;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.xml.Parser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GpxImporter {

    private static Log log = LogFactory.getLog( GpxImporter.class );
    
    
    public static void main( String[] args ) throws Exception {
        InputStream in = new FileInputStream( "/tmp/ashland.gpx" );
        
        GPXConfiguration config = new GPXConfiguration();
        System.out.println( "Config properties: " + config.getProperties() );        
        
        Parser parser = new Parser( config );
        Object result = parser.parse( in );
        //System.out.println( "Parsed result: " + result );
        JSONObject json = (JSONObject)convert2json( result );
        System.out.println( "JSON: " + json.toString( 2 ) );
        
        
//        StreamingParser parser = new StreamingParser( config, in, Feature.class );
//        Feature feature = (Feature)parser.parse();
//        System.out.println( "Parsed feature: " + feature );
        
//        parser.setNamespaceAware( false );
//        kmlEncoder.setOmitXMLDeclaration( true );
//        kmlEncoder.encode( geom, KML.Geometry, this );
    
    }

    
    protected static Object convert2json( Object elm ) throws JSONException {
        if (elm instanceof Map) {
            JSONObject result = new JSONObject();    
            for (Object e : ((Map)elm).entrySet()) {
                Map.Entry entry = (Map.Entry)e;
                result.put( (String)entry.getKey(), convert2json( entry.getValue() ) );
            }
            return result;
        }
        else if (elm.getClass().isArray()) {
            JSONArray result = new JSONArray();
            for (Object child : (Object[])elm) {
                result.put( convert2json( child ) );
            }
            return result;
        }
        else {
            return elm;
        }
    }
    
    
    protected static void printMap( Map map, String prefix ) {
        for (Object elm : map.entrySet()) {
            Map.Entry entry = (Map.Entry)elm;
            if (entry.getValue() instanceof Map) {
                System.out.println( prefix + entry.getKey() + " {" );
                printMap( (Map)entry.getValue(), prefix + "  " );
            }
            if (entry.getValue().getClass().isArray()) {
                System.out.println( prefix + entry.getKey() + " {" );
                printMap( (Map)entry.getValue(), prefix + "  " );
            }
            else if (entry.getValue() instanceof Calendar) {
                System.out.println( prefix + entry.getKey() + " : " + ((Calendar)entry.getValue()).toString() );
            }
            else {
                System.out.println( "    " + entry.getKey() + " : " + entry.getValue() );
            }
        }
    }
}
