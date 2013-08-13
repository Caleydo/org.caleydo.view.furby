/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.view.bicluster.elem.layout.ForceBasedLayout;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class AllClustersElement extends GLElementContainer {

	private GLElement dragedElement = null;


	private final List<AToolBarElement> toolbars = new ArrayList<>();

	@DeepScan
	private final ForceBasedLayout layout = new ForceBasedLayout(this);

	public AllClustersElement(GLRootElement glRootElement) {
		setLayout(layout);
		this.setzDelta(0.5f);
	}

	/**
	 * @return the toolbars, see {@link #toolbars}
	 */
	public List<AToolBarElement> getToolbars() {
		return toolbars;
	}

	public void setData(List<TablePerspective> list, TablePerspective x, TablePerspective l, TablePerspective z,
			ExecutorService executor, GLRootElement biclusterRoot) {
		this.clear();
		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(p, this, x, l, z, executor, biclusterRoot);
				this.add(el);
			}
		}
	}


	@Override
	public void layout(int deltaTimeMs) {
		layout.addDelta(deltaTimeMs);
		super.layout(deltaTimeMs);
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



}
