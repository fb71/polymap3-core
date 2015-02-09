/*
 * uDig - User Friendly Desktop Internet GIS client http://udig.refractions.net (C)
 * 2004, Refractions Research Inc.
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core;

import org.apache.commons.lang.StringUtils;

import org.eclipse.jface.dialogs.IconAndMessageDialog;

import org.polymap.core.DialogAuthenticator.NamePassword;
import org.polymap.core.runtime.IMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AuthenticationDialog
        extends IconAndMessageDialog {

    private static final IMessages  i18n = Messages.forPrefix( "AuthenticationDialog" );
    
    private String  username = "";

    private String  password = "";

    private Text    usernameText;

    private Text    passwordText;

    private String  serviceUrl;


    public AuthenticationDialog( Shell parentShell, String serviceUrl, NamePassword defaults ) {
        super( parentShell );
        setShellStyle( getShellStyle() | SWT.RESIZE );
        this.serviceUrl = serviceUrl;
        if (defaults != null) {
            username = defaults.name;
            password = defaults.passwd;
        }
    }


    // protected Point getInitialSize() {
    // return new Point(400, 400);
    // }

    @Override
    protected Image getImage() {
        return getWarningImage();
    }


    protected Control createDialogArea( Composite parent ) {
        message = i18n.get( "prompt", StringUtils.abbreviate( serviceUrl, 65 ) );

        Composite composite = (Composite)super.createDialogArea( parent );
        ((GridLayout)composite.getLayout()).numColumns = 2;
        ((GridLayout)composite.getLayout()).makeColumnsEqualWidth = false;

        createMessageArea( composite );

        Label usernameLabel = new Label( composite, SWT.NONE );
        usernameLabel.setText( i18n.get( "username" ) );
        usernameText = new Text( composite, SWT.BORDER );
        usernameText.setText( username );
        usernameText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
        Label passwordLabel = new Label( composite, SWT.NONE );
        passwordLabel.setText( i18n.get( "password" ) );
        passwordText = new Text( composite, SWT.BORDER | SWT.PASSWORD );
        passwordText.setText( password );
        passwordText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

        GridData gridData = new GridData( SWT.LEFT, SWT.FILL, true, false );
        gridData.horizontalSpan = 2;

        return composite;
    }


    protected void okPressed() {
        username = usernameText.getText();
        password = passwordText.getText();
        super.okPressed();
    }


    public String getPassword() {
        return password;
    }


    public String getUsername() {
        return username;
    }


    protected void configureShell( Shell newShell ) {
        newShell.setText( i18n.get( "title" ) );
        //newShell.setImage( UiPlugin.getDefault().create( "icon32.gif" ).createImage() ); //$NON-NLS-1$
    }
    
}
