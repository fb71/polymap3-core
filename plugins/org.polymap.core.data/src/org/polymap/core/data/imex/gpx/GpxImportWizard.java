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

import java.util.Collection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.kml.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Parser;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;
import com.vividsolutions.jts.geom.MultiLineString;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.imex.FileImportWizard;
import org.polymap.core.data.util.RetypingFeatureCollection;
import org.polymap.core.runtime.IMessages;

/**
 * GPX import.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GpxImportWizard 
        extends FileImportWizard
        implements INewWizard {

    private static Log log = LogFactory.getLog( GpxImportWizard.class );

    public static final String          ID = "org.polymap.core.data.GpxImportWizard";
    
    private static final IMessages      i18n = Messages.forPrefix( "GpxImportWizard" );
    

    public GpxImportWizard() {
        super();
    }

    
    @Override
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        super.init( workbench, selection );
        setWindowTitle( i18n.get( "windowTitle" ) );
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                DataPlugin.PLUGIN_ID, "icons/workset_wiz.png" ) );
        setNeedsProgressMonitor( true );
        
        page1 = new GpxUploadPage();
    }


    @Override
    public FeatureCollection parseKml( final InputStream in ) throws Exception {
        features = null;
        IRunnableWithProgress task = new IRunnableWithProgress() {
            public void run( final IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
                monitor.beginTask( i18n.get( "parseTaskTitle" ), 2 );
                try {
                    // transform GPX
                    monitor.subTask( "Transforming GPX data..." );
                    InputStream xslIn = DataPlugin.getDefault().getBundle().getResource( "org/polymap/core/data/imex/gpx/gpx2kml.xsl" ).openStream();
                    Source xslDoc = new StreamSource( xslIn );
                    Source xmlDoc = new StreamSource( in );
                    ByteArrayOutputStream kmlOut = new ByteArrayOutputStream();

                    Transformer trasform = TransformerFactory.newInstance().newTransformer( xslDoc );
                    trasform.transform( xmlDoc, new StreamResult( kmlOut ) );
                    log.info( "KML: " + kmlOut.toString() );
                    monitor.worked( 1 );

                    // parse KML
                    monitor.subTask( "Parsing..." );
                    KMLConfiguration config = new KMLConfiguration();

                    Parser parser = new Parser( config );
                    SimpleFeature f = (SimpleFeature)parser.parse( new ByteArrayInputStream( kmlOut.toByteArray() ) );
                    Collection<Feature> placemarks = (Collection<Feature>)f.getAttribute( "Feature" );
                    //log.info( placemarks );

                    features = DefaultFeatureCollections.newCollection();
                    features.addAll( placemarks );
                    monitor.worked( 1 );

                    // adjust type
                    FeatureType schema = features.getSchema();
                    SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                    ftb.setName( "Placemark" );
                    ftb.setNamespaceURI( schema.getName().getNamespaceURI() );
                    ftb.add( "name", String.class );
                    ftb.add( "description", String.class );
                    ftb.add( schema.getGeometryDescriptor().getLocalName(), MultiLineString.class, CRS.decode( "EPSG:4326" ) );

                    features = new RetypingFeatureCollection( features, ftb.buildFeatureType() ) {
                        private SimpleFeatureType targetSchema = getSchema();
                        
                        protected Feature retype( Feature feature ) {
                            try {
                                SimpleFeatureBuilder fb = new SimpleFeatureBuilder( targetSchema );
                                for (PropertyDescriptor prop : targetSchema.getDescriptors()) {
                                    fb.set( prop.getName(), feature.getProperty( prop.getName() ).getValue() );
                                }
                                return fb.buildFeature( feature.getIdentifier().getID() );
                            }
                            catch (Exception e) {
                                throw new RuntimeException( e );
                            }
                        }
                    };
                    
//                    features = new ReTypingFeatureCollection( features, ftb.buildFeatureType() );
                }
                catch (Exception e) {
                    log.warn( "", e );
                    throw new InvocationTargetException( e );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }            
        };

        try {
            getContainer().run( true, true, task );
        }
        catch (InvocationTargetException e) {
            Throwables.propagateIfPossible( e.getTargetException(), Exception.class );
        }
        catch (InterruptedException e) {
            throw e;
        }
        return features;
    }

}
