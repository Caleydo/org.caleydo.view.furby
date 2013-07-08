/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;

/**
 * @author user
 *
 */
public class LZThresholdChangeEvent extends AEvent {


	@Override
	public boolean checkIntegrity() {
		return true;
	}

	float recordThreshold;
	float dimensionThreshold;
	boolean fixedClusterCount;
	boolean global; 



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

	public LZThresholdChangeEvent(float recordThreshold, float dimensionThreshold, boolean fixedClusterSelection, boolean global) {
		this.recordThreshold = recordThreshold;
		this.dimensionThreshold = dimensionThreshold;
		this.fixedClusterCount = fixedClusterSelection;
		this.global = global;
		System.out.println("Erstelle Cluster mit SampleTH: " + dimensionThreshold);
		System.out.println("                     RecordTH: " + recordThreshold);
	}


}
