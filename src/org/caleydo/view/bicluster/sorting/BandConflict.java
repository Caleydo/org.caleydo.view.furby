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
package org.caleydo.view.bicluster.sorting;

import java.util.List;

/**
 * @author Michael Gillhofer
 *
 */
public class BandConflict implements Comparable<BandConflict> {
	List<Integer> first;
	List<Integer> second;
	List<Integer> conflict;

	/**
	 * @return the first, see {@link #first}
	 */
	public List<Integer> getFirst() {
		return first;
	}

	/**
	 * @return the second, see {@link #second}
	 */
	public List<Integer> getSecond() {
		return second;
	}

	/**
	 * @return the conflict, see {@link #conflict}
	 */
	public List<Integer> getConflict() {
		return conflict;
	}

	@Override
	public int compareTo(BandConflict o) {
		return new Integer(conflict.size()).compareTo(o.conflict.size());
	}

	/**
	 *
	 */
	public BandConflict(List<Integer> first, List<Integer> second, List<Integer> conflict) {
		this.first = first;
		this.second = second;
		this.conflict = conflict;
	}

}
