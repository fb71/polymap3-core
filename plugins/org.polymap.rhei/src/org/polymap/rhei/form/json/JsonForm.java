/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 * $Id:$
 */
package org.polymap.rhei.form.json;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opengis.feature.Property;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.DefaultFormPageLayouter;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3 ($Revision: $)
 * @since 3.0
 */
public class JsonForm
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( JsonForm.class );
    
    private JSONObject              json;

    private IFormEditorPageSite     site;

    private IFormEditorToolkit      tk;

    
    protected JsonForm() {
    }
    
    
    public JsonForm( JSONObject json ) {
        assert json != null;
        this.json = json;
    }
    
    
    /**
     * 
     * @param url URL to load the contents of the JSON from.
     * @throws JSONException 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public JsonForm( URL url ) 
    throws JSONException, UnsupportedEncodingException, IOException {
        Reader in = null;        
        try {
            in = new BufferedReader( new InputStreamReader( url.openStream(), "ISO-8859-1" ) );
            json = new JSONObject( new JSONTokener( in ) );
            assert json != null : "Das Formular konnte nicht gelesen werden, da es fehlerhaft ist: " + url;
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    
    protected void setJson( JSONObject json ) {
        assert json != null;
        this.json = json;
    }

    
    public String getId() {
        try {
            return json.getString( "id" );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: id", e );
        }
    }


    public String getTitle() {
        try {
            return json.getString( "title" );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: title", e );
        }
    }


    public void createFormContent( IFormEditorPageSite _site ) {
        log.debug( "createFormContent(): json= " + json );
        this.site = _site;
        this.tk = site.getToolkit();
        DefaultFormPageLayouter layouter = new DefaultFormPageLayouter();

        site.setFormTitle( getTitle() );
        site.setPartTitle( json.optString( "description", "" ) ); // see FormEditorDialog
        site.getPageBody().setLayout( new FormLayout() );
        Composite client = site.getPageBody();
        client.setLayout( layouter.newLayout() );

        try {
            JSONArray fields = json.getJSONArray( "fields" );
            for (int i=0; i<fields.length(); i++) {
                JSONObject field_json = fields.getJSONObject( i );
                
                Composite field = newFormField( client, field_json );
                layouter.setFieldLayoutData( field );
            }
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: " + e, e );
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException( "Field type not valid: " + e, e );
        }
    }

    
    protected Composite newFormField( Composite parent, JSONObject field_json )
    throws JSONException, ClassNotFoundException {
        IFormField formField = null;
        IFormFieldValidator validator = null;
        String jsonValue = field_json.optString( "value", null );
        Object value = jsonValue;
        Object range = field_json.opt( "range" );
        
        // check type -> build default field/validator
        String valueTypeName = field_json.optString( "type", "java.lang.String" );
        Class valueType = Thread.currentThread().getContextClassLoader().loadClass( valueTypeName );

        // range -> picklist
        if (range != null && range instanceof JSONArray) {
            JSONArray array = (JSONArray)range;
            Map<String,Object> rangeValues = new HashMap();
            for (int i=0; i<array.length(); i++) {
                JSONObject elm = (JSONObject)array.get( i );
                rangeValues.put( (i+1) + " - " + elm.getString( "name" ), elm.get( "value" ) );
            }
            PicklistFormField picklist = new PicklistFormField( rangeValues );
//            picklist.setLabelProvider( new LabelProvider() {
//                public String getText( String label, Object value ) {
//                    return StringUtils.substringAfterLast( label, " - " );
//                }
//            });
//            picklist.setForceTextMatch( false );
            formField = picklist;
        }
        // Date
        else if (Date.class.isAssignableFrom( valueType )) {
            formField = new DateTimeFormField();
            try {
                if (jsonValue != null) {
                    value = FastDateFormat.getDateInstance( FastDateFormat.MEDIUM, Locale.GERMANY ).parseObject( jsonValue );
                }
            }
            catch (ParseException e) {
                throw new RuntimeException( e );
            }
        }
        // String
        else if (String.class.isAssignableFrom( valueType )) {
            formField = new StringFormField();
            value = jsonValue;
        }
        // Integer
        else if (Integer.class.isAssignableFrom( valueType )) {
            formField = new StringFormField();
            validator = new NumberValidator( Integer.class, Locale.GERMANY, 10, 0 );
        }
        // Float
        else if (Float.class.isAssignableFrom( valueType )) {
            formField = new StringFormField();
            validator = new NumberValidator( Float.class, Locale.GERMANY, 10, 2 );
            value = jsonValue != null ? new Float( jsonValue ) : null;
        }
        else {
            throw new RuntimeException( "Unhandled valueType: " + valueType );
        }

        // description
        String description = field_json.optString( "description", null );

        // create the form field
        String label = field_json.optString( "label" );
        String name = field_json.getString( "name" );

        Composite result = site.newFormField( parent, 
                findProperty( name, value, valueType ), formField, validator, label, description );
        return result;
    }


    /**
     * Sub classes may overwrite to provide proper properties for the property
     * names found in the JSON form description.
     * 
     * @param propName Property name that was found in the JSON form
     *        description.
     * @param defaultValue
     * @return
     */
    protected Property findProperty( String propName, Object defaultValue, Class valueType ) {
        return new PropertyAdapter( propName, defaultValue, valueType );
    }


    public Action[] getEditorActions() {
        return null;
    }

    
//    /*
//     * 
//     */
//    class NumberValidator
//            implements IFormFieldValidator {
//
//        @Override
//        public Object transform2Field( Object modelValue )
//                throws Exception {
//            // XXX Auto-generated method stub
//            throw new RuntimeException( "not yet implemented." );
//        }
//
//        @Override
//        public Object transform2Model( Object fieldValue )
//                throws Exception {
//            // XXX Auto-generated method stub
//            throw new RuntimeException( "not yet implemented." );
//        }
//
//        @Override
//        public String validate( Object fieldValue ) {
//            // XXX Auto-generated method stub
//            throw new RuntimeException( "not yet implemented." );
//        }
//        
//    }
    
}
