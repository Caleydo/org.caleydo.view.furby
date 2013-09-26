/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

/**
 * @author Samuel Gratzl
 *
 */
public class AllEqualsSortringStrategy extends AComposeAbleSortingStrategy {

	public static final ISortingStrategy INSTANCE = new AllEqualsSortringStrategy();

	private AllEqualsSortringStrategy() {

	}

	@Override
	public int compare(IntFloat o1, IntFloat o2) {
		return 0; // always 0
	}

}
