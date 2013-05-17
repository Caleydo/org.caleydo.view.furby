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
package org.caleydo.view.bicluster.concurrent;

import java.util.List;

/**
 * @author user
 *
 */
public class ScanResult {

	private final List<Integer> indices;
	private final double max;
	private final double min;

	public ScanResult(List<Integer> indices, double max, double min) {
		this.indices = indices;
		this.max = max;
		this.min = min;
	}

	/**
	 * @return the indices, see {@link #indices}
	 */
	public List<Integer> getIndices() {
		return indices;
	}

	/**
	 * @return the max, see {@link #max}
	 */
	public double getMax() {
		return max;
	}

	public double getMin(){
		return min;
	}
}
