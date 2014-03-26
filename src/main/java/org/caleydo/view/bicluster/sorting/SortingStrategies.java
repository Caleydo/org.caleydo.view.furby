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
public final class SortingStrategies {
	private SortingStrategies() {

	}
	public static IGroupingStrategy findGrouping(ISortingStrategy s) {
		if (s instanceof IGroupingStrategy)
			return (IGroupingStrategy)s;
		if (s instanceof ComposedSortingStrategyFactory.Composed) {
			return findGrouping(((ComposedSortingStrategyFactory.Composed) s).getFirst());
		}
		return null;
	}
}
