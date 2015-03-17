/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.query.grammar;

import org.polymap.core.model2.engine.TemplateProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ComparisonPredicate<T>
        extends Predicate {

    public TemplateProperty<T>      prop;
    
    public T                        value;

//    private List<TemplateProperty>  traversed;
    
    
    public ComparisonPredicate( TemplateProperty<T> prop, T value ) {
        this.prop = prop;
        this.value = value;
    }

    
//    protected boolean evaluateTraversed( Composite target, Op op ) {
//        try {
//            // build the traverse stack
//            traversed = new ArrayList();
//            TemplateProperty<T> cursor = prop;
//            while (cursor != null
//                    // stop traversing for collection or ManyAssociation as this is handled by
//                    // the Quantifier predicates
//                    && (cursor instanceof Association || cursor instanceof Property)) {
//                
//                traversed.add( cursor );
//                cursor = cursor.getTraversed();
//            }
//            // recusivly evaluate given op
//            return evaluateTraversed( target, op, traversed.size()-1 );
//        }
//        finally {
//            traversed = null;
//        }
//    }
//    
//    
//    protected boolean evaluateTraversed( Composite target, Op op, int traverseIndex ) {
//        if (traverseIndex == 0) {
//            return op.evaluate( propValue( target, prop ) );
//        }
//        else {
//            TemplateProperty traversedProp = traversed.get( traverseIndex-1 );
//            PropertyInfo traversedInfo = traversedProp.getInfo();
//
//            PropertyBase targetProp = target.info().getProperty( traversedInfo.getName() ).get( target );
//            
//            // Composite
//            if (targetProp instanceof Property) {
//                return evaluateTraversed( (Composite)((Property)targetProp).get(), op, traverseIndex-1 );
//            }
//            // Association
//            else if (targetProp instanceof Association) {
//                return evaluateTraversed( ((Association)targetProp).get(), op, traverseIndex-1 );
//            }
//            else {
//                throw new IllegalStateException( "Unknown targetProp: " + targetProp );
//            }
//        }
//    }
    
    
    /**
     * 
     */
    interface Op<T> {
        
        public boolean evaluate( T propValue );
        
    }
    
}
