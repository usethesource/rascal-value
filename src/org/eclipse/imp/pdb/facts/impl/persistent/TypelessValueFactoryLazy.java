/*******************************************************************************
 * Copyright (c) 2013-2014 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

public class TypelessValueFactoryLazy extends org.eclipse.imp.pdb.facts.impl.fast.ValueFactory {

	protected TypelessValueFactoryLazy() {
		super();
	}

	private static class InstanceKeeper {
		public final static TypelessValueFactoryLazy instance = new TypelessValueFactoryLazy();
	}

	public static TypelessValueFactoryLazy getInstance() {
		return InstanceKeeper.instance;
	}

	public ISetWriter setWriter(Type elementType) {
		return new TypelessSetWriterLazy();
	}

	public ISetWriter setWriter() {
		return new TypelessSetWriterLazy();
	}

	public ISetWriter relationWriter(Type tupleType) {
		return new TypelessSetWriterLazy();
	}

	public ISetWriter relationWriter() {
		return new TypelessSetWriterLazy();
	}

	public ISet set(Type elementType) {
		return setWriter().done();
	}

	public ISet set(IValue... elements) {
		ISetWriter setWriter = setWriter();
		setWriter.insert(elements);
		return setWriter.done();
	}

	public ISet relation(Type tupleType) {
		return relationWriter(tupleType).done();
	}

	public ISet relation(IValue... elements) {
		return set(elements);
	}

	@Override
	public IMapWriter mapWriter(Type keyType, Type valueType) {
		return mapWriter(TypeFactory.getInstance().mapType(keyType, valueType));
	}

	@Override
	public IMapWriter mapWriter(Type mapType) {
		return new TypelessMapWriterLazy();
	}

	@Override
	public IMapWriter mapWriter() {
		return new TypelessMapWriterLazy();
	}

	@Override
	public IMap map(Type mapType) {
		return mapWriter(mapType).done();
	}

	@Override
	public IMap map(Type keyType, Type valueType) {
		return mapWriter(keyType, valueType).done();
	}

	@Override
	public String toString() {
		return "VF_PDB_PERSISTENT_CURRENT";
	}

}
