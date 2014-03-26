/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import org.caleydo.core.util.base.ILabeled;

/**
 * @author Samuel Gratzl
 *
 */
public enum EThresholdMode implements ILabeled {
	ABS, NEGATIVE_ONLY, POSITVE_ONLY;

	public boolean includeNegatives() {
		return this != POSITVE_ONLY;
	}

	public boolean includePositives() {
		return this != NEGATIVE_ONLY;
	}

	/**
	 * @return
	 */
	@Override
	public String getLabel() {
		switch(this) {
		case ABS:
			return "Absolute Levels: abs(x) >= thresh";
		case POSITVE_ONLY:
			return "Positive Levels only: x >= thresh";
		case NEGATIVE_ONLY:
			return "Negative Levels only: x <= -thresh";
		}
		throw new IllegalStateException("unknown me");
	}
}
