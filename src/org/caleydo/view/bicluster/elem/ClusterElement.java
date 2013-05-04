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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.swing.Timer;

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
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
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
import org.caleydo.view.bicluster.event.ClusterHoveredElement;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MaxThresholdChangeEvent;
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
import org.caleydo.view.heatmap.v2.IBlockColorer;

/**
 * e.g. a class for representing a cluster
 * 
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public class ClusterElement extends AnimatedGLElementContainer implements
		IBlockColorer, IGLLayout {
	private final TablePerspective data;
	private final TablePerspective x;
	private final TablePerspective l;
	private final TablePerspective z;
	private final AllClustersElement allClusters;
	private final ExecutorService executor;
	private float recThreshold = 0.08f;
	private float dimThreshold = 4;
	private Vec2d attForce = new Vec2d(0, 0);
	private Vec2d repForce = new Vec2d(0, 0);
	private Vec2d frameForce = new Vec2d(0, 0);
	private boolean isDragged = false;
	private boolean isHoovered = false;
	private boolean isHidden = false;
	private boolean hasContent = false;

	private Map<GLElement, List<Integer>> dimOverlap;
	private Map<GLElement, List<Integer>> recOverlap;

	private SortingType sortingType = SortingType.probabilitySorting;
	private List<Integer> dimProbabilitySorting;
	private List<Integer> recProbabilitySorting;

	private boolean setOnlyShowXElements;
	private int bcNr;
	private ToolBar toolBar;
	private HeaderBar headerBar;
	private ThresholdBar dimThreshBar;
	private ThresholdBar recThreshBar;
	private GLElement heatmap;
	private float opacityfactor = 1;
	private float highOpacityFactor = 1;
	private float lowOpacityFactor = 0.2f;
	private float curOpacityFactor = 1f;
	private float opacityDelta = 0.02f;

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
		heatmap = new HeatMapElement(data, this, EDetailLevel.HIGH);
		heatmap.setzDelta(0.5f);
		// setzDelta(f);
		this.add(heatmap);
		setVisibility(EVisibility.PICKABLE);
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
		this.setLayoutData(MoveTransitions.MOVE_AND_GROW_LINEAR);
	}

	Timer timer = new Timer(10, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (opacityfactor < curOpacityFactor)
				curOpacityFactor -= opacityDelta;
			else if (opacityfactor > curOpacityFactor)
				curOpacityFactor += opacityDelta;
			else
				timer.stop();
			relayout();
			repaint();
		}
	});

	@Override
	public Color apply(int recordID, int dimensionID,
			ATableBasedDataDomain dataDomain, boolean deSelected) {
		Color color = BasicBlockColorer.INSTANCE.apply(recordID, dimensionID,
				dataDomain, deSelected);

		color.a = (float) (color.a * curOpacityFactor);
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
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		float[] color = { 0, 0, 0, (float) curOpacityFactor };
		float[] highlightedColor = SelectionType.MOUSE_OVER.getColor();
		g.color(color);
		g.textColor(color);
		if (isHoovered) {
			g.color(highlightedColor);
		}
		g.drawRoundedRect(-1, -1, w + 2, h + 2, 1);
		g.drawText(getID(), 0, -16, 70, 12);
		float[] black = { 0, 0, 0, 1 };
		g.textColor(black);
	}

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case DRAGGED:
			if (isDragged == false) {
				allClusters.setDragedLayoutElement(this);
			}
			isDragged = true;
			setLocation(getLocation().x() + pick.getDx(), getLocation().y()
					+ pick.getDy());
			relayoutParent();
			repaintPick();
			break;
		case CLICKED:
			pick.setDoDragging(true);
			break;
		case MOUSE_RELEASED:
			pick.setDoDragging(false);
			break;
		case MOUSE_OVER:
			if (!pick.isAnyDragging()) {
				isHoovered = true;
				allClusters.setHooveredElement(this);
				EventPublisher.trigger(new ClusterHoveredElement(this, true));
				relayout(); // for showing the toolbar
			}
			break;
		case MOUSE_OUT:
			if (isHoovered) {
				isHoovered = false;
				allClusters.setHooveredElement(null);
				relayout(); // for showing the toolbar
				EventPublisher.trigger(new ClusterHoveredElement(this, false));
			}
			break;
		default:
			isDragged = false;
			allClusters.setDragedLayoutElement(null);
		}
	}

	private void recreateVirtualArrays(List<Integer> dimIndices,
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


	void calculateOverlap() {
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
			List<Integer> eIndizes = new ArrayList<Integer>(myDimIndizes);

			eIndizes.retainAll(e.getDimensionVirtualArray().getIDs());
			dimOverlap.put(element, eIndizes);
			dimensionOverlapSize += eIndizes.size();

			eIndizes = new ArrayList<Integer>(myRecIndizes);
			eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
			recOverlap.put(element, eIndizes);
			recordOverlapSize += eIndizes.size();
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

	private void fireTablePerspectiveChanged() {
		EventPublisher.trigger(new RecordVAUpdateEvent(data.getDataDomain()
				.getDataDomainID(), data.getRecordPerspective()
				.getPerspectiveID(), this));
		EventPublisher.trigger(new DimensionVAUpdateEvent(data.getDataDomain()
				.getDataDomainID(), data.getDimensionPerspective()
				.getPerspectiveID(), this));
		repaintAll();
	}

	private VirtualArray getDimensionVirtualArray() {
		return data.getDimensionPerspective().getVirtualArray();
	}

	private VirtualArray getRecordVirtualArray() {
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

	// int overallOverlapSize;
	int dimensionOverlapSize;
	int recordOverlapSize;
	private int dimSize;
	private int recSize;
	private boolean isFocused = false;

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
		if (isHoovered) { // depending whether we are hovered or not, show hide
							// the toolbar's
			toolbar.setBounds(-18, 0, 18, 80);
			headerbar.setBounds(0, -18, w < 55 ? 55 : w + 2, 17);
			dimthreshbar.setBounds(0,-36,w < 55 ? 55 : w + 2,17);
			recthreshbar.setBounds(-36,0,18,80);
			
		} else {
			toolbar.setBounds(0, 0, 0, 0); // hide by setting the width to 0
			headerbar.setBounds(0, 0, w < 50 ? 50 : w, 0);
			dimthreshbar.setBounds(0,0,w < 50 ? 50 : w, 0);
			recthreshbar.setBounds(0,0,0,0);
		}
		IGLLayoutElement content = children.get(4);
		content.setBounds(0, 0, w, h);
	}

	private class HeaderBar extends GLElementContainer implements
			ISelectionCallback {

		public HeaderBar() {
			super(GLLayouts.flowHorizontal(1));

			// move to the top
			setzDelta(0.5f);

			// create buttons
			createButtons();

			setSize(Float.NaN, 20);

			// define the animation used to move this element
			this.setLayoutData(new MoveTransitions.MoveTransitionBase(
					Transitions.NO, Transitions.LINEAR, Transitions.NO,
					Transitions.LINEAR));
		}

		protected void createButtons() {
			GLElement spacer = new GLButton();
			spacer.setzDelta(-0.5f);
			spacer.setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h,
						GLElement parent) {
					g.color(SelectionType.MOUSE_OVER.getColor());
					g.fillRoundedRect(0, 0, w, h, 2);

				}
			});
			this.add(spacer); // spacer
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			// nothing to click here

		}
	}

	private class ThresholdBar extends GLElementContainer
			implements
			org.caleydo.core.view.opengl.layout2.basic.GLSlider.ISelectionCallback {

		boolean isHorizontal;
		GLSlider slider;
		double maxSliderValue;

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
						Transitions.LINEAR, Transitions.LINEAR, Transitions.LINEAR,
						Transitions.NO));
			}
		}

		protected void createButtons() {
//			this.slider = new GLButton();
			this.remove(slider);
			this.slider = new GLSlider(0, (float) maxSliderValue, (float) (maxSliderValue/2));
			slider.setzDelta(-0.5f);
			slider.setCallback(this);
			slider.setSize(Float.NaN, 18);
			this.add(slider);
		}

		@Override
		public void onSelectionChanged(GLSlider slider, float value) {
			// TODO Auto-generated method stub

		}

		@ListenTo
		public void listenTo(MaxThresholdChangeEvent event) {
			maxSliderValue = isHorizontal ? event.getDimThreshold() : event
					.getRecThreshold();
			createButtons();
			relayout();
		}

	}

	/**
	 * a simple toolbar for the {@link ClusterElement}
	 * 
	 * @author Samuel Gratzl
	 * 
	 */
	private class ToolBar extends GLElementContainer implements
			ISelectionCallback {

		GLButton hide, sorting, enlarge, smaller, focus;
		SortingType sortingButtonCaption = SortingType.probabilitySorting;

		public ToolBar() {
			super(GLLayouts.flowVertical(5));

			// move to the top
			setzDelta(-0.1f);

			// create buttons
			createButtons();

			setSize(Float.NaN, 20);

			// define the animation used to move this element
			this.setLayoutData(new MoveTransitions.MoveTransitionBase(
					Transitions.LINEAR, Transitions.NO, Transitions.LINEAR,
					Transitions.LINEAR));
		}

		protected void createButtons() {
			hide = new GLButton();
			hide.setRenderer(GLRenderers
					.fillImage("./resources/icons/dialog_close.png"));
			hide.setTooltip("Close");
			hide.setSize(16, Float.NaN);
			hide.setCallback(this);
			this.add(hide);
			sorting = new GLButton();
			// sorting.setRenderer(GLRenderers.fillRect(java.awt.Color.ORANGE));
			sorting.setRenderer(GLRenderers
					.drawText(
							sortingButtonCaption == SortingType.probabilitySorting ? "P"
									: "B", VAlign.CENTER));
			sorting.setSize(16, Float.NaN);
			sorting.setTooltip("Change sorting");
			sorting.setCallback(this);
			this.add(sorting);
			focus = new GLButton();
			focus.setRenderer(GLRenderers.drawText(" F"));
			focus.setSize(16, Float.NaN);
			focus.setTooltip("Focus this Cluster");
			focus.setCallback(this);
			this.add(focus);
			enlarge = new GLButton();
			enlarge.setSize(16, Float.NaN);
			enlarge.setTooltip("Enlarge");
			enlarge.setRenderer(GLRenderers
					.fillImage("./resources/icons/zoom_in.png"));
			enlarge.setCallback(this);
			this.add(enlarge);
			smaller = new GLButton();
			smaller.setTooltip("Reduce");
			smaller.setSize(16, Float.NaN);
			smaller.setRenderer(GLRenderers
					.fillImage("./resources/icons/zoom_out.png"));
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
				scaleFactor += 0.6;
				heatmap.setzDelta(1f);
				resize();
			} else if (button == smaller) {
				scaleFactor = 1;
				heatmap.setzDelta(0.5f);
				resize();
				EventPublisher.trigger(new FocusChangeEvent(null));
			} else if (button == focus) {
				focusThisCluster();
			}
		}
	}

	public void setClusterSize(int x, int y) {
		dimSize = x;
		recSize = y;
		resize();
	}

	void focusThisCluster() {
		this.isFocused = !this.isFocused;
		if (isFocused) {
			scaleFactor = scaleFactor >= 4 ? 4 : 3;
			resize();
			EventPublisher.trigger(new FocusChangeEvent(this));
		} else {
			scaleFactor = 1;
			resize();
			EventPublisher.trigger(new FocusChangeEvent(null));
		}
		repaintAll();
	}

	@ListenTo
	public void listenTo(FocusChangeEvent e) {
		if (e.getSender() == this)
			return;
		if (!isFocused)
			return;
		scaleFactor = 1;
		resize();
		this.isFocused = false;
	}

	private double scaleFactor = 1;

	private void resize() {
		setSize((float) (dimSize * scaleFactor),
				(float) (recSize * scaleFactor));
		relayout();
	}

	public void hideThisCluster() {
		isHidden = true;
		setVisibility(EVisibility.NONE);
		isHoovered = false;
		allClusters.setHooveredElement(null);
		EventPublisher.trigger(new ClusterGetsHiddenEvent(getID()));
		EventPublisher.trigger(new ClusterHoveredElement(this, false));
	}

	@ListenTo
	public void listenTo(UnhidingClustersEvent event) {
		isHidden = false;
		if (hasContent) {
			setVisibility(EVisibility.PICKABLE);
		}
	}

	@ListenTo
	public void listenTo(SortingChangeEvent e) {
		if (e.getSender() instanceof ClusterElement && e.getSender() == this) {
			// only local change
		} else {
			sort(e.getType());

		}
		toolBar.setSortingCaption(e.getType());
	}

	@ListenTo
	public void listenTo(ClusterHoveredElement event) {
		// System.out.println("hovered:");
		ClusterElement hoveredElement = event.getElement();
		if (hoveredElement == this || getDimOverlap(hoveredElement).size() > 0
				|| getRecOverlap(hoveredElement).size() > 0)
			return;
		else if (event.isMouseOver()) {
			opacityfactor = lowOpacityFactor;

		} else {
			opacityfactor = highOpacityFactor;
		}
		timer.restart();
		relayout();
	}

	/**
	 * @param dimIndices
	 * @param recIndices
	 * @param setXElements
	 * @param id
	 * @param
	 */
	public void setIndices(List<Integer> dimIndices, List<Integer> recIndices,
			boolean setXElements, String id, int bcNr) {
		data.setLabel(id);
		dimProbabilitySorting = new ArrayList<Integer>(dimIndices);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		this.setOnlyShowXElements = setXElements;
		if (dimIndices.size() > 0 && recIndices.size() > 0) {
			hasContent = true;
			if (!isHidden)
				setVisibility(EVisibility.PICKABLE);
			recreateVirtualArrays(dimIndices, recIndices);
		} else {
			setVisibility(EVisibility.NONE);
			hasContent = false;
		}
	}

	private void sort(SortingType type) {
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

		// if (nonEmptyDimBands.size() > 0)
		// System.out.println(nonEmptyDimBands);
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

		// if (nonEmptyRecBands.size() > 0)
		// System.out.println(nonEmptyRecBands);
		BandSorting recConflicts = new BandSorting(nonEmptyRecBands);
		for (Integer i : recConflicts) {
			finalRecSorting.add(i);
		}
		finalRecSorting.addAll(recProbabilitySorting);

		recreateVirtualArrays(new ArrayList<Integer>(finalDimSorting),
				new ArrayList<Integer>(finalRecSorting));
		fireTablePerspectiveChanged();
	}

	/**
	 *
	 */
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

	@ListenTo
	public void listenTo(LZThresholdChangeEvent event) {
		if (!event.isGlobalEvent())
			return;
		if (event.getRecordThreshold() != recThreshold
				|| event.getDimensionThreshold() != dimThreshold
				|| setOnlyShowXElements != event.isFixedClusterCount()) {
			recThreshold = event.getRecordThreshold();
			dimThreshold = event.getDimensionThreshold();
			setOnlyShowXElements = event.isFixedClusterCount();
			rebuildMyData(event.isGlobalEvent());
		}
	}

	private void rebuildMyData(boolean isGlobal) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setIndices(dimIndices, recIndices, setOnlyShowXElements, getID(), bcNr);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal));
		EventPublisher.trigger(new CreateBandsEvent(this));

	}
}
