/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.annotation;

import java.nio.FloatBuffer;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.function.DoubleFunctions;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.bicluster.elem.NormalClusterElement;
import org.caleydo.view.bicluster.sorting.IntFloat;

/**
 * element for showing a heatmap of probabilities
 *
 * @author Samuel Gratzl
 *
 */
public class MembershipLZHeatmapElement extends ALZHeatmapElement {
	private final IDoubleFunction transform;

	public MembershipLZHeatmapElement(EDimension dim, double max) {
		super(dim);
		this.transform = DoubleFunctions.normalize(0, max);
	}

	@Override
	protected String getLabel(int pos) {
		NormalClusterElement p = (NormalClusterElement) getParent();
		float m = p.getMembership(dim, pos);
		return Float.isNaN(m) ? null : String.valueOf(m);
	}

	@Override
	public void pick(Pick pick) {
		super.pick(pick);
		if (pick.getPickingMode() == PickingMode.DOUBLE_CLICKED) {
			int index = toIndex(pick);
			if (index < 0)
				return;

			NormalClusterElement p = (NormalClusterElement) getParent();
			float m = p.getMembership(dim, index);
			if (!Float.isNaN(m))
				p.setLocalThreshold(dim, Math.abs(m), p.getThresholdMode(dim));
		}
	}

	@Override
	protected void updateImpl(FloatBuffer buffer, List<IntFloat> values) {
		final int width = values.size();

		float max;
		float min;
		float last;
		last = values.get(0).getMembership();
		min = max = Math.abs(last);

		int center = -1;
		boolean multiCenter = false;
		for (int i = 1; i < width; ++i) {
			float v = values.get(i).getMembership();
			float v_a = Math.abs(v);
			if (v_a > max)
				max = v_a;
			if (v_a < min)
				min = v_a;
			if (last < 0 && v > 0 && !multiCenter) {
				multiCenter = center >= 0; // already set a center -> multi center
				center = i - 1; // before me
			}
			last = v;
		}
		if (multiCenter)
			this.center = NO_CENTER;
		else if (center != -1)
			this.center = center;//in the middle
		else if (last > 0) // all positive
			this.center = -1; //first
		else
			// all negative
			this.center = width-1; //last

		// IDoubleFunction transform = ExpressionFunctions.compose(DoubleFunctions.CLAMP01,
		// DoubleFunctions.normalize(min, max));

		for (IntFloat f : values) {
			float v = Math.abs(f.getMembership());
			v = (float) transform.apply(v);
			// System.out.print(v + " ");
			v = 1 - v; // invert color mapping
			buffer.put(v);
			buffer.put(v);
			buffer.put(v);
		}
	}
}
