/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.data.SelectionUpdateEvent;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;

/**
 * @author Michael Gillhofer
 *
 */
public abstract class BandElement extends PickableGLElement {

	private static final float HIGH_OPACITY_FACTPOR = 1;
	private static final float LOW_OPACITY_FACTOR = 0.15f;
	private static final float OPACITY_CHANGE_INTERVAL = 10f;

	private static final float DEFAULT_Z_DELTA = -4;
	private static final float HOVERED_BACKGROUND_Z_DELTA = -3;
	private static final float SELECTED_Z_DELTA = -2;
	private static final float HOVERED_Z_DELTA = -1;

	protected ClusterElement first, second;
	protected List<Integer> overlap, sharedElementsWithSelection, sharedElementsWithHover;

	protected IDType idType;
	protected String dataDomainID;
	protected final SelectionType selectionType;
	protected final SelectionManager selectionManager;
	protected AllBandsElement root;

	protected BandFactory secondMergeArea, bandFactory;
	protected Map<List<Integer>, Band> splittedBands, nonSplittedBands;
	protected Map<Integer, Band> splines;
	protected List<List<Integer>> firstSubIndices, secondSubIndices;
	protected Color highlightColor, hoveredColor, defaultColor;
	protected boolean isMouseOver = false;
	protected boolean isAnyClusterHovered = false;

	protected PickingPool pickingPool;
	protected IPickingListener pickingListener;
	private int currSelectedSplineID = -1;

	private float opacityFactor = 1;

	protected BandElement(GLElement first, GLElement second, List<Integer> list, SelectionManager selectionManager,
			AllBandsElement root, float[] defaultColor) {
		this.first = (ClusterElement) first;
		this.second = (ClusterElement) second;
		this.overlap = list;
		this.root = root;
		this.selectionManager = selectionManager;
		this.defaultColor = new Color(defaultColor);
		selectionType = selectionManager.getSelectionType();
		sharedElementsWithSelection = new ArrayList<>();
		highlightColor = selectionType.getColor();
		hoveredColor = SelectionType.MOUSE_OVER.getColor();
		sharedElementsWithHover = new ArrayList<>();
		setZDeltaAccordingToState();
		initBand();
	}

	@Override
	protected void init(IGLElementContext context) {
		pickingListener = new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		};

