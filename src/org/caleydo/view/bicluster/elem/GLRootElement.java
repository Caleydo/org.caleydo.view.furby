/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.band.AllBandsElement;
import org.caleydo.view.bicluster.elem.band.DimensionBandElement;
import org.caleydo.view.bicluster.elem.band.RecordBandElement;
import org.caleydo.view.bicluster.event.ChemicalClusterAddedEvent;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxClusterSizeChangeEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.ShowToolBarEvent;
import org.caleydo.view.bicluster.event.SpecialClusterAddedEvent;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author user
 *
 */
public class GLRootElement extends GLElementContainer implements IGLLayout {
	private AllBandsElement bands;
	private final AllClustersElement clusters = new AllClustersElement(this);

	private final ParameterToolBarElement parameterToolBar = new ParameterToolBarElement();
	private final LayoutToolBarElement layoutToolBar = new LayoutToolBarElement();

	private TablePerspective x, l, z;
	private ExecutorService executor;

	public GLRootElement() {
		setLayout(this);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		// show the global toolbar as a popup
		parameterToolBar.toggle(context);
	}

	@ListenTo
	private void onShowToolBar(ShowToolBarEvent event) {
		if (context == null)
			return;
		boolean parameters = event.isShowParameter();
		if (parameters)
			parameterToolBar.toggle(context);
		else
			layoutToolBar.toggle(context);
		clusters.relayout();
	}

