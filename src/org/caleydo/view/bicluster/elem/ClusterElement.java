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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.view.bicluster.GLBiCluster;

/**
 * e.g. a class for representing a cluster
 *
 * @author Samuel Gratzl
 *
 */
public class ClusterElement extends ElementLayout {
	private static final String CLUSTER_EMBEDDING_ID = "org.caleydo.view.bicluster.cluster";

	private TablePerspective data;
	private final AGLView view;
	private MultiFormRenderer multiFormRenderer;

	public ClusterElement(AGLView view, TablePerspective data) {
		this.view = view;
		this.data = data;
		init();
	}

	private void init() {
		this.setGrabX(true);
		this.setGrabY(true);

		// find all registered embedded views that support the actual rendering
		Set<String> remoteRenderedViewIDs = ViewManager.get().getRemotePlugInViewIDs(GLBiCluster.VIEW_TYPE,
				CLUSTER_EMBEDDING_ID);

		List<String> viewIDs = new ArrayList<>(remoteRenderedViewIDs);
		Collections.sort(viewIDs);

		this.multiFormRenderer = new MultiFormRenderer(view, true);
		List<TablePerspective> tablePerspectives = Collections.singletonList(data);

		int localRendererID = -1;
		for (String viewID : remoteRenderedViewIDs) {
			localRendererID = multiFormRenderer.addPluginVisualization(viewID, GLBiCluster.VIEW_TYPE,
					CLUSTER_EMBEDDING_ID, tablePerspectives, null);
		}
		multiFormRenderer.setActive(multiFormRenderer.getDefaultRendererID());
		this.setRenderer(multiFormRenderer);
	}
}

