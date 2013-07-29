/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * @author user
 *
 */
public class ClusterResizeEvent extends AEvent {

	private final boolean zoom;

	public ClusterResizeEvent (boolean zoom) {
		this.zoom = zoom;
	}

	/**
	 * @return the zoom, see {@link #zoom}
	 */
	public boolean isZoom() {
		return zoom;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
