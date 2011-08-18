/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.spi;

import java.util.Date;

import org.eclipse.core.runtime.IPath;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IContentNode {
    
    /**
     * Note that this name MUST be consistent with URL resolution in your
     * ResourceFactory
     * <p/>
     * If they aren't consistent Milton will generate a different href in PropFind
     * responses then what clients have request and this will cause either an error
     * or no resources to be displayed
     * 
     * @return - the name of this resource. Ie just the local name, within its folder
     */
    public String getName();

    public IPath getParentPath();

    /**
     * The date and time that this resource, or any part of this resource, was last
     * modified. For dynamic rendered resources this should consider everything which
     * will influence its output.
     * <p/>
     * Resources for which no such date can be calculated should return null.
     * <P/>
     * This field, if not null, is used by the font end systems to produce optimized
     * replies in case the resource has not modified since last request and/or
     * caching.
     * <p/>
     * Although nulls are explicitly allowed, certain front end systems and/or client
     * applications might require modified dates for file browsing. For example, the
     * command line client on Vista doesn't work properly with WebDAV server if this
     * is null.
     */
    public Date getModifiedDate();

    public IContentProvider getProvider();
    
    public Object getSource();

    public Object putData( String key, Object value );
    
    public Object getData( String key );
    
}
