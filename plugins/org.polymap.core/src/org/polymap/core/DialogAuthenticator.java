package org.polymap.core;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Optional;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.window.Window;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * This is an Authenticator used when URL connection negotiation needs to ask for the
 * users credentials.
 */
public class DialogAuthenticator
        extends Authenticator {

    private static Log log = LogFactory.getLog( DialogAuthenticator.class );

    public static final String  FILENAME = "passwd.properties";
    public static final String  DELIMITER = "||";
    
    private Properties          props;
    
    private Set<String>         promptedUrlKeys = new ConcurrentSkipListSet();

    /**
     * The {@link Set} of nodeKeys that this authenticator has tried the stored
     * username/password pair for. This is to make sure that the user is asked to
     * reenter username/password instead of reusing the old invalid
     * username/password.
     */
    private Set<String>         triedUrlKeys = new ConcurrentSkipListSet();


    protected class NamePassword {
        protected String    name;
        protected String    passwd;

        public NamePassword() {
        }

        public NamePassword( NamePassword other ) {
            this.name = other.name;
            this.passwd = other.passwd;
        }
    }
    
    
    protected DialogAuthenticator() {
        props = new Properties();
        InputStream in = null;
        try {
            File f = new File( Polymap.getWorkspacePath().toFile(), FILENAME );
            if (f.exists()) {
                in = new FileInputStream( f );
                props.load( in );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }
    
    
    protected PasswordAuthentication getPasswordAuthentication() {
        String urlKey = requestUrlKey();

        NamePassword entry = load( urlKey );

        // other thread has already prompted for this key -> wait/poll
        if (promptedUrlKeys.contains( urlKey )) {
            while (entry == null && promptedUrlKeys.contains( urlKey )) {
                //
                log.info( "waiting for prompted url: " +  urlKey );
                try { Thread.sleep( 3000 ); } catch (InterruptedException e) {}
                entry = load( urlKey );
            }
            log.info( "got prompted entry: " + entry.name + "/" + entry.passwd );
            return new PasswordAuthentication( entry.name, entry.passwd.toCharArray() );
        }
        
        // no entry yet or already tried (hence wrong) -> prompt for password
        else if (entry == null || triedUrlKeys.contains( urlKey )) {
            log.info( "no entry or already tried (wrong): " +  urlKey );
            try {
                promptedUrlKeys.add( urlKey );
                
                final NamePassword newEntry = new NamePassword();
                final NamePassword currentEntry = entry != null ? new NamePassword( entry ) : null;
                
                Polymap.getSessionDisplay().syncExec( new Runnable() {
                    public void run() {
                        promptForPassword( newEntry, currentEntry );
                    }
                });
                store( urlKey, newEntry );
                triedUrlKeys.remove( urlKey );
                return new PasswordAuthentication( newEntry.name, newEntry.passwd.toCharArray() );
            }
            finally {
                promptedUrlKeys.remove( urlKey );                
            }
        }
        
        // use stored
        else {
            log.info( "stored passwd: " +  urlKey );
            triedUrlKeys.add( urlKey );
            return new PasswordAuthentication( entry.name, entry.passwd.toCharArray() );            
        }
    }


    private String requestUrlKey() {
        try {
            String url = getRequestingURL().toString();
            String baseUrl = StringUtils.substringBefore( url, "?" );
            return URLEncoder.encode( baseUrl, "UTF-8" );
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( e );
        }
    }


    private void store( String urlKey, NamePassword entry ) {
        props.put( urlKey, entry.name + DELIMITER + entry.passwd );
        
        OutputStream out = null;
        try {
            File f = new File( Polymap.getWorkspacePath().toFile(), FILENAME );
            out = new FileOutputStream( f );
            props.store( out, "Password used by " + DialogAuthenticator.class.getName() );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( out );
        }
    }


    private NamePassword load( String urlKey ) {
        String s = props.getProperty( urlKey );
        if (s != null) {
            NamePassword result = new NamePassword();
            result.name = StringUtils.substringBefore( s, DELIMITER );
            result.passwd = StringUtils.substringAfter( s, DELIMITER );
            return result;
        }
        else {
            return null;
        }
    }

    
    protected NamePassword promptForPassword( NamePassword entry, NamePassword currentEntry ) {
        Shell shell = PolymapWorkbench.getShellToParentOn();
        AuthenticationDialog dialog = new AuthenticationDialog( shell, getRequestingURL().toString(), currentEntry );
        dialog.setBlockOnOpen( true );
        int result = dialog.open();
        if (result == Window.CANCEL) {
            return null;
        }
        entry.name = Optional.fromNullable( dialog.getUsername() ).or( "" );
        entry.passwd = Optional.fromNullable( dialog.getPassword() ).or( "" );
        return entry;
    }
    
}
