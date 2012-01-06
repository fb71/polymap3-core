/*
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.data.entityfeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import java.io.IOException;

import net.refractions.udig.catalog.IService;

import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.grammar.BooleanExpression;
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
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.data.entityfeature.catalog.EntityServiceImpl;

/**
 * Provides Qi4j entities as features to the pipeline. This processor is the
 * link between features and entities. It converts between the (meta) data model
 * and APIs of both worlds.
 * <p>
 * This class provides default implementation to build {@link FeatureType} and
 * {@link Feature} instances out of the given {@link EntityProvider}. The caller
 * may provide an {@link EntityProvider2} in order to control this process.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class EntitySourceProcessor
        implements ITerminalPipelineProcessor {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );

    public static final int                 DEFAULT_CHUNK_SIZE = 100;


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
        return service instanceof EntityServiceImpl;
    }


    // instance *******************************************

    /**
     * The {@link FeatureType} of the {@link EntityType} of our
     * {@link EntityProvider}. Initialized by {@link #init(Properties)}.
     * <p>
     * This break the stateless rule for processors. This schema instance is
     * shared by all calling threads.
     */
    private FeatureType             schema;

    /** Might be of type {@link EntityProvider2}. @see #schema */
    private EntityProvider<Entity>  entityProvider;

    private Feature2EntityFilterConverter filterConverter;


    public void init( Properties props ) {
        try {
            // init schema
            ILayer layer = (ILayer)props.get( "layer" );
            EntityGeoResourceImpl geores = (EntityGeoResourceImpl)layer.getGeoResource();
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


    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        // resolve FeatureSource
//        ILayer layer = context.getLayers().iterator().next();
//        EntityGeoResourceImpl geores = (EntityGeoResourceImpl)layer.getGeoResource();
//        EntityProvider entityProvider = geores.resolve( EntityProvider.class, null );
        log.debug( "        Request: " + r + ", provider= " + entityProvider );

        // GetFeatureType
        if (r instanceof GetFeatureTypeRequest) {
            context.sendResponse( new GetFeatureTypeResponse( schema ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // AddFeatures
        else if (r instanceof AddFeaturesRequest) {
            AddFeaturesRequest request = (AddFeaturesRequest)r;
            List<FeatureId> result = addFeatures( request.getFeatures() );
            context.sendResponse( new ModifyFeaturesResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // RemoveFeatures
        else if (r instanceof RemoveFeaturesRequest) {
            RemoveFeaturesRequest request = (RemoveFeaturesRequest)r;
            removeFeatures( request.getFilter() );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // ModifyFeatures
        else if (r instanceof ModifyFeaturesRequest) {
            ModifyFeaturesRequest request = (ModifyFeaturesRequest)r;
            modifyFeatures( request.getName(), request.getValue(), request.getFilter() );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // GetFeatures
        else if (r instanceof GetFeaturesRequest) {
            GetFeaturesRequest request = (GetFeaturesRequest)r;
            getFeatures( request.getQuery(), context );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            GetFeaturesSizeRequest request = (GetFeaturesSizeRequest)r;
            int result = getFeaturesSize( request.getQuery() );
            context.sendResponse( new GetFeaturesSizeResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }


    protected int getFeaturesSize( Query query )
    throws IOException {
        // build entity query
        BooleanExpression entityQuery = filterConverter.convert( query.getFilter() );

        if (entityQuery != null) {
            int firstResult = query.getStartIndex() != null ? query.getStartIndex() : 0;
            int maxResults = query.getMaxFeatures() > 0 ? query.getMaxFeatures() : Integer.MAX_VALUE;

            return entityProvider.entitiesSize( entityQuery, firstResult, maxResults );
        }
        else {
            // 1 pass: query entities
            Iterable<Entity> entities = entityProvider.entities( null,
                    0, Integer.MAX_VALUE );

            // 2 pass: filter features
            int count = 0;
            for (Entity entity : entities) {
                Feature feature = buildFeature( entity );
                if (filterFeature( feature, query.getFilter() ) != null) {
                    count++;
                }
            }
            log.debug( "            Features size: " + count );
            return count;
        }
    }


    protected void getFeatures( Query query, ProcessorContext context )
    throws Exception {
        assert query != null && query.getFilter() != null;

        long start = System.currentTimeMillis();
        log.debug( "            Filter: " + StringUtils.abbreviate( query.getFilter().toString(), 0, 256 ) );
        int firstResult = query.getStartIndex() != null ? query.getStartIndex() : 0;
        int maxResults = query.getMaxFeatures() > 0 ? query.getMaxFeatures() : Integer.MAX_VALUE;

        // build entity query
        BooleanExpression entityQuery = filterConverter.convert( query.getFilter() );

        if (entityQuery == null) {
            log.warn( "*** No query tranlation geotools->Qi4j... fetching ALL entities! ***" );
        }

        // 1 pass: query entities
        Iterable<Entity> entities = entityQuery != null
                ? entityProvider.entities( entityQuery, firstResult, maxResults )
                : entityProvider.entities( null, 0, Integer.MAX_VALUE );

        // 2 pass: filter features
        int count = 0;
        ArrayList<Feature> chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
        for (Entity entity : entities) {

            Feature feature = null;
            // XXX synchronized because qi4j seem to have issues when loading entities
            // from several threads
            synchronized (entityProvider) {
                feature = buildFeature( entity );
            }
            feature = entityQuery == null
                    ? filterFeature( feature, query.getFilter() )
                    : feature;

            if (feature != null) {
                chunk.add( feature );
                if (chunk.size() >= DEFAULT_CHUNK_SIZE) {
                    chunk.trimToSize();
                    //log.debug( "                sending chunk: " + chunk.size() );
                    context.sendResponse( new GetFeaturesResponse( chunk ) );
                    chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
                }
                if (++count >= query.getMaxFeatures()) {
                    break;
                }
            }
        }
        if (!chunk.isEmpty()) {
            chunk.trimToSize();
            //log.debug( "                sending chunk: " + chunk.size() );
            context.sendResponse( new GetFeaturesResponse( chunk ) );
        }
        log.debug( "    getFeatures(): " + (System.currentTimeMillis()-start) + "ms" );
    }


    private Feature buildFeature( Entity entity ) {
        if (entityProvider instanceof EntityProvider2) {
            return ((EntityProvider2)entityProvider).buildFeature( entity, schema );
        }
        else {
            EntityType type = entityProvider.getEntityType();

            // this does not work with Geometry properties yet
//            return new EntityFeature( entity, type, (SimpleFeatureType)schema );
//        }

            // straight forward solution; 2 times slower and probably needs more memory
            SimpleFeatureBuilder fb = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
            try {
                for (AttributeDescriptor attr : ((SimpleFeatureType)schema).getAttributeDescriptors()) {
                    EntityType.Property entityProp = type.getProperty( attr.getName().getLocalPart() );
                    fb.set( attr.getName(), entityProp.getValue( entity ) );
                }
                return fb.buildFeature( entity.id() );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
    }


    /**
     * Filter the given features with the given query.
     * <p>
     * XXX Currently it seems simpler to fetch all features and apply the filter
     * than converting the geospatial filter to a Qi4j filter. But this can be
     * memory consuming and might be revised later.
     *
     * @param features
     * @param query
     * @return
     */
    private Feature filterFeature( Feature feature, Filter filter ) {
        return filter.evaluate( feature ) ? feature : null;
    }


    protected List<FeatureId> addFeatures( Collection<Feature> features )
    throws Exception {
        //log.debug( "            Features: " + features.size() );

        final EntityType type = entityProvider.getEntityType();

        List<FeatureId> result = new ArrayList();
        for (final Feature feature : features) {

            Entity entity = entityProvider.newEntity( new EntityCreator<Entity>() {

                public void create( Entity instance )
                throws Exception{
                    for (Property featureProp : feature.getProperties()) {
                        EntityType.Property prop = type.getProperty( featureProp.getName().getLocalPart() );
                        Object value = featureProp.getValue();
                        // check values, do not overwrite default values
                        if (prop != null && value != null) {
                            prop.setValue( instance, value );
                        }
                    }
                }
            });
            // assuming that buildFeature() uses id() as well
            result.add( new FeatureIdImpl( entity.id() ) );
        }
        return result;
    }


    protected void removeFeatures( Filter filter )
    throws IOException {
        log.debug( "            Filter: " + filter );
        throw new RuntimeException( "not yet implemented." );
    }


    protected void modifyFeatures( Name[] names, Object[] value, Filter filter )
    throws IOException {
        log.debug( "            Filter: " + filter );

        // filter entities
        List<Entity> entities = new ArrayList();
        if (filter instanceof Id) {
            for (Identifier id : ((Id)filter).getIdentifiers()) {
                entities.add( entityProvider.findEntity( (String)id.getID() ) );
            }
        }
        else {
            throw new RuntimeException( "Unknown filter type: " + filter );
        }

        // set values
        EntityType entityType = entityProvider.getEntityType();
        for (Entity entity : entities) {
            for (int i=0; i<names.length; i++) {
                try {
                    String propName = names[i].getLocalPart();
                    log.debug( "    modifying: prop=" + propName + ", value=" + value[i] + ", entity=" + entity );
                    if (entityProvider instanceof EntityProvider2) {
                        ((EntityProvider2)entityProvider).modifyFeature( entity, propName, value[i] );
                    }
                    else {
                        entityType.getProperty( propName ).setValue( entity, value[i] );
                    }
                }
                catch (Exception e) {
                    throw new IOException( "Fehler beim �ndern des Objektes: " + entity, e );
                }
            }
        }
    }


    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
    throws Exception {
        throw new RuntimeException( "This is a terminal processor." );
    }

}
