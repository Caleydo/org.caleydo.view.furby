/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.view.bicluster.concurrent.ScanProbabilityMatrix;
import org.caleydo.view.bicluster.concurrent.ScanResult;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.elem.GLRootElement;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;
import org.caleydo.view.bicluster.sorting.ProbabilityStrategy;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * <p>
 * Sample GL2 view.
 * </p>
 * <p>
 * This Template is derived from {@link ATableBasedView}, but if the view does not use a table, changing that to
 * {@link AGLView} is necessary.
 * </p>
 *
 * @author Michael Gillhofer
 * @author Marc Streit
 */

public class GLBiCluster extends AMultiTablePerspectiveElementView {
	public static final String VIEW_TYPE = "org.caleydo.view.bicluster";
	public static final String VIEW_NAME = "BiCluster Visualization";

	private TablePerspective x, l, z;

	private ExecutorService executorService = Executors.newFixedThreadPool(4);

	private float sampleThreshold = 4.5f;
	private float geneThreshold = 0.08f;
	double maxDimThreshold = 0, maxRecThreshold = 0;
	ASerializedView view;

	GLRootElement rootElement;
	private boolean setXElements = false;

	public GLBiCluster(IGLCanvas glCanvas, ASerializedView serializedView) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		this.view = serializedView;
	}

	private List<TablePerspective> initTablePerspectives() {

		List<TablePerspective> persp = new ArrayList<>();
		if (x.getDataDomain().getAllTablePerspectives().size() == 1) {
			int bcCountData = l.getDataDomain().getTable().getColumnIDList()
					.size(); // Nr of BCs in L & Z
			ATableBasedDataDomain xDataDomain = x.getDataDomain();
			Table xtable = xDataDomain.getTable();
			IDType xdimtype = xDataDomain.getDimensionIDType();
			IDType xrectype = xDataDomain.getRecordIDType();
			for (Integer i = 0; i < bcCountData; i++) {
				Perspective dim = new Perspective(xDataDomain, xdimtype);
				Perspective rec = new Perspective(xDataDomain, xrectype);
				PerspectiveInitializationData dim_init = new PerspectiveInitializationData();
				PerspectiveInitializationData rec_init = new PerspectiveInitializationData();
				dim_init.setData(new ArrayList<Integer>());
				rec_init.setData(new ArrayList<Integer>());
				dim.init(dim_init);
				rec.init(rec_init);
				xtable.registerDimensionPerspective(dim, false);
				xtable.registerRecordPerspective(rec, false);
				String dimKey = dim.getPerspectiveID();
				String recKey = rec.getPerspectiveID();
				TablePerspective custom = xDataDomain.getTablePerspective(
						recKey, dimKey, false);
				custom.setLabel(l.getDataDomain().getDimensionLabel(i));
				dim.setLabel(l.getDataDomain().getDimensionLabel(i) + " L");
				rec.setLabel(z.getDataDomain().getDimensionLabel(i) + " Z");
				persp.add(custom);
			}
		} else {
			int i = 0;
			int bcCount = l.getDataDomain().getTable().getColumnIDList().size();
			for (i = 0; i < bcCount; i++) {
				biClusterLabels.add(l.getDataDomain().getDimensionLabel(i) + " L");
			}
//			System.out.println(biClusterLabels);
			for (TablePerspective p : x.getDataDomain()
					.getAllTablePerspectives()) {
				String name = p.getDimensionPerspective().getLabel();
//				System.out.println(name);
				if (biClusterLabels.contains(name)) {
					persp.add(p);
				}
			}
		}
		return persp;
	}

	private List<String> biClusterLabels = new ArrayList<>();

	protected void createBiClusterPerspectives(TablePerspective x,
			TablePerspective l, TablePerspective z) {

		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		int bcCountData = L.getColumnIDList().size(); // Nr of BCs in L & Z

		// Tables indices for Genes and Tables of a specific BiCluster.
		Map<Integer, Future<ScanResult>> bcDimScanFut = new HashMap<>();
		Map<Integer, Future<ScanResult>> bcRecScanFut = new HashMap<>();
		for (int bcNr = 0; bcNr < bcCountData; bcNr++) {
			ASortingStrategy strategy = new ProbabilityStrategy(L, bcNr);
			Future<ScanResult> recList = executorService
					.submit(new ScanProbabilityMatrix(geneThreshold, L, bcNr,
							strategy));
			strategy = new ProbabilityStrategy(Z, bcNr);
			Future<ScanResult> dimList = executorService
					.submit(new ScanProbabilityMatrix(sampleThreshold, Z, bcNr,
							strategy));

			bcRecScanFut.put(bcNr, recList);
			bcDimScanFut.put(bcNr, dimList);
		}

		// actually alter the cluster perspectives

		for (Integer i : bcDimScanFut.keySet()) {
			try {
				ScanResult dimResult = bcDimScanFut.get(i).get();
				ScanResult recResult = bcRecScanFut.get(i).get();
				List<Integer> dimIndices = dimResult.getIndices();
				List<Integer> recIndices = recResult.getIndices();
				if (dimResult.getMax() > maxDimThreshold)
					maxDimThreshold = dimResult.getMax();
				if (recResult.getMax() > maxRecThreshold)
					maxRecThreshold = recResult.getMax();
				ClusterElement el = (ClusterElement) rootElement.getClusters()
						.get(i);

				biClusterLabels.add(l.getDataDomain().getDimensionLabel(i) + "L");
				el.setData(dimIndices, recIndices, setXElements, el.getTablePerspective().getLabel(), i, dimResult
						.getMax(), recResult.getMax(), dimResult.getMin(),
						recResult.getMin());
			} catch (InterruptedException | ExecutionException
					| NullPointerException e) {
				e.printStackTrace();
			}
		}
		rootElement.recalculateOverlap(true, true);
	}

	/**
	 * determines which of the given {@link TablePerspective} is L and Z, given the known X table
	 *
	 * @param a
	 * @param b
	 */
	private static Pair<TablePerspective, TablePerspective> findLZ(TablePerspective x,
			TablePerspective a, TablePerspective b) {
		// row: gene, row: gene
		if (a.getDataDomain().getRecordIDCategory()
				.equals(x.getDataDomain().getRecordIDCategory())) {
			return Pair.make(a, b);
		} else {
			return Pair.make(b, a);
		}
	}

	/**
	 * infers the X,L,Z tables from the dimension and record ID categories, as L
	 * and Z has the same Dimension ID Category, the remaining one will be X
	 */
	private static List<TablePerspective> findXLZ(List<TablePerspective> perspectives) {
		if (perspectives.size() != 3)
			return null;
		TablePerspective a = perspectives.get(0);
		TablePerspective b = perspectives.get(1);
		TablePerspective c = perspectives.get(2);
		IDCategory a_d = a.getDataDomain().getDimensionIDCategory();
		IDCategory b_d = b.getDataDomain().getDimensionIDCategory();
		IDCategory c_d = c.getDataDomain().getDimensionIDCategory();
		Pair<TablePerspective, TablePerspective> lz;

		TablePerspective x, l, z;
		if (a_d.equals(b_d)) {
			x = c;
			lz = findLZ(x, a, b);
		} else if (a_d.equals(c_d)) {
			x = b;
			lz = findLZ(x, a, c);
		} else {
			x = a;
			lz = findLZ(x, b, c);
		}
		l = lz.getFirst();
		z = lz.getSecond();
		return Arrays.asList(x, l, z);
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedBiClusterView serializedForm = new SerializedBiClusterView(
				this);
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "BiCluster";
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.tableBased;
	}

	@Override
	protected void applyTablePerspectives(GLElementDecorator root,
			List<TablePerspective> all, List<TablePerspective> added,
			List<TablePerspective> removed) {
		// single time init
		if (root.getContent() == null) {
			rootElement = new GLRootElement();
			root.setContent(rootElement);
		}

		List<TablePerspective> numerical = Lists.newArrayList(Iterables.filter(all,
				DataSupportDefinitions.numericalTables.asTablePerspectivePredicate()));

		if (numerical.size() != 3) { // abort on on data
			rootElement.setData(null, null, null, null, executorService);
			return;
		}

		List<TablePerspective> xlz = findXLZ(numerical);
		TablePerspective x2 = xlz.get(0);
		TablePerspective l2 = xlz.get(1);
		TablePerspective z2 = xlz.get(2);

		if (x2 != x || l2 != l || z2 != z) { // update x l z tables
			this.x = x2;
			this.l = l2;
			this.z = z2;
			rootElement.setData(initTablePerspectives(), x, l, z,
					executorService);
			EventPublisher.trigger(new DataSetSelectedEvent(x.getDataDomain()));
			// signal that we now use that data domain
			createBiClusterPerspectives(x, l, z);
			// createBiClusterPerspectives(x, l, z);
			EventPublisher.trigger(new MaxThresholdChangeEvent(maxDimThreshold,
					maxRecThreshold));
			EventPublisher.trigger(new LZThresholdChangeEvent(geneThreshold,
					sampleThreshold, false, true));
			// rootElement.createBands();
			rootElement.setClusterSizes();
		}

		handleSpecialClusters(added, removed);
	}

	/**
	 * @param added
	 * @param removed
	 */
	private void handleSpecialClusters(List<TablePerspective> added, List<TablePerspective> removed) {
		// search within the categorical table perspectives the chemical clusters
		Predicate<TablePerspective> predicate = Predicates.not(DataSupportDefinitions.numericalTables
				.asTablePerspectivePredicate());
		added = Lists.newArrayList(Iterables.filter(added,predicate));
		removed = Lists.newArrayList(Iterables.filter(removed,predicate));

		assert this.x != null;

		for (TablePerspective t : added) {
			if (findGroupings(t.getRecordPerspective())) {
				for (TablePerspective group : t.getRecordSubTablePerspectives())
					rootElement.addSpecialCluster(t.getRecordPerspective().getIdType(), group);
			}
			if (findGroupings(t.getDimensionPerspective())) {
				for (TablePerspective group : t.getDimensionSubTablePerspectives())
					rootElement.addSpecialCluster(t.getDimensionPerspective().getIdType(), group);
			}
		}
		rootElement.removeSpecialClusters(removed);
	}


	/**
	 * @param p
	 * @return
	 */
	private boolean findGroupings(Perspective p) {
		final IDType record = this.x.getRecordPerspective().getIdType();
		final IDType dimension = this.x.getDimensionPerspective().getIdType();
		final IDType idtype = p.getIdType();
		if ((!idtype.resolvesTo(record) && !idtype.resolvesTo(dimension)))
			return false;
		return true;
	}
}
