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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.core.view.opengl.layout2.GLElementAdapter;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.GLBiCluster;

/**
 * e.g. a class for representing a cluster
 *
 * @author Samuel Gratzl
 *
 */
public class ClusterElement extends GLElementAdapter {
	private static final String CLUSTER_EMBEDDING_ID = "org.caleydo.view.bicluster.cluster";

	private TablePerspective data;
	private final AGLView view;
	private MultiFormRenderer multiFormRenderer;
	private Vec2f force = new Vec2f(200, 200);
	private Vec2f velocity = new Vec2f(0, 0);
	private boolean isDragged = false;

	public ClusterElement(AGLView view, TablePerspective data) {
		super(view);
		this.view = view;
		this.data = data;
		init();
		this.setVisibility(EVisibility.PICKABLE);
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
	}

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case DRAGGED:
			isDragged = true;
			java.awt.Point p = pick.getPickedPoint();
			setLocation(pick.getPickedPoint().x - getSize().x()/2, pick.getPickedPoint().y -getSize().y()/2);
			// System.out.println("dragged: " + p.x + "/" + p.y);
			break;
		case MOUSE_OUT:
			isDragged = false;
			break;
		case MOUSE_RELEASED:
			isDragged = false;
		default:
			break;
		}

		// switch (pick.getPickingMode()) {
		// case CLICKED:
		// draged = true;
		// break;
		// case DRAGGED:
		// System.out.println("now dragged");
		// break;
		// case MOUSE_OUT:
		// draged = false;
		// break;
		// case MOUSE_OVER:
		// // onMouseOver(pick);
		// break;
		// case MOUSE_MOVED:
		// if (draged)
		// setLocation(pick.getPickedPoint().x, pick.getPickedPoint().y);
		// break;
		// case MOUSE_RELEASED:
		// draged = false;
		// default:
		// break;
		// }

	}

	private void init() {

		// find all registered embedded views that support the actual rendering
		Set<String> remoteRenderedViewIDs = ViewManager.get().getRemotePlugInViewIDs(GLBiCluster.VIEW_TYPE,
				CLUSTER_EMBEDDING_ID);

		List<String> viewIDs = new ArrayList<>(remoteRenderedViewIDs);
		Collections.sort(viewIDs);

		this.multiFormRenderer = new MultiFormRenderer(view, true);
		List<TablePerspective> tablePerspectives = Collections.singletonList(data);

		for (String viewID : remoteRenderedViewIDs) {
			multiFormRenderer.addPluginVisualization(viewID, GLBiCluster.VIEW_TYPE, CLUSTER_EMBEDDING_ID,
					tablePerspectives, null);
		}
		multiFormRenderer.setActive(multiFormRenderer.getDefaultRendererID());
		this.setRenderer(multiFormRenderer);
	}

	/**
	 * @return the force, see {@link #force}
	 */
	public Vec2f getForce() {
		return force;
	}

	/**
	 * @param force
	 *            setter, see {@link force}
	 */
	public void setForce(Vec2f force) {
		this.force = force;
	}

	/**
	 * @return the velocity, see {@link #velocity}
	 */
	public Vec2f getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity
	 *            setter, see {@link velocity}
	 */
	public void setVelocity(Vec2f velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the isDraged, see {@link #isDragged}
	 */
	public boolean isDragged() {
		return isDragged;
	}

}
