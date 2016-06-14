/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.Messages;
import org.polymap.core.runtime.session.SessionContext;

/**
 * Handles delayed event publishing. {@link Timer} is used to implement the delayed
 * execution.
 * 
 * @see JobDeferringListener
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class TimerDeferringListener
        extends DeferringListener {

    private static Log log = LogFactory.getLog( TimerDeferringListener.class );

    private static Timer                scheduler = new Timer( "TimerDeferringListener.scheduler", true );

    private SchedulerTask               task;
    
    private SessionContext              sessionContext;
    

    public TimerDeferringListener( EventListener delegate, int delay, int maxEvents ) {
        super( delegate, delay, maxEvents );
        this.sessionContext = SessionContext.current();
    }

    
    @Override
    public void handleEvent( EventObject ev ) throws Exception {
        synchronized (this) {
            if (task == null) {
                task = new SchedulerTask().schedule();
            }
            task.events.add( ev );
        }
    }

    
    /**
     * 
     */
    protected class SchedulerTask
            extends TimerTask {
        
        private volatile List<EventObject>  events = new ArrayList( 128 );
        
        public SchedulerTask schedule() {
            try {
                scheduler.schedule( this, delay );
            }
            catch (IllegalStateException e) {
                // Timer already cancelled (?)
                log.warn( "", e );
            }
            return this;
        }
        

        @Override
        public void run() {
            // primarily for testing; Eclipse's JobManager < 3.7.x does schedule in JUnit
            if (sessionContext == null) {
                assert !isDisplayListener();
                doRun();
            }
            // avoid overhead of a Job if events are handled in UI thread anyway
            else if (isDisplayListener()) {
                sessionContext.execute( () -> {
                    doRun();
                    
                    events.forEach( ev -> 
                            SessionUICallbackCounter.instance().jobFinished( TimerDeferringListener.this ) );
                });
            }
            else {
                Job job = new Job( Messages.get( "DeferringListener_jobTitle" ) ) {
                    @Override
                    protected IStatus run( IProgressMonitor monitor ) {
                        sessionContext.execute( () -> doRun() );
                        return Status.OK_STATUS;
                    }
                };
                job.setSystem( true );
                job.schedule();
            }
        }

        
        protected void doRun() {
            synchronized (TimerDeferringListener.this) {
                TimerDeferringListener.this.task = null;
            }

            if (!events.isEmpty()) {
                try {
                    DeferredEvent dev = new DeferredEvent( TimerDeferringListener.this, events );
                    delegate.handleEvent( dev );
                }
                catch (Throwable e) {
                    log.warn( "Error while handling deferred events.", e );
                }
            }
        }
    };

}
