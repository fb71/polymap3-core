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

import java.util.Collection;
import java.util.Properties;

import java.io.IOException;

import net.refractions.udig.catalog.IService;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.qi4j.api.value.ValueComposite;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.ModifyFeaturesResponse;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.pipeline.ITerminalPipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneSourceProcessor
        implements ITerminalPipelineProcessor {

    
    public static ProcessorSignature signature( LayerUseCase usecase ) {
        if (usecase == LayerUseCase.FEATURES_TRANSACTIONAL ) {
            return new ProcessorSignature(
                    new Class[] {ModifyFeaturesRequest.class, RemoveFeaturesRequest.class, AddFeaturesRequest.class, GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {},
                    new Class[] {},
                    new Class[] {ModifyFeaturesResponse.class, GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );
        }
        else {
            return new ProcessorSignature(
                    new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {},
                    new Class[] {},
                    new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );
        }
    }

    public static boolean isCompatible( IService service ) {
        return service instanceof LuceneServiceImpl;
    }


    // instance *******************************************

    /**
     * The {@link FeatureType} of the {@link EntityType} of our
     * {@link EntityProvider}. Initialized by {@link #init(Properties)}.
     * <p/>
     * This breaks the stateless rule for processors. This schema instance is
     * shared by all calling threads.
     */
    private FeatureType             schema;


    public void init( Properties props ) {
        try {
            // init schema
            ILayer layer = (ILayer)props.get( "layer" );
            LuceneGeoResourceImpl geores = (LuceneGeoResourceImpl)layer.getGeoResource();
            entityProvider = geores.resolve( EntityProvider.class, null );
            filterConverter = new Feature2EntityFilterConverter( entityProvider.getEntityType() );

            // EntityProvider2
            if (entityProvider instanceof EntityProvider2) {
                schema = ((EntityProvider2)entityProvider).buildFeatureType();
            }
            // build standard schema
            else {
                log.debug( "Building schema for layer: " + layer.getLabel() );
                EntityType entityType = entityProvider.getEntityType();

                SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                builder.setName( entityProvider.getEntityName() );

                Collection<EntityType.Property> p = entityType.getProperties();
                for (EntityType.Property prop : p) {
                    Class propType = prop.getType();

                    if (Geometry.class.isAssignableFrom( propType )) {
                        CoordinateReferenceSystem crs = entityProvider.getCoordinateReferenceSystem( prop.getName() );
                        builder.add( prop.getName(), propType, crs );
                        builder.setDefaultGeometry( prop.getName() );
                        log.debug( "    Geometry: " + prop.getName() + " / " + propType );
                    }
                    else if (ValueComposite.class.isAssignableFrom( propType )) {
                        log.debug( "    skipping complex: " + prop.getName() + " / " + propType );
                    }
                    else if (Collection.class.isAssignableFrom( propType )) {
                        log.debug( "    skipping collection: " + prop.getName() + " / " + propType );
                    }
                    else {
                        builder.add( prop.getName(), propType );
                        log.debug( "    primitive: " + prop.getName() + " / " + propType );
                    }
                }
                schema = builder.buildFeatureType();
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }


    public void processRequest( ProcessorRequest request, ProcessorContext context )
            throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
            throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
