/*
 * polymap.org 
 * Copyright 2011, Falko Br�utigam, and other contributors as indicated by
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
package org.polymap.core.data.feature.lucene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.StringReader;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.util.Utilities;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.geometry.BoundingBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import com.vividsolutions.jts.geom.Geometry;

/**
 * {@link Feature} facade for a Lucene {@link Document} representing the feature.
 * <p/>
 * Designed to cache as less as possible values in order to consume as less as
 * possible memory (compared to building every feature using
 * {@link SimpleFeatureBuilder}.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
class LuceneFeature
        implements SimpleFeature {

    private static final Log log = LogFactory.getLog( LuceneFeature.class );

    private Document                    doc;
    
    private String                      identifier;
    
    private SimpleFeatureType           featureType;
    
    private Map<String,PropertyImpl>    properties = new HashMap();
    
    
    public LuceneFeature( Document doc, SimpleFeatureType featureType ) {
        this.doc = doc;
        this.featureType = featureType;
    }

    public Name getName() {
        return featureType.getName();
    }

    public AttributeDescriptor getDescriptor() {
        throw new RuntimeException( "not yet implemented." );
    }

    public FeatureId getIdentifier() {
        return new FeatureId() {

            public String getID() {
                if (identifier == null) {
                    identifier = doc.get( "fid" );
                }
                return identifier;
            }

            public boolean matches( Object rhs ) {
                if (rhs instanceof Feature) {
                    Feature feature = (Feature)rhs;
                    return feature != null && getID().equals( feature.getIdentifier().getID() );
                }   
                return false;
            }
        };
    }

    public SimpleFeatureType getType() {
        return featureType;
    }


    /**
     * 
     */
    class PropertyImpl 
            implements Property {

        protected PropertyDescriptor    descriptor;
        
        protected Object                value;

        
        protected PropertyImpl( PropertyDescriptor descriptor ) {
            assert descriptor != null;
            this.descriptor = descriptor;
            try {
                String stringValue = doc.get( descriptor.getName().getLocalPart() );
                // memory is crucial here
//                doc.removeField( descriptor.getName().getLocalPart() ); 
                Class valueType = descriptor.getType().getBinding();
                
                // Geometry
                if (Geometry.class.isAssignableFrom( valueType )) {
                    value = new GeometryJSON( 4 ).read( new StringReader( stringValue ) );
                }
                // other
                else {
                    throw new RuntimeException( "not yet implemented." );
                    //value = ValueCoder.decode( stringValue, valueType );
                }
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        
        public Object getValue() {
            return value;
        }
        
        public void setValue( Object value ) {
            log.debug( "property= " + getName().getLocalPart() + ", value=" + value );
            this.value = value;
        }
        
        public PropertyDescriptor getDescriptor() {
            return descriptor;
        }

        public Name getName() {
            return getDescriptor().getName();
        }

        public PropertyType getType() {
            return getDescriptor().getType();
        }

        public boolean isNillable() {
            return getDescriptor().isNillable();
        }
        
        public Map<Object, Object> getUserData() {
            throw new RuntimeException( "not yet implemented." );
        }
        
        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PropertyImpl)) {
                return false;
            }
            PropertyImpl other = (PropertyImpl)obj;
            if (!Utilities.equals( descriptor, other.descriptor )) {
                return false;
            }
            if (!Utilities.deepEquals( getValue(), other.getValue() )) {
                return false;
            }
            return true;
        }

        
        public int hashCode() {
            return 37 * descriptor.hashCode()
                + (37 * (value == null ? 0 : value.hashCode()));
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer( getClass().getSimpleName() ).append( ":" );
            sb.append( getDescriptor().getName().getLocalPart() );
            sb.append( "<" );
            sb.append( getDescriptor().getType().getName().getLocalPart() );
            sb.append( ">=" );
            sb.append( getValue() );
            return sb.toString();
        }
        
    }

    /**
     * 
     */
    class GeomPropertyImpl
            extends PropertyImpl
            implements GeometryAttribute {

        protected GeomPropertyImpl( PropertyDescriptor descriptor ) {
            super( descriptor );
        }

        public GeometryType getType() {
            return (GeometryType)super.getType();
        }

        public GeometryDescriptor getDescriptor() {
            return (GeometryDescriptor)super.getDescriptor();
        }

        public BoundingBox getBounds() {
            return (BoundingBox)((Geometry)getValue()).getEnvelope();
        }

        public void setBounds( BoundingBox bounds ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public Identifier getIdentifier() {
            return null;
        }

        public void validate()
                throws IllegalAttributeException {
        }
        
    }

    
    public Collection<? extends Property> getValue() {
        return getProperties();
    }

    public void setValue( Collection<Property> props ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public void setValue( Object props ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public Collection<Property> getProperties() {
        throw new RuntimeException( "not yet implemented." );
//        List<Property> result = new ArrayList();
//        for (EntityType.Property entityProp : entityType.getProperties()) {
//            result.add( getProperty( entityProp.getName() ) );
//        }
//        return result;
    }

    public Collection<Property> getProperties( Name name ) {
        return Collections.singletonList( getProperty( name ) );
    }

    public Collection<Property> getProperties( String name ) {
        return Collections.singletonList( getProperty( name ) );
    }

    public Property getProperty( Name name ) {
        PropertyImpl result = properties.get( name.getLocalPart() );
        if (result == null) {
            result = new PropertyImpl( featureType.getDescriptor( name ) );
            properties.put( name.getLocalPart(), result );
        }
        return result;
    }

    public Property getProperty( String name ) {
        PropertyImpl result = properties.get( name );
        if (result == null) {
            result = new PropertyImpl( featureType.getDescriptor( name ) );
            properties.put( name, result );
        }
        return result;
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        String name = featureType.getGeometryDescriptor().getLocalName();
        PropertyImpl result = properties.get( name );
        if (result == null) {
            result = new GeomPropertyImpl( featureType.getDescriptor( name ) );
        }
        return (GeometryAttribute)result;
        
    }

    public void validate()
            throws IllegalAttributeException {
        throw new RuntimeException( "not yet implemented." );
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        return false;
    }


    public void setDefaultGeometryProperty( GeometryAttribute arg0 ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public BoundingBox getBounds() {
        throw new RuntimeException( "not yet implemented." );
    }

    
    // SimpleFeature
    
    public Object getAttribute( int index ) 
    throws IndexOutOfBoundsException {
        throw new RuntimeException( "not yet implemented." );
    }
    
    public Object getAttribute( String name ) {
        return getProperty( name ).getValue();
    }

    public Object getAttribute( Name name ) {
        return getProperty( name ).getValue();
    }

    public int getAttributeCount() {
        return getProperties().size();
    }

    public List<Object> getAttributes() {
        List<Object> result = new ArrayList();
        for (Property prop : getProperties() ) {
            result.add( prop.getValue() );
        }
        return result;
    }

    public void setAttribute(int index, Object value)
    throws IndexOutOfBoundsException {
        throw new RuntimeException( "not yet implemented." );
//        // first do conversion
//        Object converted = Converters.convert(value, getFeatureType().getDescriptor(index).getType().getBinding());
//        // if necessary, validation too
//        if(validating)
//            Types.validate(featureType.getDescriptor(index), converted);
//        // finally set the value into the feature
//        values[index] = converted;
    }
    
    public void setAttribute(String name, Object value) {
        throw new RuntimeException( "not yet implemented." );
//        final Integer idx = index.get(name);
//        if(idx == null)
//            throw new IllegalAttributeException("Unknown attribute " + name);
//        setAttribute( idx.intValue(), value );
    }

    public void setAttribute(Name name, Object value) {
        setAttribute( name.getLocalPart(), value );
    }

    public void setAttributes(List<Object> values) {
        throw new RuntimeException( "not yet implemented." );
//        for (int i = 0; i < this.values.length; i++) {
//            this.values[i] = values.get(i);
//        }
    }

    public void setAttributes(Object[] values) {
        throw new RuntimeException( "not yet implemented." );
//        setAttributes( Arrays.asList( values ) );
    }

    public Object getDefaultGeometry() {
        return getDefaultGeometryProperty().getValue();
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public String getID() {
        return getIdentifier().getID();
    }

    public void setDefaultGeometry( Object arg0 ) {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
