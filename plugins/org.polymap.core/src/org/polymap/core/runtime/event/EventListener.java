/* 
 * polymap.org
 * Copyright 2012-2013, Falko Bräutigam. All rights reserved.
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

import org.polymap.core.ui.UIUtils;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
interface EventListener
        extends java.util.EventListener {

    /**
     * This method was intended to allow {@link DeferringListener} to
     * {@link UIUtils#activateCallback(String)} in the event publishing thread in
     * order to make sure that delayed display handlers can update the UI.
     * 
//     * @deprecated I do not seem to get this to work properly. The code is there in
//     *             the implementations of this method in the sub-classes but it is
//     *             not called by the {@link EventManager} currently.
     */
    public void handlePublishEvent( EventObject ev );

    /**
     * 
     *
     * @param ev
     * @throws Exception
     */
    public void handleEvent( EventObject ev ) throws Exception;

}
