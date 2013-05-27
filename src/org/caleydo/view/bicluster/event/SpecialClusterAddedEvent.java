package org.caleydo.view.bicluster.event;

import java.util.List;

import org.caleydo.core.event.AEvent;

public class SpecialClusterAddedEvent extends AEvent {

	private List<Integer> elements;
	private boolean isDimCluster;
	
	
	public SpecialClusterAddedEvent(List<Integer> elements, boolean isDimCluster) {
		this.elements = elements;
		this.isDimCluster = isDimCluster;
	}
	
	public List<Integer> getElements() {
		return elements;
	}
	
	public boolean isDimensionCluster() {
		return isDimCluster;
	}
	
	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
