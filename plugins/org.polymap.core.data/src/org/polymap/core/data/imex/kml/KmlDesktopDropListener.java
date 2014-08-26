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

import java.util.List;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.core.workbench.dnd.DesktopDropEvent;
import org.polymap.core.workbench.dnd.DesktopDropListener;
import org.polymap.core.workbench.dnd.FileDropEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KmlDesktopDropListener
        implements DesktopDropListener {

    private static Log log = LogFactory.getLog( KmlDesktopDropListener.class );


    @Override
    public String onDrop( List<DesktopDropEvent> events ) {
        for (DesktopDropEvent ev : events) {
            if (ev instanceof FileDropEvent) {
                final FileDropEvent fev = (FileDropEvent)ev;
                log.info( "File: " + fev.getFileName() + " - " + fev.getContentType() );
                if (fev.getFileName().endsWith( ".kml" )) {
                    try {
                        IWorkbench workbench = PlatformUI.getWorkbench();
                        IWizardDescriptor descriptor = workbench.getImportWizardRegistry().findWizard( KmlImportWizard.ID );
                        final KmlImportWizard wizard = (KmlImportWizard)descriptor.createWizard();
                        wizard.init( workbench, StructuredSelection.EMPTY );

                        Polymap.getSessionDisplay().asyncExec( new Runnable() {
                            public void run() {
                                try {
                                    wizard.setFile( fev.getInputStream(), fev.getFileName() );
                                }
                                catch (IOException e) {
                                    throw new RuntimeException( e );
                                }
                            }                            
                        });

                        WizardDialog dialog = new WizardDialog( PolymapWorkbench.getShellToParentOn(), wizard );
                        dialog.setTitle( wizard.getWindowTitle() );
                        dialog.open();
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            }
        }
        return null;
    }

}
