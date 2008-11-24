/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.pdb.facts;

import org.eclipse.imp.pdb.facts.type.FactTypeError;
import org.eclipse.imp.pdb.facts.type.Type;

public interface ISet extends Iterable<IValue>, IValue {
	/**
	 * @return the type of the elements in this set
	 */
	public Type getElementType();
	
	/**
	 * @return true iff this set has no elements
	 */
    public boolean isEmpty();

    /**
     * @return the arity of the set, the number of elements in the set
     */
    public int size();

    /**
     * @param element
     * @return true iff this is an element of the set
     * @throws FactTypeError if this element could never be an element of this set
     *         because its type is not a subtype of the elementType
     */
    public boolean contains(IValue element) throws FactTypeError;

    /**
     * Add an element to the set. 
     * @param <SetOrRel> ISet when the result will be a set, IRelation when it will be a relation.
     * @param element
     * @return a relation if the element type is a tuple type, a set otherwise
     */
    public <SetOrRel extends ISet> SetOrRel insert(IValue element) throws FactTypeError;

    /**
     * Computes the union of two sets
     * @param <SetOrRel> ISet when the result will be a set, IRelation when it will be a relation.
     * @param element
     * @return a relation if the element type is a tuple type, a set otherwise
     */
    public <SetOrRel extends ISet> SetOrRel union(ISet set)  throws FactTypeError;
    
    /**
     * Computes the intersection of two sets
     * @param <SetOrRel> ISet when the result will be a set, IRelation when it will be a relation.
     * @param element
     * @return a relation if the element type is a tuple type, a set otherwise
     */
    public <SetOrRel extends ISet> SetOrRel intersect(ISet set)  throws FactTypeError;
    
    /**
     * Subtracts one set from the other
     * @param <SetOrRel>
     * @param set
     * @return a relation if the element type is a tuple type, a set otherwise
     */
    public <SetOrRel extends ISet> SetOrRel subtract(ISet set)  throws FactTypeError;
    
    /**
     * Computes the cartesion product of two sets
     * @param set
     * @return a relation representing the cartesion product
     */
    public IRelation product(ISet set);
    
    /**
     * @param other
     * @return true iff all elements of this set are elements of the other.
     */
    public boolean isSubSet(ISet other);
}
