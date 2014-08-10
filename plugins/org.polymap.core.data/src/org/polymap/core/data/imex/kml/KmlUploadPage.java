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
package org.polymap.core.data.imex.kml;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.geotools.feature.FeatureCollection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KmlUploadPage
        extends WizardPage
        implements IWizardPage, UploadListener {

    private static Log log = LogFactory.getLog( KmlUploadPage.class );

    public static final String          ID = "KmlUploadPage"; //$NON-NLS-1$

    private Upload                      upload;
    
    private List                        tablesList;

    private FeatureCollection           features;


    protected KmlUploadPage() {
        super( ID );
        setTitle( "KML-Datei auswählen." );
        setDescription( "Wählen Sie eine *.kml für den Import aus.");
    }


    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite( parent, SWT.NONE );
        fileSelectionArea.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        upload = new Upload( fileSelectionArea, SWT.BORDER, /*Upload.SHOW_PROGRESS |*/ Upload.SHOW_UPLOAD_BUTTON );
        upload.setBrowseButtonText( "Browse" );
        upload.setUploadButtonText( "Upload" );
        upload.addUploadListener( this );
        upload.setLayoutData( FormDataFactory.filled().clearBottom().create() );
    
        tablesList = new List( fileSelectionArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
        tablesList.setLayoutData( FormDataFactory.filled().top( upload ).create() );
        tablesList.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                checkFinish();
            }
        });
        
        setControl( fileSelectionArea );
        checkFinish();
    }

    
    protected void checkFinish() {
        setPageComplete( features != null );
        getWizard().getContainer().updateButtons();
    }

    
    // UploadListener *************************************

    public void uploadInProgress( UploadEvent ev ) {
    }
    
    
    public void uploadFinished( UploadEvent ev ) {
        InputStream in = null;
        try {
            UploadItem item = upload.getUploadItem();
            log.info( "Uploaded: " + item.getFileName() + ", path=" + item.getFilePath() );

            in = new BufferedInputStream( item.getFileInputStream() );
            features = ((KmlImportWizard)getWizard()).parseKml( in );
        } 
        finally {
            IOUtils.closeQuietly( in );
        }
        checkFinish();
    }

}
