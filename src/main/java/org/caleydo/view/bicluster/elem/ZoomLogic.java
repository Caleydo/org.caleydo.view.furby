/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Map;

import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;

import com.google.common.collect.ImmutableMap;

/**
 * encapsulate all the zoom logic relevant settings
 *
 * @author Samuel Gratzl
 *
 */
public class ZoomLogic {

	/**
	 * the target percentage to which the element should be initially scaled
	 */
	private static final double INITIAL_FILLED_PERCENTAGE = 0.15;

	public static Map<EDimension, Float> initialOverviewScaleFactor(Collection<Dimension> sizes, float w, float h) {
		DoubleStatistics.Builder stats = DoubleStatistics.builder();
		for (Dimension size : sizes) {
			stats.add(size.height == 0 ? 0 : size.getWidth() / size.height);
		}
		double mean = stats.build().getMean();
		double target = MyPreferences.getAspectRatio();

		// force on average a specific aspect ratio
		double rec = 1;
		double dim = (float) (target / mean);

		// fit the aspect ratio scaled elements to the target size
		double used = count(sizes, dim, rec);
		double targetUsed = w * h * INITIAL_FILLED_PERCENTAGE; // FIXME

		double scale = Math.sqrt(targetUsed / used);
		dim = Math.floor(dim * scale * 100) / 100.f;
		rec = Math.floor(rec * scale * 100) / 100.f;

		return ImmutableMap.of(EDimension.DIMENSION, (float) dim, EDimension.RECORD, (float) rec);
	}

	private static double count(Collection<Dimension> sizes, double dim, double rec) {
		double sum = 0;
		for (Dimension s : sizes)
			sum += s.width * s.height * dim * rec;
		return sum;
	}

	public static Map<EDimension, Float> initialFocusScaleFactor(Dimension size, float w, float h) {
		w *= 0.75f;
		h *= 0.9f;
		float dim = w / size.width;
		float rec = h / size.height;
		return ImmutableMap.of(EDimension.DIMENSION, dim, EDimension.RECORD, rec);
	}

	public static Map<EDimension, Float> initialFocusNeighborScaleFactor(Collection<Dimension> sizes,
			Dimension focusSize, float w, float h) {
		float dim = 1f;
		float rec = 1;
		return ImmutableMap.of(EDimension.DIMENSION, dim, EDimension.RECORD, rec);
	}

	public static int toDirection(IMouseEvent event, EDimension dim) {
		final int w = event.getWheelRotation();
		if (w == 0)
			return 0;
		int factor = w > 0 ? 1 : -1;
		return event.isCtrlDown() || dim.select(event.isAltDown(), event.isShiftDown()) ? factor : 0;
	}

	/**
	 * implements the zoom logic
	 *
	 * @param direction
	 *            +1 in , -1 out, 0 .. no change
	 * @param current
	 *            current scale factor
	 * @param elements
	 *            number of elements that will be scaled
	 * @return
	 */
	public static float nextZoomDelta(int direction, float current, int elements) {
		if (direction == 0)
			return 0;
		float width = current * elements;

		// FIXME logic
		return current * direction * 0.2f;
	}
}
