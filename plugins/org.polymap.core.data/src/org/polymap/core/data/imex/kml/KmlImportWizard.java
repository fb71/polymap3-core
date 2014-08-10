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
package org.polymap.core.data.imex.kml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import java.io.InputStream;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureStore;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.catalog.actions.ResetServiceAction;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.data.imex.ChooseTargetWizardPage;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KmlImportWizard 
        extends Wizard 
        implements INewWizard {

    private static Log log = LogFactory.getLog( KmlImportWizard.class );

    private static final IMessages      i18n = Messages.forPrefix( "KmlImportWizard" );
    
    private final Map<String,String>    params = new HashMap();

    protected FeatureCollection         features;

    private KmlUploadPage               page1;

    private ChooseTargetWizardPage      page2;


    public KmlImportWizard() {
        super();
    }

    
    @Override
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle( i18n.get( "windowTitle" ) );
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                DataPlugin.PLUGIN_ID, "icons/workset_wiz.png" ) );
        setNeedsProgressMonitor( true );
        
        page1 = new KmlUploadPage();
        
        page2 = new ChooseTargetWizardPage( null ) {
            public void setVisible( boolean visible ) {
                super.setVisible( visible );
                if (visible) {
                    page2.setFeatureCollection( features );
                }
            }
        };
    }

    
    @Override
    public void addPages() {
        super.addPages();
        addPage( page1 );
        addPage( page2 );
    }

    
    public FeatureCollection getFeatures() {
        return features;
    }


    /**
     * Parses the given stream as KML and stores the result in {@link #features}.
     * 
     * @return The resulting feeatures, or null if an error occured.
     */
    public FeatureCollection parseKml( final InputStream in ) {
        UIJob job = new UIJob( "Reading KML Data" ) {
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                try {
//                    // transform GPX
//                    InputStream xslIn = DataPlugin.getDefault().getBundle().getResource( "org/polymap/core/data/imex/gpx/gpx2kml.xsl" ).openStream();
//                    Source xslDoc = new StreamSource( xslIn );
//                    Source xmlDoc = new StreamSource( in );
//                    ByteArrayOutputStream kmlOut = new ByteArrayOutputStream();
//
//                    Transformer trasform = TransformerFactory.newInstance().newTransformer( xslDoc );
//                    trasform.transform( xmlDoc, new StreamResult( kmlOut ) );
//                    log.info( "KML: " + kmlOut.toString() );
                    
                    // parse KML
                    KMLConfiguration config = new KMLConfiguration();
                    log.debug( "Config properties: " + config.getProperties() );        
                    
                    Parser parser = new Parser( config );
                    SimpleFeature f = (SimpleFeature) parser.parse( in /*new ByteArrayInputStream( kmlOut.toByteArray() )*/ );
                    Collection<Feature> placemarks = (Collection<Feature>)f.getAttribute( "Feature" );
                    log.info( placemarks );
                    
                    features = DefaultFeatureCollections.newCollection();
                    features.addAll( placemarks );
                } 
                catch (Exception e) {
                    features = null;
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, KmlImportWizard.this, i18n.get( "parseError" ), e );
                }
            }            
        };
        job.setShowProgressDialog( job.getName(), false );
        job.schedule();
        
        if (! job.joinAndDispatch( 180000 )) {
            job.cancelAndInterrupt();
            MessageDialog.openInformation( getShell(), "Info", i18n.get( "timeout" ) );
        }
        return features;
    }
    
    
    /**
     * 
     */
    @Override
    public boolean performFinish() {
        final AtomicBoolean ok_flag = new AtomicBoolean( false );
        
        UIJob job = new UIJob( i18n.get( "importTaskTitle" ) ) {
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                monitor.beginTask( getName(), IProgressMonitor.UNKNOWN );

                IResolve service = page2.getTarget();
                SimpleFeatureType featureType = (SimpleFeatureType)features.getSchema();
//                CoordinateReferenceSystem crs = page1.getCrs();

                DataAccess ds = page2.getTarget().resolve( DataAccess.class, new SubProgressMonitor( monitor, 1 ) );
                ds.createSchema( featureType );

                // write the features
                FeatureStore fs = (FeatureStore)ds.getFeatureSource( featureType.getName() );

                // no transaction: save memory                        
                fs.addFeatures( features );

                // reset service in catalog
                Thread.sleep( 1000 );
                if (service instanceof IService) {
                    ResetServiceAction.reset( Collections.singletonList( (IService)service ), new SubProgressMonitor( monitor, 1 ) );
                }
                else if (service instanceof IResolveFolder) {
                    ResetServiceAction.reset( 
                            Collections.singletonList( ((IResolveFolder)service).getService( monitor ) ),
                            new SubProgressMonitor( monitor, 1 ) );                    
                }
                monitor.done();
            }
        };
        job.setUser( true );  //ShowProgressDialog( i18n( "KmlImportWizard.tasktitle" ), false );
        job.schedule();
        return true;
    }

}
