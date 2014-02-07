/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.annotation;

import java.nio.FloatBuffer;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.format.Formatter;
import org.caleydo.view.bicluster.elem.NormalClusterElement;
import org.caleydo.view.bicluster.sorting.IntFloat;

import com.google.common.base.Predicate;

/**
 * element for showing a heatmap of probabilities
 *
 * @author Samuel Gratzl
 *
 */
public class GoLZHeatmapElement extends ALZHeatmapElement {
	private final String label;
	private final float pValue;
	private final Predicate<Integer> partOf;
	private final IDataDomain origin;

	public GoLZHeatmapElement(EDimension dim, String label, float pValue, Predicate<Integer> partOf,
			IDataDomain origin) {
		super(dim, true);
		this.label = label;
		this.pValue = pValue;
		this.partOf = partOf;
		this.origin = origin;
	}

	/**
	 * @return the origin, see {@link #origin}
	 */
	public IDataDomain getOrigin() {
		return origin;
	}

	@Override
	protected String getLabel(int pos) {
		NormalClusterElement elem = (NormalClusterElement) getParent();
		String label = elem.getLabel(dim, pos);
		Integer id = elem.getVirtualArray(dim).get(pos);
		boolean partOf = this.partOf.apply(id);
		return String.format("%s (p-value: %s)\n%s (%s)", this.label, Formatter.formatNumber(pValue), label,
				partOf ? "part of"
				: "not part of");
	}

	@Override
	protected void updateImpl(FloatBuffer buffer, List<IntFloat> values) {
		center = NO_CENTER;
		final float b = pValue2brightness(pValue);
		Color notPartOf = new Color(1.f, 1.f, 1.f, 1.f); // .getColorWithSpecificBrighness(1 - b);
		// FIXME some better color
		Color partOf = new Color(0.f, 0, 1.f, 1.f).getColorWithSpecificBrighness(1 - b);

		for(IntFloat v : values) {
			Color c;
			if (this.partOf.apply(v.getIndex()))
				c = partOf;
			else
				c = notPartOf;
			buffer.put(c.r).put(c.g).put(c.b);
		}
	}

	/**
	 * @param pValue2
	 * @return
	 */
	private static float pValue2brightness(float pValue) {
		// FIXME some meaningful thing
		return Math.max(0, Math.min(1, 1 - pValue));
	}
}
