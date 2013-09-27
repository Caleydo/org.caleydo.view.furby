/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.view.bicluster.elem.EDimension;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomEvent extends ADirectedEvent {

	private final int direction;
	private final EDimension dim;

	public ZoomEvent(int direction, EDimension dim) {
		this.direction = direction;
		this.dim = dim;
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	/**
	 * @return the direction, see {@link #direction}
	 */
	public int getDirection() {
		return direction;
	}
}
