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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michael Gillhofer
 *
 */
public class BandSorting implements Iterable<Integer> {

	private List<List<Integer>> nonEmptyBands;
	private Set<BandConflict> conflicts = new TreeSet<>();
	private Set<Integer> finalSorting = new LinkedHashSet<>();

	public BandSorting(List<List<Integer>> nonEmptyBands) {
		this.nonEmptyBands = nonEmptyBands;
		init();
	}

	private void init() {
		for (int i = 0; i < nonEmptyBands.size(); i++) {
			for (int j = i; j < nonEmptyBands.size(); j++) {
				if (i == j) {
					continue;
				}
				List<Integer> potentialConflict = new ArrayList<Integer>(nonEmptyBands.get(i));
				potentialConflict.retainAll(nonEmptyBands.get(j));
				conflicts.add(new BandConflict(nonEmptyBands.get(i), nonEmptyBands.get(j), potentialConflict));

			}
		}
		for (BandConflict bc : conflicts) {
			List<Integer> firstWithoutConflicts = new ArrayList<>(bc.getFirst());
			if (bc.getConflict().size() > 0)
				firstWithoutConflicts.removeAll(bc.getConflict());
			finalSorting.addAll(firstWithoutConflicts);
			finalSorting.addAll(bc.getConflict());
			finalSorting.addAll(bc.getSecond());
		}
	}

	@Override
	public Iterator<Integer> iterator() {
		return finalSorting.iterator();
	}

}
