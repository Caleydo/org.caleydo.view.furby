/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Map;

import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;

import com.google.common.collect.ImmutableMap;

/**
 * encapsulate all the zoom logic relevant settings
 * 
 * @author Samuel Gratzl
 * 
 */
public class ZoomLogic {

	public static Map<EDimension, Float> initialOverviewScaleFactor(Collection<Dimension> sizes, float w, float h) {
		float dim = .5f;
		float rec = 1;
		return ImmutableMap.of(EDimension.DIMENSION, dim, EDimension.RECORD, rec);
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
