/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;


/**
 * in the center all highest probabilities are together
 *
 * @author Samuel Gratzl
 *
 */
public class CenterProbabilitySortingStrategy extends AComposeAbleSortingStrategy {
	public static final ISortingStrategyFactory FACTORY = new ConstantSortingStrategyFactory(
			new CenterProbabilitySortingStrategy(), "Probability (Centered Spread)");

	@Override
	public int compare(IntFloat o1, IntFloat o2) {
		final float a = o1.getProbability();
		final float b = o2.getProbability();
		if (Float.compare(a, b) == 0)
			return 0;
		if (a < 0 && b < 0)
			return a < b ? 1 : -1;
		if (a < 0 && b >= 0)
			return -1;
		if (a >= 0 && b < 0)
			return 1;
		if (a >= 0 && b >= 0)
			return a > b ? 1 : -1;
		return 0;// shoudn't happen
	}

}
