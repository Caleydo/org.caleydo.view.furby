/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

/**
 * concat to immutable list to a new one
 * 
 * @author Samuel Gratzl
 * 
 */
public class ConcatedList<T> extends AbstractList<T> implements RandomAccess {
	private final ImmutableList<T> a;
	private final ImmutableList<T> b;

	private ConcatedList(ImmutableList<T> a, ImmutableList<T> b) {
		this.a = a;
		this.b = b;
	}

	public static <T> List<T> concat(ImmutableList<T> a, ImmutableList<T> b) {
		if (a.isEmpty())
			return b;
		if (b.isEmpty())
			return a;
		return new ConcatedList<>(a, b);
	}

	@Override
	public int size() {
		return a.size() + b.size();
	}


	@Override
	public T get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException();
		if (index >= size())
			throw new IndexOutOfBoundsException();
		if (index >= a.size())
			return b.get(index - a.size());
		return a.get(index);
	}

	@Override
	public UnmodifiableIterator<T> iterator() {
		return Iterators.unmodifiableIterator(Iterators.concat(a.iterator(), b.iterator()));
	}
}
