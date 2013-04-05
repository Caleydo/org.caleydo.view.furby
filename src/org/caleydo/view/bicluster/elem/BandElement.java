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

	protected boolean highlight = false; // indicates whether the band is selected and should be drawn in a other color.
	protected ClusterElement first;
	protected ClusterElement second;
	protected List<Integer> overlap;
	protected List<Integer> highlightOverlap;

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
	protected boolean visible = false;
	protected AllBandsElement root;

	protected List<Pair<Point2D, Point2D>> bandPoints;
	protected List<Pair<Point2D, Point2D>> highlightPoints;
	private float[] highlightColor;
	private float[] hooveredColor;
	private float[] defaultColor;
	private boolean hoovered;

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
		hooveredColor = SelectionType.MOUSE_OVER.getColor();
	}

	public void selectElements() {
		if (selectionManager == null)
			return;
		// if (first.getId().contains("bicluster22") && second.getId().contains("bicluster7")) {
		// System.out.println("stop");
		// System.out.println("Band von " + first.getId() + "/" + second.getId() + " ID's: " + overlap);
		// }

		selectionManager.clearSelection(selectionType);
		// for (Integer id : overlap) {
		if (highlight) {
			// System.out.println("id: " + id + " zu selection gefügt.");
			// selectionManager.addToType(selectionType, id);
			selectionManager.addToType(selectionType, overlap);
		}
		// }
		fireSelectionChanged();
	}

	public void deselect() {
		highlight = false;
		highlightOverlap = new ArrayList<>();
		selectElements();
		repaint();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (visible) {
			float[] bandColor;
			if (highlight)
				bandColor = highlightColor;
			else if (hoovered)
				bandColor = hooveredColor;
			else
				bandColor = defaultColor;
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), bandPoints, highlight, bandColor, .5f);
			if (highlightOverlap.size() > 0)
				bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, highlight,
						highlightColor, .5f);
		}
		// super.renderImpl(g, w, h);
		// System.out.println(first.getId() + "/" + second.getId());
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (visible) {
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), bandPoints, false, defaultColor, .5f);
			bandRenderer
					.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, false, defaultColor, .5f);
		}
	}

	@Override
	protected void onClicked(Pick pick) {
		highlight = !highlight;
		if (highlight)
			root.setSelection(this);
		else
			root.setSelection(null);
		selectElements();

		super.onClicked(pick);
	}

	@Override
	protected void onMouseOver(Pick pick) {
		if (selectionManager == null)
			return;
		hoovered = true;
		selectionManager.clearSelection(SelectionType.MOUSE_OVER);
		selectionManager.addToType(SelectionType.MOUSE_OVER, overlap);
		fireSelectionChanged();
		super.onMouseOver(pick);
	}

	@Override
	protected void onMouseOut(Pick pick) {
		hoovered = false;
		selectionManager.clearSelection(SelectionType.MOUSE_OVER);
		super.onMouseOut(pick);
	}

	public abstract void updatePosition();

	protected abstract void fireSelectionChanged();

	public abstract void highlightSelectionOverlapWith(BandElement b);
}
