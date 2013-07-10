/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;

/**
 * event for showing a toolbar
 * 
 * @author Samuel Gratzl
 * 
 */
public class ShowToolBarEvent extends ADirectedEvent {
	private final boolean showParameter;

	public ShowToolBarEvent(boolean showParameter) {
		this.showParameter = showParameter;
	}

	/**
	 * @return the showParameter, see {@link #showParameter}
	 */
	public boolean isShowParameter() {
		return showParameter;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
}
