/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
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
	private final IGLLayout2 layout = new ForceBasedLayoutTuned2(this);


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
		NormalClusterElement act = null;
		for (NormalClusterElement cluster : getSortedClusters()) {
			if (act != null) {
				setFocus(cluster);
				break;
			}
			if (cluster == focussedElement)
				act = cluster;
		}
	}

	/**
	 * focus on the next logical cluster
	 */
	public void focusPrevious() {
		if (this.focussedElement == null)
			return;
		NormalClusterElement prev = null;
		for (NormalClusterElement cluster : getSortedClusters()) {
			if (cluster == focussedElement && prev != null) {
				setFocus(prev);
				break;
			}
			prev = cluster;
		}
	}

	/**
	 * @param normalClusterElement
	 */
	public void setFocus(ClusterElement elem) {
		if (this.focussedElement == elem)
			return;
		if (this.focussedElement != null)
			this.focussedElement.setFocus(false);
		this.focussedElement = elem;
		if (this.focussedElement != null) {
			this.focussedElement.setFocus(true);
			add(this.focussedElement); // sounds strange but moves the cluster at the end of the cluster list
		}
		focusChanged();
		relayout();
	}

	private void focusChanged() {
		for (ClusterElement c : allClusters())
			if (c != this.focussedElement)
				c.focusChanged(this.focussedElement);
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
		if (this.focussedElement != null)
			focusChanged();
	}
}
