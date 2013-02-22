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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.view.TablePerspectivesChangedEvent;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.core.view.opengl.layout2.GLElement;
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
	private GLBiClusterElement root;
	private final AGLView view;
	private MultiFormRenderer multiFormRenderer;
	private Vec2d force = new Vec2d(0, 0);
	private Vec2d velocity = new Vec2d(0, 0);
	private boolean isDragged = false;
	private boolean isDrawn;

	private Map<GLElement, List<Integer>> xOverlap = new HashMap<>();
	private Map<GLElement, List<Integer>> yOverlap = new HashMap<>();

	/**
	 * @return the xOverlap, see {@link #xOverlap}
	 */
	int i = 0;

	public List<Integer> getxOverlap(GLElement jElement) {
		return xOverlap.get(jElement);
	}

	/**
	 * @return the yOverlap, see {@link #yOverlap}
	 */
	public List<Integer> getyOverlap(GLElement jElement) {
		return yOverlap.get(jElement);
	}


	public ClusterElement(AGLView view, TablePerspective data, GLBiClusterElement root) {
		super(view);
		this.view = view;
		this.data = data;
		init();
		this.setVisibility(EVisibility.VISIBLE);
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
		this.root = root;
	}

	Vec2f pickLocation;

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case DRAGGED:
			if (isDragged == false)
				pickLocation = getLocation();
			isDragged = true;
			java.awt.Point now = pick.getPickedPoint();
			java.awt.Point start = pick.getDragStartPoint();
			int diffX = start.x - now.x;
			int diffY = start.y - now.y;
			setLocation(pickLocation.x() - diffX, pickLocation.y() - diffY);
			repaintAll();
			repaintPick();
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
		setSize(200, 200);
	}

	/**
	 * @return the force, see {@link #force}
	 */
	public Vec2d getForce() {
		return force;
	}

	/**
	 * @param force
	 *            setter, see {@link force}
	 */
	public void setForce(Vec2d force) {
		this.force = force;
	}

	/**
	 * @return the velocity, see {@link #velocity}
	 */
	public Vec2d getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity
	 *            setter, see {@link velocity}
	 */
	public void setVelocity(Vec2d velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the isDraged, see {@link #isDragged}
	 */
	public boolean isDragged() {
		return isDragged;
	}

	/**
	 * @param dimIndices
	 * @param recIndices
	 */
	public void setIndices(List<Integer> dimIndices, List<Integer> recIndices) {
		if (dimIndices.size() > 0 && recIndices.size() > 0) {
			setVisibility(EVisibility.PICKABLE);
			VirtualArray dimArray = getDimensionVirtualArray();
			VirtualArray recArray = getRecordVirtualArray();
			dimArray.clear();
			for (Integer i : dimIndices)
				dimArray.append(i);
			recArray.clear();
			for (Integer i : recIndices)
				recArray.append(i);
			calculateOverlap();
			isDrawn = true;
			System.out.println("Drawn wurde auf true gesetzt");
		} else {
			setVisibility(EVisibility.HIDDEN);
			isDrawn = false;
			System.out.println("Drawn wurde auf false gesetzt");
		}
		fireTablePerspectiveChanged();
	}

	private VirtualArray getDimensionVirtualArray() {
		return data.getDimensionPerspective().getVirtualArray();
	}

	private VirtualArray getRecordVirtualArray() {
		return data.getRecordPerspective().getVirtualArray();
	}

	/**
	 *
	 */
	private void calculateOverlap() {
		List<GLElement> allClusters = root.asList();
		xOverlap = new HashMap<>();
		yOverlap = new HashMap<>();
		List<Integer> myDimIndizes = getDimensionVirtualArray().getIDs();
		List<Integer> myRecIndizes = getRecordVirtualArray().getIDs();
		for (GLElement element : allClusters) {
			ClusterElement e = (ClusterElement) element;
			List<Integer> eIndizes = new ArrayList<Integer>(myDimIndizes);

			eIndizes.retainAll(e.getDimensionVirtualArray().getIDs());
			xOverlap.put(element, eIndizes);

			eIndizes = new ArrayList<Integer>(myRecIndizes);
			eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
			yOverlap.put(element, eIndizes);

		}
	}

	private void fireTablePerspectiveChanged() {
		GeneralManager.get().getEventPublisher().triggerEvent(new TablePerspectivesChangedEvent(view).from(view));
	}

	/**
	 * @return the isDrawn, see {@link #isDrawn}
	 */
	public boolean isVisible() {
		return isDrawn;
	}

}
