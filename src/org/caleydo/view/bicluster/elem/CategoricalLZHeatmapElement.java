/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.bicluster.sorting.IntFloat;

/**
 * element for showing a heatmap of categorical property e.g. chemical cluster
 * 
 * @author Samuel Gratzl
 * 
 */
public class CategoricalLZHeatmapElement extends ALZHeatmapElement {
	private final Table table;
	private final IIDTypeMapper<Integer,Integer> id2colorId;


	public CategoricalLZHeatmapElement(EDimension dim, Table table, IIDTypeMapper<Integer, Integer> id2colorId) {
		super(dim);
		this.table = table;
		this.id2colorId = id2colorId;
	}
	@Override
	protected void updateImpl(FloatBuffer buffer, List<IntFloat> values) {
		for(IntFloat v : values) {
			Set<Integer> r = id2colorId.apply(v.getIndex());
			float[] c;
			if (r == null || r.isEmpty())
				c = Color.NOT_A_NUMBER_COLOR.getRGB();
			else //if (r.size() == 1)
				c = table.getColor(0,r.iterator().next());
			buffer.put(c[0]).put(c[1]).put(c[2]);
		}
	}
}
