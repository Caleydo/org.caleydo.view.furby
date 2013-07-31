/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.elem.band.BandElement;

/**
 * event when the mouse if over a band
 *
 * @author Samuel Gratzl
 *
 */
public class MouseOverBandEvent extends AEvent {

	private final boolean in;

	public MouseOverBandEvent(BandElement band, boolean isMouseOver) {
		setSender(band);
		in = isMouseOver;
	}

	public ClusterElement getFirst() {
		return ((BandElement) getSender()).getFirst();
	}

	public ClusterElement getSecond() {
		return ((BandElement) getSender()).getSecond();
	}

	public boolean isMouseOver(){
		return in;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
