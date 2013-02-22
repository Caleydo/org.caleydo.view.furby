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
	private boolean isInitLayoutDone = false;

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
		iterations = 0;
		this.isInitLayoutDone = false;

	}

	int iterations = 0;

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		// System.out.println("do layout called");
		if (!isInitLayoutDone) {
			initLayout(children, w, h);
			isInitLayoutDone = true;
		} else {
			// if (iterations < 70) {
			forceDirectedLayout(children, w, h);
			// iterations++;
			// }
		}
	}

	float repulsion = 0.1f;
	float attraction = 0.0022f;
	float damping = 0.8f;

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */
	private void forceDirectedLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		// scale down child locations to [0,1]
		Map<ClusterElement, Vec2d> tmpPositions = new HashMap<>();
		double xMax = 0, yMax = 0, xMin = 1000, yMin = 1000;
		for (GLElement vGL : asList()) { // Set tmp Positions
			ClusterElement v = (ClusterElement) vGL;
			float xPos = v.getLocation().x();
			float yPos = v.getLocation().y();
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
		for (GLElement vGL : asList()) {
			ClusterElement v = (ClusterElement) vGL;
			Vec2f posf = v.getLocation();
			Vec2d pos = new Vec2d(posf.x(), posf.y());
			pos.add(min);
			double posX = pos.x() / max.x();
			double posY = pos.y() / max.y();
			tmpPositions.put(v, new Vec2d(posX, posY));
		}

		// layout begin
		for (GLElement vGL : asList()) {// loop through vertices
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			v.setForce(new Vec2d(0, 0));
			for (GLElement uGL : asList()) { // loop through other vertices
				{
					ClusterElement u = (ClusterElement) uGL;
					if (u == v || !u.isVisible())
						continue;
					// squared distance between "u" and "v" in 2D space
					Vec2d dist = tmpPositions.get(v).minus(tmpPositions.get(u));
					double rsq = dist.lengthSquared(); // counting the repulsion between two vertices
					double forcex = 0, forcey = 0;
					forcex = v.getForce().x() + repulsion * dist.x() / rsq;
					forcey = v.getForce().y() + repulsion * dist.y() / rsq;
					v.setForce(new Vec2d(forcex, forcey));
				}
				for (GLElement i : asList()) // loop through overlap
				{
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
						Vec2d dist = tmpPositions.get(j).minus(tmpPositions.get(v));
						float forcex = 0, forcey = 0;
						double overlapSizeX = Math.log1p(xOverlap.size());
						double overlapSizeY = Math.log1p(xOverlap.size());
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

		// after moving the childs, a further scaling to [0,1] is needed
		xMax = 0;
		yMax = 0;
		xMin = 2000;
		yMin = 2000;
		for (GLElement vGL : asList()) { // set tmp positions
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			Vec2d velocity = v.getVelocity();
			Vec2d position = tmpPositions.get(v);
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
			tmpPositions.remove(v);
			tmpPositions.put(v, newPos);
		}

		min = new Vec2d(-xMin, -yMin);
		max = new Vec2d(xMax, yMax);
		max.add(min);
		for (GLElement vGL : asList()) {
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			Vec2d pos = tmpPositions.get(v);
			pos.add(min);
			double posX = pos.x() / max.x();
			double posY = pos.y() / max.y();
			tmpPositions.put(v, new Vec2d(posX, posY));
		}

		// finally upscaling to w and h
		for (GLElement vGL : asList()) {
			ClusterElement v = (ClusterElement) vGL;
			if (!v.isVisible())
				continue;
			if (!v.isDragged()) {
				Vec2d pos = tmpPositions.get(v);
				double xPos = pos.x();
				double yPos = pos.y();
				xPos = xPos * (w - 300) + 100;
				yPos = yPos * (h - 300) + 100;
				setLocation(v, xPos, yPos);
			}
		}
	}

	private void setLocation(ClusterElement v, double xPos, double yPos) {
		v.setLocation((float) xPos, (float) yPos);
	}

	private void initLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		// set all locations
		int rows = ((int) Math.sqrt(children.size())) + 1;
		int count = 0;
		for (GLElement child : asList()) {
			// Random r = new Random();
			// child.setLocation(r.nextInt((int) (w - 200)), r.nextInt((int) (h - 200)));
			child.setLocation(200 + (count % rows) * 240, 200 + count / rows * 240);
			count++;
		}
	}

}
