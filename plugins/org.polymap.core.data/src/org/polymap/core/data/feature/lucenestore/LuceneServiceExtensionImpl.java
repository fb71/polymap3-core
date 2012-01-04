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

import java.util.Map;

import java.io.Serializable;
import java.net.URL;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class LuceneServiceExtensionImpl
        implements ServiceExtension {

    /**
     * The service key; used to identify the entities the service will provide.
     */
    public static final String KEY = "org.polymap.core.data.lucenestore";

    public static final String FOLDER_BASE_KEY = "folder";

    public static final String NAME_KEY = "name";

    
    public Map<String, Serializable> createParams( URL url ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public IService createService( URL id, Map<String, Serializable> params ) {
        if (params != null) {
            // check for the properties service key
            if (params.containsKey( KEY )) {
                // found it, create the service handle
                return new LuceneServiceImpl( id, params );
            }
        }
        // key not found
        return null;
    }

}
