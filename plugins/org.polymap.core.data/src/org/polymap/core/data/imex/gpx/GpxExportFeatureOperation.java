/* 
 * polymap.org
 * Copyright (C) 2014, Polymap GmbH. All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.gpx.GPX;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Encoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GpxExportFeatureOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( GpxExportFeatureOperation.class );

    
    public Status execute( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( 
                context.adapt( FeatureOperationExtension.class ).getLabel(),
                context.features().size() );
    
        final File f = File.createTempFile( "polymap-gpx-export-", ".gpx" );
        f.deleteOnExit();
        OutputStream out = new BufferedOutputStream( new FileOutputStream( f ) );
        
        FeatureCollection features = new ReprojectingFeatureCollection( context.features(), CRS.decode( "EPSG:4326" ) );
        
        try {
            Encoder encoder = new Encoder( new GPXConfiguration() );
            encoder.setIndenting( true );
            encoder.setEncoding( Charset.forName( "UTF-8" ) );
            encoder.setNamespaceAware( false );
            encoder.setOmitXMLDeclaration( false );
            encoder.encode( context.features(), GPX.gpx, out );
        }
        catch (OperationCanceledException e) {
            return Status.Cancel;
        }
        finally {
            IOUtils.closeQuietly( out );

            log.info( FileUtils.readFileToString( f ) );
        }

        // open download        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {
                    @Override
                    public String getContentType() {
                        return "application/gpx+xml; charset=UTF-8";
                    }
                    @Override
                    public String getFilename() {
                        return "polymap-export.gpx";
                    }
                    @Override
                    public InputStream getInputStream() throws Exception {
                        return new BufferedInputStream( new FileInputStream( f ) );
                    }
                    @Override
                    public boolean done( boolean success ) {
                        f.delete();
                        return true;
                    }
                });
                
                log.info( "Download URL: " + url );

//                String filename = view.getLayer() != null
//                        ? view.getLayer().getLabel() + "_export.csv" : "polymap3_export.csv";
//                String linkTarget = "../csv/" + id + "/" + filename;
//                String htmlTarget = "../csv/download.html?id=" + id + "&filename=" + filename;

                ExternalBrowser.open( "download_window", url,
                        ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        });
        monitor.done();
        return Status.OK;
    }


    public Status undo( IProgressMonitor monitor ) throws Exception {
        return Status.OK;
    }

    public Status redo( IProgressMonitor monitor ) throws Exception {
        return Status.OK;
    }

    public boolean canExecute() {
        return true;
    }

    public boolean canRedo() {
        return false;
    }

    public boolean canUndo() {
        return false;
    }
    
}
