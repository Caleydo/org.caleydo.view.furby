package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

public class ClusterGetsHiddenEvent extends AEvent {

	String clusterID;

	public ClusterGetsHiddenEvent(String clusterID) {
		this.clusterID = clusterID;
	}
	
	public  String getClusterID(){
		return clusterID;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
