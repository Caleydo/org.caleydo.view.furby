package org.caleydo.view.bicluster.event;



import org.caleydo.core.event.AEvent;

public class MinClusterSizeThresholdChangeEvent extends AEvent {

	private float minClusterSize;

	public MinClusterSizeThresholdChangeEvent(float minClusterSize) {
		this.minClusterSize = minClusterSize;
	}

	public float getMinClusterSize() {
		return minClusterSize;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
