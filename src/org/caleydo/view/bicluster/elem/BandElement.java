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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLContext;

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
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;

/**
 * @author Michael Gillhofer
 *
 */
public abstract class BandElement extends PickableGLElement {

	protected static ConnectionBandRenderer bandRenderer = new ConnectionBandRenderer();

	{
		bandRenderer.init(GLContext.getCurrentGL().getGL2());
	}

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

	protected List<Pair<Point2D, Point2D>> bandPoints;
	protected List<Pair<Point2D, Point2D>> highlightPoints;
	private float[] highlightColor;
	private float[] hoveredColor;
	private float[] defaultColor;
	protected boolean hoverd;

	protected BandElement(GLElement first, GLElement second, List<Integer> list, SelectionManager selectionManager,
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
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), bandPoints, false, bandColor, .5f);
			if (highlightOverlap.size() > 0) {
				bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, true,
						highlightColor, .5f);
			} else if (hoverOverlap.size() > 0) {
				bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, true, hoveredColor,
						.5f);
			}
		}
		// super.renderImpl(g, w, h);
		// System.out.println(first.getId() + "/" + second.getId());
	}

	/**
	 * @return
	 */
	private boolean isVisible() {
		// TODO Auto-generated method stub
		return getVisibility() == EVisibility.PICKABLE;
	}

	private boolean isHighlighted() {
		return highlightOverlap.size() != 0 && highlightOverlap.size() == overlap.size();
	}

	private boolean isHovered() {
		return hoverOverlap.size() != 0 && hoverOverlap.size() == overlap.size();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (isVisible()) {
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), bandPoints, false, defaultColor, .5f);
			bandRenderer
					.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, false, defaultColor, .5f);
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
		hoverOverlap = new ArrayList<>(selectionManager.getElements(SelectionType.MOUSE_OVER));
		hoverOverlap.retainAll(overlap);
		highlightOverlap = new ArrayList<>(selectionManager.getElements(selectionType));
		highlightOverlap.retainAll(overlap);
		updatePosition();

	}

	protected Pair<Point2D, Point2D> pair(float x1, float y1, float x2, float y2) {
		Point2D _1 = new Point2D.Float(x1, y1);
		Point2D _2 = new Point2D.Float(x2, y2);
		return Pair.make(_1, _2);
	}

}
