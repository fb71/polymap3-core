/* 
 * polymap.org
 * Copyright 2010, Falko Br�utigam, and other contributors as indicated
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
 * $Id: $
 */
package org.polymap.rhei.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.field.StringFormField;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @version ($Revision$)
 */
public class StandardPageProvider
        implements IFormPageProvider {

    private static Log log = LogFactory.getLog( StandardPageProvider.class );
    
    
    public List<IFormEditorPage> addPages( final FormEditor formEditor, final Feature feature ) {
        log.debug( "feature= " + feature );
  
        List<IFormEditorPage> result = new ArrayList();
        
        result.add( new IFormEditorPage() {

            public void dispose() {
            }
            
            public String getId() {
                return  "_standard_";
            }

            public String getTitle() {
                return  "Standard";
            }

            public Action[] getEditorActions() {
                return null;
            }

            public void createFormContent( IFormEditorPageSite site ) {
                site.setFormTitle( feature.getType().getName().getLocalPart() + ": " + feature.getIdentifier().getID() );
                site.getPageBody().setLayout( new FormLayout() );
  
//                site.getToolkit().createLabel( site.getPageBody(), "Test Label" );
                
                // properties
                Composite last = null;
                FeatureType schema = feature.getType();
                for (PropertyDescriptor prop : schema.getDescriptors()) {
                    
                    Collection<Property> values = feature.getProperties( prop.getName() );
                   
                    for (Property value : values) {
                        Composite field = site.newFormField( 
                                null, value, new StringFormField(), new NullValidator() );
                        
                        FormData layoutData = new FormData();
                        layoutData.left = new FormAttachment( 20, 0 );
                        layoutData.right = new FormAttachment( 80, 0 );
                        layoutData.top = last != null
                                ? new FormAttachment( last, 2 )
                                : new FormAttachment( 0 );
                        field.setLayoutData( layoutData );
                        
                        last = field;
                    }
                }
            }
        });
        return result;
    }

}
