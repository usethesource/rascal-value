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
package io.usethesource.vallang.impl.persistent;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import io.usethesource.capsule.Map;
import io.usethesource.capsule.util.EqualityComparator;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.IMapWriter;
import io.usethesource.vallang.IRelation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.util.AbstractTypeBag;
import io.usethesource.vallang.util.EqualityUtils;

public final class PersistentHashMap implements IMap {

	private static final EqualityComparator<Object> equivalenceComparator =
			EqualityUtils.getEquivalenceComparator();
	
	private @MonotonicNonNull Type cachedMapType;
	private final AbstractTypeBag keyTypeBag;
	private final AbstractTypeBag valTypeBag;
	private final Map.Immutable<@NonNull IValue, @NonNull IValue> content;
	
	/* 
	 * Passing an pre-calulated map type is only allowed from inside this class.
	 */
	protected PersistentHashMap(AbstractTypeBag keyTypeBag, AbstractTypeBag valTypeBag, Map.Immutable<@NonNull IValue, @NonNull IValue> content) {
		this.keyTypeBag = keyTypeBag;
		this.valTypeBag = valTypeBag;
		this.content = content;
	}
	
	@Override
	public String toString() {
	    return defaultToString();
	}
	
	@Override
	public Type getType() {
		if (cachedMapType == null) {
			cachedMapType = TF.mapType(keyTypeBag.lub(), valTypeBag.lub());
		}
		
		return cachedMapType;		
	}

	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}

	@Override
	@SuppressWarnings("deprecation")
	public IMap put(IValue key, IValue value) {
		final Map.Immutable<IValue,IValue> contentNew =
				content.__putEquivalent(key, value, equivalenceComparator);
		
		if (content == contentNew)
			return this;

		final AbstractTypeBag keyBagNew;
		final AbstractTypeBag valBagNew;

		if (content.size() == contentNew.size()) {
			// value replaced
			final IValue replaced = content.getEquivalent(key, equivalenceComparator);
			keyBagNew = keyTypeBag;
			valBagNew = valTypeBag.decrease(replaced.getType()).increase(value.getType());
		} else {
			// pair added
			keyBagNew = keyTypeBag.increase(key.getType());			
			valBagNew = valTypeBag.increase(value.getType());
		}
		
		return new PersistentHashMap(keyBagNew, valBagNew, contentNew);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public IMap removeKey(IValue key) {
	    final Map.Immutable<IValue, IValue> newContent = 
				content.__removeEquivalent(key, equivalenceComparator);
		
	    if (newContent == content) {
	        return this;
	    }
		
	    final IValue removedValue = content.getEquivalent(key, equivalenceComparator);
	    final AbstractTypeBag newKeyBag = keyTypeBag.decrease(key.getType());
	    final AbstractTypeBag newValBag = valTypeBag.decrease(removedValue.getType());
		
	    return new PersistentHashMap(newKeyBag, newValBag, newContent);
	}	
	
	@Override
	public int size() {
		return content.size();
	}

	@Override
	@EnsuresNonNullIf(expression="get(#1)", result=true)
    @SuppressWarnings({"deprecation", "contracts.conditional.postcondition.not.satisfied"}) // that's impossible to prove for the Checker Framework
	public boolean containsKey(IValue key) {
		return content.containsKeyEquivalent(key, equivalenceComparator);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean containsValue(IValue value) {
		return content.containsValueEquivalent(value, equivalenceComparator);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public IValue get(IValue key) {
		return content.getEquivalent(key, equivalenceComparator);
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object other) {
		if (other == this) {
			return true;
		}
		
		if (other == null) {
			return false;
		}
		
		if (other instanceof PersistentHashMap) {
			PersistentHashMap that = (PersistentHashMap) other;

			if (this.getType() != that.getType())
				return false;

			if (this.size() != that.size())
				return false;

			return content.equals(that.content);
		}
		
		if (other instanceof IMap) {
			return defaultEquals(other);	
		}
		
		return false;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean isEqual(IValue other) {
		if (other == this) {
			return true;
		}
		
		if (other instanceof PersistentHashMap) {
			PersistentHashMap that = (PersistentHashMap) other;

			if (this.size() != that.size())
				return false;

			return content.equivalent(that.content, equivalenceComparator);
		}

		if (other instanceof IMap) {
			return IMap.super.isEqual(other);
		}

		return false;
	}
	
	@Override
	public Iterator<@NonNull @KeyFor("this") IValue> iterator() {
		return content.keyIterator();
	}
	
	@Override
	public Iterator<IValue> valueIterator() {
		return content.valueIterator();
	}

	@Override
	public Iterator<Entry<@KeyFor("this") IValue, IValue>> entryIterator() {
		return content.entryIterator();
	}

	@Override
	@SuppressWarnings("deprecation")
	public IMap join(IMap other) {
		if (other instanceof PersistentHashMap) {
			PersistentHashMap that = (PersistentHashMap) other;

			final Map.Transient<IValue, IValue> transientContent = content.asTransient();

			boolean isModified = false;
			int previousSize = size();

			AbstractTypeBag keyBagNew = keyTypeBag;
			AbstractTypeBag valBagNew = valTypeBag;

			for (Iterator<Entry<IValue, IValue>> it = that.entryIterator(); it.hasNext();) {
				Entry<IValue, IValue> tuple = it.next();
				IValue key = tuple.getKey();
				IValue value = tuple.getValue();

				final IValue replaced = transientContent.__putEquivalent(key, value,
								equivalenceComparator);

				if (replaced != null) {
					// value replaced
					valBagNew = valBagNew.decrease(replaced.getType()).increase(value.getType());

					isModified = true;
				} else if (previousSize != transientContent.size()) {
					// pair added
					keyBagNew = keyBagNew.increase(key.getType());
					valBagNew = valBagNew.increase(value.getType());

					isModified = true;
					previousSize++;
				}
			}

			if (isModified) {
				return new PersistentHashMap(keyBagNew, valBagNew, transientContent.freeze());
			} else {
				return this;
			}
		} else {
			return IMap.super.join(other);
		}
	}
	
    @Override
    public Type getElementType() {
        return keyTypeBag.lub();
    }

    @Override
    public IMap empty() {
        return writer().done();
    }

    @Override
    public IMapWriter writer() {
        return new MapWriter();
    }

    @Override
    public Stream<IValue> stream() {
        return StreamSupport.stream(spliterator(), false).map(key -> Tuple.newTuple(key, get(key)));
    }
    
    @Override
    public IRelation<IMap> asRelation() {
        throw new UnsupportedOperationException();
    }

}
