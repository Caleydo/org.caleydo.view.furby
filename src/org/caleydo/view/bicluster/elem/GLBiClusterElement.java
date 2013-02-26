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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class GLBiClusterElement extends GLElementContainer implements IGLLayout {

	private final AGLView view;

	float layoutStabilisationTime = 5000; // After X Milliseconds the layout is fixed until a cluster is moved
											// (resetDamping()
	// is called)
	float repulsion = 0.6f;
	float attractionFactor = 0.02f;

	public Integer fixedElementsCount = 15;

	/**
	 * @return the fixedElementsCount, see {@link #fixedElementsCount}
	 */
	public Integer getFixedElementsCount() {
		return fixedElementsCount;
	}

	/**
	 * @param fixedElementsCount setter, see {@link fixedElementsCount}
	 */
	public void setFixedElementsCount(Integer fixedElementsCount) {
		this.fixedElementsCount = fixedElementsCount;
	}

	public GLBiClusterElement(AGLView view) {
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

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		if (!isInitLayoutDone) {
			initialLayout(children, w, h);
			isInitLayoutDone = true;
			dampingTimer.schedule(timerTask, 500, (long) timerInterval);
		} else {
			forceDirectedLayout(children, w, h);

		}
		relayout();
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
			setDamping();

		}
	};

	protected void setDamping() {
		double amount = (1. / (layoutStabilisationTime / timerInterval));
		if (damping >= amount)
			damping -= amount;
		else
			damping = 0;
	}

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
		int overallOverlapSize = 0;
		for (GLElement vGL : asList()) {
			ClusterElement v = (ClusterElement) vGL;
			overallOverlapSize += v.getOverallOverlapSize();
		}
		// System.out.println(overallOverlapSize);
		double attraction = attractionFactor / overallOverlapSize;

		// layout begin
		for (GLElement vGL : asList()) {// loop through vertices
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			v.setForce(new Vec2d(0, 0));
			for (GLElement uGL : asList()) { // loop through other vertices
				ClusterElement u = (ClusterElement) uGL;
				if (u == v || !u.isVisible())
					continue;
				// squared distance between "u" and "v" in 2D space
				// calculate the repulsion between two vertices
				Vec2d dist = virtualPositions.get(v).minus(virtualPositions.get(u));
				double rsq = dist.lengthSquared();
				double forcex = 0, forcey = 0;
				forcex = v.getForce().x() + repulsion * dist.x() / rsq;
				forcey = v.getForce().y() + repulsion * dist.y() / rsq;
				v.setForce(new Vec2d(forcex, forcey));

				// attraction force calculation
				for (GLElement i : asList()) {
					ClusterElement iElement = (ClusterElement) i;
					if (!iElement.isVisible())
						continue;
					for (GLElement j : asList()) {
						if (i == j)
							continue;

						ClusterElement jElement = (ClusterElement) j;
						if (!jElement.isVisible())
							continue;
						List<Integer> xOverlap = iElement.getxOverlap(jElement);
						List<Integer> yOverlap = iElement.getyOverlap(jElement);
						if (xOverlap.size() == 0 && yOverlap.size() == 0)
							continue;

						// counting the attraction
						dist = virtualPositions.get(j).minus(virtualPositions.get(v));
						forcex = 0;
						forcey = 0;
						// double overlapSizeX = Math.log1p(xOverlap.size());
						// double overlapSizeY = Math.log1p(xOverlap.size());
						int overlapSizeX = xOverlap.size();
						int overlapSizeY = xOverlap.size();
						forcex += v.getForce().x() + attraction * dist.x() * overlapSizeX;
						forcey += v.getForce().y() + attraction * dist.y() * overlapSizeY;
						v.setForce(new Vec2d(forcex, forcey));
					}
				}
				// counting the damped velocity
				Vec2d velocity = v.getVelocity();
				Vec2d force = v.getForce();
				Vec2d sum = velocity.plus(force);
				v.setVelocity(new Vec2d(sum.x() * damping, sum.y() * damping));
			}
		}

		// after moving all elements, rescaling to [0,1] must take place, elements could be out of the drawing area
		xMax = 0;
		yMax = 0;
		xMin = 2000;
		yMin = 2000;
		for (GLElement vGL : asList()) { // set tmp positions
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			Vec2d velocity = v.getVelocity();
			Vec2d position = virtualPositions.get(v);
			Vec2d newPos = velocity.plus(position);
			double xPos = newPos.x();
			double yPos = newPos.y();
			if (xPos < xMin)
				xMin = xPos;
			if (xPos > xMax)
				xMax = xPos;
			if (yPos < yMin)
				yMin = yPos;
			if (yPos > yMax)
				yMax = yPos;
			virtualPositions.remove(v);
			virtualPositions.put(v, newPos);
		}

		Vec2d min = new Vec2d(-xMin, -yMin);
		Vec2d max = new Vec2d(xMax, yMax);
		max.add(min);
		for (GLElement vGL : asList()) {
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			Vec2d pos = virtualPositions.get(v);
			pos.add(min);
			double posX = pos.x() / max.x();
			double posY = pos.y() / max.y();
			virtualPositions.put(v, new Vec2d(posX, posY));
		}

		// finally drawing the virtual positions
		for (GLElement vGL : asList()) {
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			if (!v.isDragged()) {
				Vec2d pos = virtualPositions.get(v);
				double xPos = pos.x();
				double yPos = pos.y();
				setLocation(v, xPos, yPos, w, h);
			} else {
				Vec2f pos = v.getLocation();
				// Vec2f size = v.getSize();
				double posX = (pos.x()) / w;
				double posY = (pos.y()) / h;
				virtualPositions.remove(v);
				virtualPositions.put(v, new Vec2d(posX, posY));
			}
		}
	}

	private void setLocation(ClusterElement v, double xPos, double yPos, float w, float h) {
		xPos = xPos * (w - 300) + 100;
		yPos = yPos * (h - 300) + 100;
		if (xPos > w || xPos < 0 || yPos > h || yPos < 0)
			System.out.println(xPos + "/" + yPos);
		v.getIGLayoutElement().setLocation((float) xPos, (float) yPos);
	}

	private void initialLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		for (GLElement child : asList()) {
			Random r = new Random();
			Vec2d pos = new Vec2d(r.nextFloat(), r.nextFloat());
			virtualPositions.put((ClusterElement) child, pos);
			setLocation((ClusterElement) child, r.nextFloat(), r.nextFloat(), w, h);
		}
	}

}
