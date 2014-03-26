/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.sorting.ISortingStrategyFactory;

/**
 * @author user
 *
 */
public class SortingChangeEvent extends AEvent {

	private final ISortingStrategyFactory factory;
	private final EDimension dimension;

	public SortingChangeEvent(ISortingStrategyFactory factory, EDimension dimension) {
		this.factory = factory;
		this.dimension = dimension;
	}

	/**
	 * @return the dimension, see {@link #dimension}
	 */
	public EDimension getDimension() {
		return dimension;
	}

	/**
	 * @return the factory, see {@link #factory}
	 */
	public ISortingStrategyFactory getFactory() {
		return factory;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
}
