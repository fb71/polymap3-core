/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated by
 * the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package net.refractions.udig.ui;

import java.util.Comparator;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class FeatureTableViewerSorter
        extends ViewerSorter {

    public boolean isSorterProperty( Object element, String property ) {
        return true;
    }


    public void sort( Viewer viewer, Object[] elements ) {
        super.sort( viewer, elements );
    }


    public int compare( Viewer viewer, Object e1, Object e2 ) {
        if (e1 instanceof Comparable && e2 instanceof Comparable) {
            Comparable c1 = (Comparable)e1;
            Comparable c2 = (Comparable)e2;
            return c1.compareTo( c2 );
        }
        else if (e1 == null) {
            return e2 != null ? 1 : 0;
        }
        else if (e2 == null) {
            return e1 != null ? -1 : 0;
        }
        else {
            return e1.toString().toLowerCase().compareTo( e2.toString().toLowerCase() );
        }
    }


    @Override
    public Collator getCollator() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public int category( Object element ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    protected Comparator getComparator() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}