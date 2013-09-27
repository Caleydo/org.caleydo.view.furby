/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomLogic {

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
		float expected = current * elements;
		// FIXME logic
		return direction * 0.2f;
	}
}
