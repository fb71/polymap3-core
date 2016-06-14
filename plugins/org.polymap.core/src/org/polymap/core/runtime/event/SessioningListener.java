/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.runtime.event;

import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.runtime.session.SessionContext;

/**
 * Preserves the {@link SessionContext} of a subscribed listener
 * and restores this context for every event dispatch
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class SessioningListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( SessioningListener.class );
    
    private static Map<EventObject,SessionContext>  publishingContexts = new ConcurrentHashMap();
    
    /**
     * 
//     * @deprecated Depends on {@link EventListener#handlePublishEvent(EventObject)}.
     */
    public static SessionContext publishingContext( EventObject ev ) {
        //assert publishingContexts.containsKey( ev ) : "No publishing SessionContext.";
        return publishingContexts.get( ev );    
    }
    
    
    // instance *******************************************
    
    private SessionContext          session;
    
    private Object                  mapKey;

    private Class                   handlerClass;
    

    /**
     * 
     * @param delegate
     * @param mapKey
     * @param handlerClass 
     */
    public SessioningListener( EventListener delegate, Object mapKey, final SessionContext session, Class handlerClass ) {
        super( delegate );
        assert mapKey != null;
        assert session != null;
        this.session = session;
        this.mapKey = mapKey;
        this.handlerClass = handlerClass;
    }

    
    
    @Override
    public void handlePublishEvent( EventObject ev ) {
        //
        publishingContexts.put( ev, SessionContext.current() );

        //
        if (session != null && !session.isDestroyed()) {
            session.execute( () -> {
                SessioningListener.super.handlePublishEvent( ev );
            });
        }
    }



    @Override
    public void handleEvent( final EventObject ev ) throws Exception {
        try {
            if (session != null) {
                if (!session.isDestroyed()) {
                    session.execute( () -> {
                        delegate.handleEvent( ev );
                        return null;
                    });
                }
                else {
                    log.warn( "Removing event handler for destroyed session: " + session.getClass().getSimpleName() + ", handler: " + handlerClass );
                    EventManager.instance().removeKey( mapKey );
                    session = null;
                    delegate = null;
                }
            }
        }
        finally {
            publishingContexts.remove( ev );
            log.debug( "Global publishingContexts: " + publishingContexts.size() );
        }
    }

}
