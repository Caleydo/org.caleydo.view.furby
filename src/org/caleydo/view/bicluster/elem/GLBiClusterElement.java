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

	private float[][] forcesX, forcesY;

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
				ClusterElement el = new ClusterElement(view, p);
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
		int size = clusterOverlap.length;
		forcesX = new float[size][size];
		forcesY = new float[size][size];
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children_doNotUse, float w, float h) {
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

	float repulsion = 1000f;
	float attraction = 0.0005f;
	float damping = 0.90f;

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */
	private void forceDirectedLayout(List<ClusterElement> children, float w, float h) {

		Map<ClusterElement, Vec2f> tmpPositions = new HashMap<>();
		for (ClusterElement v : children) {// loop through vertices
			v.setForce(new Vec2f(0, 0));
			for (ClusterElement u : children) // loop through other vertices
			{
				if (u == v)
					continue;
				// squared distance between "u" and "v" in 2D space
				Vec2f dist = v.getLocation().minus(u.getLocation());
				float rsq = dist.x() * dist.x() + dist.y() * dist.y();
				// counting the repulsion between two vertices
				float forcex = 0, forcey = 0;
				forcex = v.getForce().x() + repulsion * dist.x() / rsq;
				forcey = v.getForce().y() + repulsion * dist.y() / rsq;
				v.setForce(new Vec2f(forcex, forcey));
			}
			for (int i = 0; i < clusterOverlap.length; i++) // loop through edges
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
					Vec2f dist = u.getLocation().minus(v.getLocation());
					float forcex = 0, forcey = 0;
					forcex += v.getForce().x() + attraction * dist.x() * xOverlap.size();
					forcey += v.getForce().y() + attraction * dist.y() * yOverlap.size();
					v.setForce(new Vec2f(forcex, forcey));
				}
			}
			// counting the damped velocity
			Vec2f velocity = v.getVelocity();
			Vec2f force = v.getForce();
			Vec2f sum = velocity.plus(force);
			v.setVelocity(new Vec2f(sum.x() * damping, sum.y() * damping));
		}
		float xMax = 0, yMax = 0, xMin = 0, yMin = 0;
		for (ClusterElement v : children) // set tmp positions
		{
			// if (v.isDragged) {
			// v.x = mouseX;
			// v.y = mouseY;
			// } else {
			Vec2f velocity = v.getVelocity();
			Vec2f position = v.getLocation();
			Vec2f newPos = velocity.plus(position);

			tmpPositions.put(v, newPos);
			float xPos = newPos.x();
			float yPos = newPos.y();
			if (xPos < xMin)
				xMin = xPos;
			if (xPos > xMax)
				xMax = xPos;
			if (yPos < yMin)
				yMin = yPos;
			if (yPos > yMax)
				yMax = yPos;
			v.setLocation(xPos, yPos);
		}
		float dx = xMax - xMin;
		float dy = yMax - yMin;
		Vec2f min = new Vec2f(-xMin, -yMin);
		Vec2f max = new Vec2f(xMax, yMax);
		max.add(min);
		for (ClusterElement ce : tmpPositions.keySet()) {
			Vec2f pos = tmpPositions.get(ce);
			pos.add(min);
		}
		for (ClusterElement ce : tmpPositions.keySet()) {
			Vec2f pos = tmpPositions.get(ce);
			float xPos = pos.x();
			float yPos = pos.y();
			xPos = xPos * (w - 200) / max.x();
			yPos = yPos * (h - 200) / max.y();
			ce.setLocation(xPos, yPos);
		}
	}

	int iterations = 0;

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */

	/**
	 * @param i
	 * @return
	 */
	private float sumX(int k) {
		double sum = 0;
		for (int i = 0; i < forcesX.length; i++) {
			sum += forcesX[i][k];
		}
		return (float) sum;
	}

	private float sumY(int k) {
		double sum = 0;
		for (int i = 0; i < forcesX.length; i++) {
			sum += forcesY[i][k];
		}
		return (float) sum;
	}

	private void initLayout(List<ClusterElement> children, float w, float h) {

		// set all sizes
		for (ClusterElement child : children) {
			child.setSize(200, 150);

		}

		// set all locations
		int rows = ((int) Math.sqrt(children.size())) + 1;
		int count = 0;
		for (ClusterElement child : children) {
			Random r = new Random();
			// child.setLocation(r.nextInt((int) (w - 200)), r.nextInt((int) (h - 200)));
			child.setLocation(200 + (count % rows) * 240, 200 + count / rows * 240);
			count++;
		}
	}

}
