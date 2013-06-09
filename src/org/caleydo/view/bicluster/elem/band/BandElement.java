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
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;

/**
 * @author Michael Gillhofer
 * 
 */
public abstract class BandElement extends PickableGLElement {
	protected ClusterElement first;
	protected ClusterElement second;
	protected List<Integer> overlap;
	protected List<Integer> highlightOverlap;
	protected List<Integer> hoverOverlap;

	/**
	 * @return the overlap, see {@link #overlap}
	 */
	public List<Integer> getOverlap() {
		return overlap;
	}

	protected IDType idType;
	protected String dataDomainID;
	protected SelectionType selectionType;
	protected SelectionManager selectionManager;
	protected AllBandsElement root;

	protected BandMergeArea secondMergeArea, firstMergeArea;
	protected Band band;
	protected Map<List<Integer>, Band> subBands, highlightedSubBands;
	protected List<List<Integer>> firstSubIndices;
	protected List<List<Integer>> secondSubIndices;
	protected List<Pair<Vec3f, Vec3f>> highlightPoints;
	protected Band highlightBand;
	protected float[] highlightColor;
	protected float[] hoveredColor;
	protected float[] defaultColor;
	protected boolean hoverd;
	protected boolean isAnyClusterHovered = false;

	private float opacityFactor = 1;
	private float highOpacityFactor = 1;
	private float lowOpacityFactor = 0.15f;
	private float opacityChangeInterval = 10f;

	protected BandElement(GLElement first, GLElement second,
			List<Integer> list, SelectionManager selectionManager,
			AllBandsElement root, float[] defaultColor) {
		this.first = (ClusterElement) first;
		this.second = (ClusterElement) second;
		this.overlap = list;
		this.root = root;
		this.selectionManager = selectionManager;
		this.defaultColor = defaultColor;
		selectionType = selectionManager.getSelectionType();
		highlightPoints = new ArrayList<>();
		highlightOverlap = new ArrayList<>();
		highlightColor = selectionType.getColor();
		hoveredColor = SelectionType.MOUSE_OVER.getColor();
		hoverOverlap = new ArrayList<>();
		initBand();
	}

	protected abstract void initBand();

	private int accu; // for animating the opacity fading

	@Override
	public void layout(int deltaTimeMs) {
		if (deltaTimeMs + accu > opacityChangeInterval) {

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
		float[] bandColor;
		if (isVisible()) {
			if (isHighlighted())
				bandColor = highlightColor;
			else if (isHovered())
				bandColor = hoveredColor;
			else
				bandColor = defaultColor;
			if (band != null) {
				g.color(bandColor[0], bandColor[1], bandColor[2],
						0.8f * curOpacityFactor);
				g.drawPath(band);
				if (subBands != null)
					for (Band b : subBands.values()) {
						g.drawPath(b);
					}
				g.color(bandColor[0], bandColor[1], bandColor[2],
						0.5f * curOpacityFactor);
				g.fillPolygon(band);
				if (subBands != null)
					for (Band b : subBands.values()) {
						g.fillPolygon(b);
					}
			}
			if (highlightOverlap.size() > 0) {
				g.color(highlightColor[0], highlightColor[1],
						highlightColor[2], 0.8f);
				g.drawPath(highlightBand);
				if (subBands != null)
					for (Band b : highlightedSubBands.values()) {
						g.drawPath(b);
					}
				g.color(highlightColor[0], highlightColor[1],
						highlightColor[2], 0.5f);
				g.fillPolygon(highlightBand);
				if (subBands != null)
					for (Band b : highlightedSubBands.values()) {
						g.fillPolygon(b);
					}
			} else if (hoverOverlap.size() > 0) {
				g.color(hoveredColor[0], hoveredColor[1], hoveredColor[2], 0.8f);
				g.drawPath(highlightBand);
				if (subBands != null)
					for (Band b : highlightedSubBands.values()) {
						g.drawPath(b);
					}
				g.color(hoveredColor[0], hoveredColor[1], hoveredColor[2], 0.8f);
				g.fillPolygon(highlightBand);
				if (subBands != null)
					for (Band b : highlightedSubBands.values()) {
						g.fillPolygon(b);
					}
			}
		}
	}

	protected boolean isVisible() {
		return getVisibility() == EVisibility.PICKABLE;
	}

	protected boolean isHighlighted() {
		return highlightOverlap.size() != 0
				&& highlightOverlap.size() == overlap.size();
	}

	protected boolean isHovered() {
		return hoverOverlap.size() != 0
				&& hoverOverlap.size() == overlap.size();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (isVisible() && !isAnyClusterHovered) {
			g.color(defaultColor);
			if (subBands != null)
				for (Band b : subBands.values())
					g.fillPolygon(b);
			if (highlightBand != null)
				g.fillPolygon(highlightBand);
			if (band != null)
				g.fillPolygon(band);
		}
	}

	@Override
	protected void onClicked(Pick pick) {
		if (isHighlighted()) {
			highlightOverlap = new ArrayList<>();
			root.setSelection(null);
		} else {
			highlightOverlap = new ArrayList<>(overlap);
			root.setSelection(this);
		}
		selectElement();
		super.onClicked(pick);
	}

	protected void selectElement() {
		if (selectionManager == null)
			return;
		selectionManager.clearSelection(selectionType);
		if (isHighlighted()) {
			selectionManager.addToType(selectionType, overlap);
		}
		fireSelectionChanged();
		relayout();
	}

	public void deselect() {
		highlightOverlap = new ArrayList<>();
		updateSelection();
		repaint();
	}

	@Override
	protected void onMouseOver(Pick pick) {
		hoverd = true;
		hoverElement();
		super.onMouseOver(pick);
	}

	@Override
	protected void onMouseOut(Pick pick) {
		hoverd = false;
		hoverElement();
		repaint();
		super.onMouseOut(pick);
	}

	protected void hoverElement() {
		if (selectionManager == null)
			return;
		selectionManager.clearSelection(SelectionType.MOUSE_OVER);
		if (hoverd) {
			selectionManager.addToType(SelectionType.MOUSE_OVER, overlap);
		}
		fireSelectionChanged();
		relayout();
	}

	protected void recalculateSelection() {
		if (root.getSelection() != this)
			return;
		highlightOverlap = new ArrayList<>(overlap);
		selectElement();
	}

	public abstract void updateStructure();

	public abstract void updatePosition();

	public abstract void updateSelection();

	protected abstract void fireSelectionChanged();

	@ListenTo
	public void listenToSelectionEvent(SelectionUpdateEvent e) {
		hoverOverlap = new ArrayList<>(
				selectionManager.getElements(SelectionType.MOUSE_OVER));
		hoverOverlap.retainAll(overlap);
		highlightOverlap = new ArrayList<>(
				selectionManager.getElements(selectionType));
		highlightOverlap.retainAll(overlap);
		updateSelection();
	}

	protected float curOpacityFactor = 1;

	@ListenTo
	public void listenTo(MouseOverClusterEvent event) {
		isAnyClusterHovered = event.isMouseOver();
		if (event.getElement() == first || event.getElement() == second)
			return;
		else if (event.isMouseOver())
			opacityFactor = lowOpacityFactor;
		else
			opacityFactor = highOpacityFactor;
		repaintAll();
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
