/* 
 * polymap.org
 * Copyright (C) 2012-2016, Falko Bräutigam. All rights reserved.
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

import static org.polymap.core.runtime.UIThreadExecutor.async;
import static org.polymap.core.runtime.UIThreadExecutor.asyncFast;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.session.SessionSingleton;
import org.polymap.core.ui.UIUtils;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class DeferringListener
        extends DecoratingListener {

    private static Log log = LogFactory.getLog( DeferringListener.class );

    protected int                       delay;
    
    protected int                       maxEvents = 10000;

    
    public DeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate );
        assert delay > 0;
        this.delay = delay;
        assert maxEvents > 100;
        this.maxEvents = maxEvents;

//        // XXX work around for deprecated EventListener#handlePublishEvent()
//        if (isDisplayListener()) {
//            UIUtils.activateCallback( "DeferringListener" );
//        }
    }


    protected boolean isDisplayListener() {
        return delegate instanceof DisplayingListener;
    }
    

    @Override
    public void handlePublishEvent( EventObject ev ) {
        super.handlePublishEvent( ev );

        if (isDisplayListener()) {
            SessionUICallbackCounter.instance().jobStarted( this );
        }
    }



    /**
     * 
     */
    public static class DeferredEvent
            extends Event {
    
        private List<EventObject>       events;
        
        DeferredEvent( Object source, List<EventObject> events ) {
            super( source );
            assert events != null;
            this.events = new ArrayList( events );
        }
        
        public List<EventObject> events() {
            return events;
        }
        
        public List<EventObject> events( EventFilter filter ) {
            return events.stream()
                    .filter( ev -> filter.apply( ev ) )
                    .collect( Collectors.toList() );
        }
    }



    /**
     * Keep a callback request open while there are pending delayed, display events.
     */
    static class SessionUICallbackCounter
            extends SessionSingleton {
        
        protected static SessionUICallbackCounter instance() {
            //return SingletonUtil.getSessionInstance( ServerPushManager.class );
            return instance( SessionUICallbackCounter.class );
        }
        
        
        // instance ***************************************
        
        private Map<String,Integer> activatedIds = new ConcurrentHashMap();
        
        
        protected void jobStarted( DeferringListener listener ) {
            if (listener.isDisplayListener()) {
                String id = String.valueOf( listener.hashCode() );

                Integer activeCount = activatedIds.compute( id, (k,v) -> v != null ? v+1 : 1 );
                log.info( "id: " + id + ", activeCount: +" + activeCount );            

                if (activeCount == 1) {
                    asyncFast( () -> { 
                        UIUtils.activateCallback( id );
                        log.info( "Callback started for: " + id + ". counter: " + activatedIds.size() );
                    });
                }
            }
        }
        
        protected void jobFinished( DeferringListener listener ) {
            if (listener.isDisplayListener()) {
                String id = String.valueOf( listener.hashCode() );

                Integer activeCount = activatedIds.compute( id, (k,v) -> v>1 ? v-1 : null );
                log.info( "id: " + id + ", activeCount: -" + activeCount );            

                if (activeCount == null) {
                    async( () -> { 
                        UIUtils.deactivateCallback( id );
                        log.info( "Callback finished for: " + id + ". counter: " + activatedIds.size() );
                    });
                }
            }
        }
    }
    
}
