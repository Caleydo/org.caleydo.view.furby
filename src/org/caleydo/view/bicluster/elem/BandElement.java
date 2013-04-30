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

import gleem.linalg.Vec3f;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

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
import org.caleydo.view.bicluster.event.ClusterHoveredElement;

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

	protected List<Pair<Vec3f, Vec3f>> bandPoints;
	protected Band band;
	protected List<Pair<Vec3f, Vec3f>> highlightPoints;
	protected Band highlightBand;
	private float[] highlightColor;
	private float[] hoveredColor;
	private float[] defaultColor;
	protected boolean hoverd;

	private float opacityFactor = 1;

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
				g.color(bandColor[0], bandColor[1], bandColor[2],
						0.5f * curOpacityFactor);
				g.fillPolygon(band);
			}
			if (highlightOverlap.size() > 0) {
				g.color(highlightColor[0], highlightColor[1],
						highlightColor[2], 0.8f);
				g.drawPath(highlightBand);
				g.color(highlightColor[0], highlightColor[1],
						highlightColor[2], 0.5f);
				g.fillPolygon(highlightBand);
			} else if (hoverOverlap.size() > 0) {
				g.color(hoveredColor[0], hoveredColor[1], hoveredColor[2],
						0.8f);
				g.drawPath(highlightBand);
				g.color(hoveredColor[0], hoveredColor[1], hoveredColor[2],
						0.8f);
				g.fillPolygon(highlightBand);
			}
		}
	}

	/**
	 * @return
	 */
	private boolean isVisible() {
		return getVisibility() == EVisibility.PICKABLE;
	}

	private boolean isHighlighted() {
		return highlightOverlap.size() != 0
				&& highlightOverlap.size() == overlap.size();
	}

	private boolean isHovered() {
		return hoverOverlap.size() != 0
				&& hoverOverlap.size() == overlap.size();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (isVisible()) {
			g.color(defaultColor);
			if (highlightBand != null)
				g.fillPolygon(highlightBand);
			if (band != null)
				g.fillPolygon(band);
			// bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(),
			// bandPoints, false, defaultColor, .5f);
			// bandRenderer
			// .renderComplexBand(GLContext.getCurrentGL().getGL2(),
			// highlightPoints, false, defaultColor, .5f);
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
		// highlightOverlap = new ArrayList<>();
		updatePosition();
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
		// hoverOverlap = new ArrayList<>();
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

	public abstract void updatePosition();

	protected abstract void fireSelectionChanged();

	@ListenTo
	public void listenToSelectionEvent(SelectionUpdateEvent e) {
		hoverOverlap = new ArrayList<>(
				selectionManager.getElements(SelectionType.MOUSE_OVER));
		hoverOverlap.retainAll(overlap);
		highlightOverlap = new ArrayList<>(
				selectionManager.getElements(selectionType));
		highlightOverlap.retainAll(overlap);
		updatePosition();
	}

	private float opacityDelta=0.02f;
	private float curOpacityFactor = 1;

	Timer timer = new Timer(10, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (opacityFactor < curOpacityFactor)
				curOpacityFactor -= opacityDelta;
			else if (opacityFactor > curOpacityFactor)
				curOpacityFactor += opacityDelta;
			else timer.stop();
			relayout();
			repaint();
		}
	});
	
	
	@ListenTo
	public void listenTo(ClusterHoveredElement event) {
		if (event.getElement() == first || event.getElement() == second)
			return;
		else if (event.isMouseOver())
			opacityFactor = 0.15f;
		else
			opacityFactor = 1f;
		timer.restart();
		relayout();
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
