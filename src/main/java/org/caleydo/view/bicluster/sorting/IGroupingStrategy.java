/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.List;

import org.caleydo.core.data.virtualarray.group.GroupList;

/**
 * special version of a {@link ISortingStrategy}, which determines a grouping criteria
 *
 * @author Samuel Gratzl
 *
 */
public interface IGroupingStrategy {

	GroupList getGrouping(List<IntFloat> sortedList);
}
