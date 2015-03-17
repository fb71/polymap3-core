/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.model2.store.recordstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import org.polymap.core.model2.Entity;
import org.polymap.core.model2.engine.TemplateProperty;
import org.polymap.core.model2.query.grammar.BooleanExpression;
import org.polymap.core.model2.query.grammar.CompositeCollectionQuantifier;
import org.polymap.core.model2.query.grammar.ManyAssociationQuantifier;
import org.polymap.core.model2.query.grammar.Quantifier;
import org.polymap.core.model2.query.grammar.TheAssociationQuantifier;
import org.polymap.core.model2.query.grammar.Quantifier.Type;
import org.polymap.core.model2.query.grammar.TheCompositeQuantifier;
import org.polymap.core.model2.runtime.ModelRuntimeException;
import org.polymap.core.runtime.Stringer;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordFieldSelector;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.SimpleQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordState;

/**
 * Handles all {@link Quantifier} types. 
 * <p/>
 * <b>FIXME</b> The {@link #subQuery(Class, BooleanExpression)} does not reflect
 * uncommitted modifications of the UnitOfWork. However, if the Entity itself is
 * modified then Quantifier implementation evaluates against the modified
 * (associated) entities in-memory, which returnes a correct result. So, the issue
 * exists just if the associated entities are modified but source entity of the
 * association is unmodified.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class LuceneQuantifierHandler
        extends LuceneExpressionHandler<Quantifier> {

    private static Log log = LogFactory.getLog( LuceneQuantifierHandler.class );

    
    @Override
    public Query handle( Quantifier quantifier ) {
        // composite
        if (quantifier instanceof TheCompositeQuantifier) {
            String fieldname = simpleFieldname( (TemplateProperty)quantifier.prop );
            log( "THE Composite", fieldname );
            return builder.processExpression( fieldname, quantifier.subExp(), resultType );
        }

        // composite collection
        else if (quantifier instanceof CompositeCollectionQuantifier) {
            String fieldname = simpleFieldname( (TemplateProperty)quantifier.prop );
            int maxElements = maxElementsInCollection( prefixedFieldname( (TemplateProperty)quantifier.prop ) );
            
            log( quantifier.type + " of Composites", fieldname );
            BooleanQuery result = new BooleanQuery();
            for (int i=0; i<maxElements; i++) {
                String elmname = Stringer.on( fieldname ).append( '[' ).append( i ).append( ']' ).toString();
                Query propQuery = builder.processExpression( elmname, quantifier.subExp(), resultType );
                result.add( propQuery, 
                        quantifier.type == Type.ANY ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST );
            }
            return result;
        }
        
        // association
        if (quantifier instanceof TheAssociationQuantifier) {
            Class<? extends Entity> assocEntityType = quantifier.prop.getInfo().getType();
            Object[] assocIds = subQuery( assocEntityType, quantifier.subExp() );
            
            String fieldname = prefixedFieldname( (TemplateProperty)quantifier.prop );
            log( "THE association: " + fieldname + " contains any of", assocIds );
            return idQuery( fieldname, assocIds );
        }
        
        // many association
        if (quantifier instanceof ManyAssociationQuantifier) {
            assert quantifier.type == Type.ANY;
            
            Class<? extends Entity> assocEntityType = quantifier.prop.getInfo().getType();
            Object[] assocIds = subQuery( assocEntityType, quantifier.subExp() );
            
            int maxElements = maxElementsInCollection( prefixedFieldname( (TemplateProperty)quantifier.prop ) );
            String fieldname = simpleFieldname( (TemplateProperty)quantifier.prop );
            
            log( quantifier.type + " of association: " + fieldname + " contains any of", assocIds );
            BooleanQuery result = new BooleanQuery();
            for (int i=0; i<maxElements; i++) {
                String elmname = Stringer.on( fieldname ).append( '[' ).append( i ).append( ']' ).toString();
                result.add( idQuery( elmname, assocIds ), BooleanClause.Occur.SHOULD );
            }
            return result;
        }
        
        else {
            throw new ModelRuntimeException( "Quantifier type not supported: " + quantifier );
        }
    }

    
    protected int maxElementsInCollection( String fieldname ) {
        try {
            Timer timer = new Timer();
            
            String lengthFieldname = fieldname + "/__size__";
            
            RecordQuery query = new SimpleQuery()
                    .eq( RecordCompositeState.TYPE_KEY, resultType.getName() )
                    .sort( lengthFieldname, SimpleQuery.DESC, Integer.class )
                    .setMaxResults( 1 );
            
            ResultSet lengthResult = builder.store.find( query );
            int result = 0;
            if (lengthResult.count() > 0) {
                IRecordState biggest = lengthResult.get( 0 );
                result = biggest.get( lengthFieldname );
            }
            log.debug( "    LUCENE: maxLength: " + result + " (" + timer.elapsedTime() + "ms)" );
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    protected Object[] subQuery( Class<? extends Entity> entityType, BooleanExpression exp ) {
        // FIXME this sub-query does not reflect uncommitted modifications of the UnitOfWork.
        // see class comment

        String subLogIndent = builder.logIndent + "    SUB[" + entityType.getSimpleName() + "] ";
        RecordQuery recordQuery = new LuceneQueryBuilder( builder.store, subLogIndent ).createQuery( entityType, exp );
        
        // load just the ID field
        recordQuery.setFieldSelector( new IRecordFieldSelector() {
            public boolean accept( String key ) {
                return key.equals( LuceneRecordState.ID_FIELD );
            }
        });

        try {
            ResultSet rs = builder.store.find( recordQuery );
            Object[] result = new Object[ rs.count() ];
            int i = 0;
            for (IRecordState state : rs) {
                result[i] = state.id();
                assert result[i] != null;
            }
            return result;
        }
        catch (Exception e) {
            throw new ModelRuntimeException( "Exception during association sub-query.", e );
        }
    }


    protected Query idQuery( String fieldname, Object[] ids ) {
        if (ids.length == 1) {
            return idQuery( fieldname, ids[0] );
        }
        else {
            BooleanQuery result = new BooleanQuery();
            for (Object id : ids) {
                result.add( idQuery( fieldname, id ), BooleanClause.Occur.SHOULD );
            }
            return result;
        }
    }


    protected org.apache.lucene.search.Query idQuery( String fieldname, Object id ) {
        return builder.valueCoders.searchQuery( 
                new QueryExpression.Equal( fieldname, id ) );        
    }
    
}


///**
//* Handle the contains predicate.
//* <p/>
//* Impl. note: This needs a patch in
//* org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl<T> to work with
//* Qi4j 1.0.
//*/
//protected Query processContainsPredicate( ContainsPredicate predicate, String resultType ) {
// final ValueCoders valueCoders = store.getValueCoders();
//
// PropertyReference property = predicate.propertyReference();
// final String baseFieldname = property2Fieldname( property ).toString();
// SingleValueExpression valueExpression = (SingleValueExpression)predicate.valueExpression();
//
// //
// int maxElements = 10;
// try {
//     Timer timer = new Timer();
//     String lengthFieldname = baseFieldname + "__length";
//     RecordQuery query = new SimpleQuery()
//             .eq( "type", resultType )
//             .sort( lengthFieldname, SimpleQuery.DESC, Integer.class )
//             .setMaxResults( 1 );
//     ResultSet lengthResult = store.find( query );
//     IRecordState biggest = lengthResult.get( 0 );
//     maxElements = biggest.get( lengthFieldname );
//     log.debug( "    LUCENE: maxLength query: result: " + maxElements + " (" + timer.elapsedTime() + "ms)" );
// }
// catch (Exception e) {
//     throw new RuntimeException( e );
// }
// 
// //
// BooleanQuery result = new BooleanQuery();
// for (int i=0; i<maxElements; i++) {
//     final BooleanQuery valueQuery = new BooleanQuery();
//
//     final ValueComposite value = (ValueComposite)valueExpression.value();
//     ValueModel valueModel = (ValueModel)ValueInstance.getValueInstance( value ).compositeModel();
//     List<PropertyType> actualTypes = valueModel.valueType().types();
//     //                    json.key( "_type" ).value( valueModel.valueType().type().name() );
//
//
//     // all properties of the value
//     final int index = i;
//     value.state().visitProperties( new StateVisitor() {
//         public void visitProperty( QualifiedName name, Object propValue ) {
//             if (propValue == null) {
//             }
//             else if (propValue.toString().equals( "-1" )) {
//                 // FIXME hack to signal that this non-optional(!) value is not to be considered
//                 log.warn( "Non-optional field ommitted: " + name.name() + ", value=" + propValue );
//             }
//             else {
//                 String fieldname = Joiner.on( "" ).join( 
//                         baseFieldname, "[", index, "]", 
//                         LuceneEntityState.SEPARATOR_PROP, name.name() );
//                 
//                 //Property<Object> fieldProp = value.state().getProperty( name );
//
////               // this might not be the semantics of contains predicate but it is useless
////               // if one cannot do a search without (instead of just a strict match)
//                 Query propQuery = propValue instanceof String
//                         && !StringUtils.containsNone( (String)propValue, "*?")
//                         ? valueCoders.searchQuery( new QueryExpression.Match( fieldname, propValue ) ) 
//                         : valueCoders.searchQuery( new QueryExpression.Equal( fieldname, propValue ) ); 
//
//                 valueQuery.add( propQuery, BooleanClause.Occur.MUST );
//             }
//         }
//     });
//
//     result.add( valueQuery, BooleanClause.Occur.SHOULD );
// }
// return result;
//}
