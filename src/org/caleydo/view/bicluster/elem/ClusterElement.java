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
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.events.DimensionVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.RecordVAUpdateEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
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
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.concurrent.ScanProbabilityMatrix;
import org.caleydo.view.bicluster.concurrent.ScanResult;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;
import org.caleydo.view.bicluster.sorting.BandSorting;
import org.caleydo.view.bicluster.sorting.ProbabilityStrategy;
import org.caleydo.view.bicluster.util.Vec2d;
import org.caleydo.view.heatmap.v2.BasicBlockColorer;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.HeatMapElement.EShowLabels;
import org.caleydo.view.heatmap.v2.IBlockColorer;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

/**
 * e.g. a class for representing a cluster
 * 
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public class ClusterElement extends AnimatedGLElementContainer implements
		IBlockColorer, IGLLayout {
	protected static final float highOpacityFactor = 1;
	protected static final float lowOpacityFactor = 0.2f;
	protected static final float opacityChangeInterval = 10f;

	protected final TablePerspective data;
	protected final TablePerspective x;
	protected final TablePerspective l;
	protected final TablePerspective z;
	protected final AllClustersElement allClusters;
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

	protected boolean setOnlyShowXElements;
	protected int bcNr;
	protected ToolBar toolBar;
	protected HeaderBar headerBar;
	protected ThresholdBar dimThreshBar;
	protected ThresholdBar recThreshBar;
	protected GLElement content;

	protected float recThreshold = 0.08f;
	protected float dimThreshold = 4.5f;
	protected double clusterSizeThreshold;
	protected double elementCountBiggestCluster;
	
	int dimensionOverlapSize;
	int recordOverlapSize;
	private double dimSize;
	private double recSize;
	protected double scaleFactor;
	protected boolean isFocused = false;


	public ClusterElement(TablePerspective data, AllClustersElement root,
			TablePerspective x, TablePerspective l, TablePerspective z,
			ExecutorService executor) {
		setLayout(this);
		this.data = data;
		this.allClusters = root;
		this.x = x;
		this.l = l;
		this.z = z;
		this.executor = executor;
		toolBar = new ToolBar();
		headerBar = new HeaderBar();
		dimThreshBar = new ThresholdBar(true);
		recThreshBar = new ThresholdBar(false);
		this.add(toolBar); // add a element toolbar
		this.add(headerBar);
		this.add(dimThreshBar);
		this.add(recThreshBar);
		initContent();
		setVisibility();
		resetScaleFactor();
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
		this.setLayoutData(MoveTransitions.MOVE_AND_GROW_LINEAR);
		this.cluster = this;
	}

	protected void initContent() {
		final HeatMapElement heatmapImpl = new HeatMapElement(data, this,
				EDetailLevel.HIGH);

		// heatmapImpl.setRecordLabels(EShowLabels.RIGHT);
		// let's use a fish-eye spacing strategy where selected lines have a
		// height of at least 16 pixels
		// heatmapImpl.setRecordSpacingStrategy(SpacingStrategies.fishEye(16));
		// heatmapImpl.setDimensionLabels(EShowLabels.RIGHT);
		// heatmap = new ScrollingDecorator(heatmapImpl, new ScrollBar(true),
		// new ScrollBar(false), 5);
		content = heatmapImpl;
		content.setzDelta(0.5f);
		// setzDelta(f);
		this.add(content);
	}

	@Override
	public Color apply(int recordID, int dimensionID,
			ATableBasedDataDomain dataDomain, boolean deSelected) {
		Color color = BasicBlockColorer.INSTANCE.apply(recordID, dimensionID,
				dataDomain, deSelected);

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

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(java.awt.Color.black);
//		if (getID().contains("Special")){
//			System.out.println("stop");
//		}
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
		// if (getID().contains("24")){
		// System.out.println("stop");
		// }

		float[] color = { 0, 0, 0, curOpacityFactor };
		float[] highlightedColor = SelectionType.MOUSE_OVER.getColor();
		g.color(color);
		if (isHovered) {
			g.color(highlightedColor);
		}
		g.drawRect(-1, -1, w + 2, h + 2);

	}

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		// case DRAGGED:
		// if (!pick.isDoDragging()) return;
		// if (isDragged == false) {
		// allClusters.setDragedLayoutElement(this);
		// }
		// isDragged = true;
		// setLocation(getLocation().x() + pick.getDx(), getLocation().y()
		// + pick.getDy());
		// relayoutParent();
		// repaintPick();
		// break;
		// case CLICKED:
		// if (!pick.isAnyDragging())pick.setDoDragging(true);
		// break;
		// case MOUSE_RELEASED:
		// pick.setDoDragging(false);
		// break;
		case MOUSE_OVER:
			if (!pick.isAnyDragging()) {
				isHovered = true;
				allClusters.setHooveredElement(this);
				EventPublisher.trigger(new MouseOverClusterEvent(this, true));
				relayout(); // for showing the toolbar
			}
			break;
		case MOUSE_OUT:
			mouseOut();
			break;
		// default:
		// isDragged = false;
		// allClusters.setDragedLayoutElement(null);
		}
	}

	protected void mouseOut() {
		if (isHovered && !headerBar.isClicked()) {
			// System.out.println("out");
			isHovered = false;
			if (wasResizedWhileHovered)
				setClusterSize(newDimSize, newRecSize,
						elementCountBiggestCluster);
			allClusters.setHooveredElement(null);
			opacityfactor = highOpacityFactor;
			// timer.restart();
			relayout(); // for showing the toolbar
			repaintAll();
			for (GLElement child : this)
				child.repaint();
			EventPublisher.trigger(new MouseOverClusterEvent(this, false));
		}
	}

	protected void recreateVirtualArrays(List<Integer> dimIndices,
			List<Integer> recIndices) {
		VirtualArray dimArray = getDimensionVirtualArray();
		VirtualArray recArray = getRecordVirtualArray();
		dimArray.clear();
		int count = 0;
		for (Integer i : dimIndices) {
			if (setOnlyShowXElements
					&& allClusters.getFixedElementsCount() <= count)
				break;
			dimArray.append(i);
			count++;
		}
		count = 0;
		recArray.clear();
		for (Integer i : recIndices) {
			if (setOnlyShowXElements
					&& allClusters.getFixedElementsCount() <= count)
				break;
			recArray.append(i);
			count++;
		}
	}

	void calculateOverlap(boolean dimBandsEnabled, boolean recBandsEnabled) {
		if (getID().contains("10")) {
			System.out.println("stzop");
		}
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
				if (getID().contains("10") && eIndizes.size() > 0)
					System.out.println("halt");
				dimensionOverlapSize += eIndizes.size();
			} else {
				dimOverlap.put(element, new ArrayList<Integer>());
			}
			if (recBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myRecIndizes);
				eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
				if (getID().contains("10") && eIndizes.size() > 0)
					System.out.println("halt");
				recOverlap.put(element, eIndizes);
				recordOverlapSize += eIndizes.size();
			} else {
				recOverlap.put(element, new ArrayList<Integer>());
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
//		if (frameForce.x() > 100) {
//			System.out.println(getID() + ": Frame Force X zu stark");
//			frameForce.setX(100);
//		} 
//		if ( frameForce.y() > 100){
//			System.out.println(getID() +  ": Frame Force Y zu stark");
//			frameForce.setY(100);
//		}
		this.frameForce = frameForce;
	}

	public void setPerspectiveLabel(String dimensionName, String recordName) {
		data.getDimensionPerspective().setLabel(dimensionName);
		data.getRecordPerspective().setLabel(recordName);
	}

	protected void fireTablePerspectiveChanged() {
		EventPublisher.trigger(new RecordVAUpdateEvent(data.getDataDomain()
				.getDataDomainID(), data.getRecordPerspective()
				.getPerspectiveID(), this));
		EventPublisher.trigger(new DimensionVAUpdateEvent(data.getDataDomain()
				.getDataDomainID(), data.getDimensionPerspective()
				.getPerspectiveID(), this));
		repaintAll();
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
		return dimOverlap.get(jElement);
	}

	public List<Integer> getRecOverlap(GLElement jElement) {
		return recOverlap.get(jElement);
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
	public void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		// if (isHidden) return;
		IGLLayoutElement toolbar = children.get(0);
		IGLLayoutElement headerbar = children.get(1);
		IGLLayoutElement dimthreshbar = children.get(2);
		IGLLayoutElement recthreshbar = children.get(3);
		if (isHovered) { // depending whether we are hovered or not, show hide
							// the toolbar's
			toolbar.setBounds(-38, 0, 18, 100);
			headerbar.setBounds(0, -39, w < 55 ? 57 : w + 2, 20);
			dimthreshbar.setBounds(-1, -20, w < 55 ? 56 : w + 1, 20);
			recthreshbar.setBounds(-20, -1, 20, h < 60 ? 61 : h + 1);

		} else {
			toolbar.setBounds(0, 0, 0, 0); // hide by setting the width to 0
			headerbar.setBounds(0, -18, w < 50 ? 50 : w, 17);
			dimthreshbar.setBounds(-1, -20, 0, 0);
			recthreshbar.setBounds(-20, -1, 0, 0);
		}
		IGLLayoutElement igllContent = children.get(4);
		if (isFocused) {
			igllContent.setBounds(0, 0, w + 79, h + 79);
		} else {
			igllContent.setBounds(0, 0, w, h);
		}
	}

	protected class HeaderBar extends GLButton implements ISelectionCallback {

		private boolean clicked = false;

		public boolean isClicked() {
			return clicked;
		}

		public HeaderBar() {
			setzDelta(0.5f);
			createButtons();
			setSize(Float.NaN, 20);
		}

		protected void createButtons() {
			setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h,
						GLElement parent) {
					if (isFocused) {
						g.color(SelectionType.SELECTION.getColor());
						g.fillRoundedRect(0, 0, w, h, 2);
					} else if (isHovered) {
						g.color(SelectionType.MOUSE_OVER.getColor());
						g.fillRoundedRect(0, 0, w, h, 2);
					}
					float[] color = { 0, 0, 0, curOpacityFactor };
					g.textColor(color);
					if (isHovered)
						g.drawText(" " + (scaleFactor == 1 ? getID() : getID())
								+ " (" + (int) (100 * scaleFactor) + "%)", 0,
								0, 100, 12);
					else
						g.drawText(scaleFactor == 1 ? getID() : getID() + " ("
								+ (int) (100 * scaleFactor) + "%)", 0, 0, 100,
								12);

					float[] black = { 0, 0, 0, 1 };
					g.textColor(black);

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
				cluster.setLocation(cluster.getLocation().x() + pick.getDx(),
						cluster.getLocation().y() + pick.getDy());
				cluster.relayout();
				cluster.repaintPick();
				break;
			case CLICKED:
				if (!pick.isAnyDragging()) {
					pick.setDoDragging(true);
					clicked = true;
				}
				break;
			case MOUSE_RELEASED:
				pick.setDoDragging(false);
				clicked = false;
				break;
			default:
				isDragged = false;
				allClusters.setDragedLayoutElement(null);
			}
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			// TODO Auto-generated method stub

		}

	}

	protected class ThresholdBar extends GLElementContainer
			implements
			org.caleydo.core.view.opengl.layout2.basic.GLSlider.ISelectionCallback {

		boolean isHorizontal;
		GLSlider slider;
		float globalMaxThreshold;
		float localMaxSliderValue;
		float localMinSliderValue;

		protected ThresholdBar(boolean layout) {
			super(layout ? GLLayouts.flowHorizontal(1) : GLLayouts
					.flowVertical(1));
			isHorizontal = layout;
			// move to the top
			setzDelta(+0.5f);

			// create buttons
			createButtons();

			setSize(Float.NaN, 20);

			// define the animation used to move this element
			if (isHorizontal) {
				this.setLayoutData(new MoveTransitions.MoveTransitionBase(
						Transitions.LINEAR, Transitions.LINEAR, Transitions.NO,
						Transitions.LINEAR));
			} else {
				this.setLayoutData(new MoveTransitions.MoveTransitionBase(
						Transitions.LINEAR, Transitions.LINEAR,
						Transitions.LINEAR, Transitions.NO));
			}
		}

		protected void createButtons() {
			this.remove(slider);
			float max = localMaxSliderValue > localMinSliderValue ? localMaxSliderValue
					: localMinSliderValue;
			this.slider = new GLSlider(0, max, max / 2);
			// slider.setzDelta(-0.5f);
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
			if (isHorizontal)
				dimThreshold = value;
			else
				recThreshold = value;
			rebuildMyData(false);

		}

		protected void updateSliders(double maxValue, double minValue) {
			localMaxSliderValue = (float) maxValue;
			localMinSliderValue = (float) minValue;
			// createButtons();
			relayout();
		}

		@ListenTo
		public void listenTo(MaxThresholdChangeEvent event) {
			globalMaxThreshold = (float) (isHorizontal ? event
					.getDimThreshold() : event.getRecThreshold());
			createButtons();
		}

		@ListenTo
		public void listenTo(LZThresholdChangeEvent event) {
			if (event.isGlobalEvent()) {
				ignoreNextChange = true;
				slider.setValue(isHorizontal ? event.getDimensionThreshold()
						: event.getRecordThreshold());
			}
		}
	}

	protected class ToolBar extends GLElementContainer implements
			ISelectionCallback {

		GLButton hide, sorting, enlarge, smaller, focus, lock;
		SortingType sortingButtonCaption = SortingType.probabilitySorting;

		public ToolBar() {
			super(GLLayouts.flowVertical(6));
			setzDelta(-0.1f);
			createButtons();
			setSize(Float.NaN, 20);
			this.setLayoutData(new MoveTransitions.MoveTransitionBase(
					Transitions.LINEAR, Transitions.NO, Transitions.LINEAR,
					Transitions.LINEAR));
		}

		protected void createButtons() {
			hide = new GLButton();
			hide.setRenderer(GLRenderers
					.fillImage("resources/icons/dialog_close.png"));
			hide.setTooltip("Close");
			hide.setSize(16, Float.NaN);
			hide.setCallback(this);
			this.add(hide);
			sorting = new GLButton();
			sorting.setRenderer(GLRenderers
					.drawText(
							sortingButtonCaption == SortingType.probabilitySorting ? "P"
									: "B", VAlign.CENTER));
			sorting.setSize(16, Float.NaN);
			sorting.setTooltip("Change sorting");
			sorting.setCallback(this);
			this.add(sorting);
			focus = new GLButton();
			focus.setRenderer(GLRenderers
					.fillImage("resources/icons/target.png"));
			focus.setSize(16, Float.NaN);
			focus.setTooltip("Focus this Cluster");
			focus.setCallback(this);
			this.add(focus);
			lock = new GLButton();
			lock.setTooltip("Lock this cluster. It will not recieve threshold updates.");
			lock.setSize(16, Float.NaN);
			lock.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h,
						GLElement parent) {
					if (isLocked)
						g.fillImage("resources/icons/lock_open.png", 0, 0, w, h);
					else
						g.fillImage("resources/icons/lock.png", 0, 0, w, h);
				}

			});
			lock.setCallback(this);
			this.add(lock);
			enlarge = new GLButton();
			enlarge.setSize(16, Float.NaN);
			enlarge.setTooltip("Enlarge");
			enlarge.setRenderer(GLRenderers
					.fillImage("resources/icons/zoom_in.png"));
			enlarge.setCallback(this);
			this.add(enlarge);
			smaller = new GLButton();
			smaller.setTooltip("Reduce");
			smaller.setSize(16, Float.NaN);
			smaller.setRenderer(GLRenderers
					.fillImage("resources/icons/zoom_out.png"));
			smaller.setCallback(this);
			this.add(smaller);

		}

		void setSortingCaption(SortingType caption) {
			sortingButtonCaption = caption;
			sorting.setRenderer(GLRenderers
					.drawText(
							sortingButtonCaption == SortingType.probabilitySorting ? "P"
									: "B", VAlign.CENTER));
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			if (button == hide) {
				hideThisCluster();
			} else if (button == sorting) {
				setSortingCaption(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
				sort(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
			} else if (button == enlarge) {
				upscale();
				content.setzDelta(1f);
				resize();
			} else if (button == smaller) {
				resetScaleFactor();
				content.setzDelta(0.5f);
				resize();
				EventPublisher.trigger(new FocusChangeEvent(null));
			} else if (button == focus) {
				focusThisCluster();
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

	protected void resetScaleFactor() {
		scaleFactor = 1;
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
		setSize((float) (dimSize * scaleFactor),
				(float) (recSize * scaleFactor));
		relayout();
	}

	protected void focusThisCluster() {
		this.isFocused = !this.isFocused;
		HeatMapElement hm = (HeatMapElement) content;
		if (isFocused) {
			scaleFactor = scaleFactor >= 4 ? 4 : 3;
			hm.setDimensionLabels(EShowLabels.RIGHT);
			hm.setRecordLabels(EShowLabels.RIGHT);
			hm.setRecordSpacingStrategy(SpacingStrategies.fishEye(18));
			hm.setDimensionSpacingStrategy(SpacingStrategies.fishEye(18));
			resize();
			EventPublisher.trigger(new FocusChangeEvent(this));
		} else {
			resetScaleFactor();
			hm.setDimensionLabels(EShowLabels.NONE);
			hm.setRecordLabels(EShowLabels.NONE);
			hm.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
			hm.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
			resize();
			EventPublisher.trigger(new FocusChangeEvent(null));
			mouseOut();
		}
		repaintAll();
	}

	private void hideThisCluster() {
		isHidden = true;
		setVisibility();
		isHovered = false;
		relayout();
		allClusters.setHooveredElement(null);
		EventPublisher.trigger(new ClusterGetsHiddenEvent(getID()));
		EventPublisher.trigger(new MouseOverClusterEvent(this, false));
		repaintAll();
	}

	@ListenTo
	private void listenTo(FocusChangeEvent e) {
		if (e.getSender() == this)
			return;
		if (!isFocused)
			return;
		resetScaleFactor();
		resize();
		this.isFocused = false;
	}

	@ListenTo
	private void listenTo(UnhidingClustersEvent event) {
		isHidden = false;
		setVisibility();
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
		ClusterElement hoveredElement = event.getElement();
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
	private void listenTo(LZThresholdChangeEvent event) {
		if (getID().contains("Special")) {
			System.out.println("Threshold Change");
		}
		if (!event.isGlobalEvent()) {
			return;
		}
		recThreshold = event.getRecordThreshold();
		dimThreshold = event.getDimensionThreshold();
		setOnlyShowXElements = event.isFixedClusterCount();
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

	public void setData(List<Integer> dimIndices, List<Integer> recIndices,
			boolean setXElements, String id, int bcNr, double maxDim,
			double maxRec, double minDim, double minRec) {
		setLabel(id);
		if (maxDim >= 0 && maxRec >= 0) {
			dimThreshBar.updateSliders(maxDim, minDim);
			recThreshBar.updateSliders(maxRec, minRec);
		}
		dimProbabilitySorting = new ArrayList<Integer>(dimIndices);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		this.setOnlyShowXElements = setXElements;
		setHasContent(dimIndices, recIndices);
		setVisibility();
	}

	protected void setLabel(String id) {
		data.setLabel(id);
	}

	protected void setHasContent(List<Integer> dimIndices,
			List<Integer> recIndices) {
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
		recreateVirtualArrays(new ArrayList<Integer>(finalDimSorting),
				new ArrayList<Integer>(finalRecSorting));
		fireTablePerspectiveChanged();
	}

	private void probabilitySorting() {
		sortingType = SortingType.probabilitySorting;
		recreateVirtualArrays(dimProbabilitySorting, recProbabilitySorting);
		fireTablePerspectiveChanged();
	}

	public boolean isContinuousRecSequenze(List<Integer> overlap) {
		List<Integer> recordArray = getRecordVirtualArray().getIDs();
		int index = 0;
		for (Integer i : recordArray) {
			if (overlap.contains(i))
				break;
			index++;
		}
		if (index > recordArray.size() - overlap.size())
			return false;
		int done = 1;
		for (Integer i : recordArray.subList(index, recordArray.size() - 1)) {
			if (done++ >= overlap.size())
				break;
			if (!overlap.contains(i))
				return false;
		}
		return true;
	}

	public boolean isContinuousDimSequenze(List<Integer> overlap) {
		List<Integer> recordArray = getDimensionVirtualArray().getIDs();
		int index = 0;
		for (Integer i : recordArray) {
			if (overlap.contains(i))
				break;
			index++;
		}
		if (index > recordArray.size() - overlap.size())
			return false;
		int done = 1;
		for (Integer i : recordArray.subList(index, recordArray.size() - 1)) {
			if (done++ >= overlap.size())
				break;
			if (!overlap.contains(i))
				return false;
		}
		return true;
	}

	public int getDimIndexOf(int value) {
		return getDimensionVirtualArray().indexOf(value);
	}

	public int getRecIndexOf(int value) {
		return getRecordVirtualArray().indexOf(value);
	}

	protected void rebuildMyData(boolean isGlobal) {
		if (isLocked)
			return;
		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		Future<ScanResult> recList = null, dimList = null;
		ASortingStrategy strategy = new ProbabilityStrategy(L, bcNr);
		recList = executor.submit(new ScanProbabilityMatrix(recThreshold, L,
				bcNr, strategy));
		strategy = new ProbabilityStrategy(Z, bcNr);
		dimList = executor.submit(new ScanProbabilityMatrix(dimThreshold, Z,
				bcNr, strategy));
		List<Integer> dimIndices = null, recIndices = null;
		try {
			dimIndices = dimList.get().getIndices();
			recIndices = recList.get().getIndices();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		setData(dimIndices, recIndices, setOnlyShowXElements, getID(), bcNr,
				-1, -1, -1, -1);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		if (!isGlobal)
			EventPublisher.trigger(new MouseOverClusterEvent(this, true));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal,
				dimBandsEnabled, recBandsEnabled));
		EventPublisher.trigger(new CreateBandsEvent(this));

	}
}
