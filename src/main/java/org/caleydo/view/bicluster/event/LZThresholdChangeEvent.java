/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.util.logging.Logger;

/**
 * @author user
 *
 */
public class LZThresholdChangeEvent extends AEvent {
	private static final Logger log = Logger.create(LZThresholdChangeEvent.class);

	private float recordThreshold;
	private float dimensionThreshold;
	private boolean global;
	private final int recordNumberThreshold;
	private final int dimensionNumberThreshold;

	public LZThresholdChangeEvent(float recordThreshold, float dimensionThreshold, int recordNumberThreshold,
			int dimensionNumberThreshold, boolean global) {
		this.recordThreshold = recordThreshold;
		this.dimensionThreshold = dimensionThreshold;
		this.recordNumberThreshold = recordNumberThreshold;
		this.dimensionNumberThreshold = dimensionNumberThreshold;
		this.global = global;
		log.debug("changing threshold to: dim: " + dimensionThreshold + "#" + dimensionNumberThreshold + " rec: "
				+ recordThreshold + "#" + recordNumberThreshold);
	}

	/**
	 * @return the recordNumberThreshold, see {@link #recordNumberThreshold}
	 */
	public int getRecordNumberThreshold() {
		return recordNumberThreshold;
	}

	/**
	 * @return the dimensionNumberThreshold, see {@link #dimensionNumberThreshold}
	 */
	public int getDimensionNumberThreshold() {
		return dimensionNumberThreshold;
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
