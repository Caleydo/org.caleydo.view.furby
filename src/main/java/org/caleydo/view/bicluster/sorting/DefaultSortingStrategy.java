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
public class DefaultSortingStrategy extends AComposeAbleSortingStrategy {
	public static final ISortingStrategyFactory FACTORY = new ConstantSortingStrategyFactory(
			new DefaultSortingStrategy(), "Data Order");

	private DefaultSortingStrategy() {

	}

	@Override
	public int compare(IntFloat o1, IntFloat o2) {
		return o1.getIndex() - o2.getIndex();
	}
}
