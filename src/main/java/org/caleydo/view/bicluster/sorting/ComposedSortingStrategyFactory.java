/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.bicluster.elem.NormalClusterElement;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy.IComposeAbleSortingStrategyFactory;


/**
 * @author Samuel Gratzl
 *
 */
public class ComposedSortingStrategyFactory implements ISortingStrategyFactory {
	private final IComposeAbleSortingStrategyFactory[] factories;

	public ComposedSortingStrategyFactory(IComposeAbleSortingStrategyFactory... factories) {
		this.factories = factories;
	}

	@Override
	public String getLabel() {
		return "Composed " + StringUtils.join(factories);
	}

	@Override
	public ISortingStrategy create(NormalClusterElement cluster, EDimension dim) {
		AComposeAbleSortingStrategy[] comparators = new AComposeAbleSortingStrategy[factories.length];
		for (int i = 0; i < comparators.length; ++i)
			comparators[i] = factories[i].create(cluster, dim);
		return new Composed(comparators);
	}

	static final class Composed extends AComposeAbleSortingStrategy {
		private final AComposeAbleSortingStrategy[] comparators;

		public Composed(AComposeAbleSortingStrategy... comparators) {
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

		/**
		 * @return
		 */
		public ISortingStrategy getFirst() {
			return comparators[0];
		}
	}
}
