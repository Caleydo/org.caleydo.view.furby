/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import java.util.List;
import java.util.Map;

import org.caleydo.core.event.AEvent;

public class ChemicalClusterAddedEvent extends AEvent {

	private List<String> clusterList;
	public List<String> getClusterList() {
		return clusterList;
	}

	public Map<Integer, String> getElementToClusterMap() {
		return elementToClusterMap;
	}

	private Map<Integer, String> elementToClusterMap;

	public ChemicalClusterAddedEvent(List<String> clusterList,
			Map<Integer, String> elementToClusterMap) {
		this.clusterList = clusterList;
		this.elementToClusterMap = elementToClusterMap;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

}
