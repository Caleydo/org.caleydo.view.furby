/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.NumericalTable;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.view.bicluster.sorting.FuzzyClustering;

/**
 * container for the combined biclustering information
 *
 * @author Samuel Gratzl
 *
 */
public class BiClustering {
	private final NumericalTable x;
	private final NumericalTable l; // record
	private final NumericalTable z; // dimension

	private final List<FuzzyClustering> lClusterings;
	private final List<FuzzyClustering> zClusterings;
	private final List<TablePerspective> clusterTablePerspectives;

	public BiClustering(NumericalTable x, NumericalTable l, NumericalTable z, List<FuzzyClustering> lClusterings,
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

	public IDType getBiClusterIDType() {
		return l.getDataDomain().getDimensionIDType();
	}

	public IDType getIDType(EDimension dim) {
		if (dim.isHorizontal())
			return l.getDataDomain().getDimensionIDType();
		else
			return l.getDataDomain().getRecordIDType();

	}

	/**
	 * @return the x, see {@link #x}
	 */
	public NumericalTable getX() {
		return x;
	}


	/**
	 * @return the l, see {@link #l}
	 */
	public NumericalTable getL() {
		return l;
	}

	/**
	 * @return the z, see {@link #z}
	 */
	public NumericalTable getZ() {
		return z;
	}

	/**
	 * @param dimension
	 * @return
	 */
	public NumericalTable getLZ(EDimension dimension) {
		return dimension.select(z, l);
	}

	/**
	 * @param dimension
	 * @return
	 */
	public DoubleStatistics getStats(EDimension dimension) {
		return dimension.select(z, l).getDatasetStatistics();
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
		return dim.isHorizontal() ? getDimClustering(bcNr) : getRecClustering(bcNr);
	}

	public float getMembership(EDimension dim, int bcNr, int index) {
		return dim.select(z, l).getRaw(bcNr, index);
	}

	public String getLabel(EDimension dim, int index) {
		ATableBasedDataDomain d = getXDataDomain();
		if (dim.isHorizontal()) {
			return d.getDimensionLabel(index);
		} else
			return d.getRecordLabel(index);
	}

	/**
	 * @param dimension
	 * @return
	 */
	public double getMaxAbsMembership(EDimension dimension) {
		DoubleStatistics stats = getStats(dimension);
		return Math.max(Math.abs(stats.getMin()), Math.abs(stats.getMax()));
	}

}
