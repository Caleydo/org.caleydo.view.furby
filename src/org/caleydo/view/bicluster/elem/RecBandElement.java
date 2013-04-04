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

import gleem.linalg.Vec2f;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GLContext;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.Pick;

/**
 * @author Michael Gillhofer
 *
 */
public class RecBandElement extends BandElement {

	private float[] color = Colors.GREEN.getRGBA();

	/**
	 * @param view
	 */
	public RecBandElement(GLElement first, GLElement second, AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getRecOverlap(second), root.getSelectionMixin()
				.getRecordSelectionManager(), root);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.view.opengl.layout2.GLElementAdapter#renderImpl(org.caleydo.core.view.opengl.layout2.GLGraphics,
	 * float, float)
	 */
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (visible) {
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), bandPoints, highlight,
					highlight ? Colors.RED.getRGBA() : color, .5f);
			if (highlightOverlap.size() > 0)
				bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, highlight,
						Colors.RED.getRGBA(), .5f);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.view.opengl.layout2.GLElement#renderPickImpl(org.caleydo.core.view.opengl.layout2.GLGraphics,
	 * float, float)
	 */
	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (visible) {
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), bandPoints, false, color, .5f);
			bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), highlightPoints, false, color, .5f);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.view.opengl.layout2.PickableGLElement#onClicked(org.caleydo.core.view.opengl.picking.Pick)
	 */
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
	public void updatePosition() {
		overlap = first.getRecOverlap(second);
		int overlapSize = overlap.size();
		if (overlapSize > 0 && first.isVisible() && second.isVisible()) {
			visible = true;
			double endRecBandScaleFactor = second.getSize().y() / (double) second.getNumberOfRecElements();
			double startRecBandScaleFactor = first.getSize().y() / (double) first.getNumberOfRecElements();
			addPointsToBand(startRecBandScaleFactor, endRecBandScaleFactor);
		} else
			visible = false;
		repaintAll();

		// bandRenderer.renderComplexBand(GLContext.getCurrentGL().getGL2(), point, highlight, colorY, .5f);

	}

	private void addPointsToBand(double firRecScaFac, double secRecScaFac) {
		Vec2f fLoc = first.getLocation();
		Vec2f sLoc = second.getLocation();
		Vec2f fSize = first.getSize();
		Vec2f sSize = second.getSize();
		bandPoints = new ArrayList<>();
		highlightPoints = new ArrayList<>();
		int os = overlap.size();
		int hOS = highlightOverlap.size();
		// if (first.getId().contains("22"))
		// System.out.println("here");
		if (fLoc.x() < sLoc.x()) {
			// second right
			if (fLoc.x() + fSize.x() < sLoc.x()) {
				// second far at right
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x() + fSize.x(), fLoc.y(), fLoc.x() + fSize.x(),
							(float) (fLoc.y() + firRecScaFac * hOS)));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), sLoc.x(), (float) (sLoc.y() + secRecScaFac * hOS)));
					bandPoints.add(pair(fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac * hOS),
							fLoc.x() + fSize.x(), (float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), (float) (sLoc.y() + secRecScaFac * hOS), sLoc.x(),
							(float) (sLoc.y() + secRecScaFac * os)));

				} else {
					bandPoints.add(pair(fLoc.x() + fSize.x(), fLoc.y(), fLoc.x() + fSize.x(),
							(float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), sLoc.x(), (float) (sLoc.y() + secRecScaFac * os)));
				}

			} else {
				// second in between
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS)));
					highlightPoints.add(pair(sLoc.x(), (float) (sLoc.y() - secRecScaFac * hOS), sLoc.x(), sLoc.y()));
					bandPoints.add(pair(fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS), fLoc.x(),
							(float) (fLoc.y() + firRecScaFac * os)));
					bandPoints.add(pair(sLoc.x(), sLoc.y(), second.getLocation().x(), second.getLocation().y()));
				} else {
					bandPoints.add(pair(first.getLocation().x(), first.getLocation().y(), first.getLocation().x(),
							(float) (first.getLocation().y() + firRecScaFac * os)));
					bandPoints.add(pair(second.getLocation().x(),
							(float) (second.getLocation().y() - secRecScaFac * os), second.getLocation().x(), second
									.getLocation().y()));
				}
			}

		} else {
			// second left
			if (sLoc.x() + sSize.x() < fLoc.x()) {
				// second far at left
				if (hOS > 0) {
					highlightPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y(), sLoc.x() + sSize.x(),
							(float) (sLoc.y() + secRecScaFac * os)));
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS)));
					bandPoints.add(pair(sLoc.x() + sSize.x(), (float) (sLoc.y() + secRecScaFac * os),
							sLoc.x() + sSize.x(), (float) (sLoc.y() + secRecScaFac * os)));
					bandPoints.add(pair(fLoc.x(), (float) (fLoc.y() + firRecScaFac * hOS), fLoc.x(),
							(float) (fLoc.y() + firRecScaFac * hOS)));
				} else {
					bandPoints.add(pair(sLoc.x() + sSize.x(), sLoc.y(), sLoc.x() + sSize.x(),
							(float) (sLoc.y() + secRecScaFac * os)));
					bandPoints.add(pair(fLoc.x(), fLoc.y(), fLoc.x(), (float) (fLoc.y() + firRecScaFac * os)));
				}
			} else {
				if (hOS > 0) {
					highlightPoints.add(pair(fLoc.x(), fLoc.y(), (float) (fLoc.x() + firRecScaFac * os), fLoc.y()));
					highlightPoints.add(pair(sLoc.x(), sLoc.y(), (float) (sLoc.x() + secRecScaFac * hOS), sLoc.y()));
					bandPoints.add(pair((float) (fLoc.x() + firRecScaFac * os), fLoc.y(),
							(float) (fLoc.x() + firRecScaFac * os), fLoc.y()));
					bandPoints.add(pair((float) (sLoc.x() + secRecScaFac * hOS), sLoc.y(),
							(float) (sLoc.x() + secRecScaFac * hOS), sLoc.y()));
				} else {
					bandPoints.add(pair(first.getLocation().x(), first.getLocation().y(), (float) (first.getLocation()
							.x() + firRecScaFac * os), first.getLocation().y()));
					bandPoints.add(pair(second.getLocation().x(), second.getLocation().y(), (float) (second
							.getLocation().x() + secRecScaFac * hOS), second.getLocation().y()));

				}
			}
		}
	}

	private Pair<Point2D, Point2D> pair(float x1, float y1, float x2, float y2) {
		Point2D _1 = new Point2D.Float(x1, y1);
		Point2D _2 = new Point2D.Float(x2, y2);
		return Pair.make(_1, _2);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.data.selection.IEventBasedSelectionManagerUser#notifyOfSelectionChange(org.caleydo.core.data
	 * .selection.EventBasedSelectionManager)
	 */
	@Override
	public void notifyOfSelectionChange(EventBasedSelectionManager selectionManager) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.view.bicluster.elem.BandElement#highlightOverlapWith(org.caleydo.view.bicluster.elem.BandElement)
	 */
	@Override
	public void highlightOverlapWith(BandElement b) {
		highlightOverlap = new ArrayList<>();
		if (b instanceof RecBandElement) {
			List<Integer> highList = new LinkedList<>(overlap);
			highList.retainAll(b.overlap);
			highlightOverlap = highList;
		}
		updatePosition();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.view.bicluster.elem.BandElement#fireSelectionChanged()
	 */
	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireRecordSelectionDelta();

	}

}
