/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.caleydo.core.data.collection.table.CategoricalTable;
import org.caleydo.core.data.collection.table.NumericalTable;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.GLThreadListenerWrapper;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.view.bicluster.elem.BiClustering;
import org.caleydo.view.bicluster.elem.EDimension;
import org.caleydo.view.bicluster.elem.GLRootElement;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;
import org.caleydo.view.bicluster.sorting.FuzzyClustering;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
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

public class GLBiCluster extends AMultiTablePerspectiveElementView implements IGLKeyListener {
	public static final String VIEW_TYPE = "org.caleydo.view.bicluster";
	public static final String VIEW_NAME = "Furby";

	private TablePerspective x, l, z;

	private float dimThreshold = MyPreferences.getDimThreshold(); // 4.5f;
	private float recThreshold = MyPreferences.getRecThreshold(); // 0.08f;
	double maxDimThreshold = 0, maxRecThreshold = 0;
	ASerializedView view;

	GLRootElement rootElement;

	@DeepScan
	private final IGLKeyListener focusSwitcher = GLThreadListenerWrapper.wrap(this);

	public GLBiCluster(IGLCanvas glCanvas, ASerializedView serializedView) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		this.view = serializedView;
		glCanvas.addKeyListener(focusSwitcher);
	}

	@Override
	public void keyPressed(IKeyEvent e) {
		// dummy
	}

	@Override
	public void keyReleased(IKeyEvent e) {
		if (rootElement == null)
			return;
		if (e.isKey(ESpecialKey.LEFT))
			rootElement.focusPrevious();
		else if (e.isKey(ESpecialKey.RIGHT))
			rootElement.focusNext();
	}

	private List<TablePerspective> initTablePerspectives() {
		List<TablePerspective> persp = new ArrayList<>();
		ATableBasedDataDomain xDataDomain = x.getDataDomain();
		Table xtable = xDataDomain.getTable();
		IDType xdimtype = xDataDomain.getDimensionIDType();
		IDType xrectype = xDataDomain.getRecordIDType();

		final Collection<TablePerspective> existing = ImmutableList.copyOf(x.getDataDomain().getAllTablePerspectives());
		final int bcCount = l.getDataDomain().getTable().getColumnIDList().size(); // Nr of BCs in L & Z

		for (int i = 0; i < bcCount; i++) {
			final String act = l.getDataDomain().getDimensionLabel(i) + " L";

			// slower but preserves the correct order
			boolean found = false;
			for (TablePerspective p : existing) {
				String name = p.getDimensionPerspective().getLabel();
				// System.out.println(name);
				if (act.equals(name)) {
					persp.add(p);
					found = true;
					break;
				}
			}
			if (!found) {// create new one
				persp.add(createTablePerspective(xDataDomain, xtable, xdimtype, xrectype, i));
			}
		}
		return persp;
	}

	private TablePerspective createTablePerspective(ATableBasedDataDomain xDataDomain, Table xtable, IDType xdimtype,
			IDType xrectype, int i) {
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
		return custom;
	}


	private Pair<List<FuzzyClustering>, List<FuzzyClustering>> createFuzzyClustering(TablePerspective x,
			TablePerspective l, TablePerspective z) {

		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		int bcCountData = L.size(); // Nr of BCs in L & Z

		ExecutorService service = Executors.newFixedThreadPool(2);
		Future<List<FuzzyClustering>> lClusterings = service.submit(new ScanLZTable(L));
		Future<List<FuzzyClustering>> zClusterings = service.submit(new ScanLZTable(Z));

		try {
			List<FuzzyClustering> lClustering = lClusterings.get();
			List<FuzzyClustering> zClustering = zClusterings.get();
			assert lClustering.size() == zClustering.size() && lClustering.size() == bcCountData;
			float maxL = Float.NEGATIVE_INFINITY;
			float maxZ = Float.NEGATIVE_INFINITY;
			for(int i = 0; i < bcCountData; ++i) {
				FuzzyClustering l_i = lClustering.get(i);
				FuzzyClustering z_i = zClustering.get(i);
				maxL = Math.max(maxL, l_i.getAbsMaxValue());
				maxZ = Math.max(maxZ, z_i.getAbsMaxValue());
			}
			maxRecThreshold = maxL;
			maxDimThreshold = maxZ;
			return Pair.make(lClustering, zClustering);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		} catch (ExecutionException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		} finally {
			service.shutdown();
		}
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
		return serializedForm;
	}

	@Override
	public String toString() {
		return "Furbi";
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
			rootElement.reset();
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
			EventPublisher.trigger(new DataSetSelectedEvent(x.getDataDomain()));
			List<TablePerspective> clusterTablePerspectives = initTablePerspectives();
			Pair<List<FuzzyClustering>, List<FuzzyClustering>> clusterings = createFuzzyClustering(x, l, z);

			BiClustering biClustering = new BiClustering(asTable(x), asTable(l), asTable(z), clusterings.getFirst(),
					clusterings.getSecond(), clusterTablePerspectives);
			// actually alter the cluster perspectives
			// createBiClusterPerspectives(x, l, z);
			EventPublisher.trigger(new MaxThresholdChangeEvent(maxDimThreshold, maxRecThreshold));
			EventPublisher.trigger(new LZThresholdChangeEvent(recThreshold, dimThreshold, MyPreferences.getRecTopNElements(), MyPreferences.getDimTopNElements(), true));
			// rootElement.createBands();

			// signal that we now use that data domain
			rootElement.init(biClustering, x);
			rootElement.setClusterSizes(null);
		}

		handleSpecialClusters(added, removed);
		handleThresholds(added, removed);
	}

	/**
	 * @param x2
	 * @return
	 */
	private static NumericalTable asTable(TablePerspective x2) {
		return (NumericalTable) x2.getDataDomain().getTable();
	}

	private void handleThresholds(List<TablePerspective> added, List<TablePerspective> removed) {
		if (added.isEmpty())
			return;
		// search within the categorical table perspectives the chemical clusters
		Predicate<TablePerspective> predicate = new Predicate<TablePerspective>() {
			@Override
			public boolean apply(TablePerspective arg0) {
				ATableBasedDataDomain datadomain = arg0.getDataDomain();
				if (datadomain.getTable() instanceof CategoricalTable<?>
						|| datadomain.getTable() instanceof NumericalTable)
					return false;
				// inhomogenous
				if (l != null
						&& !arg0.getRecordPerspective().getIdType().resolvesTo(l.getDimensionPerspective().getIdType()))
					return false;
				// single column
				return arg0.getDimensionPerspective().getVirtualArray().size() == 1;
			}
		};
		added = Lists.newArrayList(Iterables.filter(added, predicate));

		assert this.x != null;

		for (TablePerspective add : added) {
			// use the label as indicator whether for L or Z thresholds
			String label = add.getLabel();
			boolean isZ = label.toLowerCase().contains("z");
			Map<Integer, Float> thresholds = new TreeMap<>();
			Integer col = add.getDimensionPerspective().getVirtualArray().get(0);
			Table table = add.getDataDomain().getTable();
			for (Integer row : add.getRecordPerspective().getVirtualArray()) {
				Float t = table.getRaw(col, row);
				if (t == null || t.isNaN())
					continue;
				thresholds.put(row, t);
			}
			rootElement.setThresholds(EDimension.get(isZ), thresholds);
		}
	}
	/**
	 * @param added
	 * @param removed
	 */
	private void handleSpecialClusters(List<TablePerspective> added, List<TablePerspective> removed) {
		// search within the categorical table perspectives the chemical clusters
		Predicate<TablePerspective> predicate = new Predicate<TablePerspective>() {
			@Override
			public boolean apply(TablePerspective arg0) {
				ATableBasedDataDomain datadomain = arg0.getDataDomain();
				if (datadomain.getTable() instanceof CategoricalTable<?>)
					return true;
				if (datadomain.getTable() instanceof NumericalTable)
					return false;
				return hasGroups(arg0.getRecordPerspective()) || hasGroups(arg0.getDimensionPerspective());
			}

			private boolean hasGroups(Perspective p) {
				return p.getVirtualArray().getGroupList().size() > 1;
			}
		};
		added = Lists.newArrayList(Iterables.filter(added,predicate));
		removed = Lists.newArrayList(Iterables.filter(removed,predicate));

		assert this.x != null;

		for (TablePerspective t : added) {
			if (findGroupings(t.getRecordPerspective())) {
				rootElement.addAnnotation(EDimension.RECORD, t);
			}
			if (findGroupings(t.getDimensionPerspective())) {
				rootElement.addAnnotation(EDimension.DIMENSION, t);
			}
		}
		for (TablePerspective t : removed) {
			if (findGroupings(t.getRecordPerspective())) {
				rootElement.removeAnnotation(EDimension.RECORD, t);
			}
			if (findGroupings(t.getDimensionPerspective())) {
				rootElement.removeAnnotation(EDimension.DIMENSION, t);
			}
		}
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
		return p.getVirtualArray().getGroupList().size() > 1;
	}
}
