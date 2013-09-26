/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.sorting.ISortingStrategyFactory;

/**
 * @author user
 *
 */
public class SortingChangeEvent extends AEvent {

	private final ISortingStrategyFactory factory;

	public SortingChangeEvent(ISortingStrategyFactory factory) {
		this.factory = factory;
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
