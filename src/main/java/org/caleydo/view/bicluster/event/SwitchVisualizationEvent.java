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
public class SwitchVisualizationEvent extends ADirectedEvent {
	private final String id;

	public SwitchVisualizationEvent(String id) {
		this.id = id;
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public String getId() {
		return id;
	}
}