	public void setData(List<TablePerspective> list, TablePerspective x, TablePerspective l, TablePerspective z,
			ExecutorService executor) {
		this.x = x;
		this.l = l;
		this.z = z;
		this.executor = executor;

		parameterToolBar.setXTablePerspective(x);
		layoutToolBar.setXTablePerspective(x);

		if (clusters.size() > 0)
			clusters.clear();
		this.clear();
		bands = new AllBandsElement(x);
		this.add(clusters);
		this.add(bands);

		if (list != null) {
			System.out.println(list.size() + " Cluster geladen.");
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(p, clusters, x, l, z, executor, this);
				clusters.add(el);
			}
		}
		clusters.setToolBars(parameterToolBar, layoutToolBar);
	}

	public void createBands() {
		if (bands == null)
			return;
		if (bands.size() == 0) {
			int i = 1;
			for (GLElement start : clusters) {
				for (GLElement end : clusters.asList().subList(i, clusters.asList().size())) {
					if (start == end)
						continue;
					bands.add(new RecordBandElement(start, end, bands));
					bands.add(new DimensionBandElement(start, end, bands));
				}
				i++;
			}
		}
		bands.updateSelection();
		bands.updateStructure();
	}




	public void setClusterSizes() {
		double maxDimClusterElements = 1;
		double maxRecClusterElements = 1;

		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			if (!i.isVisible())
				continue;
			if (maxDimClusterElements < i.getNumberOfDimElements()) {
				maxDimClusterElements = i.getNumberOfDimElements();
			}
			if (maxRecClusterElements < i.getNumberOfRecElements()) {
				maxRecClusterElements = i.getNumberOfRecElements();
			}
		}
		double maxSize = maxDimClusterElements < maxRecClusterElements ? maxRecClusterElements : maxDimClusterElements;
		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			double recSize = i.getNumberOfRecElements() * maxRecClusterSize / maxRecClusterElements;
			double dimSize = i.getNumberOfDimElements() * maxDimClusterSize / maxDimClusterElements;
			i.setClusterSize(dimSize, recSize, maxSize);
			i.setVisibility();
			i.relayout();
		}
	}

	public AllClustersElement getClusters() {
		return clusters;
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement child : children) {
			child.setBounds(0, 0, w, h);
			child.asElement().relayout();
		}
	}

	public void recalculateOverlap(boolean dimBands, boolean recBands) {
		this.dimBands = dimBands;
		this.recBands = recBands;
		for (GLElement iGL : clusters) {
			((ClusterElement) iGL).calculateOverlap(dimBands, recBands);
		}

	}

	@ListenTo
	public void listenTo(ClusterScaleEvent event) {
		setClusterSizes();
	}

	int bandCount = 0;
	int count = 0;

	private int maxDimClusterSize = 150;
	private int maxRecClusterSize = 150;

	@ListenTo
	private void listenTo(MaxClusterSizeChangeEvent e) {
		maxDimClusterSize = (int) e.getMaxDimensionSize() + 1;
		maxRecClusterSize = (int) e.getMaxRecordSize() + 1;
		setClusterSizes();
	}

	boolean dimBands, recBands;

	@ListenTo
	private void listenTo(CreateBandsEvent event) {
		if (!(event.getSender() instanceof ClusterElement)) {
			createBands();
			return;
		}
		bandCount++;
		if (bandCount == clusters.size()) {
			createBands();
			bandCount = 0;
		}
	}

	@ListenTo
	private void listenTo(RecalculateOverlapEvent event) {
		if (event.isGlobal())
			count++;
		else {
			recalculateOverlap(event.isDimBandEnabled(), event.isRecBandEnabled());
			count = 0;
			return;
		}
		if (count == clusters.size()) {
			recalculateOverlap(event.isDimBandEnabled(), event.isRecBandEnabled());
			count = 0;
		}
	}

	@ListenTo
	private void listenTo(LZThresholdChangeEvent event) {
		bands.updateStructure();
	}

	@ListenTo
	private void listenTo(SpecialClusterAddedEvent event) {
		ClusterElement specialCluster = new SpecialRecordClusterElement(x, clusters, x, l, z, executor,
				event.getElements(), this);
		specialCluster.setLocation(1000, 1000);
		clusters.add(specialCluster);
		setClusterSizes();
		recalculateOverlap(dimBands, recBands);
		for (GLElement start : clusters) {
			if (start == specialCluster)
				continue;
			if (!event.isDimensionCluster())
				bands.add(new RecordBandElement(start, specialCluster, bands));
			else
				bands.add(new DimensionBandElement(start, specialCluster, bands));
		}
		bands.updateSelection();
		relayout();
	}

	@ListenTo
	private void listenTo(ChemicalClusterAddedEvent e) {
		ClusterElement specialCluster = new ChemicalClusterElement(x, clusters, x, l, z, executor, e.getClusterList(),
				e.getElementToClusterMap(), this);
		specialCluster.setLocation(1000, 1000);
		clusters.add(specialCluster);
		setClusterSizes();
		recalculateOverlap(dimBands, recBands);
		for (GLElement start : clusters) {
			if (start == specialCluster)
				continue;
			bands.add(new DimensionBandElement(start, specialCluster, bands));
		}
		bands.updateSelection();
		relayout();
	}

	/**
	 * @param idType
	 * @param group
	 */
	public void addSpecialCluster(IDType idType, TablePerspective group) {
		SpecialGenericClusterElement specialCluster = new SpecialGenericClusterElement(group, clusters, x, l,
				z,
				executor, this);
		specialCluster.setLocation(1000, 1000);
		clusters.add(specialCluster);
		setClusterSizes();
		recalculateOverlap(dimBands, recBands);
		for (GLElement start : clusters) {
			if (start == specialCluster)
				continue;
			if (this.x.getRecordPerspective().getIdType().resolvesTo(idType))
				bands.add(new RecordBandElement(start, specialCluster, bands));
			else
				bands.add(new DimensionBandElement(start, specialCluster, bands));
		}
		bands.updateSelection();
		relayout();
	}

	/**
	 * @param removed
	 */
	public void removeSpecialClusters(List<TablePerspective> parents) {
		if (parents.isEmpty())
			return;
		for (SpecialGenericClusterElement cluster : Lists.newArrayList(Iterables.filter(clusters,
				SpecialGenericClusterElement.class))) {
			if (parents.contains(cluster.getTablePerspective().getParentTablePerspective()))
				cluster.remove();
		}
	}

	@ListenTo
	private void listenTo(ClusterGetsHiddenEvent e) {
		setClusterSizes();
	}

	@ListenTo
	private void listenTo(UnhidingClustersEvent e) {
		setClusterSizes();
	}


}
