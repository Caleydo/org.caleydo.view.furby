/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
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
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;
import org.caleydo.view.bicluster.util.SetUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author user
 *
 */
public class GLRootElement extends GLElementContainer {
	private AllBandsElement bands;
	private final AllClustersElement clusters = new AllClustersElement(this);

	private final ParameterToolBarElement parameterToolBar = new ParameterToolBarElement();
	private final LayoutToolBarElement layoutToolBar = new LayoutToolBarElement();

	private float dimScaleFactor = MyPreferences.getDimScaleFactor();
	private float recScaleFactor = MyPreferences.getRecScaleFactor();
	private double scaleFactor = MyPreferences.getScaleFactor();

	private BiClustering clustering;

	private final GLElement zoomLayer = new GLElement().setVisibility(EVisibility.PICKABLE)
.setPicker(
			GLRenderers.fillRect(Color.CYAN));
	private TablePerspective x;

	public GLRootElement() {
		setLayout(GLLayouts.LAYERS);
		zoomLayer.onPick(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				if (pick.getPickingMode() == PickingMode.MOUSE_WHEEL && ((IMouseEvent) pick).isCtrlDown()) {
					scaleFactor = Math.max(0.2, scaleFactor
							* (((IMouseEvent) pick).getWheelRotation() > 0 ? 1.1 : 1 / 1.1));
					setClusterSizes(null);
				}
			}
		});
		this.add(zoomLayer);

		clusters.setToolBars(parameterToolBar, layoutToolBar);
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


		/**
	 * @param biClustering
	 */
	public void init(BiClustering biClustering, TablePerspective x) {
		this.x = x;
		this.clustering = biClustering;

		parameterToolBar.setXTablePerspective(x);
		layoutToolBar.setXTablePerspective(x);

		bands = new AllBandsElement(x);
		this.add(zoomLayer);
		this.add(bands);
		this.add(clusters);

		System.out.println(biClustering.getBiClusterCount() + " bi clusters loaded.");
		for (int i = 0; i < biClustering.getBiClusterCount(); ++i) {
			final ClusterElement el = new NormalClusterElement(i, clustering.getData(i), clustering);
			clusters.add(el);
		}
	}

	/**
	 *
	 */
	public void reset() {
		this.x = null;
		this.clustering = null;

		this.clear();
		clusters.clear();
		this.bands = null;
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

	public void setClusterSizes(Object causer) {
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
		final double maxSize = maxDimClusterElements < maxRecClusterElements ? maxRecClusterElements
				: maxDimClusterElements;

		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			Vec2f preferredSize = i.getPreferredSize(dimScaleFactor, recScaleFactor);
			double recSize = preferredSize.y() * scaleFactor;
			double dimSize = preferredSize.x() * scaleFactor;
			i.setClusterSize(dimSize, recSize, maxSize, causer);
			i.updateVisibility();
			i.relayout();
		}
	}

	public AllClustersElement getClusters() {
		return clusters;
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
		setClusterSizes(event.getSender());
	}

	int bandCount = 0;
	int count = 0;


	@ListenTo
	private void listenTo(MaxClusterSizeChangeEvent e) {
		dimScaleFactor = (int) e.getMaxDimensionSize() + 1;
		recScaleFactor = (int) e.getMaxRecordSize() + 1;
		setClusterSizes(null);
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
		bands.relayout();
	}

	@ListenTo
	private void listenTo(SpecialClusterAddedEvent event) {
		ClusterElement specialCluster = new SpecialRecordClusterElement(x, clustering, event.getElements());
		specialCluster.setLocation(1000, 1000);
		clusters.add(specialCluster);
		setClusterSizes(specialCluster);
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
	}

	@ListenTo
	private void listenTo(ChemicalClusterAddedEvent e) {
		ClusterElement specialCluster = new ChemicalClusterElement(x, clustering, e.getClusterList(),
				e.getElementToClusterMap());
		specialCluster.setLocation(1000, 1000);
		clusters.add(specialCluster);
		setClusterSizes(specialCluster);
		recalculateOverlap(dimBands, recBands);
		for (GLElement start : clusters) {
			if (start == specialCluster)
				continue;
			bands.add(new DimensionBandElement(start, specialCluster, bands));
		}
		bands.updateSelection();
	}

	/**
	 * @param idType
	 * @param group
	 */
	public void addSpecialCluster(IDType idType, TablePerspective group) {
		SpecialGenericClusterElement specialCluster = new SpecialGenericClusterElement(group, clustering);
		specialCluster.setLocation(1000, 1000);
		clusters.add(specialCluster);
		setClusterSizes(specialCluster);
		recalculateOverlap(dimBands, recBands);
		for (GLElement start : clusters) {
			if (start == specialCluster)
				continue;
			if (clustering.getXDataDomain().getRecordIDType().resolvesTo(idType))
				bands.add(new RecordBandElement(start, specialCluster, bands));
			else
				bands.add(new DimensionBandElement(start, specialCluster, bands));
		}
		bands.updateSelection();
	}

	/**
	 *
	 * @param isDimensionThresholds
	 * @param thresholds
	 *            bicluster id x threshold
	 */
	public void setThresholds(boolean isDimensionThresholds, Map<Integer, Float> thresholds) {
		for (NormalClusterElement elem : Iterables.filter(clusters, NormalClusterElement.class)) {
			int number = elem.getBiClusterNumber();
			if (thresholds.containsKey(number)) {
				float t = thresholds.get(number);
				elem.setThreshold(isDimensionThresholds, t);
			}
		}
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
		setClusterSizes(null);
	}

	@ListenTo
	private void listenTo(UnhidingClustersEvent e) {
		for (ClusterElement elem : Iterables.filter(clusters, ClusterElement.class)) {
			elem.show();
		}
		setClusterSizes(null);
	}

	/**
	 * @param shared
	 * @param selection
	 * @param mouseOver
	 * @return
	 */
	public boolean isAnyRecSelected(Collection<Integer> ids, SelectionType... toCheck) {
		return isAnySelected(ids, bands.getRecordSelectionManager(), toCheck);
	}

	/**
	 * checks whether any of the given ids is an active element within the given SelectionTypes
	 *
	 * @param ids
	 * @param manager
	 * @param toCheck
	 * @return
	 */
	private boolean isAnySelected(Collection<Integer> ids, SelectionManager manager, SelectionType[] toCheck) {
		if (toCheck.length == 0)
			return false;
		for (SelectionType type : toCheck) {
			if (SetUtils.containsAny(manager.getElements(type), ids))
				return true;
		}
		return false;
	}

	public boolean isAnyDimSelected(Collection<Integer> ids, SelectionType... toCheck) {
		return isAnySelected(ids, bands.getRecordSelectionManager(), toCheck);
	}

}
