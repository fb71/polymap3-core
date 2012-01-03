/*
 * polymap.org 
 * Copyright 2012, Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.core.data.feature.lucenestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.Messages;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * This service references a {@link LuceneFeatureStore}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneServiceImpl
        extends IService {

    private static final Log log = LogFactory.getLog( LuceneServiceImpl.class );

    private String                      name;
    
    private URL                         id;
    
    private Map<String,Serializable>    params;
    
    private LuceneFeatureStore          store;
    
    private List<LuceneGeoResourceImpl> resources; 

    
    public LuceneServiceImpl( String name, String id ) {
        try {
            this.id = new URL( id );
        }
        catch (MalformedURLException e) {
            throw new RuntimeException( e );
        }
        this.name = name;
        
        // providers / resources
        this.providers = providers;
        this.resources = new ArrayList( providers.length );
        for (EntityProvider provider : providers) {
            resources.add( new LuceneGeoResourceImpl( this, provider ) );
        }
        
        // build params
        this.params = new HashMap();
        this.params.put( EntityServiceExtensionImpl.KEY, id );
        this.params.put( EntityServiceExtensionImpl.NAME_KEY, name );
        for (int i=0; i<providers.length; i++) {
            this.params.put( 
                    EntityServiceExtensionImpl.PROVIDER_BASE_KEY + i,
                    providers[i].getClass().getName() );
            
        }
    }


    protected IServiceInfo createInfo( IProgressMonitor monitor )
    throws IOException {
        
        return new IServiceInfo() {

            public String getTitle() {
                return name; //Messages.get( "Qi4jServiceImpl_title" ); 
            }

            public String getDescription() {
                return Messages.get( "LuceneServiceImpl_description" ); 
            }
        };
    }


    public Map<String, Serializable> getConnectionParams() {
        return params;
    }


    public List<? extends IGeoResource> resources( IProgressMonitor monitor )
            throws IOException {
        return resources;
    }


    public URL getIdentifier() {
        return id;
    }


    public Throwable getMessage() {
        return null;
    }


    public Status getStatus() {
        return Status.CONNECTED;
    }
    
}
