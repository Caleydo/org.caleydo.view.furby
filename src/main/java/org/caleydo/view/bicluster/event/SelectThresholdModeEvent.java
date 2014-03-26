/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.view.bicluster.sorting.EThresholdMode;

/**
 * @author Samuel Gratzl
 *
 */
public class SelectThresholdModeEvent extends ADirectedEvent {
	private final EThresholdMode mode;

	public SelectThresholdModeEvent(EThresholdMode mode) {
		this.mode = mode;
	}

	/**
	 * @return the mode, see {@link #mode}
	 */
	public EThresholdMode getMode() {
		return mode;
	}
}
