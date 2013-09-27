/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.ADirectedEvent;

/**
 * toolbar changes for show/hide bands
 * 
 * @author Samuel Gratzl
 * 
 */
public class ShowHideBandsEvent extends ADirectedEvent {
	private final boolean showDimBand;
	private final boolean showRecBand;

	public ShowHideBandsEvent(boolean showDimBand, boolean showRecBand) {
		this.showDimBand = showDimBand;
		this.showRecBand = showRecBand;
	}

	/**
	 * @return the showDimBand, see {@link #showDimBand}
	 */
	public boolean isShowDimBand() {
		return showDimBand;
	}

	/**
	 * @return the showRecBand, see {@link #showRecBand}
	 */
	public boolean isShowRecBand() {
		return showRecBand;
	}
}
