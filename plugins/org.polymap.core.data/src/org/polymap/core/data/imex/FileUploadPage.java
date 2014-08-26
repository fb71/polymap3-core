/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.imex;

import static org.polymap.core.ui.FormDataFactory.filled;
import static org.polymap.core.ui.FormDataFactory.offset;
import static org.polymap.core.ui.FormDataFactory.Alignment.CENTER;

import java.io.BufferedInputStream;
import java.io.InputStream;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.ui.CatalogTreeViewer;

import org.geotools.data.DataAccess;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;
import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.Messages;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ui.util.SelectionAdapter;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FileUploadPage
        extends WizardPage
        implements IWizardPage, UploadListener {

    private static Log log = LogFactory.getLog( FileUploadPage.class );

    public static final String          ID = "FileUploadPage"; //$NON-NLS-1$
    
    private static final IMessages      i18n = Messages.forPrefix( "FileImportWizard_FileUploadPage" );

    private Upload                      upload;
    
    /** {@link #upload} or a label if {@link #setFile(InputStream)} was called. */
    private Control                     top;
    
    private FeatureTableViewer          featuresViewer;
    
    private CatalogTreeViewer           catalogViewer;

    protected FeatureCollection         features;

    protected IResolve                  target;
    
    private Text                        typeNameText;

    protected String                    typeName;
    
    protected boolean                   createLayer = true;


    protected FileUploadPage() {
        super( ID );
        setTitle( i18n.get( "title" ) );
        setDescription( i18n.get( "description" ) );
    }


    public void setFile( InputStream in, String filename ) {
        if (upload != null) {
            upload.dispose();
            upload = null;

            Label l = new Label( featuresViewer.getControl().getParent(), SWT.NONE );
            l.setText( filename );
            FormDataFactory.filled().clearBottom().applyTo( l );
            top = l;
        }
        
        typeNameText.setText( typeName = FilenameUtils.getBaseName( filename ) );

        parseData( in );
    }
    
    
    public IResolve getTarget() {
        return target;
    }

    public String getTypeName() {
        return typeName;
    }
    
    public boolean isCreateLayer() {
        return createLayer;
    }

    
    @Override
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource( "FileImport" );
    }


    public void createControl( Composite parent ) {
        ((WizardDialog)getWizard().getContainer()).setMinimumPageSize( 450, 500 );

        SashForm sash = new SashForm( parent, SWT.VERTICAL );
        
        Group srcGroup = new Group( sash, SWT.NONE );
        srcGroup.setText( i18n.get( "srcGroup" ) );
        srcGroup.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 3 ).create() );
        
        top = upload = new Upload( srcGroup, SWT.BORDER, SWT.NONE /*Upload.SHOW_PROGRESS | Upload.SHOW_UPLOAD_BUTTON*/ );
        FormDataFactory.filled().clearBottom().applyTo( upload );
        upload.setBrowseButtonText( i18n.get( "browse" ) );
        upload.setUploadButtonText( "Upload" );
        upload.addUploadListener( this );
        upload.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                upload.performUpload();
            }
        });

        // feature table
        featuresViewer = new FeatureTableViewer( srcGroup, SWT.NONE );
        FormDataFactory.filled().top( upload ).applyTo( featuresViewer.getControl() );

        // target group
        Group targetGroup = new Group( sash, SWT.NONE );
        targetGroup.setText( i18n.get( "targetGroup" ) );
        targetGroup.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 3 ).create() );

        Label l = new Label( targetGroup, SWT.NONE );
        l.setText( i18n.get( "typeName" ) );
        l.setToolTipText( i18n.get( "typeNameTip" ) );

        typeNameText = new Text( targetGroup, SWT.BORDER );
        typeNameText.setText( typeName != null ? typeName : "" );
        typeNameText.setToolTipText( i18n.get( "typeNameTip" ) );
        typeNameText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                typeName = typeNameText.getText();
                checkFinish();
            }
        });

        // catalog
        catalogViewer = new CatalogTreeViewer( targetGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, true );
        catalogViewer.setInput( CatalogPlugin.getDefault().getLocalCatalog() );
        catalogViewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                target = new SelectionAdapter( ev.getSelection() ).first( IResolve.class );
                target = target != null && target.canResolve( DataAccess.class ) ? target : null;
                checkFinish();
            }
        });
        
        // create layer check
        Label l2 = new Label( targetGroup, SWT.NONE );
        l2.setText( i18n.get( "createLayer" ) );
        l2.setToolTipText( i18n.get( "createLayerTip" ) );

        final Button layerBtn = new Button( targetGroup, SWT.CHECK );
        layerBtn.setSelection( createLayer );
        layerBtn.addSelectionListener( new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                createLayer = layerBtn.getSelection();
            }
        });
        if (ProjectPlugin.getSelectedMap() == null ) {
            createLayer = false;
            l2.setEnabled( createLayer );
            layerBtn.setEnabled( createLayer );
            layerBtn.setSelection( createLayer );
        }

        l.setLayoutData( offset( 5 ).left( 0 ).top( typeNameText, 0, CENTER ).create() );
        typeNameText.setLayoutData( offset( 0 ).top( 0 ).left( l ).right( 100 ).create() );
        catalogViewer.getControl().setLayoutData( filled().top( typeNameText ).bottom( 100, -25 ).create() );
        l2.setLayoutData( offset( 0 ).left( layerBtn ).top( layerBtn, 3, CENTER ).bottom( 100 ).create() );
        layerBtn.setLayoutData( offset( 0 ).left( 0 ).top( catalogViewer.getControl() ).bottom( 100 ).create() );
        
        sash.setWeights( new int[] { 2, 3 } );
        setControl( parent );
        checkFinish();
    }

    
    protected void updateFeaturesViewer() {
        if (features != null) {
            if (featuresViewer != null) {
                featuresViewer.dispose();
            }
            
            featuresViewer = new FeatureTableViewer( top.getParent(), SWT.NONE );
            FormDataFactory.filled().top( top ).applyTo( featuresViewer.getControl() );

            FeatureType schema = features.getSchema();
            for (PropertyDescriptor prop : schema.getDescriptors()) {
                if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                    // skip Geometry
                }
                else {
                    featuresViewer.addColumn( new DefaultFeatureTableColumn( prop ) );
                }
            }
            featuresViewer.setContent( features );
            featuresViewer.getControl().getParent().layout( true );
        }        
    }
    
    
    protected void checkFinish() {
        setPageComplete( true );
        setMessage( null );
        
        if (features == null) {
            setMessage( i18n.get( "noFeatures" ), DialogPage.WARNING );
            setPageComplete( false );
        }
        else if (target == null) {
            setMessage( i18n.get( "noTarget" ), DialogPage.WARNING );
            setPageComplete( false );
        }
        else if (StringUtils.isEmpty( typeName )) {
            setMessage( i18n.get( "noTypeName" ), DialogPage.WARNING );
            setPageComplete( false );
        }
        if (getWizard() != null && getWizard().getContainer() != null) {
            getWizard().getContainer().updateButtons();
        }
    }

    
    protected void parseData( InputStream in ) {
        setMessage( null );
        try {
            features = ((FileImportWizard)getWizard()).parseKml( in );
            updateFeaturesViewer();
            checkFinish();
        }
        catch (Exception e) {
            Throwable rootCause = Throwables.getRootCause( e );
            log.warn( i18n.get( "uploadError", rootCause.getLocalizedMessage() ), e );
            setMessage( i18n.get( "uploadError", rootCause.getLocalizedMessage() ), DialogPage.ERROR );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }
    
    
    // UploadListener *************************************

    public void uploadInProgress( UploadEvent ev ) {
    }
    
    
    public void uploadFinished( UploadEvent ev ) {
        UploadItem item = upload.getUploadItem();
        log.info( "Uploaded: " + item.getFileName() + ", path=" + item.getFilePath() );

        parseData( new BufferedInputStream( item.getFileInputStream() ) );
        
        // set text only if parseData successfully set features; otherwise
        // ModificationListener of the text would overwrite (checkfinish) error msg
        if (features != null) {
            typeNameText.setText( typeName = FilenameUtils.getBaseName( item.getFileName() ) );
        }
    }

}
