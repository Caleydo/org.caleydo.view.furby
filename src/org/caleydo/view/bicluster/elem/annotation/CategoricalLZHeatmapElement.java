/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.annotation;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.bicluster.elem.EDimension;
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
	private final Integer oppositeID;


	public CategoricalLZHeatmapElement(EDimension dim, Integer oppositeID, Table table,
			IIDTypeMapper<Integer, Integer> id2colorId) {
		super(dim);
		this.oppositeID = oppositeID;
		this.table = table;
		this.id2colorId = id2colorId;
	}
	@Override
	protected void updateImpl(FloatBuffer buffer, List<IntFloat> values) {
		center = NO_CENTER;
		for(IntFloat v : values) {
			Set<Integer> r = id2colorId.apply(v.getIndex());
			float[] c;
			if (r == null || r.isEmpty())
				c = Color.NOT_A_NUMBER_COLOR.getRGB();
			else //if (r.size() == 1)
				c = getColor(r.iterator().next());
			buffer.put(c[0]).put(c[1]).put(c[2]);
		}
	}

	private float[] getColor(Integer id) {
		if (table.getDataDomain().getDimensionIDType().equals(id2colorId.getTarget()))
			return table.getColor(id, oppositeID);
		else
			return table.getColor(oppositeID, id);
	}

	public boolean is(Integer oppositeID, IDType target) {
		return oppositeID.equals(oppositeID) && target.equals(id2colorId.getTarget());
	}
}
