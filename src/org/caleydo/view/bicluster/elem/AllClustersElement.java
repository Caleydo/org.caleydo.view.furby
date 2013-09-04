/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.view.bicluster.elem.layout.ForceBasedLayoutTuned;
import org.caleydo.view.bicluster.event.AlwaysShowToolBarEvent;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class AllClustersElement extends GLElementContainer {

	private GLElement dragedElement = null;


	private final List<AToolBarElement> toolbars = new ArrayList<>();
	private boolean isShowAlwaysToolBar = false;

	@DeepScan
	private final IGLLayout2 layout = new ForceBasedLayoutTuned(this);

	public AllClustersElement(GLRootElement glRootElement) {
		setLayout(layout);
		this.setzDelta(0.5f);
	}

	@Override
	public void layout(int deltaTimeMs) {
		for (AToolBarElement toolbar : toolbars)
			if (toolbar.hasMoved()) {
				relayout();
				break;
			}
		super.layout(deltaTimeMs);
	}

	/**
	 * @return the isShowAlwaysToolBar, see {@link #isShowAlwaysToolBar}
	 */
	public boolean isShowAlwaysToolBar() {
		return isShowAlwaysToolBar;
	}

	@ListenTo
	private void onAlwaysShowToolBarEvent(AlwaysShowToolBarEvent event) {
		this.isShowAlwaysToolBar = !isShowAlwaysToolBar;
		for (GLElement elem : this)
			GLElementAccessor.relayoutDown(elem);
	}

	/**
	 * @return the toolbars, see {@link #toolbars}
	 */
	public List<AToolBarElement> getToolbars() {
		return toolbars;
	}

	/**
	 * @return the fixLayout, see {@link #fixLayout}
	 */
	public boolean isLayoutFixed() {
		return dragedElement == null;
	}

	/**
	 * @return the dragedElement, see {@link #dragedElement}
	 */
	public GLElement getDragedElement() {
		return dragedElement;
	}

	/**
	 * @param fixLayout
	 *            setter, see {@link fixLayout}
	 */
	public void setDragedLayoutElement(ClusterElement element) {
		this.dragedElement = element;
	}

	public void setToolBars(AToolBarElement... elements) {
		this.toolbars.addAll(Arrays.asList(elements));
	}

	/**
	 * @return
	 */
	public ClusterElement findFocused() {
		for (ClusterElement elem : allClusters()) {
			if (elem.isFocused())
				return elem;
		}
		return null;
	}

	private Iterable<ClusterElement> allClusters() {
		return Iterables.filter(this, ClusterElement.class);
	}
}
