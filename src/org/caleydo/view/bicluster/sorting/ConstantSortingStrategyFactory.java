/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import org.caleydo.view.bicluster.elem.EDimension;
import org.caleydo.view.bicluster.elem.NormalClusterElement;

/**
 * @author Samuel Gratzl
 *
 */
public class ConstantSortingStrategyFactory implements ISortingStrategyFactory {
	private final ISortingStrategy instance;
	private final String label;

	public ConstantSortingStrategyFactory(ISortingStrategy instance, String label) {
		this.instance = instance;
		this.label = label;
	}

	@Override
	public ISortingStrategy create(NormalClusterElement cluster, EDimension dim) {
		return instance;
	}

	@Override
	public String getLabel() {
		return label;
	}
}
