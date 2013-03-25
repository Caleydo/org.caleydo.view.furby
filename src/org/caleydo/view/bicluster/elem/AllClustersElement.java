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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

import util.Vec2d;

import com.google.common.base.Stopwatch;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class AllClustersElement extends GLElementContainer implements IGLLayout {

	private final AGLView view;


	float layoutStabilisationTime = 3000; // After X Milliseconds the layout is fixed until a cluster is moved
	// resetDamping(); is called)

	float repulsion = 0.04f;
	float attractionFactor = 1f;
	// double aD = 0.3;

	public Integer fixedElementsCount = 15;

	/**
	 * @return the fixedElementsCount, see {@link #fixedElementsCount}
	 */
	public Integer getFixedElementsCount() {
		return fixedElementsCount;
	}

	/**
	 * @param fixedElementsCount
	 *            setter, see {@link fixedElementsCount}
	 */
	public void setFixedElementsCount(Integer fixedElementsCount) {
		this.fixedElementsCount = fixedElementsCount;
	}

	public AllClustersElement(AGLView view) {
		this.view = view;
		setLayout(this);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
	}

	public void setData(List<TablePerspective> list) {
		this.clear();
		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(view, p, this);
				this.add(el);
			}
		}
	}

	private boolean isInitLayoutDone = false;
	private GLElement dragedElement = null;
	Stopwatch stopwatch = new Stopwatch();

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		if (!isInitLayoutDone && !children.isEmpty()) {
			initialLayout(children, w, h);
			isInitLayoutDone = true;
		} else {
			if (dragedElement == null) {
				forceDirectedLayout(children, w, h);
			} else {
				dragElement(w, h);
			}
		}
		relayout();
	}

	/**
	 *
	 */
	private void dragElement(float w, float h) {

		GLElement iElement = dragedElement;
		ClusterElement i = (ClusterElement) iElement;
		Vec2f pos = i.getLocation();
		double posX = (pos.x()) / w;
		double posY = (pos.y()) / h;

		virtualPositions.put(i, new Vec2d(posX, posY));

	}

	double damping = 1f;

	double timerInterval = 100; // ms

	Timer dampingTimer = new Timer();

	public void resetDamping() {
		damping = 1.f;
	}

	TimerTask timerTask = new TimerTask() { // periodic tasks for stabilizing layout after layoutStabilisationTime
		// seconds.

		@Override
		public void run() {
			// setDamping();

		}

		protected void setDamping() {
			double amount = (1. / (layoutStabilisationTime / timerInterval));
			if (damping >= amount)
				damping -= amount;
			else
				damping = 0;
		}
	};

	/**
	 *
	 */

	// contains positions of the childs in [0,1] x [0,1] space
	Map<ClusterElement, Vec2d> virtualPositions = new HashMap<>();

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */
	private void forceDirectedLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		double xMax = 0, yMax = 0, xMin = 3000, yMin = 3000;

		// calculate the attraction based on the size of all overlaps
		// int overallOverlapSize = 0;
		int xOverlapSize = 0, yOverlapSize = 0;
		for (IGLLayoutElement iGLE : children) {
			GLElement vGL = iGLE.asElement();
			ClusterElement v = (ClusterElement) vGL;
			xOverlapSize += v.getXOverlapSize();
			yOverlapSize += v.getYOverlapSize();
		}
		// System.out.println(overallOverlapSize);
		// double attraction = attractionFactor / (xOverlapSize + yOverlapSize);
		double attractionX = 1;

		attractionX = attractionFactor / (xOverlapSize + yOverlapSize);
		double attractionY = 1;

		attractionY = attractionFactor / (yOverlapSize + xOverlapSize);

		// layout begin
		for (IGLLayoutElement iGLE : children) { // Loop through Vertices
			GLElement vGL = iGLE.asElement();
			ClusterElement i = (ClusterElement) vGL;
			// if (i.getId() == 4) {
			// System.out.println("haltepunkt");
			// }
			i.setRepForce(new Vec2d(0, 0));
			i.setAttForce(new Vec2d(0, 0));
			// repulsion
			for (IGLLayoutElement jGLL : children) { // loop through other vertices
				GLElement jElement = jGLL.asElement();
				ClusterElement j = (ClusterElement) jElement;
				if (j == i)
					continue;
				// squared distance between "u" and "v" in 2D space
				// calculate the repulsion between two vertices
				// Vec2d distVec = getDistance(i, j, w, h);
				Vec2d distVec = virtualPositions.get(i).minus(virtualPositions.get(j));
				double rsq = distVec.lengthSquared();
				// rsq = rsq * rsq;
				double forcex = repulsion * distVec.x() / rsq;
				double forcey = repulsion * distVec.y() / rsq;
				forcex += i.getRepForce().x();
				forcey += i.getRepForce().y();
				i.setRepForce(new Vec2d(forcex, forcey));
			}
			// attraction force calculation
			for (IGLLayoutElement jGLL : children) {
				GLElement jElement = jGLL.asElement();
				ClusterElement j = (ClusterElement) jElement;
				if (i == j)
					continue;
				List<Integer> xOverlap = i.getxOverlap(j);
				List<Integer> yOverlap = i.getyOverlap(j);
				if (xOverlap.size() == 0 && yOverlap.size() == 0)
					continue;
				int overlapSizeX = xOverlap.size();
				int overlapSizeY = yOverlap.size();
				Vec2d distVec = virtualPositions.get(j).minus(virtualPositions.get(i));
				// Vec2d distVec = getDistance(j, i, w, h);
				double dist = distVec.length/* Squared */();
				// int isXNeg = distVec.x() < 0 ? -1 : 1;
				// int isYNeg = distVec.y() < 0 ? -1 : 1;
				// dist = dist * distVec.length();
				// double distanceFactor = Math.log(dist / aD);
				// dist = Math.log((dist / aD));
				double forcex = attractionX * distVec.x() * (overlapSizeX + overlapSizeY) / dist; // * isXNeg;
				double forcey = attractionY * distVec.y() * (overlapSizeY + overlapSizeX) / dist; // * isYNeg;
				// counting the attraction
				forcex = i.getAttForce().x() + forcex;
				forcey = i.getAttForce().y() + forcey;
				i.setAttForce(new Vec2d(forcex, forcey));

			}

		}

		for (IGLLayoutElement iGLL : children) {
			ClusterElement i = (ClusterElement) iGLL.asElement();
			Vec2d force = i.getAttForce().plus(i.getRepForce());
			Vec2d pos = virtualPositions.get(i);
			pos = force.times(damping).plus(pos);
			// virtualPositions.remove(vEl);
			virtualPositions.put(i, pos);
		}

		// after moving all elements, rescaling to [0,1] must take place, elements could be out of the drawing area
		xMax = -100;
		yMax = -100;
		xMin = 100;
		yMin = 100;
		// set tmp positions
		for (IGLLayoutElement iGLL : children) {
			GLElement iElement = iGLL.asElement();
			ClusterElement i = (ClusterElement) iElement;
			Vec2d pos = virtualPositions.get(i);

			double xPos = pos.x();
			double yPos = pos.y();
			if (xPos < xMin)
				xMin = xPos;
			if (xPos > xMax)
				xMax = xPos;
			if (yPos < yMin)
				yMin = yPos;
			if (yPos > yMax)
				yMax = yPos;
		}

		Vec2d min = new Vec2d(-xMin, -yMin);
		Vec2d max = new Vec2d(xMax, yMax);
		max.add(min);
		for (IGLLayoutElement iGLL : children) {
			GLElement iElement = iGLL.asElement();
			ClusterElement i = (ClusterElement) iElement;
			Vec2d pos = virtualPositions.get(i);
			pos.add(min);
			double posX = pos.x() / max.x();
			double posY = pos.y() / max.y();
			virtualPositions.put(i, new Vec2d(posX, posY));
		}

		// finally drawing the virtual positions
		for (IGLLayoutElement iGLL : children) {
			GLElement iElement = iGLL.asElement();
			ClusterElement i = (ClusterElement) iElement;

			Vec2d pos = virtualPositions.get(i);
			setLocation(i, pos.x(), pos.y(), w, h);

			// System.out.println("ID: " + i.getId() + i.getLocation() + "   " + virtualPositions.get(i));
			// System.out.println("   Att Force: " + i.getAttForce() + "  RepForce: " + i.getRepForce());
		}
	}

	/**
	 * @param i
	 * @param j
	 * @param h
	 * @param w
	 * @return
	 */
	private Vec2d getDistance(ClusterElement i, ClusterElement j, float w, float h) {
		Vec2f iLoc = i.getLocation();
		Vec2f jLoc = j.getLocation();
		Vec2f iSize = i.getSize();
		Vec2f jSize = j.getSize();

		Vec2d iMiddle = new Vec2d(iLoc.x() + iSize.x() / 2., iLoc.y() + iSize.y() / 2.);
		Vec2d jMiddle = new Vec2d(jLoc.x() + jSize.x() / 2., jLoc.y() + jSize.y() / 2.);
		Vec2d dist = iMiddle.minus(jMiddle);
		return new Vec2d(dist.x() / w, dist.y() / h);
	}

	private void setLocation(ClusterElement v, double xPos, double yPos, float w, float h) {
		xPos = xPos * (w - 300) + 100;
		yPos = yPos * (h - 275) + 50;
		if (xPos > w || xPos < 0 || yPos > h || yPos < 0)
			System.out.println(xPos + "/" + yPos);
		v.getIGLayoutElement().setLocation((float) xPos, (float) yPos);
		v.repaintPick();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.view.opengl.layout2.GLElementContainer#renderImpl(org.caleydo.core.view.opengl.layout2.GLGraphics
	 * , float, float)
	 */
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		for (GLElement iGLL : this) {
			ClusterElement i = (ClusterElement) iGLL;
			if (!i.isVisible())
				continue;
			g.fillRect(i.getLocation().x(), i.getLocation().y(), i.getSize().x(), i.getSize().y());
			// System.out.println(i.getLocation() + " " + i.getSize());
			g.drawText(i.getId(), i.getLocation().x(), i.getLocation().y() - 15, 70, 12);
		}
		// super.renderImpl(g, w, h);


	}

	private void initialLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		int rowCount = ((int) (Math.sqrt(children.size())) + 1);
		double space = 1. / rowCount;
		int i = 0;
		for (GLElement child : asList()) {

			Vec2d pos = new Vec2d(i / rowCount * space, i % rowCount * space);
			virtualPositions.put((ClusterElement) child, pos);
			setLocation((ClusterElement) child, pos.x(), pos.y(), w, h);
			i++;
		}
	}


	/**
	 * @return the fixLayout, see {@link #fixLayout}
	 */
	public boolean isLayoutFixed() {
		return dragedElement == null;
	}

	/**
	 * @param fixLayout
	 *            setter, see {@link fixLayout}
	 */
	public void setDragedLayoutElement(ClusterElement element) {
		this.dragedElement = element;
	}



}
