package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class SpecialClusterRemoveEvent extends AEvent {

	boolean isDimCluster;
	
	public boolean isDimCluster() {
		return isDimCluster;
	}


	public SpecialClusterRemoveEvent(ClusterElement sender, boolean isDimCluster) {
		this.setSender(sender);
		this.isDimCluster = isDimCluster;
	}
	
	
	
	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
