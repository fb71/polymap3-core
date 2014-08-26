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
package org.polymap.core.data.imex.gpx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.wizard.IWizardPage;

import org.polymap.core.data.Messages;
import org.polymap.core.data.imex.FileUploadPage;
import org.polymap.core.runtime.IMessages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GpxUploadPage
        extends FileUploadPage
        implements IWizardPage, UploadListener {

    private static Log log = LogFactory.getLog( GpxUploadPage.class );

    private static final IMessages      i18n = Messages.forPrefix( "GpxImportWizard_GpxUploadPage" );

    
    protected GpxUploadPage() {
        super();
        setTitle( i18n.get( "title" ) );
    }

}
