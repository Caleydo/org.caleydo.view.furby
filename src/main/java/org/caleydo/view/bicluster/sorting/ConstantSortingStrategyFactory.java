/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.bicluster.elem.NormalClusterElement;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy.IComposeAbleSortingStrategyFactory;

/**
 * @author Samuel Gratzl
 *
 */
public class ConstantSortingStrategyFactory implements IComposeAbleSortingStrategyFactory {
	private final AComposeAbleSortingStrategy instance;
	private final String label;

	public ConstantSortingStrategyFactory(AComposeAbleSortingStrategy instance, String label) {
		this.instance = instance;
		this.label = label;
	}

	@Override
	public AComposeAbleSortingStrategy create(NormalClusterElement cluster, EDimension dim) {
		return instance;
	}

	@Override
	public String getLabel() {
		return label;
	}
}
