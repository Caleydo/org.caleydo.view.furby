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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.AGLView;
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
	private Object[][][] clusterOverlap;
	private List<ClusterElement> children;
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
		children = new ArrayList<ClusterElement>();
		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(view, p);
				this.add(el);
				children.add(el);
			}
		}
	}

	/**
	 * @param clusterOverlap
	 */
	public void setOverlap(Object[][][] clusterOverlap) {
		iterations = 0;
		this.isInitLayoutDone = false;
		this.clusterOverlap = clusterOverlap;
	}

	int iterations = 0;

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children_doNotUse, float w, float h) {
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

	float repulsion = 0.01f;
	float attraction = 0.012f;
	float damping = 0.6f;

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */
	private void forceDirectedLayout(List<ClusterElement> children, float w, float h) {

		// scale down child locations to [0,1]
		Map<ClusterElement, Vec2d> tmpPositions = new HashMap<>();
		double xMax = 0, yMax = 0, xMin = 1000, yMin = 1000;
		for (ClusterElement v : children) // set tmp positions
		{
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
		Vec2d d;
		Vec2d min = new Vec2d(-xMin, -yMin);
		Vec2d max = new Vec2d(xMax, yMax);
		max.add(min);
		for (ClusterElement v : children) {
			Vec2f posf = v.getLocation();
			Vec2d pos = new Vec2d(posf.x(), posf.y());
			pos.add(min);
			double posX = pos.x() / max.x();
			double posY = pos.y() / max.y();
			tmpPositions.put(v, new Vec2d(posX, posY));
		}

		// layout begin
		for (ClusterElement v : children) {// loop through vertices
			v.setForce(new Vec2d(0, 0));
			for (ClusterElement u : children) // loop through other vertices
			{
				if (u == v)
					continue;
				// squared distance between "u" and "v" in 2D space
				Vec2d dist = tmpPositions.get(v).minus(tmpPositions.get(u));
				double rsq = dist.lengthSquared(); // counting the repulsion between two vertices
				double forcex = 0, forcey = 0;
				forcex = v.getForce().x() + repulsion * dist.x() / rsq;
				forcey = v.getForce().y() + repulsion * dist.y() / rsq;
				v.setForce(new Vec2d(forcex, forcey));
			}
			for (int i = 0; i < clusterOverlap.length; i++) // loop through overlap
			{
				for (int j = 0; j < clusterOverlap[i].length; j++) {
					if (i == j)
						continue;
					@SuppressWarnings("unchecked")
					List<Integer> xOverlap = ((List<Integer>) clusterOverlap[i][j][0]);
					@SuppressWarnings("unchecked")
					List<Integer> yOverlap = ((List<Integer>) clusterOverlap[i][j][1]);
					if (xOverlap.size() == 0 && yOverlap.size() == 0)
						continue;
					ClusterElement u = children.get(j);
					// counting the attraction
					Vec2d dist = tmpPositions.get(u).minus(tmpPositions.get(v));
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

		// after moving the childs, a further scaling to [0,1] is needed
		xMax = 0; yMax = 0; xMin = 1000; yMin = 1000;
		for (ClusterElement v : children) { // set tmp positions
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
		for (ClusterElement v : children) {
			Vec2d pos = tmpPositions.get(v);
			pos.add(min);
			double posX = pos.x() / max.x();
			double posY = pos.y() / max.y();
			tmpPositions.put(v, new Vec2d(posX, posY));
		}

		// finally upscaling to w and h
		for (ClusterElement v : children) {

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

	private void initLayout(List<ClusterElement> children, float w, float h) {

		// set all sizes
		for (ClusterElement child : children) {
			child.setSize(150, 100);
		}

		// set all locations
		int rows = ((int) Math.sqrt(children.size())) + 1;
		int count = 0;
		for (ClusterElement child : children) {
			Random r = new Random();
			child.setLocation(r.nextInt((int) (w - 200)), r.nextInt((int) (h - 200)));
			// child.setLocation(200 + (count % rows) * 240, 200 + count / rows * 240);
			count++;
		}
	}

}
