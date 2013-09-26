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
public class ComposedSortingStrategy extends AComposeAbleSortingStrategy {

	private final AComposeAbleSortingStrategy[] comparators;

	@SafeVarargs
	public ComposedSortingStrategy(AComposeAbleSortingStrategy... comparators) {
		this.comparators = comparators;
	}

	@Override
	public int compare(IntFloat o1, IntFloat o2) {
		for (AComposeAbleSortingStrategy c : comparators) {
			int r = c.compare(o1, o2);
			if (r != 0)
				return r;
		}
		return 0;
	}

}
