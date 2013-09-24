/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.List;

import org.caleydo.core.data.collection.table.NumericalTable;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.view.bicluster.sorting.FuzzyClustering;

/**
 * container for the combined biclustering information
 *
 * @author Samuel Gratzl
 *
 */
public class BiClustering {
	private final Table x;
	private final Table l; // record
	private final Table z; // dimension

	private final List<FuzzyClustering> lClusterings;
	private final List<FuzzyClustering> zClusterings;
	private final List<TablePerspective> clusterTablePerspectives;

	public BiClustering(Table x, Table l, Table z, List<FuzzyClustering> lClusterings,
			List<FuzzyClustering> zClusterings, List<TablePerspective> clusterTablePerspectives) {
		this.x = x;
		this.l = l;
		this.z = z;
		this.lClusterings = lClusterings;
		this.zClusterings = zClusterings;
		this.clusterTablePerspectives = clusterTablePerspectives;
	}

	public int getBiClusterCount() {
		return clusterTablePerspectives.size();
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

	/**
	 * @param dimension
	 * @return
	 */
	public DoubleStatistics getStats(EDimension dimension) {
		return ((NumericalTable) dimension.select(z, l)).getDatasetStatistics();
	}

	/**
	 * @return
	 */
	public ATableBasedDataDomain getXDataDomain() {
		return x.getDataDomain();
	}

	public FuzzyClustering getDimClustering(int bcNr) {
		return zClusterings.get(bcNr);
	}

	public TablePerspective getData(int bcNr) {
		return clusterTablePerspectives.get(bcNr);
	}

	public FuzzyClustering getRecClustering(int bcNr) {
		return lClusterings.get(bcNr);
	}

	public FuzzyClustering getClustering(EDimension dim, int bcNr) {
		return dim.isHorizontal() ? getDimClustering(bcNr) : zClusterings.get(bcNr);
	}

	public float getProbability(EDimension dim, int bcNr, int index) {
		return dim.select(l, z).getRaw(bcNr, index);
	}

	/**
	 * @param dimension
	 * @return
	 */
	public double getMaxAbsProbability(EDimension dimension) {
		DoubleStatistics stats = getStats(dimension);
		return Math.max(Math.abs(stats.getMin()), Math.abs(stats.getMax()));
	}

}
