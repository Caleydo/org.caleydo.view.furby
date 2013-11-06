/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import static org.caleydo.view.bicluster.elem.ZoomLogic.initialFocusNeighborScaleFactor;
import static org.caleydo.view.bicluster.elem.ZoomLogic.initialFocusScaleFactor;
import gleem.linalg.Vec2f;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.view.bicluster.elem.layout.AForceBasedLayout;
import org.caleydo.view.bicluster.elem.layout.ForceBasedLayoutTuned2;
import org.caleydo.view.bicluster.elem.toolbar.AToolBarElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class AllClustersElement extends GLElementContainer {

	private ClusterElement draggedElement = null;
	private ClusterElement focussedElement = null;
	private ClusterElement hoveredElement = null;

	private static final Comparator<NormalClusterElement> BY_BI_CLUSTER_NUMBER = new Comparator<NormalClusterElement>() {
		@Override
		public int compare(NormalClusterElement o1, NormalClusterElement o2) {
			return o1.getBiClusterNumber() - o2.getBiClusterNumber();
		}
	};

	@DeepScan
	private final AForceBasedLayout layout = new ForceBasedLayoutTuned2(this);


	public AllClustersElement(GLRootElement glRootElement) {
		setLayout(layout);
		this.setzDelta(0.5f);
	}
	/**
	 * @return the fixLayout, see {@link #fixLayout}
	 */
	public boolean isLayoutFixed() {
		return draggedElement == null;
	}

	/**
	 * @return the dragedElement, see {@link #draggedElement}
	 */
	public GLElement getDraggedElement() {
		return draggedElement;
	}

	/**
	 * @param hoveredElement
	 *            setter, see {@link hoveredElement}
	 */
	public void setHoveredElement(ClusterElement hoveredElement) {
		if (this.hoveredElement == hoveredElement)
			return;
		this.hoveredElement = hoveredElement;
		relayout();
	}

	/**
	 * @param fixLayout
	 *            setter, see {@link fixLayout}
	 */
	public void setDragedElement(ClusterElement element) {
		if (this.draggedElement == element)
			return;
		this.draggedElement = element;
		relayout();
	}

	Iterable<ClusterElement> allClusters() {
		return Iterables.filter(this, ClusterElement.class);
	}


	private List<NormalClusterElement> getSortedClusters() {
		List<NormalClusterElement> c = Lists.newArrayList(Iterables.filter(this, NormalClusterElement.class));
		Collections.sort(c, BY_BI_CLUSTER_NUMBER);
		return c;
	}

	/**
	 * focus on the previous logical cluster
	 */
	public void focusNext() {
		if (this.focussedElement == null)
			return;
		focusPrevious(Lists.reverse(getSortedClusters()));
	}

	/**
	 * focus on the next logical cluster
	 */
	public void focusPrevious() {
		if (this.focussedElement == null)
			return;
		focusPrevious(getSortedClusters());
	}

	private void focusPrevious(final List<NormalClusterElement> sortedClusters) {
		NormalClusterElement prev = null;
		for (NormalClusterElement cluster : sortedClusters) {
			if (cluster.getDimSize() <= 0 || cluster.getRecSize() <= 0)
				continue;
			if (cluster == focussedElement && prev != null) {
				setFocus(prev);
				return;
			}
			prev = cluster;
		}

		// else use the last focussable one, round trip
		for (NormalClusterElement cluster : Lists.reverse(sortedClusters)) {
			if (cluster.getDimSize() <= 0 || cluster.getRecSize() <= 0)
				continue;
			if (cluster == focussedElement)
				break;
			setFocus(cluster);
			break;
		}
	}

	/**
	 * @param normalClusterElement
	 */
	public void setFocus(ClusterElement elem) {
		if (this.focussedElement == elem)
			return;
		ClusterElement prev = this.focussedElement;
		if (this.focussedElement != null) {
			this.focussedElement.setFocus(false);
		}
		this.focussedElement = elem;
		if (this.focussedElement != null) {
			this.focussedElement.setFocus(true);
			if (prev != null) // reuse zoom settings
				this.focussedElement.setZoom(prev.getZoom(EDimension.DIMENSION), prev.getZoom(EDimension.RECORD));
			else {
				Vec2f size = getSize();
				if (this.focussedElement.needsUniformScaling()) {
					Vec2f s = this.focussedElement.getMinSize();
					float scale = Math.min(size.x() / s.x(), size.y() / s.y());
					this.focussedElement.setZoom(scale,scale);
				} else {
					Map<EDimension, Float> s = initialFocusScaleFactor(this.focussedElement.getSizes(), size.x(),
							size.y());
					this.focussedElement.setZoom(s.get(EDimension.DIMENSION), s.get(EDimension.RECORD));
				}
			}

			add(this.focussedElement); // sounds strange but moves the cluster at the end of the cluster list
		}
		focusChanged();
		relayout();
	}

	private void focusChanged() {
		List<Dimension> dims = new ArrayList<>();
		for (ClusterElement c : allClusters()) {
			if (c == this.focussedElement)
				continue;
			c.focusChanged(this.focussedElement);
			if (c.isVisible())
				dims.add(c.getSizes());
		}
		if (this.focussedElement != null) {
			// set neighbor size for focused element
			Vec2f size = getSize();
			Map<EDimension, Float> s = initialFocusNeighborScaleFactor(dims, this.focussedElement.getSizes(), size.x(),
					size.y());
			for (ClusterElement c : allClusters()) {
				if (c == this.focussedElement)
					continue;
				c.setZoom(s.get(EDimension.DIMENSION), s.get(EDimension.RECORD));
			}
		}
	}

	public boolean isFocussed(ClusterElement elem) {
		return focussedElement == elem;
	}

	public boolean isDragged(ClusterElement elem) {
		return draggedElement == elem;
	}

	public ClusterElement getFocussedElement() {
		return focussedElement;
	}

	public boolean isAnyFocussed() {
		return focussedElement != null;
	}

	public ClusterElement getHoveredElement() {
		return hoveredElement;
	}

	/**
	 * @return
	 */
	public List<AToolBarElement> getToolbars() {
		return findParent(GLRootElement.class).getToolbars();
	}

	/**
	 *
	 */
	public void onChangeMaxDistance() {
		if (isAnyFocussed())
			focusChanged();
	}

	/**
	 *
	 */
	public void onMouseOut() {
		if (this.hoveredElement != null) {
			this.hoveredElement.mouseOut();
		}
	}

	/**
	 * @return the layout, see {@link #layout}
	 */
	public AForceBasedLayout getLayout() {
		return layout;
	}
}
