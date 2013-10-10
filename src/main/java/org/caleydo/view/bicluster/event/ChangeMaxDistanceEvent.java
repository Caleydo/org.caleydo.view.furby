/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class ChangeMaxDistanceEvent extends ADirectedEvent {

	private final int maxDistance;

	public ChangeMaxDistanceEvent(int maxDistance) {
		this.maxDistance = maxDistance;
	}

	/**
	 * @return the maxDistance, see {@link #maxDistance}
	 */
	public int getMaxDistance() {
		return maxDistance;
	}
}
