/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.bicluster.elem.annotation.CategoricalLZHeatmapElement;
import org.caleydo.view.bicluster.elem.band.AllBandsElement;
import org.caleydo.view.bicluster.elem.band.BandElement;
import org.caleydo.view.bicluster.elem.toolbar.AToolBarElement;
import org.caleydo.view.bicluster.elem.toolbar.LayoutToolBarElement;
import org.caleydo.view.bicluster.elem.toolbar.ParameterToolBarElement;
import org.caleydo.view.bicluster.event.AlwaysShowToolBarEvent;
import org.caleydo.view.bicluster.event.ChangeMaxDistanceEvent;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxClusterSizeChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.ShowHideBandsEvent;
import org.caleydo.view.bicluster.event.ShowToolBarEvent;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;
import org.caleydo.view.bicluster.util.SetUtils;

import com.google.common.collect.Iterables;

/**
 * @author user
 *
 */
public class GLRootElement extends GLElementContainer {
	private static final Logger log = Logger.create(GLRootElement.class);

	private final List<AToolBarElement> toolbars = Arrays.asList(new ParameterToolBarElement(),
			new LayoutToolBarElement());

	private boolean dimBands = MyPreferences.isShowDimBands();
	private boolean recBands = MyPreferences.isShowRecBands();

	private float dimScaleFactor = MyPreferences.getDimScaleFactor();
	private float recScaleFactor = MyPreferences.getRecScaleFactor();
	private double scaleFactor = MyPreferences.getScaleFactor();
	private int maxDistance = MyPreferences.getMaxDistance();
	private float clusterSizeThreshold = 0;

	private boolean isShowAlwaysToolBar = false;

	private BiClustering clustering;
	private final AllClustersElement clusters = new AllClustersElement(this);
	private AllBandsElement bands;
	private TablePerspective x;

	/**
	 * a special layer just for watching for zooming events
	 */
	private final GLElement zoomLayer = new GLElement().setVisibility(EVisibility.PICKABLE)
.setPicker(
			GLRenderers.fillRect(Color.CYAN));

	private int maxSize;

	private IIDTypeMapper<Integer, String> dim2label;

	private IIDTypeMapper<Integer, String> rec2label;


