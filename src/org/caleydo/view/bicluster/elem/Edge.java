/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.id.IDType;

/**
 * @author Samuel Gratzl
 *
 */
public class Edge {
	private final ClusterElement a;
	private final ClusterElement b;
	private final Set<Integer> recOverlap = new TreeSet<>();
	private final Set<Integer> dimOverlap = new TreeSet<>();

	public Edge(ClusterElement a, ClusterElement b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * @return the a, see {@link #a}
	 */
	public ClusterElement getA() {
		return a;
	}

	/**
	 * @return the b, see {@link #b}
	 */
	public ClusterElement getB() {
		return b;
	}

	public boolean anyVisible() {
		return a.isVisible() || b.isVisible();
	}

	public ClusterElement getOpposite(ClusterElement elem) {
		return a == elem ? b : a;
	}

	public int getDimOverlap() {
		return dimOverlap.size();
	}

	public int getRecOverlap() {
		return recOverlap.size();
	}

	public int getOverlap(EDimension dim) {
		return dim == EDimension.DIMENSION ? getDimOverlap() : getRecOverlap();
	}

	/**
	 * @return
	 */
	public Collection<Integer> getRecOverlapIndices() {
		return recOverlap;
	}

	public Collection<Integer> getDimOverlapIndices() {
		return dimOverlap;
	}

	public Collection<Integer> getOverlapIndices(EDimension dim) {
		return dim == EDimension.DIMENSION ? getDimOverlapIndices() : getRecOverlapIndices();
	}

	public void update(EDimension dim) {
		if (dim == EDimension.DIMENSION)
			updateDim();
		else
			updateRec();
	}

	/**
	 * updates the dim overlap
	 *
	 * @return the difference to the previous stored values
	 */
	public int updateDim() {
		final int bak = getDimOverlap();
		final VirtualArray a_v = a.getDimVirtualArray();
		final VirtualArray b_v = b.getDimVirtualArray();

		final VirtualArray smaller = a_v.size() < b_v.size() ? a_v : b_v;
		final VirtualArray opposite = smaller == a_v ? b_v : a_v;

		dimOverlap.clear();
		for (Integer id : smaller)
			if (opposite.contains(id))
				dimOverlap.add(id);
		return getDimOverlap() - bak;
	}

	public int updateRec() {
		final int bak = getRecOverlap();
		final VirtualArray a_v = a.getRecVirtualArray();
		final VirtualArray b_v = b.getRecVirtualArray();

		final VirtualArray smaller = a_v.size() < b_v.size() ? a_v : b_v;
		final VirtualArray opposite = smaller == a_v ? b_v : a_v;

		recOverlap.clear();
		for (Integer id : smaller)
			if (opposite.contains(id))
				recOverlap.add(id);

		return getRecOverlap() - bak;
	}

	public IDType getIDType(EDimension dim) {
		return dim == EDimension.DIMENSION ? a.getDimensionIDType() : a.getRecordIDType();
	}
	/**
	 * @return
	 */
	public IDType getDimIDType() {
		return a.getDimensionIDType();
	}

	/**
	 * @return
	 */
	public IDType getRecordIDType() {
		return a.getRecordIDType();
	}

}
