/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.bicluster.concurrent.ScanProbabilityMatrix;
import org.caleydo.view.bicluster.concurrent.ScanResult;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;
import org.caleydo.view.bicluster.sorting.ProbabilityStrategy;

/**
 * container for the combined biclustering information
 * 
 * @author Samuel Gratzl
 * 
 */
public class BiClustering {
	private final Table x;
	private final Table l;
	private final Table z;
	private final ExecutorService executor;

	public BiClustering(Table x, Table l, Table z, ExecutorService executor) {
		this.x = x;
		this.l = l;
		this.z = z;
		this.executor = executor;
	}

	/**
	 * @return the x, see {@link #x}
	 */
	public Table getX() {
		return x;
	}

	/**
	 * @return the l, see {@link #l}
	 */
	public Table getL() {
		return l;
	}

	/**
	 * @return the z, see {@link #z}
	 */
	public Table getZ() {
		return z;
	}

	public Pair<List<Integer>, List<Integer>> scan(int bcNr, float dimThreshold, float recThreshold) {
		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		// Pair<List<Integer>,List<Integer>> r = compute.comptue(bcNr, recThreshold, dimThreshold));
		Future<ScanResult> recList = null, dimList = null;
		ASortingStrategy strategy = new ProbabilityStrategy(L, bcNr);
		recList = executor.submit(new ScanProbabilityMatrix(recThreshold, L, bcNr, strategy));
		strategy = new ProbabilityStrategy(Z, bcNr);
		dimList = executor.submit(new ScanProbabilityMatrix(dimThreshold, Z, bcNr, strategy));
		List<Integer> dimIndices = null, recIndices = null;
		try {
			dimIndices = dimList.get().getIndices();
			recIndices = recList.get().getIndices();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return Pair.make(dimIndices, recIndices);
	}

	/**
	 * @return
	 */
	public ATableBasedDataDomain getXDataDomain() {
		return x.getDataDomain();
	}
}
