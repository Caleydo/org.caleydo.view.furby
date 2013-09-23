/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public enum EDimension {
	RECORD, DIMENSION;

	public static EDimension get(boolean dimension) {
		return dimension ? DIMENSION : RECORD;
	}


	public Color getBandColor() {
		switch(this) {
		case DIMENSION:
			return Color.NEUTRAL_GREY;
		case RECORD:
			return Color.LIGHT_GRAY;
		}
		throw new IllegalStateException();

	}

	public boolean select(boolean dim, boolean rec) {
		return this == DIMENSION ? dim : rec;
	}

}
