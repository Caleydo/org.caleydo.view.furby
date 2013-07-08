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

	SortingType sortingType;





	public SortingType getType() {
		return sortingType;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}


	public SortingChangeEvent(SortingType sortingType, Object source) {
		this.sortingType = sortingType;
		this.setSender(source);
	}

	public enum SortingType {
		probabilitySorting, bandSorting
	}

}
