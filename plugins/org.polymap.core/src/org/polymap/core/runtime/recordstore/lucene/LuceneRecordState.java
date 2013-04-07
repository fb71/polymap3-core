/* 
 * polymap.org
 * Copyright 2011-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.recordstore.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import com.google.common.base.Supplier;

import org.polymap.core.runtime.recordstore.IRecordFieldSelector;
import org.polymap.core.runtime.recordstore.IRecordState;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class LuceneRecordState
        implements IRecordState {

    private static Log log = LogFactory.getLog( LuceneRecordState.class );

    public static final String  ID_FIELD = "identity";
    
    private static long         idCount = System.currentTimeMillis();
    
    /**
     * 
     */
    public static class Document
            extends StoredFieldVisitor 
            implements Iterable<IndexableField> {
        
        private IRecordFieldSelector        fieldSelector;

        private Map<String,IndexableField>  fields = new HashMap( 48 );

        public Document( IRecordFieldSelector fieldSelector ) {
            this.fieldSelector = fieldSelector;
        }

        public Document() {
        }

        public void add( Field field ) {
            IndexableField previous = fields.put( field.name(), field );
            if (previous != null) {
                throw new IllegalStateException( "Field already exists: " + field.name() );
            }
        }
        
        public Field put( Field field ) {
            return (Field)fields.put( field.name(), field );
        }
        
        public boolean removeField( String key ) {
            return fields.remove( key ) != null;
        }
        
        public Field getField( String key ) {
            return (Field)fields.get( key );
        }

        /** Get or create field. */
        public Field getField( String key, Supplier<Field> supplier ) {
            Field field = (Field)fields.get( key );
            if (field == null) {
                field = supplier.get();
                put( field );
                return field;
            }
            return field;
        }

        @Override
        public Iterator<IndexableField> iterator() {
            return fields.values().iterator();
        }

        @Override
        public Status needsField( FieldInfo fieldInfo ) throws IOException {
            if (fieldInfo.name.equals( LuceneRecordState.ID_FIELD )) {
                return Status.YES;
            }
            else if (fieldSelector != null && fieldSelector.accept( fieldInfo.name )) { 
                return Status.YES;
            }
            return Status.NO;
        }
        
        @Override
        public void binaryField( FieldInfo fieldInfo, byte[] value ) throws IOException {
            add( new StoredField( fieldInfo.name, value ) );
        }
        @Override
        public void stringField( FieldInfo fieldInfo, String value ) throws IOException {
            add( new StoredField( fieldInfo.name, value ) );
        }
        @Override
        public void intField( FieldInfo fieldInfo, int value ) throws IOException {
            add( new StoredField( fieldInfo.name, value ) );
        }
        @Override
        public void longField( FieldInfo fieldInfo, long value ) throws IOException {
            add( new StoredField( fieldInfo.name, value ) );
        }
        @Override
        public void floatField( FieldInfo fieldInfo, float value ) throws IOException {
            add( new StoredField( fieldInfo.name, value ) );
        }
        @Override
        public void doubleField( FieldInfo fieldInfo, double value ) throws IOException {
            add( new StoredField( fieldInfo.name, value ) );
        }
    }

    
    // instance *******************************************
    
    private LuceneRecordStore   store;
    
    private Document            doc;
    
    private boolean             sharedDoc = false;
    
    
    protected LuceneRecordState( LuceneRecordStore store, Document doc, boolean sharedDoc ) {
        this.store = store;
        this.doc = doc;
        this.sharedDoc = sharedDoc;
    }

    
    Document document() {
        return doc;
    }

    void setShared( boolean shared ) {
        this.sharedDoc = shared;
    }
    
    
    public String toString() {
        StringBuilder result = new StringBuilder( "LuceneRecordState[" );
        for (Entry<String,Object> entry : this) {
            result.append( entry.getKey() ).append( " = " ).append( entry.getValue() );
            result.append( ",\n    " );
        }
        result.append( "]" );
        return result.toString();
    }

    
    public Object id() {
        Field field = doc.getField( ID_FIELD );
        return field != null ? field.stringValue() : null;
    }

    
    void createId() {
        assert doc.getField( ID_FIELD ) == null : "ID already set for this record";

        doc.add( new StringField( ID_FIELD, String.valueOf( idCount++ ), Store.YES ) );
    }
    
    
    protected void checkCopyOnWrite() {
        if (sharedDoc) {
            synchronized (this) {
                if (sharedDoc) {
                    
                    sharedDoc = false;
                    
                    try {
                        store.lock.readLock().lock();
                        // XXX direct way to load TermDoc???
                        TopDocs topDocs = store.searcher.search( new TermQuery( new Term( LuceneRecordState.ID_FIELD, (String)id() ) ), 1 );
                        //TermDocs termDocs = store.reader.termDocs( new Term( LuceneRecordState.ID_FIELD, (String)id() ) );
                        if (topDocs.scoreDocs.length > 0) {
                            doc = new Document( doc.fieldSelector );
                            store.reader.document( topDocs.scoreDocs[0].doc, doc );
                        }
                        else {
                            throw new RuntimeException( "Unable to copy Lucene document on write." );
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException( "Unable to copy Lucene document on write." );
                    }
                    finally {
                        store.lock.readLock().unlock();
                    }
                }
            }
        }
    }


    public <T> LuceneRecordState put( String key, T value ) {
        assert key != null && key.length() > 0 : "Key must not be null or empty.";
        assert value != null : "Value must not be null.";
        
        checkCopyOnWrite();
        
        boolean indexed = store.getIndexFieldSelector().accept( key );
        store.valueCoders.encode( doc, key, value, indexed );
        
        return this;
    }

    
    public LuceneRecordState add( String key, Object value ) {
        assert key != null;
        assert value != null : "Value must not be null.";

        checkCopyOnWrite();
        
        Field lengthField = doc.getField( key + "_length" );
        int length = -1;
        
        if (lengthField == null) {
            length = 1;
            doc.put( new IntField( key + "_length", 1, Store.YES ) );
        }
        else {
            length = Integer.parseInt( lengthField.stringValue() ) + 1;
            lengthField.setIntValue( length );
        }
        
        StringBuilder arrayKey = new StringBuilder( 32 )
                .append( key ).append( '[' ).append( length-1 ).append( ']' );
        
        put( arrayKey.toString(), value );
        
        return this;
    }

    
    public <T> T get( String key ) {
        return (T)store.valueCoders.decode( doc, key );
    }

    
    public <T> List<T> getList( String key ) {
        // XXX try a lazy facade!?
        List<T> result = new ArrayList<T>();
        
        String lengthString = doc.getField( key + "_length" ).stringValue();
        int length = lengthString != null ? Integer.parseInt( lengthString ) : 0;
        for (int i=0; i<length; i++) {
            StringBuilder arrayKey = new StringBuilder( 32 )
                    .append( key ).append( '[' ).append( i ).append( ']' );
            result.add( (T)get( arrayKey.toString() ) );
        }
        return result;
    }


    public LuceneRecordState remove( String key ) {
        checkCopyOnWrite();

        doc.removeField( key );
        return this;
    }

    
    public Iterator<Entry<String,Object>> iterator() {
        return new Iterator<Entry<String, Object>>() {
            private Iterator<IndexableField> it = doc.iterator();
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public Entry<String, Object> next() {
                return new Entry<String,Object>() {
                    private Field   field = (Field)it.next();
                    @Override
                    public String getKey() {
                        return field.name();
                    }
                    @Override
                    public Object getValue() {
                        return store.valueCoders.decode( doc, getKey() );
                    }
                    @Override
                    public Object setValue( Object value ) {
                        Object old = getValue();
                        LuceneRecordState.this.put( getKey(), value );
                        return old;
                    }
                };
            }

            public void remove() {
                throw new UnsupportedOperationException( "remove()" );
                //LuceneRecordState.this.remove( field.name() );
            }
        };
    }
    
}
