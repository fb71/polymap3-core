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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.io.IOException;
import java.net.URI;

import org.geotools.data.DataAccess;
import org.geotools.data.ServiceInfo;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.compose;
import static com.google.common.collect.Iterables.find;
import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Arrays;

import org.polymap.core.runtime.recordstore.lucene.GeometryValueCoder;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneDataStore
        implements DataAccess {

    private static Log log = LogFactory.getLog( LuceneDataStore.class );

    private List<FeatureType>       schemas = new ArrayList();
    
    private LuceneRecordStore       store;
    
    
    public LuceneDataStore( LuceneRecordStore store, List<FeatureType> schemas ) {
        this.store = store;
        this.schemas = schemas;
    }


    public LuceneDataStore( LuceneRecordStore store, FeatureType... schemas ) {
        this( store, Arrays.asList( schemas ) );
        store.getValueCoders().addValueCoder( new GeometryValueCoder() );
    }


    public void dispose() {
        if (store != null) {
            store.close();
            store = null;
            schemas = null;
        }
    }


    public List<Name> getNames() throws IOException {
        return Lists.transform( schemas, Schemas.toName() );
    }


    public List<FeatureType> getSchemas() {
        return schemas;
    }


    public FeatureType getSchema( Name name ) throws IOException {
        return find( schemas, compose( equalTo( name ), Schemas.toName() ) );
    }


    public FeatureType getSchema( String name ) throws IOException {
        return find( schemas, compose( equalTo( name ), Schemas.toNameString() ) );
    }


    public LuceneRecordStore getRecordStore() {
        return store;
    }


    public ServiceInfo getInfo() {
        return new ServiceInfo() {
            public String getTitle() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public URI getSource() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public URI getSchema() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public URI getPublisher() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public Set<String> getKeywords() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            public String getDescription() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        };
    }


    public LuceneFeatureStore getFeatureSource( Name typeName )
    throws IOException {
        FeatureType schema = getSchema( typeName );
        if (schema == null) {
            throw new RuntimeException( "No schema exists for name: " + typeName );
        }
        return new LuceneFeatureStore( this, schema );
    }


    public void createSchema( FeatureType featureType )
    throws IOException {
        throw new RuntimeException( "not yet implemented." );
    }


    public void updateSchema( Name typeName, FeatureType featureType )
    throws IOException {
        throw new RuntimeException( "not yet implemented." );
    }

}
