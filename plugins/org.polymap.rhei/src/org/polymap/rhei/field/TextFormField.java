/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
package org.polymap.rhei.field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TextFormField
        extends StringFormField {

//    private int             minHeight;
    
    private int                 style = SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL;
    
    /**
     * Create a new field with style set: <code>SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL</code>.
     */
    public TextFormField() {
    }

    /**
     * 
     */
    public TextFormField( int style ) {
        this.style = style;
    }

    public TextFormField addStyle( int styleConstant ) {
        this.style |= styleConstant;
        return this;
    }

    public void init( IFormFieldSite _site ) {
        super.init( _site );
    }

    public void dispose() {
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        createControl( parent, toolkit, SWT.MULTI | style  );
        text.setText( "*\n*\n*\n" );
        return text;
    }

}
