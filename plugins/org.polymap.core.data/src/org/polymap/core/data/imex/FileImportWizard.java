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
package org.polymap.core.data.imex;

import static com.google.common.collect.Iterables.find;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveFolder;
import net.refractions.udig.catalog.IService;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureStore;
import org.geotools.data.store.ReTypingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.catalog.actions.ResetServiceAction;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.NewLayerOperation;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 *
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FileImportWizard 
        extends Wizard 
        implements INewWizard {

    private static Log log = LogFactory.getLog( FileImportWizard.class );

    private static final IMessages      i18n = Messages.forPrefix( "FileImportWizard" );
    
    private final Map<String,String>    params = new HashMap();

    protected FeatureCollection         features;

    protected FileUploadPage            page1;


    public FileImportWizard() {
        super();
    }

    
    @Override
    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle( i18n.get( "windowTitle" ) );
        setDefaultPageImageDescriptor( AbstractUIPlugin.imageDescriptorFromPlugin(
                DataPlugin.PLUGIN_ID, "icons/workset_wiz.png" ) );
        setNeedsProgressMonitor( true );
        setHelpAvailable( true );
    }

    
    @Override
    public void setContainer( IWizardContainer container ) {
        // does not work (without DataPlugin static WizardDialog.setDialogHelpAvailable()
        if (container instanceof WizardDialog) {
            ((WizardDialog)container).setHelpAvailable( true );
        }
        super.setContainer( container );
    }


    @Override
    public void addPages() {
        super.addPages();
        addPage( page1 );
    }


    public void setFile( InputStream in, String filename ) {
        page1.setFile( in, filename );
    }
    
    
    public FeatureCollection getFeatures() {
        return features;
    }


    /**
     * Parses the given stream and stores the result in {@link #features}.
     * 
     * @param in The data to parse. Stream is closed after this method returns.
     * @return The resulting feeatures, or null if an error occured.
     * @throws Exception 
     */
    protected abstract FeatureCollection parseKml( InputStream in ) throws Exception;
    
    
    /**
     * 
     */
    @Override
    public boolean performFinish() {
        final AtomicBoolean ok_flag = new AtomicBoolean( false );
        
        IRunnableWithProgress task = new IRunnableWithProgress() {
            public void run( final IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
                try {
                    monitor.beginTask( i18n.get( "finishTaskTitle" ), 3 );

                    IResolve service = page1.getTarget();
                    SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();

                    // adjust type name
                    SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                    ftb.init( schema );
                    ftb.setName( page1.getTypeName() );
                    //ftb.setNamespaceURI( "" );
                    final SimpleFeatureType targetSchema = ftb.buildFeatureType();
                    ReTypingFeatureCollection targetFeatures = new ReTypingFeatureCollection( features, targetSchema );

//                DefaultTransaction tx = new DefaultTransaction( "import" );

                    // find service and create schema
                    SubProgressMonitor subMonitor = new SubProgressMonitor( monitor, 1 );
                    subMonitor.beginTask( "Finding Service...", 1 );
                    DataAccess ds = service.resolve( DataAccess.class, subMonitor );
                    try {
                        ds.getSchema( targetSchema.getName() ).getName();
                    }
                    catch (Exception e) {
                        ds.createSchema( targetSchema );
                    }
                    monitor.worked( 1 );

                    // write the features
                    monitor.subTask( "Writing Features..." );
                    FeatureStore fs = (FeatureStore)ds.getFeatureSource( targetSchema.getName() );
                    fs.addFeatures( targetFeatures );
                    monitor.worked( 1 );

                    // reset service in catalog
                    Thread.sleep( 1000 );
                    subMonitor = new SubProgressMonitor( monitor, 1 );
                    subMonitor.beginTask( "Resetting Service...", 1 );
                    if (service instanceof IService) {
                        ResetServiceAction.reset( Collections.singletonList( (IService)service ), subMonitor );
                    }
                    else if (service instanceof IResolveFolder) {
                        ResetServiceAction.reset( 
                                Collections.singletonList( ((IResolveFolder)service).getService( monitor ) ), subMonitor );                    
                    }
                    monitor.worked( 1 );
                    
                    // create layer
                    if (page1.isCreateLayer()) {
                        subMonitor = new SubProgressMonitor( monitor, 1 );
                        subMonitor.beginTask( "Creating Layer...", 1 );

                        try {
                            IService _service = service instanceof IService ? (IService)service : ((IResolveFolder)service).getService( monitor );
                            IGeoResource geores = find( _service.resources( subMonitor ), new Predicate<IGeoResource>() {
                                public boolean apply( IGeoResource input ) {
                                    try {
                                        return input.getInfo( new NullProgressMonitor() ).getName().equals( targetSchema.getName().getLocalPart() );
                                    }
                                    catch (IOException e) {
                                        log.warn( "", e );
                                        return false;
                                    }
                                }
                            });
                            NewLayerOperation op = new NewLayerOperation(); 
                            op.init( ProjectPlugin.getSelectedMap(), geores ); 
                            OperationSupport.instance().execute( op, false, false );
                        }
                        catch (ExecutionException e) {
                            throw new InvocationTargetException( e.getCause() );
                        }
                        monitor.worked( 1 );
                    }
                    monitor.done();
                }
                catch (IOException e) {
                    throw new InvocationTargetException( e );
                }
            }
        };
        try {
            getContainer().run( true, true, task );
        }
        catch (InvocationTargetException e) {
            features = null;
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, FileImportWizard.this, i18n.get( "finishTaskError" ), e.getCause() );
        }
        catch (InterruptedException e) {
            //MessageDialog.openInformation( getShell(), "Info", i18n.get( "timeout" ) );
        }
        return true;
    }

}
