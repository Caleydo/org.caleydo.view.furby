/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.util;

import org.caleydo.core.event.ADirectedEvent;

public class ClusterRenameEvent extends ADirectedEvent {

	private final String newName;

	public ClusterRenameEvent(String label) {
		this.newName = label;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

	public String getNewName() {
		return newName;
	}

}
