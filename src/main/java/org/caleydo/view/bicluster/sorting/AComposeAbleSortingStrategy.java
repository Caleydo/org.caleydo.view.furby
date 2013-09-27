/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.caleydo.view.bicluster.elem.EDimension;
import org.caleydo.view.bicluster.elem.NormalClusterElement;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AComposeAbleSortingStrategy implements ISortingStrategy, Comparator<IntFloat> {
	@Override
	public final List<IntFloat> apply(List<IntFloat> list) {
		List<IntFloat> r = new ArrayList<>(list);
		Collections.sort(r, this);
		return r;
	}

	@Override
	public final boolean needsResortAfterBandsUpdate() {
		return false;
	}

	public interface IComposeAbleSortingStrategyFactory extends ISortingStrategyFactory {

		@Override
		AComposeAbleSortingStrategy create(NormalClusterElement cluster, EDimension dim);
	}
}
