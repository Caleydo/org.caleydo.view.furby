/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster;

import org.caleydo.core.gui.SimpleEventAction;
import org.caleydo.core.view.ARcpGLElementViewPart;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.view.bicluster.event.ShowToolBarEvent;
import org.caleydo.view.bicluster.internal.SearchAction;
import org.eclipse.jface.action.IToolBarManager;

/**
 * TODO: DOCUMENT ME!
 *
 * @author <INSERT_YOUR_NAME>
 */
public class RcpGLBiClusterView extends ARcpGLElementViewPart {


	private SearchAction searchAction;

	public RcpGLBiClusterView() {
		super(SerializedBiClusterView.class);
	}

	@Override
	protected AGLElementView createView(IGLCanvas canvas) {
		return new GLBiCluster(glCanvas, serializedView);
	}

	@Override
	public void createPartControlGL() {
		this.searchAction = new SearchAction();
		glCanvas.addKeyListener(this.searchAction);

		super.createPartControlGL();
	}


	@Override
	public void addToolBarContent(IToolBarManager toolBarManager) {
		super.addToolBarContent(toolBarManager);
		toolBarManager.add(searchAction);
		toolBarManager.add(new SimpleEventAction("Show &Parameter Settings", BiClusterRenderStyle.ICON_TOOLS,
				Activator.getResourceLoader(), new ShowToolBarEvent(true)));
		toolBarManager.add(new SimpleEventAction("Show &Layout Settings", BiClusterRenderStyle.ICON_LAYOUT, Activator
				.getResourceLoader(),
				new ShowToolBarEvent(false)));
	}


}