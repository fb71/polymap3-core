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

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.ui.CatalogTreeViewer;
import net.refractions.udig.ui.FeatureTableControl;

import org.geotools.data.DataAccess;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.data.Messages;
import org.polymap.core.project.ui.util.SelectionAdapter;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.IMessages;

/**
 * Choose a IResolve from Catalog to be used as target for an import. Also displays
 * already imported features.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ChooseTargetWizardPage
        extends WizardPage {

    private static Log log = LogFactory.getLog( ChooseTargetWizardPage.class );

    private static final IMessages    i18n = Messages.forPrefix( "ChooseTargetWizardPage" );

    public static final String        ID = "ChooseTargetWizardPage"; //$NON-NLS-1$
    
    private FeatureTableControl       tableViewer;
    
    private FeatureCollection<SimpleFeatureType,SimpleFeature> features;

    private Composite                 fileSelectionArea;

    private Text                      typeNameText;

    private String                    typeName;
    
    private IResolve                  target;


    public ChooseTargetWizardPage( String pageName ) {
        super( ID );
        setTitle( pageName != null ? pageName : i18n.get( "title" ) );
        setDescription( i18n.get( "description" ) );        
    }

    
    @Override
    public Wizard getWizard() {
        return (Wizard)super.getWizard();
    }

    
    public IResolve getTarget() {
        return target;
    }

    
    public String getTypeName() {
        return typeName;
    }


    @Override
    public void createControl( Composite parent ) {
        getWizard().getShell().setSize( 520, 650 );
        
        fileSelectionArea = new Composite( parent, SWT.NONE );
        fileSelectionArea.setLayout( new GridLayout() );

        Group inputGroup = new Group( fileSelectionArea, SWT.None );
        inputGroup.setText( i18n.get( "importinto" ) );
        inputGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ) );
        inputGroup.setLayout( new FormLayout() );

        Label l = new Label( inputGroup, SWT.NONE );
        l.setText( i18n.get( "defaultTypename" ) );
        l.setLayoutData( SimpleFormData.offset( 5 ).left( 0 ).top( 0 ).create() );
        
//        typeName = getWizard().getCsvFilename();
        typeNameText = new Text( inputGroup, SWT.BORDER );
        typeNameText.setLayoutData( SimpleFormData.offset( 5 ).left( l ).right( 100 ).create() );
        typeNameText.setText( typeName != null ? typeName : "" );
        typeNameText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                typeName = typeNameText.getText();
                log.info( "typeName= " + typeName );
//                getWizard().createCsvFeatureCollection();
            }
        });
        
        CatalogTreeViewer catalogViewer = new CatalogTreeViewer( inputGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, true );
        catalogViewer.getControl().setLayoutData( SimpleFormData.offset( 5 ).left( 0 ).right( 100 ).top( typeNameText ).height( 150 ).bottom( 100 ).create() );
        catalogViewer.setInput( CatalogPlugin.getDefault().getLocalCatalog() );
        catalogViewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                target = new SelectionAdapter( ev.getSelection() ).first( IResolve.class );
                target = target != null && target.canResolve( DataAccess.class ) ? target : null;
                checkFinish();
            }
        });
        
        setControl( fileSelectionArea );
        
        checkFinish();
    }

    
    

    
//    public void setVisible( boolean visible ) {
//        super.setVisible( visible );
//
//        if (visible) {
//            getWizard().createCsvFeatureCollection();
//            setFeatureCollection( getWizard().csvFeatureCollection );
//        }
//    }

    
    public void setFeatureCollection( FeatureCollection fc ) {
        this.features = fc;
        
        try {
            log.debug( "Features size: " + features.size() );
            if (tableViewer == null) {
                GridData gridData1 = new GridData();
                gridData1.horizontalSpan = 2;
                gridData1.horizontalAlignment = GridData.FILL;
                gridData1.grabExcessHorizontalSpace = true;
                gridData1.grabExcessVerticalSpace = true;
                gridData1.verticalAlignment = GridData.FILL;
                Composite comp = new Composite( fileSelectionArea, SWT.NONE );
                comp.setLayout( new FillLayout() );
                comp.setLayoutData( gridData1 );
                
                tableViewer = new FeatureTableControl( comp, features );
                fileSelectionArea.layout( true );
            }
            else {
                tableViewer.setFeatures( features );
                fileSelectionArea.layout( true );
            }
        }
        catch (Exception e) {
            log.warn( "unhandled: ", e );
        }
    }


    protected void checkFinish() {
        setMessage( null );
        setPageComplete( true );
        
        if (target == null) {
            setMessage( i18n.get( "notarget" ), WARNING );
            setPageComplete( false );
        }
        if (StringUtils.isEmpty( typeName )) {
            setMessage( i18n.get( "notypename" ), WARNING );
            setPageComplete( false );
        }
    }
    
}
