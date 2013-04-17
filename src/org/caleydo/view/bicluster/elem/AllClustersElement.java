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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.util.Vec2d;

import com.google.common.base.Stopwatch;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class AllClustersElement extends GLElementContainer implements IGLLayout {
	float layoutStabilisationTime = 3000; // After X Milliseconds the layout is fixed until a cluster is moved
	// resetDamping(); is called)

	float repulsion = 250000f;
	float attractionFactor = 300f;
	float borderForceFactor = 200f;
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

	public AllClustersElement() {
		setLayout(this);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
	}

	public void setData(List<TablePerspective> list, TablePerspective x) {
		this.clear();
		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(p, x, this);
				this.add(el);
			}
		}
	}

	private boolean isInitLayoutDone = false;
	private GLElement dragedElement = null;
	Stopwatch stopwatch = new Stopwatch();

	float lastW, lastH;

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		lastW = w;
		lastH = h;
		if (!isInitLayoutDone && !children.isEmpty()) {
			initialLayout(children, w, h);
			isInitLayoutDone = true;
		} else {
			forceDirectedLayout(children, w, h);

		}
		for (IGLLayoutElement child : children) {
			child.setSize(child.getSetWidth(), child.getSetHeight());
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
	private ClusterElement hoveredElement;

	/**
	 *
	 */

	// contains positions of the childs in [0,1] x [0,1] space
	// Map<ClusterElement, Vec2d> virtualPositions = new HashMap<>();

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */
	private void forceDirectedLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		double xMax = 0, yMax = 0, xMin = 3000, yMin = 3000;

		// calculate the attraction based on the size of all overlaps
		// int overallOverlapSize = 0;
		double xOverlapSize = 0, yOverlapSize = 0;
		for (IGLLayoutElement iGLE : children) {
			GLElement vGL = iGLE.asElement();
			ClusterElement v = (ClusterElement) vGL;
			xOverlapSize += v.getDimensionOverlapSize();
			yOverlapSize += v.getRecordOverlapSize();
		}
		// System.out.println(overallOverlapSize);
		// double attraction = attractionFactor / (xOverlapSize + yOverlapSize);
		double attractionX = 1;
		double attractionY = 1;
		attractionX = attractionFactor / (xOverlapSize + yOverlapSize);
		attractionY = attractionFactor / (yOverlapSize + xOverlapSize);
		xOverlapSize /= 3;
		yOverlapSize /= 3;



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
				Vec2d distVec = getDistance(i, j);
				// Vec2d distVec = virtualPositions.get(i).minus(virtualPositions.get(j));
				double rsq = distVec.lengthSquared();
				rsq *= distVec.length();
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
				List<Integer> xOverlap = i.getDimOverlap(j);
				List<Integer> yOverlap = i.getRecOverlap(j);
				if (xOverlap.size() == 0 && yOverlap.size() == 0)
					continue;
				int overlapSizeX = xOverlap.size();
				int overlapSizeY = yOverlap.size();
				// Vec2d distVec = virtualPositions.get(j).minus(virtualPositions.get(i));
				Vec2d distVec = getDistance(j, i);
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
			Vec2d distFromFrame = getDistanceFromBorder(i, w, h);
			double forceX = Math.exp(borderForceFactor / Math.abs(distFromFrame.x())) - 1;
			double forceY = Math.exp(borderForceFactor / Math.abs(distFromFrame.y())) - 1;
			forceX = distFromFrame.x() < 0 ? -forceX : forceX;
			forceY = distFromFrame.y() < 0 ? -forceY : forceY;
			// double forceY = borderForceFactor * distFromFrame.lengthSquared() / distFromFrame.y();
			i.setFrameForce(new Vec2d(forceX, forceY));
		}

		for (IGLLayoutElement iGLL : children) {
			ClusterElement i = (ClusterElement) iGLL.asElement();
			Vec2d force = i.getAttForce().plus(i.getRepForce()).plus(i.getFrameForce());
			while (force.length() > 100)
				force.scale(0.1);
			Vec2d pos = getCenter(i);
			pos = force.times(damping).plus(pos);
			// System.out.println(i.getId() + ": ");
			// System.out.println("  Att: " + i.getAttForce());
			// System.out.println("  Rep: " + i.getRepForce());
			// System.out.println("  Fra: " + i.getCenterForce());
			// System.out.println("  Sum: " + force);
			// virtualPositions.remove(vEl);
			if (i != dragedElement && i != hoveredElement)
				setLocation(i, (float) pos.x(), (float) pos.y(), w, h);
			// virtualPositions.put(i, pos);
		}
		// System.out.println("iteration done");



	}

	private Vec2d getDistanceFromBorder(ClusterElement i, float w, float h) {
		Vec2d pos = getCenter(i);
		Vec2d dist = pos;
		if (dist.x() > w / 2)
			dist.setX(-(w - dist.x()));
		if (dist.y() > h / 2)
			dist.setY(-(h - dist.y()));
		return dist;
	}

	private Vec2d getCenter(ClusterElement i) {
		Vec2f vec = i.getLocation().addScaled(0.5f, i.getSize());
		return new Vec2d(vec.x(), vec.y());
	}

	private Vec2d getDistance(ClusterElement i, ClusterElement j) {
		return getCenter(i).minus(getCenter(j));
	}

	private void setLocation(ClusterElement v, double xPos, double yPos, float w, float h) {
		// xPos = xPos * (w - 200) + 100 - v.getSize().x() / 2;
		// yPos = yPos * (h - 175) + 90 - v.getSize().y() / 2;
		if (xPos > w || xPos < 0 || yPos > h || yPos < 0)
			System.out.println(v.getId() + ": " + xPos + "/" + yPos);
		v.setLocation((float) (xPos - v.getSize().x() / 2), (float) (yPos - v.getSize().y() / 2));
		// v.getIGLayoutElement().setLocation((float) ((xPos - v.getSize().x()) * 0.8 + 100),
		// (float) ((yPos - v.getSize().y()) * 0.8 + 50));
		v.repaintPick();
	}

	private void initialLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		int rowCount = ((int) (Math.sqrt(children.size())) + 1);
		int i = 0;
		for (GLElement child : asList()) {
			Vec2d pos = new Vec2d(i / rowCount * 160 + 200, (i % rowCount) * 160 + 100);
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
	
	public void setHooveredElement(ClusterElement hooveredElement) {
		this.hoveredElement = hooveredElement;
	}

}
