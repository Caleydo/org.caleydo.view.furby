/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.List;
import java.util.Set;

import org.caleydo.core.util.collection.Pair;

/**
 * @author Michael Gillhofer
 *
 */

public abstract class ASortingStrategy implements Iterable<Integer> {



	protected Set<Pair<Integer, Float>> indices;

	public abstract void setIndices(List<Integer> dimIndices);

	public abstract void addIndex(int index);

}
