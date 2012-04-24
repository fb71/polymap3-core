/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.rhei.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;

import org.geotools.data.FeatureStore;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import org.polymap.core.data.PipelineFeatureSource;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FormEditorInput
        implements IEditorInput, IPersistableElement {

    private static Log log = LogFactory.getLog( FormEditorInput.class );
    
    public static final String  FACTORY_ID = "org.polymap.rhei.FormEditorInputFactory";

    private FeatureStore        fs;
    
    private Feature             feature;
    
    
    public FormEditorInput( FeatureStore fs, Feature feature ) {
        super();
        assert fs != null : "fs is null!";
        assert feature != null : "feature is null!";
        this.feature = feature;
        this.fs = fs;
    }

    public IPersistableElement getPersistable() {
        return this;
    }

    public void saveState( IMemento memento ) {
        if (feature != null && (fs instanceof PipelineFeatureSource)) {
            memento.putString( "fid", feature.getIdentifier().getID() );
            memento.putString( "layerId", ((PipelineFeatureSource)fs).getLayer().id() );
        }
    }

    public String getFactoryId() {
        return FACTORY_ID;
    }

    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof FormEditorInput) {
            return ((FormEditorInput)obj).feature.equals( feature );
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return feature.hashCode();
    }

    public FeatureStore getFeatureStore() {
        return fs;
    }

    public Feature getFeature() {
        return feature;
    }

    public String getEditorId() {
        return FormEditor.ID;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "FormEditorInput";
    }

    public String getToolTipText() {
        return "tooltip";
    }

    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom( feature.getClass() )) {
            return feature;
        }
        return null;
    }

}
