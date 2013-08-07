/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.events.DimensionVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.RecordVAUpdateEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.gui.util.RenameNameDialog;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.Transitions;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider.EValueVisibility;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.BiClusterRenderStyle;
import org.caleydo.view.bicluster.concurrent.ScanProbabilityMatrix;
import org.caleydo.view.bicluster.concurrent.ScanResult;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MouseOverBandEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SearchClusterEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;
import org.caleydo.view.bicluster.sorting.BandSorting;
import org.caleydo.view.bicluster.sorting.ProbabilityStrategy;
import org.caleydo.view.bicluster.util.ClusterRenameEvent;
import org.caleydo.view.bicluster.util.Vec2d;
import org.caleydo.view.heatmap.v2.BasicBlockColorer;
import org.caleydo.view.heatmap.v2.HeatMapElement.EShowLabels;
import org.caleydo.view.heatmap.v2.IBlockColorer;
import org.eclipse.swt.widgets.Display;


/**
 * e.g. a class for representing a cluster
 *
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public class ClusterElement extends AnimatedGLElementContainer implements IBlockColorer, IGLLayout, ILabeled {
	protected static final float highOpacityFactor = 1;
	protected static final float lowOpacityFactor = 0.2f;
	protected static final float opacityChangeInterval = 10f;
	protected static final float DEFAULT_Z_DELTA = 0;
	protected static final float FOCUSED_Z_DELTA = 1;
	protected static final float HOVERED_NOT_FOCUSED_Z_DELTA = 2;
	protected static final float DRAGGING_Z_DELTA = 4;

	protected final TablePerspective data;
	protected final TablePerspective x;
	protected final TablePerspective l;
	protected final TablePerspective z;
	protected final AllClustersElement allClusters;
	protected final GLRootElement biclusterRoot;
	protected final ExecutorService executor;
	protected Vec2d attForce = new Vec2d(0, 0);
	protected Vec2d repForce = new Vec2d(0, 0);
	protected Vec2d frameForce = new Vec2d(0, 0);
	protected boolean isDragged = false;
	protected boolean isHovered = false;
	protected boolean isHidden = false;
	protected boolean isLocked = false;
	protected boolean hasContent = false;
	protected boolean dimBandsEnabled, recBandsEnabled;
	protected ClusterElement cluster;
	protected float curOpacityFactor = 1f;
	protected float opacityfactor = 1;

	protected Map<GLElement, List<Integer>> dimOverlap, recOverlap;

	protected SortingType sortingType = SortingType.probabilitySorting;
	protected List<Integer> dimProbabilitySorting;
	protected List<Integer> recProbabilitySorting;

	protected int bcNr = -1;
	protected ToolBar toolBar;
	protected HeaderBar headerBar;
	protected ThresholdBar dimThreshBar;
	protected ThresholdBar recThreshBar;
	protected GLElement content;

	protected float recThreshold = ParameterToolBarElement.DEFAULT_REC_THRESHOLD;
	protected int recNumberThreshold = ParameterToolBarElement.UNBOUND_NUMBER;
	protected float dimThreshold = ParameterToolBarElement.DEFAULT_DIM_THRESHOLD;
	protected int dimNumberThreshold = ParameterToolBarElement.UNBOUND_NUMBER;
	protected double clusterSizeThreshold;
	protected double elementCountBiggestCluster;

	int dimensionOverlapSize;
	int recordOverlapSize;
	private double dimSize;
	private double recSize;
	protected double scaleFactor;
	protected double standardScaleFactor;
	protected boolean isFocused = false;

	/**
	 * elements for showing the probability heatmaps
	 */
	private LZHeatmapElement propabilityHeatMapHor;
	private LZHeatmapElement propabilityHeatMapVer;

	public ClusterElement(TablePerspective data, AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor, GLRootElement biclusterRoot) {
		setLayout(this);
		this.data = data;
		this.allClusters = root;
		this.x = x;
		this.l = l;
		this.z = z;
		this.executor = executor;
		this.biclusterRoot = biclusterRoot;
		standardScaleFactor = 0.25;
		initContent();
		setVisibility();
		scaleFactor = 1.0f;
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
		this.setLayoutData(MoveTransitions.MOVE_AND_GROW_LINEAR);
		this.cluster = this;
	}

	/**
	 * @return the bcNr, see {@link #bcNr}
	 */
	public int getBiClusterNumber() {
		return bcNr;
	}

	protected void initContent() {
		toolBar = new ToolBar();
		headerBar = new HeaderBar();
		dimThreshBar = new ThresholdBar(true);
		recThreshBar = new ThresholdBar(false);
		this.add(toolBar); // add a element toolbar
		this.add(headerBar);
		this.add(dimThreshBar);
		this.add(recThreshBar);

		content = createContent();
		setZValuesAccordingToState();
		this.add(content);

		this.propabilityHeatMapHor = new LZHeatmapElement(z.getDataDomain().getTable(), true);
		this.add(this.propabilityHeatMapHor);
		this.propabilityHeatMapVer = new LZHeatmapElement(l.getDataDomain().getTable(), false);
		this.add(this.propabilityHeatMapVer);
	}

	/**
	 * @return
	 */
	protected final ClusterContentElement createContent() {
		Builder builder = GLElementFactoryContext.builder();
		builder.withData(data);
		builder.put(EDetailLevel.class, EDetailLevel.MEDIUM);
		builder.put(IBlockColorer.class, this);
		ClusterContentElement c = new ClusterContentElement(builder);

		if (toolBar != null) {
			toolBar.add(c.createVerticalButtonBar());
		}
		// trigger a scale event on vis change
		c.onActiveChanged(new GLElementFactorySwitcher.IActiveChangedCallback() {
			@Override
			public void onActiveChanged(int active) {
				EventPublisher.trigger(new ClusterScaleEvent(ClusterElement.this));
			}
		});


		return c;
	}

	@Override
	public Color apply(int recordID, int dimensionID, ATableBasedDataDomain dataDomain, boolean deSelected) {
		Color color = BasicBlockColorer.INSTANCE.apply(recordID, dimensionID, dataDomain, deSelected);

		if (!isFocused)
			color.a = color.a * curOpacityFactor;
		return color;
	}

	public IDCategory getRecordIDCategory() {
		return data.getDataDomain().getRecordIDCategory();
	}

	public IDCategory getDimensionIDCategory() {
		return data.getDataDomain().getDimensionIDCategory();
	}

	public IDType getDimensionIDType() {
		return getDimensionVirtualArray().getIdType();
	}

	public IDType getRecordIDType() {
		return getRecordVirtualArray().getIdType();
	}

	public String getDataDomainID() {
		return data.getDataDomain().getDataDomainID();
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public String getID() {
		return data.getLabel();
	}

	protected void setZValuesAccordingToState() {
		if (isDragged) {
			setzDelta(DRAGGING_Z_DELTA);
		} else if (isFocused) {
			setzDelta(FOCUSED_Z_DELTA);
		} else if (isHovered) {
			setzDelta(HOVERED_NOT_FOCUSED_Z_DELTA);
		} else {
			setzDelta(DEFAULT_Z_DELTA);
		}
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.BLACK);
		if (isHovered) {
			g.fillRect(-20, -20, w < 55 ? 120 : w + 65, h < 80 ? 150 : h + 70);
		}
		super.renderPickImpl(g, w, h);
	}

	private int accu; // for animating the opacity fading

	@Override
	public void layout(int deltaTimeMs) {
		// duration -= delta
		if (deltaTimeMs + accu > opacityChangeInterval) {

			if (opacityfactor < curOpacityFactor)
				curOpacityFactor -= 0.02;
			else if (opacityfactor > curOpacityFactor)
				curOpacityFactor += 0.02;

			repaint();
			for (GLElement child : this)
				child.repaint();
			accu = 0;
		} else
			accu += deltaTimeMs;
		super.layout(deltaTimeMs);

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Color color = null;
		if (isFocused)
			color = new Color(0, 0, 0, 1.0f);
		else if (isHovered) {
			color = SelectionType.MOUSE_OVER.getColor();
		} else
			color = new Color(0, 0, 0, curOpacityFactor);
		g.color(color);
		g.drawRect(-1, -1, w + 2, h + 2);
	}

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!pick.isAnyDragging()) {
				isHovered = true;
				EventPublisher.trigger(new MouseOverClusterEvent(this, true));
				EventPublisher.trigger(new DataSetSelectedEvent(data));
				relayout(); // for showing the bars
			}
			break;
		case MOUSE_OUT:
			if (isHovered)
				EventPublisher.trigger(new DataSetSelectedEvent(data.getDataDomain()));
			mouseOut();
			break;
		case MOUSE_WHEEL:
			// zoom on CTRL+mouse wheel
			IMouseEvent event = ((IMouseEvent) pick);
			int r = (event).getWheelRotation();
			if (r != 0 && event.isCtrlDown()) {
				scaleFactor = Math.max(standardScaleFactor, scaleFactor * (r > 0 ? 1.1 : 1 / 1.1));
				resize();
			}
			break;
		default:
			break;

		}
	}

	protected void mouseOut() {
		if (isHovered && !headerBar.isClicked()) {
			isHovered = false;
			if (wasResizedWhileHovered)
				setClusterSize(newDimSize, newRecSize, elementCountBiggestCluster);
			opacityfactor = highOpacityFactor;
			for (GLElement child : this)
				child.repaint();
			EventPublisher.trigger(new MouseOverClusterEvent(this, false));
			relayout(); // for hiding the bars
		}
	}

	protected void recreateVirtualArrays(List<Integer> dimIndices, List<Integer> recIndices) {
		VirtualArray dimArray = getDimensionVirtualArray();
		VirtualArray recArray = getRecordVirtualArray();
		addAll(dimArray, dimIndices, dimNumberThreshold);
		addAll(recArray, recIndices, recNumberThreshold);

		this.data.invalidateContainerStatistics();

		if (propabilityHeatMapHor != null)
			propabilityHeatMapHor.update(dimThreshold, this.bcNr, dimArray);
		if (propabilityHeatMapVer != null)
			propabilityHeatMapVer.update(recThreshold, this.bcNr, recArray);
	}

	private static void addAll(VirtualArray array, List<Integer> indices, int treshold) {
		array.clear();
		if (treshold == ParameterToolBarElement.UNBOUND_NUMBER) // unbound flush all
			array.addAll(indices);
		else
			// sublist of the real elements
			array.addAll(indices.subList(0, Math.min(indices.size(), treshold)));
	}

	void calculateOverlap(boolean dimBandsEnabled, boolean recBandsEnabled) {
		this.dimBandsEnabled = dimBandsEnabled;
		this.recBandsEnabled = recBandsEnabled;
		dimOverlap = new HashMap<>();
		recOverlap = new HashMap<>();
		List<Integer> myDimIndizes = getDimensionVirtualArray().getIDs();
		List<Integer> myRecIndizes = getRecordVirtualArray().getIDs();
		dimensionOverlapSize = 0;
		recordOverlapSize = 0;
		for (GLElement element : allClusters.asList()) {
			if (element == this)
				continue;
			ClusterElement e = (ClusterElement) element;
			List<Integer> eIndizes = null;
			if (dimBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myDimIndizes);
				eIndizes.retainAll(e.getDimensionVirtualArray().getIDs());
				dimOverlap.put(element, eIndizes);
				dimensionOverlapSize += eIndizes.size();
			}
			if (recBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myRecIndizes);
				eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
				recOverlap.put(element, eIndizes);
				recordOverlapSize += eIndizes.size();
			}
		}
		if (getVisibility() == EVisibility.PICKABLE)
			sort(sortingType);
		fireTablePerspectiveChanged();
	}

	public Vec2d getAttForce() {
		return attForce;
	}

	public void setAttForce(Vec2d force) {
		this.attForce = force;
	}

	public void setRepForce(Vec2d force) {
		this.repForce = force;
	}

	public Vec2d getRepForce() {
		return repForce;
	}

	public Vec2d getFrameForce() {
		return frameForce;
	}

	public void setFrameForce(Vec2d frameForce) {
		this.frameForce = frameForce;
	}

	public void setPerspectiveLabel(String dimensionName, String recordName) {
		data.getDimensionPerspective().setLabel(dimensionName);
		data.getRecordPerspective().setLabel(recordName);
	}

	protected void fireTablePerspectiveChanged() {
		EventPublisher.trigger(new RecordVAUpdateEvent(data.getDataDomain().getDataDomainID(), data
				.getRecordPerspective().getPerspectiveID(), this));
		EventPublisher.trigger(new DimensionVAUpdateEvent(data.getDataDomain().getDataDomainID(), data
				.getDimensionPerspective().getPerspectiveID(), this));
	}

	protected VirtualArray getDimensionVirtualArray() {
		return data.getDimensionPerspective().getVirtualArray();
	}

	protected VirtualArray getRecordVirtualArray() {
		return data.getRecordPerspective().getVirtualArray();
	}

	public int getNumberOfDimElements() {
		return getDimensionVirtualArray().size();
	}

	public int getNumberOfRecElements() {
		return getRecordVirtualArray().size();
	}

	public boolean isDragged() {
		return isDragged;
	}

	public boolean isVisible() {
		return getVisibility().doRender();
	}

	public List<Integer> getDimOverlap(GLElement jElement) {
		if (dimOverlap.containsKey(jElement))
			return dimOverlap.get(jElement);
		return Collections.emptyList();
	}

	public List<Integer> getRecOverlap(GLElement jElement) {
		if (recOverlap.containsKey(jElement))
			return recOverlap.get(jElement);
		return Collections.emptyList();
	}

	public int getDimensionOverlapSize() {
		return dimensionOverlapSize;
	}

	public int getRecordOverlapSize() {
		return recordOverlapSize;
	}

	protected IGLLayoutElement getIGLayoutElement() {
		return GLElementAccessor.asLayoutElement(this);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		// if (isHidden) return;
		IGLLayoutElement toolbar = children.get(0);
		IGLLayoutElement headerbar = children.get(1);
		IGLLayoutElement dimthreshbar = children.get(2);
		IGLLayoutElement recthreshbar = children.get(3);

		// shift for propability heat maps
		float shift = children.size() > 5 ? 10 : 0;

		if (isHovered) { // depending whether we are hovered or not, show hide
							// the toolbar's
			toolbar.setBounds(-38 - shift, 0, 18, 100);
			headerbar.setBounds(0, -39 - shift, w < 55 ? 57 : w + 2, 20);
			dimthreshbar.setBounds(-1, -20 - shift, w < 55 ? 56 : w + 1, 20);
			recthreshbar.setBounds(-20 - shift, -1, 20, h < 60 ? 61 : h + 1);

		} else {
			// hide by setting the width to 0
			toolbar.setBounds(-38 - shift, 0, 0, 100);
			headerbar.setBounds(0, -18 - shift, w < 50 ? 50 : w, 17);
			dimthreshbar.setBounds(-1, -20 - shift, 0, 0);
			recthreshbar.setBounds(-20 - shift, -1, 0, 0);
		}
		if (children.size() > 5) {
			children.get(5).setBounds(0, -shift, w, shift);
			children.get(6).setBounds(-shift, 0, shift, h);
		}

		IGLLayoutElement igllContent = children.get(4);
		if (isFocused && doesShowLabels(igllContent.asElement())) {
			igllContent.setBounds(0, 0, w + 79, h + 79);
		} else {
			igllContent.setBounds(0, 0, w, h);
		}

	}

	/**
	 * @param asElement
	 * @return
	 */
	private static boolean doesShowLabels(GLElement asElement) {
		return (asElement instanceof ClusterContentElement && ((ClusterContentElement) asElement).doesShowLabels());
	}

	protected class HeaderBar extends GLButton implements ISelectionCallback {

		private boolean clicked = false;

		public boolean isClicked() {
			return clicked;
		}

		public HeaderBar() {
			setzDelta(DEFAULT_Z_DELTA);
			createButtons();
			setSize(Float.NaN, 20);
		}

		protected void createButtons() {
			setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					if (isFocused) {
						g.color(SelectionType.SELECTION.getColor());
						g.fillRoundedRect(0, 0, w, h, 2);
						g.textColor(Color.BLACK);
					} else if (isHovered) {
						g.color(SelectionType.MOUSE_OVER.getColor());
						g.fillRoundedRect(0, 0, w, h, 2);
					} else
						g.textColor(new Color(0, 0, 0, curOpacityFactor));

					if (isHovered) {
						final String text = " " + (scaleFactor == standardScaleFactor ? getID() : getID()) + " ("
								+ (int) (100 * scaleFactor) + "%)";

						g.drawText(text, 0, 0, g.text.getTextWidth(text, 12) + 2, 12);
					} else {
						final String text = scaleFactor == standardScaleFactor ? getID() : getID() + " ("
								+ (int) (100 * scaleFactor) + "%)";
						g.drawText(text, 0, 0, g.text.getTextWidth(text, 12) + 2, 12);
					}
					g.textColor(Color.BLACK);

				}
			});
		}

		@Override
		protected void onPicked(Pick pick) {
			switch (pick.getPickingMode()) {
			case DRAGGED:
				if (!pick.isDoDragging())
					return;
				if (isDragged == false) {
					allClusters.setDragedLayoutElement(cluster);
				}
				isDragged = true;
				setzDelta(DRAGGING_Z_DELTA);
				cluster.setLocation(cluster.getLocation().x() + pick.getDx(), cluster.getLocation().y() + pick.getDy());
				cluster.relayout();
				cluster.repaintPick();
				break;
			case CLICKED:
				if (!pick.isAnyDragging()) {
					pick.setDoDragging(true);
					clicked = true;
				}
				break;
			case DOUBLE_CLICKED:
				renameCluster();
				break;
			case MOUSE_RELEASED:
				pick.setDoDragging(false);
				clicked = false;
				isDragged = false;
				allClusters.setDragedLayoutElement(null);
				setzDelta(DEFAULT_Z_DELTA);
				if (isClusterCollision())
					mouseOut();
				break;
			default:
				if (!pick.isDoDragging())
					return;
				isDragged = false;
				allClusters.setDragedLayoutElement(null);
			}
			setZValuesAccordingToState();
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			// TODO Auto-generated method stub

		}

	}

	private boolean isClusterCollision() {
		Vec2f mySize = getSize();
		Vec2f myLoc = getLocation();
		Rectangle myRec = new Rectangle((int) myLoc.x() - 10, (int) myLoc.y() - 10, (int) mySize.x() + 20,
				(int) mySize.y() + 20);
		for (GLElement jGLE : allClusters) {
			ClusterElement j = (ClusterElement) jGLE;
			if (j == this)
				continue;
			Vec2f jSize = j.getSize();
			Vec2f jLoc = j.getLocation();
			Rectangle jRec = new Rectangle((int) jLoc.x() - 10, (int) jLoc.y() - 10, (int) jSize.x() + 20,
					(int) jSize.y() + 20);
			if (myRec.intersects(jRec)) {
				return true;
			}
		}
		return false;
	}

	protected class ThresholdBar extends GLElementContainer implements
			org.caleydo.core.view.opengl.layout2.basic.GLSlider.ISelectionCallback {

		boolean isHorizontal;
		GLSlider slider;
		float globalMaxThreshold;
		float localMaxSliderValue;
		float localMinSliderValue;

		protected ThresholdBar(boolean layout) {
			super(layout ? GLLayouts.flowHorizontal(1) : GLLayouts.flowVertical(1));
			isHorizontal = layout;
			// move to the top
			setzDelta(DEFAULT_Z_DELTA);

			// create buttons
			createButtons();

			setSize(Float.NaN, 20);

			// define the animation used to move this element
			if (isHorizontal) {
				this.setLayoutData(new MoveTransitions.MoveTransitionBase(Transitions.LINEAR, Transitions.LINEAR,
						Transitions.NO, Transitions.LINEAR));
			} else {
				this.setLayoutData(new MoveTransitions.MoveTransitionBase(Transitions.LINEAR, Transitions.LINEAR,
						Transitions.LINEAR, Transitions.NO));
			}
		}

		protected void createButtons() {
			this.remove(slider);
			float max = localMaxSliderValue > localMinSliderValue ? localMaxSliderValue : localMinSliderValue;
			this.slider = new GLSlider(0, max, max / 2);
			slider.setCallback(this);
			slider.setHorizontal(isHorizontal);
			if (isHorizontal) {
				slider.setSize(Float.NaN, 18);
			} else {
				slider.setSize(18, Float.NaN);
			}
			slider.setMinMaxVisibility(EValueVisibility.VISIBLE_HOVERED);
			this.add(slider);
		}

		boolean ignoreNextChange = true;

		@Override
		public void onSelectionChanged(GLSlider slider, float value) {
			if (ignoreNextChange) {
				ignoreNextChange = false;
				return;
			}
			if (value <= localMinSliderValue || value >= localMaxSliderValue)
				return;
			setThresholdImpl(isHorizontal, value);

		}

		protected void updateSliders(double maxValue, double minValue) {
			localMaxSliderValue = (float) maxValue;
			localMinSliderValue = (float) minValue;
			relayout();
		}

		@ListenTo
		public void listenTo(MaxThresholdChangeEvent event) {
			globalMaxThreshold = (float) (isHorizontal ? event.getDimThreshold() : event.getRecThreshold());
			createButtons();
		}

		@ListenTo
		public void listenTo(LZThresholdChangeEvent event) {
			if (event.isGlobalEvent()) {
				ignoreNextChange = true;
				slider.setValue(isHorizontal ? event.getDimensionThreshold() : event.getRecordThreshold());
			}
		}

		/**
		 * @param value
		 */
		public void setValue(float value) {
			slider.setCallback(null);
			slider.setValue(value);
			slider.setCallback(this);
		}
	}

	protected class ToolBar extends GLElementContainer implements ISelectionCallback {

		GLButton hide, sorting, enlarge, smaller, focus, lock;
		SortingType sortingButtonCaption = SortingType.probabilitySorting;

		public ToolBar() {
			super(GLLayouts.flowVertical(6));
			setzDelta(-0.1f);
			createButtons();
			setSize(Float.NaN, 20);
			this.setLayoutData(new MoveTransitions.MoveTransitionBase(Transitions.LINEAR, Transitions.NO,
					Transitions.LINEAR, Transitions.LINEAR));
		}

		protected void createButtons() {
			hide = createHideClusterButton();
			this.add(hide);
			sorting = new GLButton();
			sorting.setRenderer(GLRenderers.drawText(
					sortingButtonCaption == SortingType.probabilitySorting ? "P" : "B", VAlign.CENTER));
			sorting.setSize(16, Float.NaN);
			sorting.setTooltip("Change sorting");
			sorting.setCallback(this);
			this.add(sorting);
			focus = new GLButton();
			focus.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_FOCUS));
			focus.setSize(16, Float.NaN);
			focus.setTooltip("Focus this Cluster");
			focus.setCallback(this);
			this.add(focus);
			lock = new GLButton();
			lock.setTooltip("Lock this cluster. It will not recieve threshold updates.");
			lock.setSize(16, Float.NaN);
			lock.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					if (isLocked)
						g.fillImage(BiClusterRenderStyle.ICON_UNLOCK, 0, 0, w, h);
					else
						g.fillImage(BiClusterRenderStyle.ICON_LOCK, 0, 0, w, h);
				}

			});
			lock.setCallback(this);
			this.add(lock);
			enlarge = new GLButton();
			enlarge.setSize(16, Float.NaN);
			enlarge.setTooltip("Enlarge");
			enlarge.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_ZOOM_IN));
			enlarge.setCallback(this);
			this.add(enlarge);
			smaller = new GLButton();
			smaller.setTooltip("Reduce");
			smaller.setSize(16, Float.NaN);
			smaller.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_ZOOM_OUT));
			smaller.setCallback(this);
			this.add(smaller);

		}

		void setSortingCaption(SortingType caption) {
			sortingButtonCaption = caption;
			sorting.setRenderer(GLRenderers.drawText(
					sortingButtonCaption == SortingType.probabilitySorting ? "P" : "B", VAlign.CENTER));
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			if (button == sorting) {
				setSortingCaption(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
				sort(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
			} else if (button == enlarge) {
				upscale();
				resize();
			} else if (button == smaller) {
				reduceScaleFactor();
				resize();
			} else if (button == focus) {
				EventPublisher.trigger(new FocusChangeEvent(cluster));
			} else if (button == lock) {
				isLocked = !isLocked;
				lock.setTooltip(isLocked ? "UnLock this cluster. It will again recieve threshold updates."
						: "Lock this cluster. It will not recieve threshold updates.");
			}
		}

	}

	protected void upscale() {
		scaleFactor += 0.6;
	}

	public final void renameCluster() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				String label = getLabel();
				String newlabel = RenameNameDialog.show(null, "Rename Cluster: " + label, label);
				if (newlabel != null && !label.equals(newlabel))
					EventPublisher.trigger(new ClusterRenameEvent(newlabel).to(ClusterElement.this));
			}
		});
	}

	@ListenTo(sendToMe = true)
	private void listenTo(ClusterRenameEvent e) {
		setLabel(e.getNewName());
	}

	protected void reduceScaleFactor() {
		scaleFactor -= 0.6;
		if (scaleFactor <= standardScaleFactor)
			resetScaleFactor();
	}

	protected void resetScaleFactor() {
		scaleFactor = standardScaleFactor;
	}

	private boolean wasResizedWhileHovered = false;
	private double newRecSize = 0;
	private double newDimSize = 0;

	public void setClusterSize(double x, double y, double maxClusterSize) {
		if (isHovered || isLocked) {
			wasResizedWhileHovered = true;
			newRecSize = y;
			newDimSize = x;
		} else {
			wasResizedWhileHovered = false;
			newRecSize = 0;
			newDimSize = 0;
			dimSize = x;
			recSize = y;
			resize();
		}
		elementCountBiggestCluster = maxClusterSize;
	}

	protected void resize() {
		setSize((float) (dimSize * scaleFactor), (float) (recSize * scaleFactor));
		relayout();
	}

	protected void handleFocus() {
		if (isFocused) {
			scaleFactor = defaultFocusScaleFactor();
			if (content instanceof ClusterContentElement) {
				((ClusterContentElement) content).showLabels(EShowLabels.RIGHT);
				if (propabilityHeatMapHor != null)
					propabilityHeatMapHor.nonUniformLayout(((ClusterContentElement) content));
				if (propabilityHeatMapVer != null)
					propabilityHeatMapVer.nonUniformLayout(((ClusterContentElement) content));
			}
			resize();
		} else {
			resetScaleFactor();
			if (content instanceof ClusterContentElement) {
				((ClusterContentElement) content).hideLabels();
				if (propabilityHeatMapHor != null)
					propabilityHeatMapHor.uniformLayout();
				if (propabilityHeatMapVer != null)
					propabilityHeatMapVer.uniformLayout();
			}
			resize();
			mouseOut();
		}
	}

	/**
	 * generate the default scale factor to use during focus
	 *
	 * @return
	 */
	private double defaultFocusScaleFactor() {
		double s = 0;
		double ws = focusSize(getNumberOfDimElements()) / dimSize;
		double hs = focusSize(getNumberOfRecElements()) / recSize;
		s = Math.max(ws, hs);
		return Math.max(scaleFactor, s);
	}

	/**
	 * @param dimensionElementSize
	 * @return
	 */
	private float focusSize(int elements) {
		if (elements < 5)
			return 20 * elements;
		else if (elements < 10)
			return 16 * elements;
		return Math.min(14 * elements, 14 * 20);
	}

	private void hideThisCluster() {
		isHidden = true;
		isHovered = false;
		setVisibility();
		EventPublisher.trigger(new ClusterGetsHiddenEvent(getID()));
		EventPublisher.trigger(new MouseOverClusterEvent(this, false));
		relayout();
	}

	@ListenTo
	private void listenTo(FocusChangeEvent e) {
		if (e.getSender() == this) {
			this.isFocused = !this.isFocused;
		} else {
			resetScaleFactor();
			resize();
			this.isFocused = false;
		}
		handleFocus();
	}

	@ListenTo
	private void listenTo(UnhidingClustersEvent event) {
		isHidden = false;
		setVisibility();
		biclusterRoot.setClusterSizes();
	}

	@ListenTo
	private void listenTo(SortingChangeEvent e) {
		if (e.getSender() instanceof ClusterElement && e.getSender() == this) {
			// only local change
		} else {
			sort(e.getType());
		}
		toolBar.setSortingCaption(e.getType());
	}

	@ListenTo
	private void listenTo(MouseOverClusterEvent event) {
		ClusterElement hoveredElement = (ClusterElement) event.getSender();
		if (hoveredElement == this || getDimOverlap(hoveredElement).size() > 0
				|| getRecOverlap(hoveredElement).size() > 0) {
			opacityfactor = highOpacityFactor;
			return;
		} else if (event.isMouseOver()) {
			opacityfactor = lowOpacityFactor;
		} else {
			opacityfactor = highOpacityFactor;
		}
	}

	@ListenTo
	private void listenTo(MouseOverBandEvent event) {
		if (event.getFirst() == this || event.getSecond() == this) {
			opacityfactor = highOpacityFactor;
			return;
		} else if (event.isMouseOver()) {
			opacityfactor = lowOpacityFactor;
		} else {
			opacityfactor = highOpacityFactor;
		}
	}

	@ListenTo
	private void onSearchClusterEvent(SearchClusterEvent event) {
		if (event.isClear()) {
			opacityfactor = highOpacityFactor;
		} else if (StringUtils.containsIgnoreCase(getLabel(), event.getText())) {
			opacityfactor = highOpacityFactor;
			System.out.println(getLabel() + " matches " + event.getText());
		} else {
			opacityfactor = lowOpacityFactor;
			System.out.println(getLabel() + " not matches " + event.getText());
		}
		if (content != null)
			content.repaint();
		else
			repaint();
	}

	@ListenTo
	private void listenTo(LZThresholdChangeEvent event) {
		if (getID().contains("Special")) {
			System.out.println("Threshold Change");
		}
		if (!event.isGlobalEvent()) {
			return;
		}
		if (bcNr == 0) {
			System.out.println(recThreshold + " " + dimThreshold + " " + recNumberThreshold + " " + dimNumberThreshold);
		}
		recThreshold = event.getRecordThreshold();
		dimThreshold = event.getDimensionThreshold();
		recNumberThreshold = event.getRecordNumberThreshold();
		dimNumberThreshold = event.getDimensionNumberThreshold();
		if (bcNr == 0) {
			System.out.println(recThreshold + " " + dimThreshold + " " + recNumberThreshold + " " + dimNumberThreshold);
		}
		rebuildMyData(event.isGlobalEvent());

	}

	@ListenTo
	private void listenTo(MinClusterSizeThresholdChangeEvent event) {
		this.clusterSizeThreshold = event.getMinClusterSize();
		setVisibility();
	}

	public void setVisibility() {
		if (isHidden || !hasContent)
			setVisibility(EVisibility.NONE);
		else if (getDimensionVirtualArray().size() / elementCountBiggestCluster > clusterSizeThreshold)
			setVisibility(EVisibility.PICKABLE);
		else if (getRecordVirtualArray().size() / elementCountBiggestCluster > clusterSizeThreshold)
			setVisibility(EVisibility.PICKABLE);
		else
			setVisibility(EVisibility.NONE);
	}

	public void setData(List<Integer> dimIndices, List<Integer> recIndices, String id, int bcNr,
			double maxDim, double maxRec, double minDim, double minRec) {
		setLabel(id);
		if (maxDim >= 0 && maxRec >= 0) {
			dimThreshBar.updateSliders(maxDim, minDim);
			recThreshBar.updateSliders(maxRec, minRec);
		}
		dimProbabilitySorting = new ArrayList<Integer>(dimIndices);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		setHasContent(dimIndices, recIndices);
		setVisibility();
	}

	protected void setLabel(String id) {
		data.setLabel(id);
	}

	@Override
	public String getLabel() {
		return getID();
	}

	protected void setHasContent(List<Integer> dimIndices, List<Integer> recIndices) {
		if (dimIndices.size() > 0 && recIndices.size() > 0) {
			hasContent = true;
			recreateVirtualArrays(dimIndices, recIndices);
		} else {
			hasContent = false;
		}
	}

	protected void sort(SortingType type) {
		switch (type) {
		case probabilitySorting:
			sortingType = SortingType.probabilitySorting;
			probabilitySorting();
			break;
		case bandSorting:
			sortingType = SortingType.bandSorting;
			bandSorting();
			break;
		default:
		}
	}

	private void bandSorting() {
		Set<Integer> finalDimSorting = new LinkedHashSet<Integer>();
		List<List<Integer>> nonEmptyDimBands = new ArrayList<>();
		for (List<Integer> dimBand : dimOverlap.values()) {
			if (dimBand.size() > 0)
				nonEmptyDimBands.add(dimBand);
		}
		BandSorting dimConflicts = new BandSorting(nonEmptyDimBands);
		for (Integer i : dimConflicts) {
			finalDimSorting.add(i);
		}
		finalDimSorting.addAll(dimProbabilitySorting);

		Set<Integer> finalRecSorting = new LinkedHashSet<Integer>();
		List<List<Integer>> nonEmptyRecBands = new ArrayList<>();
		for (List<Integer> recBand : recOverlap.values()) {
			if (recBand.size() > 0)
				nonEmptyRecBands.add(recBand);
		}
		BandSorting recConflicts = new BandSorting(nonEmptyRecBands);
		for (Integer i : recConflicts) {
			finalRecSorting.add(i);
		}
		finalRecSorting.addAll(recProbabilitySorting);
		recreateVirtualArrays(new ArrayList<Integer>(finalDimSorting), new ArrayList<Integer>(finalRecSorting));
		fireTablePerspectiveChanged();
	}

	private void probabilitySorting() {
		sortingType = SortingType.probabilitySorting;
		recreateVirtualArrays(dimProbabilitySorting, recProbabilitySorting);
		fireTablePerspectiveChanged();
	}

	public List<List<Integer>> getListOfContinousRecSequenzes(List<Integer> overlap) {
		return getListOfContinousIDs(overlap, getRecordVirtualArray().getIDs());
	}

	public List<List<Integer>> getListOfContinousDimSequences(List<Integer> overlap) {
		return getListOfContinousIDs(overlap, getDimensionVirtualArray().getIDs());
	}

	private List<List<Integer>> getListOfContinousIDs(List<Integer> overlap, List<Integer> indices) {
		List<List<Integer>> sequences = new ArrayList<List<Integer>>();
		if (overlap.size() == 0)
			return sequences;
		List<Integer> accu = new ArrayList<Integer>();
		for (Integer i : indices) {
			if (overlap.contains(i)) {
				accu.add(i);
			} else if (accu.size() > 0) { // don't add empty lists
				sequences.add(accu);
				accu = new ArrayList<>();
			}
		}
		if (accu.size() > 0)
			sequences.add(accu);
		return sequences;
	}

	public float getDimPosOf(int index) {
		if (isFocused && content instanceof ClusterContentElement) {
			int ind = getDimensionVirtualArray().indexOf(index);
			return ((ClusterContentElement) content).getDimensionPos(ind);
		} else {
			return getDimIndexOf(index) * getSize().x() / getDimensionVirtualArray().size();
		}
	}

	public float getRecPosOf(int index) {
		if (isFocused && content instanceof ClusterContentElement) {
			int ind = getRecordVirtualArray().indexOf(index);
			return ((ClusterContentElement) content).getRecordPos(ind);
		} else {
			return getRecIndexOf(index) * getSize().y() / getRecordVirtualArray().size();
		}

	}

	protected void rebuildMyData(boolean isGlobal) {
		if (isLocked)
			return;
		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
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
		setData(dimIndices, recIndices, getID(), bcNr, -1, -1, -1, -1);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		if (!isGlobal)
			EventPublisher.trigger(new MouseOverClusterEvent(this, true));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal, dimBandsEnabled, recBandsEnabled));
		EventPublisher.trigger(new CreateBandsEvent(this));

	}

	public int getDimIndexOf(int value) {
		return getDimensionVirtualArray().indexOf(value);
	}

	public int getRecIndexOf(int value) {
		return getRecordVirtualArray().indexOf(value);
	}

	public float getDimensionElementSize() {
		return getSize().x() / getDimensionVirtualArray().size();
	}

	public float getRecordElementSize() {
		return getSize().y() / getRecordVirtualArray().size();
	}

	public int getNrOfElements(List<Integer> band) {
		return band.size();
	}

	protected GLButton createHideClusterButton() {
		GLButton hide = new GLButton();
		hide.setRenderer(GLRenderers.fillImage(BiClusterRenderStyle.ICON_CLOSE));
		hide.setTooltip("Close");
		hide.setSize(16, Float.NaN);
		hide.setCallback(new ISelectionCallback() {

			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				hideThisCluster();
			}

		});
		return hide;
	}

	public TablePerspective getTablePerspective() {
		return data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClusterElement [").append(getLabel());
		builder.append("]");
		return builder.toString();
	}

	public void setThreshold(boolean isDimension, float value) {
		setThresholdImpl(isDimension, value);
		if (isDimension && dimThreshBar != null)
			dimThreshBar.setValue(value);
		else if (!isDimension && recThreshBar != null)
			recThreshBar.setValue(value);
	}

	/**
	 * @param isDimension
	 * @param value
	 */
	void setThresholdImpl(boolean isDimension, float value) {
		if ((isDimension && dimThreshold == value) || (!isDimension && recThreshold == value))
			return;
		if (isDimension)
			dimThreshold = value;
		else
			recThreshold = value;
		rebuildMyData(false);
	}

	public Vec2f getPreferredSize() {
		if (content instanceof ClusterContentElement) {
			return ((ClusterContentElement) content).getMinSize();
		}
		return new Vec2f(getNumberOfRecElements(), getNumberOfDimElements());
	}
}
