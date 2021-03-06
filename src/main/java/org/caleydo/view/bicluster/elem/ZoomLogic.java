/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Map;

import org.caleydo.core.data.collection.EDimension;
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

	/**
	 * computes the initial scale factors for the presented clusters with the given dimensions
	 *
	 * @param sizes
	 * @param w
	 * @param h
	 * @return
	 */
	public static Map<EDimension, Float> initialOverviewScaleFactor(Collection<Dimension> sizes, float w, float h) {
		DoubleStatistics.Builder aspectRatios = DoubleStatistics.builder();
		DoubleStatistics.Builder dims = DoubleStatistics.builder();
		DoubleStatistics.Builder recs = DoubleStatistics.builder();

		for (Dimension size : sizes) {
			if (size.width == 0 || size.height == 0)
				continue;
			dims.add(size.width);
			recs.add(size.height);
			aspectRatios.add(size.getWidth() / size.height);
		}
		final double target = MyPreferences.getAspectRatio();
		final double mean = dims.build().getMax() / recs.build().getMax();

		// force on average a specific aspect ratio
		double rec = 1;
		double dim = (float) (target / mean);

		// fit the aspect ratio scaled elements to the target size
		double used = count(sizes, dim, rec);
		double targetUsed = w * h * INITIAL_FILLED_PERCENTAGE; // FIXME

		double scale = Math.sqrt(targetUsed / used);
		dim = Math.floor(dim * scale * 100) / 100.f;
		rec = Math.floor(rec * scale * 100) / 100.f;

		return pair((float) dim, (float) rec);
	}

	private static double count(Collection<Dimension> sizes, double dim, double rec) {
		double sum = 0;
		for (Dimension s : sizes)
			sum += s.width * s.height * dim * rec;
		return sum;
	}

	/**
	 * computes the initial focus scale factor
	 *
	 * @param size
	 * @param w
	 * @param h
	 * @return
	 */
	public static Map<EDimension, Float> initialFocusScaleFactor(Dimension size, float w, float h) {
		w *= 0.75f;
		h *= 0.9f;
		float dim = w / size.width;
		float rec = h / size.height;
		return pair(dim, rec);
	}

	/**
	 * computes the initial focus scale factor for focus neighbors
	 *
	 * @param sizes
	 * @param focusSize
	 * @param w
	 * @param h
	 * @return
	 */
	public static Map<EDimension, Float> initialFocusNeighborScaleFactor(Collection<Dimension> sizes,
			Dimension focusSize, float w, float h) {
		float dim = 1f;
		float rec = 1;
		return pair(dim, rec);
	}

	private static ImmutableMap<EDimension, Float> pair(float dim, float rec) {
		return ImmutableMap.of(EDimension.DIMENSION, dim, EDimension.RECORD, rec);
	}

	/**
	 * convert a {@link IMouseEvent} to a direction information
	 *
	 * @param event
	 * @param dim
	 * @return -1 smaller, +1 larger, and 0 nothing
	 */
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

	public static float adaptScaleFactorToSize(EDimension dir, Dimension oldSize, Dimension newSize, float currentDim,
			float currentRec, float w, float h) {
		final int delta = dir.select(newSize.width - oldSize.width, newSize.height - oldSize.height);
		if (delta == 0)
			return 1; // no change
		final float scale = dir.select(currentDim, currentRec);
		final float total = dir.select(w, h);
		final float old = dir.select(oldSize.width, oldSize.height);
		final float new_ = dir.select(newSize.width, newSize.height);

		if (new_ == 0) // not visible anymore
			return 1;

		if (delta > 0 && new_ * scale > total * 0.15) { // increase and too large
			float filled = new_ * scale / (total * 0.3f);
			if (filled > 1)
				filled = 1;
			float f = lookup(filled);
			float factor = (old + delta * f) / new_;
			// System.out.println(old + " " + delta + "*" + f + " / " + new_ + " " + filled);
			return factor;
		} else if (delta < 0 && new_ * scale < total * 0.1) { // shrink and too small
			float filled = new_ * scale / (total * 0.1f);
			if (filled > 1)
				filled = 1;
			float f = lookup(1 - filled);
			float factor = (old + delta * f) / new_;
			// System.out.println("" + old + " " + delta + "*" + f + " / " + new_ + " " + filled);
			return factor;
		}

		// float deltaDim = dim * currentDim;
		// float deltaRec = rec * currentRec;
		//
		// if (dim != 0) {
		// if (dim < 0 && newSize.width * currentDim > w * 0.05) {
		// float filled = newSize.width * currentDim / (w * 0.05f);
		// float f = lookup(filled);
		// float factor = (oldSize.width + -dim * f) / newSize.width;
		// return factor;
		// } else if (dim > 0 && oldSize.width * currentDim > w * 0.05 && newSize.width * currentDim < w * 0.03) {
		// float factor = (oldSize.width + -dim * 1.1f) / newSize.width;
		// return factor;
		// }
		// }
		// if (rec != 0) {
		// if (rec < 0 && newSize.height * currentRec > h * 0.05) {
		// return (float) Math.pow(0.999f, -rec);
		// } else if (rec > 0 && oldSize.height * currentRec > h * 0.05)
		// return (float) Math.pow(0.999f, -rec);
		// }
		return 1.f;
	}

	/**
	 * @param filled
	 * @return
	 */
	private static float lookup(float filled) {
		if (filled <= .8f) // small enough
			return 1.f;
		if (filled <= .9f) //
			return 0.65f + 0.35f * (filled - .8f);
		return 0.2f + 0.45f * (filled - .9f);

	}
}
