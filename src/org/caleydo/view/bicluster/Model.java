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
package org.caleydo.view.bicluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.id.IDType;
import org.caleydo.view.bicluster.concurrent.ScanProbabilityMatrix;

/**
 * @author Michael Gillhofer
 *
 */
public class Model {

	ExecutorService executorService;

	float sampleThreshold = 2f;

	/**
	 * @param sampleThreshold
	 *            setter, see {@link sampleThreshold}
	 */
	public void setSampleThreshold(float sampleThreshold) {
		this.sampleThreshold = sampleThreshold;
	}

	float geneThreshold = 0.1f;
	/**
	 * @param geneThreshold
	 *            setter, see {@link geneThreshold}
	 */
	public void setGeneThreshold(float geneThreshold) {
		this.geneThreshold = geneThreshold;
	}


	public static Model getModel() {
		return new Model();
	}

	private Model() {
		this.executorService = Executors.newFixedThreadPool(4);
	}

	@SuppressWarnings("null")
	protected List<TablePerspective> createBiClusterPerspectives(TablePerspective x, TablePerspective l,
			TablePerspective z) {
		System.out.println("Erstelle Cluster mit SampleTH: " + sampleThreshold);
		System.out.println("                     RecordTH: " + geneThreshold);
		ATableBasedDataDomain xdd = x.getDataDomain();
		Table xtable = xdd.getTable();
		IDType xdimtype = xdd.getDimensionIDType();
		IDType xrectype = xdd.getRecordIDType();

		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		int bcCountData = L.getColumnIDList().size(); // Nr of BCs in L & Z

		// Tables indices for Genes and Tables of a specific BiCluster.
		Map<Integer, Future<ArrayList<Integer>>> bcDimScanFut = new HashMap<>();
		Map<Integer, Future<ArrayList<Integer>>> bcRecScanFut = new HashMap<>();
		for (int bcNr = 0; bcNr < bcCountData; bcNr++) {
			Future<ArrayList<Integer>> recList = executorService.submit(new ScanProbabilityMatrix(geneThreshold, L,
					bcNr));
			Future<ArrayList<Integer>> dimList = executorService.submit(new ScanProbabilityMatrix(sampleThreshold, Z,
					bcNr));

			bcRecScanFut.put(bcNr, recList);
			bcDimScanFut.put(bcNr, dimList);
		}


		List<TablePerspective> perspectives = new ArrayList<TablePerspective>();

		// actually create the cluster perspectives
		for (Integer i : bcDimScanFut.keySet()) {
			ArrayList<Integer> recIndices = null;
			ArrayList<Integer> dimIndices = null;
			try {
				dimIndices = bcDimScanFut.get(i).get();
				recIndices = bcRecScanFut.get(i).get();
				if (dimIndices.size() > 0 && recIndices.size() > 0) {
					addBiClusterTablePerspective(xdd, xtable, xdimtype, xrectype, dimIndices, recIndices, perspectives);
				}
			} catch (InterruptedException | ExecutionException | NullPointerException e) {

				e.printStackTrace();
			}
		}
		return perspectives;
	}

	private void addBiClusterTablePerspective(ATableBasedDataDomain xdataDomain, Table xtable, IDType xdimtype,
			IDType xrectype, ArrayList<Integer> bcDimIndices, ArrayList<Integer> bcRecIndices,
			List<TablePerspective> perspectives) {
		Perspective dim = new Perspective(xdataDomain, xdimtype);
		Perspective rec = new Perspective(xdataDomain, xrectype);
		PerspectiveInitializationData dim_init = new PerspectiveInitializationData();
		PerspectiveInitializationData rec_init = new PerspectiveInitializationData();
		dim_init.setData(bcDimIndices);
		rec_init.setData(bcRecIndices);
		dim.init(dim_init);
		rec.init(rec_init);
		xtable.registerDimensionPerspective(dim, false);
		xtable.registerRecordPerspective(rec, false);
		String dimKey = dim.getPerspectiveID();
		String recKey = rec.getPerspectiveID();
		TablePerspective custom = xdataDomain.getTablePerspective(recKey, dimKey, false);
		perspectives.add(custom);
	}

}
