/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.annotation;

import java.nio.FloatBuffer;
import java.util.List;

import org.caleydo.core.id.IDType;
import org.caleydo.view.bicluster.elem.EDimension;
import org.caleydo.view.bicluster.elem.NormalClusterElement;
import org.caleydo.view.bicluster.sorting.CategoricalSortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.IntFloat;

/**
 * element for showing a heatmap of categorical property e.g. chemical cluster
 *
 * @author Samuel Gratzl
 *
 */
public class CategoricalLZHeatmapElement extends ALZHeatmapElement {
	private final CategoricalSortingStrategyFactory data;


	public CategoricalLZHeatmapElement(EDimension dim, CategoricalSortingStrategyFactory data) {
		super(dim);
		this.data = data;
	}

	@Override
	protected String getLabel(int pos) {
		NormalClusterElement p = (NormalClusterElement) getParent();
		int index = p.getVirtualArray(dim).get(pos);
		return data.getLabel(index);
	}

	@Override
	protected void updateImpl(FloatBuffer buffer, List<IntFloat> values) {
		center = NO_CENTER;
		for(IntFloat v : values) {
			float[] c = data.getColor(v.getIndex());
			buffer.put(c[0]).put(c[1]).put(c[2]);
		}
	}

	public boolean is(Integer oppositeID, IDType target) {
		return oppositeID.equals(oppositeID) && target.equals(data.getTarget());
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public CategoricalSortingStrategyFactory getData() {
		return data;
	}
}
