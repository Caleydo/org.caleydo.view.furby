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

	public BandElement getBand() {
		return (BandElement) getSender();
	}

	public ClusterElement getFirst() {
		return getBand().getFirst();
	}

	public ClusterElement getSecond() {
		return getBand().getSecond();
	}

	public boolean isMouseOver(){
		return in;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
