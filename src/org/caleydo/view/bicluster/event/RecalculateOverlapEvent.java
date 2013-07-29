/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;

public class RecalculateOverlapEvent extends AEvent {

	private final boolean isGlobal;
	private final boolean dimBands;
	private final boolean recBands;

	public RecalculateOverlapEvent(GLElement sender, boolean isGlobal, boolean dimBands, boolean recBands) {
		this.setSender(sender);
		this.isGlobal = isGlobal;
		this.dimBands = dimBands;
		this.recBands = recBands;
	}
	public boolean isGlobal(){
		return isGlobal;
	}

	public boolean isDimBandEnabled() {
		return dimBands;
	}

	public boolean isRecBandEnabled() {
		return recBands;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}


}