	public GLRootElement() {
		setLayout(GLLayouts.LAYERS);
		zoomLayer.onPick(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				// mouse wheel zoom
				if (pick.getPickingMode() == PickingMode.MOUSE_WHEEL && ((IMouseEvent) pick).isCtrlDown()) {
					setScaleFactor(Math.max(0.2, scaleFactor
							* (((IMouseEvent) pick).getWheelRotation() > 0 ? 1.1 : 1 / 1.1)));
				}
			}
		});
		this.add(zoomLayer);
	}


	@Override
	public void layout(int deltaTimeMs) {
		for (AToolBarElement toolbar : toolbars)
			if (toolbar.hasMoved()) {
				clusters.relayout();
				bands.relayout();
				break;
			}
		super.layout(deltaTimeMs);
	}

	/**
	 * @return the isShowAlwaysToolBar, see {@link #isShowAlwaysToolBar}
	 */
	public boolean isShowAlwaysToolBar() {
		return isShowAlwaysToolBar;
	}

	@ListenTo
	private void onAlwaysShowToolBarEvent(AlwaysShowToolBarEvent event) {
		this.isShowAlwaysToolBar = !isShowAlwaysToolBar;
		for (GLElement elem : clusters)
			GLElementAccessor.relayoutDown(elem);
	}

	/**
	 * @return the toolbars, see {@link #toolbars}
	 */
	public List<AToolBarElement> getToolbars() {
		return toolbars;
	}

	public boolean isRecBandsEnabled() {
		return recBands;
	}

	public boolean isDimBandsEnabled() {
		return dimBands;
	}

	public boolean isBandsEnabled(EDimension dim) {
		return dim.select(dimBands, recBands);
	}

	/**
	 * @return the maxDistance, see {@link #maxDistance}
	 */
	public int getMaxDistance() {
		return maxDistance;
	}

	@ListenTo
	private void onChangeMaxDistanceEvent(ChangeMaxDistanceEvent event) {
		this.maxDistance = event.getMaxDistance();
		this.clusters.onChangeMaxDistance();
	}

	/**
	 * @param max
	 */
	protected void setScaleFactor(double factor) {
		scaleFactor = factor;
		setClusterSizes(null);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		// show the global toolbar as a popup
		toolbars.get(0).toggle(context);
	}

	@ListenTo
	private void onShowToolBar(ShowToolBarEvent event) {
		if (context == null)
			return;
		boolean parameters = event.isShowParameter();
		if (parameters)
			toolbars.get(0).toggle(context);
		else
			toolbars.get(1).toggle(context);
		clusters.relayout();
	}

	/**
	 * @param biClustering
	 */
	public void init(BiClustering biClustering, TablePerspective x) {
		this.x = x;
		this.clustering = biClustering;

		this.dim2label = x2Label(this.x.getDimensionPerspective().getIdType());
		this.rec2label = x2Label(this.x.getRecordPerspective().getIdType());

		for (AToolBarElement toolbar : getToolbars())
			toolbar.init(x);

		bands = new AllBandsElement(x);
		this.clear();
		this.add(zoomLayer);
		this.add(bands);
		this.add(clusters);

		final int count = biClustering.getBiClusterCount();
		log.info(count + " bi clusters loaded.");
		for (int i = 0; i < count; ++i) {
			final ClusterElement el = new NormalClusterElement(i, clustering.getData(i), clustering);
			clusters.add(el);
		}
		log.info("creating bands");
		final List<GLElement> l = clusters.asList();
		for (int i = 0; i < l.size(); ++i) {
			ClusterElement start = (ClusterElement) l.get(i);
			for (int j = i + 1; j < l.size(); ++j) {
				ClusterElement end = (ClusterElement) l.get(j);
				Edge edge = new Edge(start, end);
				start.addEdge(end, edge);
				end.addEdge(start, edge);
				bands.add(new BandElement(edge, EDimension.RECORD, bands.getRecordSelectionManager(), rec2label));
				bands.add(new BandElement(edge, EDimension.DIMENSION, bands.getDimensionSelectionManager(), dim2label));
			}
		}
		bands.updateSelection();
		bands.updateStructure();
	}

	/**
	 * @param idType
	 * @return
	 */
	private static IIDTypeMapper<Integer, String> x2Label(IDType idType) {
		return IDMappingManagerRegistry.get().getIDMappingManager(idType)
				.getIDTypeMapper(idType, idType.getIDCategory().getHumanReadableIDType());
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

	public void setClusterSizes(Object causer) {
		int maxDimClusterElements = 1;
		int maxRecClusterElements = 1;

		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			if (!i.isVisible())
				continue;
			maxDimClusterElements = Math.max(maxDimClusterElements, i.getDimSize());
			maxRecClusterElements = Math.max(maxRecClusterElements, i.getRecSize());
		}
		this.maxSize = Math.max(maxDimClusterElements, maxRecClusterElements);

		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			Vec2f preferredSize = i.getPreferredSize(dimScaleFactor, recScaleFactor);
			double recSize = preferredSize.y() * scaleFactor;
			double dimSize = preferredSize.x() * scaleFactor;
			i.setClusterSize(dimSize, recSize, causer);
			i.updateVisibility();
			i.relayout();
		}
	}

	/**
	 * @return
	 */
	public int getBiggestDimSize() {
		return maxSize;
	}

	@ListenTo
	public void listenTo(ClusterScaleEvent event) {
		setClusterSizes(event.getSender());
	}

	@ListenTo
	private void listenTo(MinClusterSizeThresholdChangeEvent event) {
		this.clusterSizeThreshold = event.getMinClusterSize();
		updateClusterVisibilities();
	}

	/**
	 * @return the clusterSizeThreshold, see {@link #clusterSizeThreshold}
	 */
	public float getClusterSizeThreshold() {
		return clusterSizeThreshold;
	}

	private void updateClusterVisibilities() {
		for (ClusterElement elem : clusters.allClusters())
			elem.updateVisibility();
	}

	@ListenTo
	private void listenTo(MaxClusterSizeChangeEvent e) {
		dimScaleFactor = (int) e.getMaxDimensionSize() + 1;
		recScaleFactor = (int) e.getMaxRecordSize() + 1;
		setClusterSizes(null);
	}

	@ListenTo
	private void listenTo(LZThresholdChangeEvent event) {
		final int dimNumberThreshold = event.getDimensionNumberThreshold();
		final float dimThreshold = event.getDimensionThreshold();
		final int recNumberThreshold = event.getRecordNumberThreshold();
		final float recThreshold = event.getRecordThreshold();

		// 1. update thresholds
		Iterable<NormalClusterElement> allNormalClusters = allNormalClusters();
		for (NormalClusterElement cluster : allNormalClusters) {
			cluster.setThresholds(dimThreshold, dimNumberThreshold, recThreshold, recNumberThreshold);
		}
		// 2. update overlaps
		updateAllEdges();
	}

	private Iterable<NormalClusterElement> allNormalClusters() {
		return Iterables.filter(clusters, NormalClusterElement.class);
	}

	@ListenTo
	private void onShowHideBandsEvent(ShowHideBandsEvent event) {
		this.dimBands = event.isShowDimBand();
		this.recBands = event.isShowRecBand();
		bands.relayout();
	}

	/**
	 *
	 * @param isDimensionThresholds
	 * @param thresholds
	 *            bicluster id x threshold
	 */
	public void setThresholds(EDimension dimension, Map<Integer, Float> thresholds) {
		for (NormalClusterElement elem : allNormalClusters()) {
			int number = elem.getBiClusterNumber();
			if (thresholds.containsKey(number)) {
				float t = thresholds.get(number);
				elem.setThreshold(dimension, t);
			}
		}
		updateAllEdges();
	}

	private void updateAllEdges() {
		// update all edges
		for (ClusterElement elem : clusters.allClusters()) {
			elem.updateOutgoingEdges(true, true);
		}
		for (ClusterElement elem : clusters.allClusters()) {
			elem.onEdgeUpdateDone();
		}
		bands.relayout();
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

	/**
	 *
	 */
	public void focusPrevious() {
		if (clusters != null)
			clusters.focusPrevious();
	}

	public void focusNext() {
		if (clusters != null)
			clusters.focusNext();
	}

	/**
	 * @param record
	 * @param t
	 */
	public void addAnnotation(EDimension dimension, TablePerspective t) {
		final Integer oppositeID = dimension.select(t.getRecordPerspective(), t.getDimensionPerspective())
				.getVirtualArray()
				.get(0);
		final Table table = t.getDataDomain().getTable();
		final IDType target = dimension.select(t.getDataDomain().getDimensionIDType(), t.getDataDomain()
				.getRecordIDType());
		ATableBasedDataDomain source = clustering.getXDataDomain();
		if (source.getDimensionIDCategory().isOfCategory(target)) {
			final IIDTypeMapper<Integer, Integer> mapper = source.getDimensionIDMappingManager().getIDTypeMapper(
					source.getDimensionIDType(), target);
			for (NormalClusterElement cluster : allNormalClusters())
				cluster.addAnnotation(new CategoricalLZHeatmapElement(EDimension.DIMENSION, oppositeID, table, mapper));
		} else if (source.getRecordIDCategory().isOfCategory(target)) {
			final IIDTypeMapper<Integer, Integer> mapper = source.getRecordIDMappingManager().getIDTypeMapper(
					source.getRecordIDType(), target);
			for (NormalClusterElement cluster : allNormalClusters())
				cluster.addAnnotation(new CategoricalLZHeatmapElement(EDimension.RECORD, oppositeID, table, mapper));
		}
	}

	/**
	 * @param record
	 * @param t
	 */
	public void removeAnnotation(EDimension dimension, TablePerspective t) {
		final Integer oppositeID = dimension.select(t.getRecordPerspective(), t.getDimensionPerspective())
				.getVirtualArray().get(0);
		final IDType target = dimension.select(t.getDataDomain().getDimensionIDType(), t.getDataDomain()
				.getRecordIDType());
		ATableBasedDataDomain source = clustering.getXDataDomain();
		if (source.getDimensionIDCategory().isOfCategory(target)) {
			for (NormalClusterElement cluster : allNormalClusters()) {
				for (CategoricalLZHeatmapElement h : Iterables.filter(cluster.getAnnotations(),
						CategoricalLZHeatmapElement.class)) {
					if (h.is(oppositeID, target)) {
						cluster.removeAnnotation(h);
						break;
					}
				}
			}
		} else if (source.getRecordIDCategory().isOfCategory(target)) {
			for (NormalClusterElement cluster : allNormalClusters()) {
				for (CategoricalLZHeatmapElement h : Iterables.filter(cluster.getAnnotations(),
						CategoricalLZHeatmapElement.class)) {
					if (h.is(oppositeID, target)) {
						cluster.removeAnnotation(h);
						break;
					}
				}
			}
		}
	}

}
