/* 
 * polymap.org
 * Copyright 2009, 2011 Polymap GmbH. All rights reserved.
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

package org.polymap.core.mapeditor;

/**
 * Support interface for {@link MapEditor}. Implementations provide extensions
 * of {@link MapEditor} for several aspects, like navigation, feature selection,
 * editing.
 * <p/>
 * Support interfaces are active or inactive, which means that left mouse button
 * is assigned to the corresponding action or not. Activating one support may
 * deactivate one or more other. The {@link MapEditor} signals this via
 * {@link IMapEditorSupportListener} API.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public interface IMapEditorSupport
        extends IMapEditorSupportListener {

    /**
     * Deregister support listener from {@link MapEditor} and do any
     * additional cleanup.
     */
    public void dispose();

    //public void setActive( boolean active );

    public boolean isActive();

}
