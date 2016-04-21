/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.model;

import java.awt.Color;

import org.polymap.model2.Description;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Simple point style. Roughly modelling: 
 * <ul>
 * <li>{@link org.opengis.style.PointSymbolizer}</li>
 * <li>{@link org.opengis.style.Mark}</li>
 * </ul>
 *
 * @author Falko Br�utigam
 */
public class PointStyle
        extends Style {

    /**
     * Initializes a newly created instance with default values.
     */
    @SuppressWarnings("hiding")
    public static final ValueInitializer<PointStyle> defaults = new ValueInitializer<PointStyle>() {
        @Override
        public PointStyle initialize( PointStyle proto ) throws Exception {
            Style.defaults.initialize( proto );
            return proto;
        }
    };

    @UIOrder( 10 )
    @Description( "Outer stroke: width" )
    @DoubleRange( from=0, to=Double.MAX_VALUE, defaultValue=1 )
    public Property<StylePropertyValue<Double>> strokeWidth;
    
    @UIOrder( 20 )
    @Description( "Outer stroke: color" )
    public Property<StylePropertyValue<Color>>  strokeColor;
    
    @UIOrder( 30 )
    @Description( "Outer stroke: opacity" )
    @DoubleRange( from=0, to=1, defaultValue=1 )
    public Property<StylePropertyValue<Double>> strokeOpacity;
    
    @UIOrder( 40 )
    @Description( "Fill: color" )
    public Property<StylePropertyValue<Color>>  fillColor;
    
    @UIOrder( 50 )
    @Description( "Fill: opacity" )
    @DoubleRange( from=0, to=1, defaultValue=1 )
    public Property<StylePropertyValue<Double>> fillOpacity;
    
}
