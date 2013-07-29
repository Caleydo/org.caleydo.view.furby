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
public class LZThresholdChangeEvent extends AEvent {
	private float recordThreshold;
	private float dimensionThreshold;
	private boolean fixedClusterCount;
	private boolean global;

	public LZThresholdChangeEvent(float recordThreshold, float dimensionThreshold, boolean fixedClusterSelection,
			boolean global) {
		this.recordThreshold = recordThreshold;
		this.dimensionThreshold = dimensionThreshold;
		this.fixedClusterCount = fixedClusterSelection;
		this.global = global;
		System.out.println("Erstelle Cluster mit SampleTH: " + dimensionThreshold);
		System.out.println("                     RecordTH: " + recordThreshold);
	}

	public boolean isFixedClusterCount() {
		return fixedClusterCount;
	}

	public float getRecordThreshold() {
		return recordThreshold;
	}

	public float getDimensionThreshold() {
		return dimensionThreshold;
	}

	public boolean isGlobalEvent() {
		return global;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}


}
