/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

/**
 * different zoom mode each has a different stored zoom state
 * 
 * @author Samuel Gratzl
 * 
 */
public enum EZoomMode {
	OVERVIEW, FOCUS, FOCUS_NEIGHBOR;

	public int getOffset() {
		return ordinal()*2;
	}
}