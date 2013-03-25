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
import org.caleydo.core.data.virtualarray.events.DimensionVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.RecordVAUpdateEvent;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.view.TablePerspectivesChangedEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementAdapter;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.GLBiCluster;
import org.caleydo.view.bicluster.util.Vec2d;


/**
 * e.g. a class for representing a cluster
 *
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class ClusterElement extends GLElementAdapter {
	private static final String CLUSTER_EMBEDDING_ID = "org.caleydo.view.bicluster.cluster";

	private TablePerspective data;
	private AllClustersElement root;
	private final AGLView view;
	private MultiFormRenderer multiFormRenderer;
	private Vec2d attForce = new Vec2d(0, 0);
	private Vec2d repForce = new Vec2d(0, 0);
	private Vec2d velocity = new Vec2d(0, 0);
	private boolean isDragged = false;
	private boolean isVisible;

	private Map<GLElement, List<Integer>> xOverlap;
	private Map<GLElement, List<Integer>> yOverlap;

	private String id;


	public ClusterElement(AGLView view, TablePerspective data, AllClustersElement root) {

		super(view);
		this.view = view;
		this.data = data;
		this.root = root;
		init();
	}

	public IDCategory getRecordIDCategory() {
		return data.getDataDomain().getRecordIDCategory();
	}

	public IDCategory getDimensionIDCategory() {
		return data.getDataDomain().getDimensionIDCategory();
	}

	public IDType getDimensionIDType() {
		return getDimensionVirtualArray().getIdType();
	}

	public IDType getRecordIDType() {
		return getRecordVirtualArray().getIdType();
	}

	public String getDataDomainID() {
		return data.getDataDomain().getDataDomainID();
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public String getId() {
		return id;
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
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});

		// GLElementAccessor.asLayoutElement(this).setSize(200, 200);
		setVisibility(EVisibility.PICKABLE);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.view.opengl.layout2.GLElementAdapter#renderPickImpl(org.caleydo.core.view.opengl.layout2.GLGraphics
	 * , float, float)
	 */
	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub
		// super.renderPickImpl(g, w, h);
		g.color(Colors.BLACK);
		g.fillRect(0, 0, w, h);
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
		// TODO Auto-generated method stub
		// super.renderImpl(g, w, h);
		// if (isDragged) {
		// g.color(Colors.RED);
		// g.fillRect(0, 0, w, h);
		// g.color(Colors.BLACK);
		// }
	}

	private Vec2f pickLocation;

	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case DRAGGED:
			if (isDragged == false) {
				pickLocation = getLocation();
				root.setDragedLayoutElement(this);
			}
			root.resetDamping();
			isDragged = true;
			java.awt.Point now = pick.getPickedPoint();
			java.awt.Point start = pick.getDragStartPoint();
			int diffX = start.x - now.x;
			int diffY = start.y - now.y;
			setLocation(pickLocation.x() - diffX, pickLocation.y() - diffY);
			relayoutParent();
			repaintPick();
			break;
		default:
			isDragged = false;
			root.setDragedLayoutElement(null);
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

	/**
	 * @param dimIndices
	 * @param recIndices
	 * @param setXElements
	 * @param string
	 */
	public void setIndices(List<Integer> dimIndices, List<Integer> recIndices, boolean setXElements, String string) {
		this.id = string;
		if (dimIndices.size() > 0 && recIndices.size() > 0) {
			setVisibility(EVisibility.PICKABLE);
			isVisible = true;
			VirtualArray dimArray = getDimensionVirtualArray();
			VirtualArray recArray = getRecordVirtualArray();
			dimArray.clear();
			int count = 0;
			for (Integer i : dimIndices) {
				if (setXElements && root.getFixedElementsCount() <= count)
					break;
				dimArray.append(i);
				count++;
			}
			count = 0;
			recArray.clear();
			for (Integer i : recIndices) {
				if (setXElements && root.getFixedElementsCount() <= count)
					break;
				recArray.append(i);
				count++;
			}
		} else {
			setVisibility(EVisibility.NONE);
			isVisible = false;
		}
		calculateOverlap();
		fireTablePerspectiveChanged();
		view.resetView();
		// setSize(200, 200);
	}

	/**
	 *
	 */
	private void calculateOverlap() {
		xOverlap = new HashMap<>();
		yOverlap = new HashMap<>();
		List<Integer> myDimIndizes = getDimensionVirtualArray().getIDs();
		List<Integer> myRecIndizes = getRecordVirtualArray().getIDs();
		// overallOverlapSize = 0;
		xOverlapSize = 0;
		yOverlapSize = 0;
		for (GLElement element : root.asList()) {
			if (element == this)
				continue;
			ClusterElement e = (ClusterElement) element;
			List<Integer> eIndizes = new ArrayList<Integer>(myDimIndizes);

			eIndizes.retainAll(e.getDimensionVirtualArray().getIDs());
			xOverlap.put(element, eIndizes);
			xOverlapSize += eIndizes.size();

			eIndizes = new ArrayList<Integer>(myRecIndizes);
			eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
			yOverlap.put(element, eIndizes);
			yOverlapSize += eIndizes.size();

		}
	}

	/**
	 * @return the force, see {@link #attForce}
	 */
	public Vec2d getAttForce() {
		return attForce;
	}

	/**
	 * @param force
	 *            setter, see {@link force}
	 */
	public void setAttForce(Vec2d force) {
		this.attForce = force;
	}

	/**
	 * @param force
	 *            setter, see {@link force}
	 */
	public void setRepForce(Vec2d force) {
		this.repForce = force;
	}

	/**
	 * @return the force, see {@link #attForce}
	 */
	public Vec2d getRepForce() {
		return repForce;
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
	 *
	 * @param dimensionName
	 * @param recordName
	 */

	public void setPerspectiveLabel(String dimensionName, String recordName) {
		data.getDimensionPerspective().setLabel(dimensionName);
		data.getRecordPerspective().setLabel(recordName);
	}

	private void fireTablePerspectiveChanged() {
		EventPublisher.publishEvent(new TablePerspectivesChangedEvent(view).from(view));
		EventPublisher.publishEvent(new RecordVAUpdateEvent(data.getDataDomain().getDataDomainID(), data
				.getRecordPerspective().getPerspectiveID(), this));
		EventPublisher.publishEvent(new DimensionVAUpdateEvent(data.getDataDomain().getDataDomainID(), data
				.getDimensionPerspective().getPerspectiveID(), this));

		repaintAll();
	}

	private VirtualArray getDimensionVirtualArray() {
		return data.getDimensionPerspective().getVirtualArray();
	}

	private VirtualArray getRecordVirtualArray() {
		return data.getRecordPerspective().getVirtualArray();
	}

	public int getNumberOfDimElements() {
		return getDimensionVirtualArray().size();
	}

	public int getNumberOfRecElements() {
		return getRecordVirtualArray().size();
	}

	/**
	 * @return the isDraged, see {@link #isDragged}
	 */
	public boolean isDragged() {
		return isDragged;
	}

	/**
	 * @return the isDrawn, see {@link #isVisible}
	 */
	public boolean isVisible() {
		return isVisible;
	}

	public List<Integer> getDimOverlap(GLElement jElement) {
		return xOverlap.get(jElement);
	}

	/**
	 * @return the yOverlap, see {@link #yOverlap}
	 */
	public List<Integer> getRecOverlap(GLElement jElement) {
		return yOverlap.get(jElement);
	}

	// int overallOverlapSize;
	int xOverlapSize;
	int yOverlapSize;

	public int getXOverlapSize() {
		return xOverlapSize;
	}

	public int getYOverlapSize() {
		return yOverlapSize;
	}

	/**
	 * @return
	 */
	protected IGLLayoutElement getIGLayoutElement() {
		return GLElementAccessor.asLayoutElement(this);
	}

}