		pickingPool = new PickingPool(context, pickingListener);
		super.init(context);
	}

	@Override
	protected void takeDown() {
		pickingPool.clear();
		super.takeDown();
	}

	public List<Integer> getOverlap() {
		return overlap;
	}

	private void setZDeltaAccordingToState() {
		float oldZ = getzDelta();
		float z = DEFAULT_Z_DELTA;
		if (hasSharedElementsWithHoveredBand())
			z = HOVERED_BACKGROUND_Z_DELTA;
		if (hasSharedElementsWithSelectedBand())
			z = SELECTED_Z_DELTA;
		if (isMouseOver)
			z = HOVERED_Z_DELTA;
		if (oldZ != z) {
			setzDelta(z);
			root.triggerResort();
		}
	}

	@Override
	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case CLICKED:
			onClicked(pick);
			break;
		case MOUSE_OUT:
			onMouseOut(pick);
			break;
		case MOUSE_OVER:
			onMouseOver(pick);
			break;
		default:
			break;
		}
		setZDeltaAccordingToState();
	}

	protected abstract void initBand();

	private int accu; // for animating the opacity fading

	@Override
	public void layout(int deltaTimeMs) {
		if (deltaTimeMs + accu > OPACITY_CHANGE_INTERVAL) {

			if (opacityFactor < curOpacityFactor)
				curOpacityFactor -= 0.03;
			else if (opacityFactor > curOpacityFactor)
				curOpacityFactor += 0.03;
			repaint();
			accu = 0;
		} else
			accu += deltaTimeMs;
		super.layout(deltaTimeMs);

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		Color bandColor;
		if (isVisible()) {
			if (hasSharedElementsWithSelectedBand())
				bandColor = highlightColor;
			else if (hasSharedElementsWithHoveredBand())
				bandColor = hoveredColor;
			else
				bandColor = defaultColor;
			if (isMouseOver) {
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.8f * curOpacityFactor);
				for (Band b : splittedBands.values()) {
					g.drawPath(b);
				}
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.5f * curOpacityFactor);
				for (Band b : splittedBands.values()) {
					g.fillPolygon(b);
				}

				g.color(bandColor.r, bandColor.g, bandColor.b, 0.25f * curOpacityFactor);
				Band currSelectedSpline = splines.get(currSelectedSplineID - 1);
				for (Band b : splines.values()) {
					if (b == currSelectedSpline)
						continue;
					g.fillPolygon(b);
				}
				if (currSelectedSpline != null) {
					g.color(0, 0, 0, 0.85f * curOpacityFactor).lineWidth(2);
					g.fillPolygon(currSelectedSpline);
					g.lineWidth(1);
				}
			} else {
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.8f * curOpacityFactor);
				for (Band b : nonSplittedBands.values()) {
					g.drawPath(b);
				}
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.5f * curOpacityFactor);
				for (Band b : nonSplittedBands.values()) {
					g.fillPolygon(b);
				}
			}
		}
	}

	protected boolean isVisible() {
		return first.isVisible() && second.isVisible() && overlap != null;
	}

	protected boolean hasSharedElementsWithSelectedBand() {
		return sharedElementsWithSelection.size() != 0;
	}

	protected boolean hasSharedElementsWithHoveredBand() {
		return sharedElementsWithHover.size() != 0;
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (isVisible() && !isAnyClusterHovered) {
			g.color(defaultColor);
			if (isMouseOver == true) {
				for (Band b : splittedBands.values())
					g.fillPolygon(b);
				g.incZ();
				for (Integer elementIndex : splines.keySet()) {
					g.pushName(pickingPool.get(elementIndex + 1));
					g.fillPolygon(splines.get(elementIndex));
					g.popName();
				}
				g.decZ();
			} else {
				if (nonSplittedBands != null)
					for (Band b : nonSplittedBands.values())
						g.fillPolygon(b);
			}
		}
	}

	@Override
	protected void onClicked(Pick pick) {
		if (pick.getObjectID() != 0)
			return;
		if (hasSharedElementsWithSelectedBand()) {
			sharedElementsWithSelection = new ArrayList<>();
			root.setSelection(null);
		} else {
			sharedElementsWithSelection = new ArrayList<>(overlap);
			root.setSelection(this);
		}
		selectElement();
	}

	protected void selectElement() {
		if (selectionManager == null)
			return;
		selectionManager.clearSelection(selectionType);
		if (hasSharedElementsWithSelectedBand()) {
			selectionManager.addToType(selectionType, overlap);
		}
		fireSelectionChanged();
	}

	public void deselect() {
		sharedElementsWithSelection = new ArrayList<>();
		updateSelection();
		setZDeltaAccordingToState();
	}

	@Override
	protected void onMouseOver(Pick pick) {
		isMouseOver = true;
		currSelectedSplineID = pick.getObjectID();
		hoverElement();
		repaintAll();
	}

	@Override
	protected void onMouseOut(Pick pick) {
		isMouseOver = false;
		currSelectedSplineID = -1;
		hoverElement();
		repaintAll();
	}

	protected void hoverElement() {
		if (selectionManager == null)
			return;
		selectionManager.clearSelection(SelectionType.MOUSE_OVER);
		if (isMouseOver) {
			selectionManager.addToType(SelectionType.MOUSE_OVER, overlap);
		}
		fireSelectionChanged();
	}

	protected void recalculateSelection() {
		if (root.getSelection() != this)
			return;
		sharedElementsWithSelection = new ArrayList<>(overlap);
		selectElement();
	}

	public abstract void updateStructure();

	public abstract void updatePosition();

	public abstract void updateSelection();

	protected abstract void fireSelectionChanged();

	@ListenTo
	public void listenToSelectionEvent(SelectionUpdateEvent e) {
		sharedElementsWithHover = new ArrayList<>(selectionManager.getElements(SelectionType.MOUSE_OVER));
		sharedElementsWithHover.retainAll(overlap);
		sharedElementsWithSelection = new ArrayList<>(selectionManager.getElements(selectionType));
		sharedElementsWithSelection.retainAll(overlap);
		updateSelection();
		setZDeltaAccordingToState();
	}

	protected float curOpacityFactor = 1;

	@ListenTo
	public void listenTo(MouseOverClusterEvent event) {
		isAnyClusterHovered = event.isMouseOver();
		if (event.getSender() == first || event.getSender() == second)
			return;
		else if (event.isMouseOver())
			opacityFactor = LOW_OPACITY_FACTOR;
		else
			opacityFactor = HIGH_OPACITY_FACTPOR;
		setZDeltaAccordingToState();
	}

	protected Pair<Vec3f, Vec3f> pair(float x1, float y1, float x2, float y2) {
		Vec3f _1 = new Vec3f(x1, y1, 0);
		Vec3f _2 = new Vec3f(x2, y2, 0);
		return Pair.make(_1, _2);
	}

	public enum BandType {
		recordBand, dimensionBand;
	}

}
