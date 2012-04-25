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
 */
package org.polymap.rhei.internal.form;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * Default implementation of the {@link IFormEditorToolkit} interface.
 * Basically this implementation delegates method calls to {@link FormToolkit}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormEditorToolkit
        implements IFormEditorToolkit {

    private FormToolkit         delegate;

    public static final Color   textBackground = Graphics.getColor( 0xFF, 0xFE, 0xE1 );
    
    public static final Color   textBackgroundFocus = Graphics.getColor( /*0xE6, 0xED, 0xFF*/ /*0xFA, 0xEB, 0xFF*/ 0xFF, 0xED, 0xD2 );

    public static final Color   textBackgroundDisabled = Graphics.getColor( 0xF6, 0xF4, 0xF4 );

    public static final Color   borderFocus = Graphics.getColor( 0xC0, 0xC0, 0xC0 );
    
    public static final Color   labelForeground = Graphics.getColor( 0x70, 0x70, 0x70 );
    
    
    public FormEditorToolkit( FormToolkit delegate ) {
        super();
        this.delegate = delegate;
        this.delegate.setBorderStyle( SWT.BORDER );
    }

    public Button createButton( Composite parent, String text, int style ) {
        Button result = delegate.createButton( parent, text, style );
        //result.setData( WidgetUtil.CUSTOM_VARIANT, "formeditor" );
        return result;
    }

    public Composite createComposite( Composite parent, int style ) {
        return delegate.createComposite( parent, style | SWT.NO_FOCUS );
    }

    public Composite createComposite( Composite parent ) {
        return delegate.createComposite( parent, SWT.NO_FOCUS );
    }

    public Composite createCompositeSeparator( Composite parent ) {
        return delegate.createCompositeSeparator( parent );
    }

    public ExpandableComposite createExpandableComposite( Composite parent, int expansionStyle ) {
        return delegate.createExpandableComposite( parent, expansionStyle | SWT.NO_FOCUS );
    }

    public Form createForm( Composite parent ) {
        return delegate.createForm( parent );
    }

    public FormText createFormText( Composite parent, boolean trackFocus ) {
        return delegate.createFormText( parent, trackFocus );
    }

    public Hyperlink createHyperlink( Composite parent, String text, int style ) {
        return delegate.createHyperlink( parent, text, style );
    }

    public ImageHyperlink createImageHyperlink( Composite parent, int style ) {
        return delegate.createImageHyperlink( parent, style );
    }

    public Label createLabel( Composite parent, String text, int style ) {
        Label result = delegate.createLabel( parent, text, style );
        result.setForeground( labelForeground );
        return result;
    }

    public Label createLabel( Composite parent, String text ) {
        Label result = delegate.createLabel( parent, text );
        result.setForeground( labelForeground );
        //result.setFont( Graphics.getFont( result.getFont().getFontData()[0].
        return result;
    }

    public ScrolledPageBook createPageBook( Composite parent, int style ) {
        return delegate.createPageBook( parent, style );
    }

    public ScrolledForm createScrolledForm( Composite parent ) {
        return delegate.createScrolledForm( parent/*, SWT.NO_FOCUS*/ );
    }

    public Section createSection( Composite parent, int sectionStyle ) {
        Section result = delegate.createSection( parent, sectionStyle | SWT.NO_FOCUS /*| ExpandableComposite.FOCUS_TITLE*/ );
        return result;
    }

    public Label createSeparator( Composite parent, int style ) {
        return delegate.createSeparator( parent, style | SWT.NO_FOCUS );
    }

    public Table createTable( Composite parent, int style ) {
        return delegate.createTable( parent, style | SWT.NO_FOCUS );
    }

    public List createList( Composite parent, int style ) {
        List result = new List( parent, style );
        result.setBackground( textBackground );
        result.setData( WidgetUtil.CUSTOM_VARIANT, "formeditor" );
        return result;
    }

    public Text createText( Composite parent, String value, int style ) {
        Text result = delegate.createText( parent, value, style );
        // XXX background does not work with styling!?
        result.setBackground( textBackground );
        result.setData( WidgetUtil.CUSTOM_VARIANT, "formeditor" );
        return result;
    }

    public Text createText( Composite parent, String value ) {
        Text result = delegate.createText( parent, value );
        result.setBackground( textBackground );
        result.setData( WidgetUtil.CUSTOM_VARIANT, "formeditor" );
        return result;
    }

    public Tree createTree( Composite parent, int style ) {
        return delegate.createTree( parent, style );
    }

    public Combo createCombo( Composite parent, Set<String> values ) {
        return createCombo( parent, values, SWT.DROP_DOWN );
    }

    public Combo createCombo( Composite parent, Set<String> values, int style ) {
        Combo combo = new Combo( parent, style );
        delegate.adapt( combo );
        combo.setBackground( textBackground );
        combo.setData( WidgetUtil.CUSTOM_VARIANT, "formeditor" );
        for (String value : values) {
            combo.add( value );
        }
        return combo;
    }

    public DateTime createDateTime( Composite parent, Date value ) {
        return createDateTime( parent, value, SWT.DROP_DOWN );
    }
    
    public DateTime createDateTime( Composite parent, Date value, int style ) {
        DateTime result = new DateTime( parent, style );
        delegate.adapt( result );
        result.setBackground( textBackground );
        result.setData( WidgetUtil.CUSTOM_VARIANT, "formeditor" );
        
        if (value != null) {
            Calendar cal = Calendar.getInstance();
            result.setDate( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DATE ) );
            result.setTime( cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ) );
        }
        return result;
    }
    
}
