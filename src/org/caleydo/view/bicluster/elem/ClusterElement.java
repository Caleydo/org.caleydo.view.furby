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

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
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
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.ClusterHoveredElement;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.event.UnhidingClustersEvent;
import org.caleydo.view.bicluster.sorting.BandSorting;
import org.caleydo.view.bicluster.util.Vec2d;
import org.caleydo.view.heatmap.v2.BasicBlockColorer;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.IBlockColorer;

/**
 * e.g. a class for representing a cluster
 * 
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class ClusterElement extends AnimatedGLElementContainer implements
		IBlockColorer, IGLLayout {
	private final TablePerspective data;
	private final AllClustersElement root;
	private Vec2d attForce = new Vec2d(0, 0);
	private Vec2d repForce = new Vec2d(0, 0);
	private Vec2d frameForce = new Vec2d(0, 0);
	private boolean isDragged = false;
	private boolean isHoovered = false;
	private boolean isHidden = false;
	private boolean hasContent = false;
	private final TablePerspective x;

	private Map<GLElement, List<Integer>> dimOverlap;
	private Map<GLElement, List<Integer>> recOverlap;

	private SortingType sortingType = SortingType.probabilitySorting;
	private List<Integer> dimProbabilitySorting;
	private List<Integer> recProbabilitySorting;

	private boolean setOnlyShowXElements;
	private int bcNr;
	private ToolBar toolBar;

	// 0 Prob
	// 1 value

	public ClusterElement(TablePerspective data, TablePerspective x,
			AllClustersElement root) {
		setLayout(this);
		this.data = data;
		this.root = root;
		this.x = x;
		toolBar = new ToolBar();
		this.add(toolBar); // add a element toolbar
		this.add(new HeatMapElement(data, this, EDetailLevel.HIGH));

		setVisibility(EVisibility.PICKABLE);
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		// if (isHidden) return;
		IGLLayoutElement toolbar = children.get(0);
		if (isHoovered) { // depending whether we are hovered or not, show hide
							// the toolbar
			toolbar.setBounds(0, -16, w, 16);
		} else {
			toolbar.setBounds(0, 0, w, 0); // hide by setting the height to 0
		}
		IGLLayoutElement content = children.get(1);
		content.setBounds(0, 0, w, h);
	}

	@Override
	public Color apply(int recordID, int dimensionID,
			ATableBasedDataDomain dataDomain, boolean deSelected) {
		// TODO custom implementation with alpha values or something like that
		Color color = BasicBlockColorer.INSTANCE.apply(recordID, dimensionID,
				dataDomain, deSelected);

		color.a = (float) (color.a * opacityfactor);
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
		// if (isDragged) {
		// g.color(Colors.RED);
		// g.fillRect(0, 0, w, h);
		// g.color(Colors.BLACK);
		// }
		g.drawText(getID(), 0, -15, 70, 12);
	}

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case DRAGGED:
			if (isDragged == false) {
				root.setDragedLayoutElement(this);
			}
			root.resetDamping();
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
				root.setHooveredElement(this);
				EventPublisher.trigger(new ClusterHoveredElement(this, true));
				relayout(); // for showing the toolbar
			}
			break;
		case MOUSE_OUT:
			if (isHoovered) {
				isHoovered = false;
				root.setHooveredElement(null);
				relayout(); // for showing the toolbar
				EventPublisher.trigger(new ClusterHoveredElement(this, false));
			}
			break;
		default:
			isDragged = false;
			root.setDragedLayoutElement(null);
		}
	}

	private void recreateVirtualArrays(List<Integer> dimIndices,
			List<Integer> recIndices) {
		VirtualArray dimArray = getDimensionVirtualArray();
		VirtualArray recArray = getRecordVirtualArray();
		dimArray.clear();
		int count = 0;
		for (Integer i : dimIndices) {
			if (setOnlyShowXElements && root.getFixedElementsCount() <= count)
				break;
			dimArray.append(i);
			count++;
		}
		count = 0;
		recArray.clear();
		for (Integer i : recIndices) {
			if (setOnlyShowXElements && root.getFixedElementsCount() <= count)
				break;
			recArray.append(i);
			count++;
		}
	}

	/**
	 *
	 */
	private void calculateOverlap() {
		dimOverlap = new HashMap<>();
		recOverlap = new HashMap<>();
		List<Integer> myDimIndizes = getDimensionVirtualArray().getIDs();
		List<Integer> myRecIndizes = getRecordVirtualArray().getIDs();
		// overallOverlapSize = 0;
		dimensionOverlapSize = 0;
		recordOverlapSize = 0;
		for (GLElement element : root.asList()) {
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
	}

	/**
	 * @return the force, see {@link #attForce}
	 */
	public Vec2d getAttForce() {
		return attForce;
	}

	/**
	 * @param force
	 *            setter, see {@link force}
	 */
	public void setAttForce(Vec2d force) {

		this.attForce = force;
	}

	/**
	 * @param force
	 *            setter, see {@link force}
	 */
	public void setRepForce(Vec2d force) {

		this.repForce = force;
	}

	/**
	 * @return the force, see {@link #attForce}
	 */
	public Vec2d getRepForce() {
		return repForce;
	}

	public Vec2d getFrameForce() {

		return frameForce;
	}

	/**
	 * @param frameForce
	 *            setter, see {@link centerForce}
	 */
	public void setFrameForce(Vec2d frameForce) {
		this.frameForce = frameForce;
	}

	/**
	 * 
	 * @param dimensionName
	 * @param recordName
	 */

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

	/**
	 * @return the isDraged, see {@link #isDragged}
	 */
	public boolean isDragged() {
		return isDragged;
	}

	/**
	 * @return the isDrawn, see {@link #isVisible}
	 */
	public boolean isVisible() {
		return getVisibility().doRender();
	}

	public List<Integer> getDimOverlap(GLElement jElement) {
		return dimOverlap.get(jElement);
	}

	/**
	 * @return the yOverlap, see {@link #recOverlap}
	 */
	public List<Integer> getRecOverlap(GLElement jElement) {
		return recOverlap.get(jElement);
	}

	// int overallOverlapSize;
	int dimensionOverlapSize;
	int recordOverlapSize;
	private double opacityfactor = 1;

	public int getDimensionOverlapSize() {
		return dimensionOverlapSize;
	}

	public int getRecordOverlapSize() {
		return recordOverlapSize;
	}

	/**
	 * @return
	 */
	protected IGLLayoutElement getIGLayoutElement() {
		return GLElementAccessor.asLayoutElement(this);
	}

	/**
	 * a simple toolbar for the {@link ClusterElement}
	 * 
	 * @author Samuel Gratzl
	 * 
	 */
	private class ToolBar extends GLElementContainer implements
			ISelectionCallback {

		GLButton hide, sorting;
		SortingType sortingButtonCaption = SortingType.probabilitySorting;

		public ToolBar() {
			super(GLLayouts.flowHorizontal(2));

			// move to the top
			setzDelta(0.5f);

			// create buttons
			createButtons();

			setSize(Float.NaN, 16);

			// define the animation used to move this element
			this.setLayoutData(new MoveTransitions.MoveTransitionBase(
					Transitions.NO, Transitions.LINEAR, Transitions.NO,
					Transitions.LINEAR));
		}

		protected void createButtons() {
			GLElement spacer = new GLButton();
			this.add(spacer); // spacer
			sorting = new GLButton();
			sorting.setRenderer(GLRenderers.fillRect(java.awt.Color.ORANGE));
			sorting.setRenderer(GLRenderers
					.drawText(
							sortingButtonCaption == SortingType.probabilitySorting ? "P"
									: "B", VAlign.CENTER));
			sorting.setSize(16, Float.NaN);
			sorting.setCallback(this);
			this.add(sorting);
			hide = new GLButton();
			hide.setRenderer(GLRenderers.fillRect(java.awt.Color.ORANGE));
			hide.setRenderer(GLRenderers.drawText("X", VAlign.CENTER));
			hide.setSize(16, Float.NaN);
			hide.setCallback(this);
			this.add(hide);
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
				// for (GLElement i : this) {
				// i.setVisibility(EVisibility.NONE);
				// }
				hideThisElement();
			} else if (button == sorting) {
				setSortingCaption(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
				sort(sortingType == SortingType.probabilitySorting ? SortingType.bandSorting
						: SortingType.probabilitySorting);
			}
		}
	}

	public void hideThisElement() {
		isHidden = true;
		setVisibility(EVisibility.NONE);
		isHoovered = false;
		root.setHooveredElement(null);
		EventPublisher.trigger(new ClusterGetsHiddenEvent(getID()));
	}

	@ListenTo
	public void listenTo(UnhidingClustersEvent event) {
		isHidden = false;
		if (hasContent) {
			setVisibility(EVisibility.PICKABLE);
			// for (GLElement i : this) {
			// i.setVisibility(EVisibility.PICKABLE);
			// }
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
		if (event.getElement() == this
				|| getDimOverlap(event.getElement()).size() > 0
				|| getRecOverlap(event.getElement()).size() > 0)
			return;
		else if (event.isMouseOver())
			opacityfactor = 0.3;
		else
			opacityfactor = 1;
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
		calculateOverlap();
		if (getVisibility() == EVisibility.PICKABLE)
			sort(sortingType);
		fireTablePerspectiveChanged();
		// setSize(200, 200);
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

}
