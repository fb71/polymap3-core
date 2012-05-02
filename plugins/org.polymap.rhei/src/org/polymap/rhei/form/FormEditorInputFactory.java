/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated by
 * the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.rhei.form;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;

import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

import org.eclipse.core.runtime.IAdaptable;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectRepository;

/**
 * Factory for re-creating {@link FormEditorInput}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormEditorInputFactory
        implements IElementFactory {

    private static Log log = LogFactory.getLog( FormEditorInputFactory.class );
    

    public IAdaptable createElement( IMemento memento ) {
        String fid = memento.getString( "fid" );
        String layerId = memento.getString( "layerId" );
        if (fid != null && layerId != null) {
            FeatureIterator it = null;
            try {
                ILayer layer = ProjectRepository.instance().findEntity( ILayer.class, layerId );
                if (layer != null) {
                    final PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, true );
                    FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
                    Id filter = ff.id( Collections.singleton( ff.featureId( fid ) ) );

                    it = fs.getFeatures( filter ).features();
                    if (it.hasNext()) {
                        return new FormEditorInput( fs, it.next() );
                    }
                }
            }
            catch (Exception e) {
                log.warn( "Unable to restore FormEditorInput.", e );
            }
            finally {
                if (it != null) { it.close(); }
            }
        }
        return null;
    }
}
