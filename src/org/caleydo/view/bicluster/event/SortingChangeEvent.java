/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * @author user
 *
 */
public class SortingChangeEvent extends AEvent {

	private final SortingType sortingType;

	public SortingChangeEvent(SortingType sortingType, Object source) {
		this.sortingType = sortingType;
		this.setSender(source);
	}

	public SortingType getType() {
		return sortingType;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

	public enum SortingType {
		BY_PROBABILITY, BY_BAND
	}

}
