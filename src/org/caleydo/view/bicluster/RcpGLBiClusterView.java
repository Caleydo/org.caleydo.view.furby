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
package org.caleydo.view.bicluster;

import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.core.view.MinimumSizeComposite;
import org.caleydo.core.view.ViewManager;
import org.caleydo.view.bicluster.view.Toolbar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * TODO: DOCUMENT ME!
 *
 * @author <INSERT_YOUR_NAME>
 */
public class RcpGLBiClusterView extends ARcpGLViewPart {

	public RcpGLBiClusterView() {
		super(SerializedBiClusterView.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite p = new Composite(parent, SWT.NONE);
		GridLayout d = new GridLayout(1, true);
		d.horizontalSpacing = 0;
		d.verticalSpacing = 0;
		d.marginHeight = 0;
		d.marginWidth = 0;
		p.setLayout(d);

		// custom area for a toolbar
		// Composite toolArea = new Composite(p, SWT.NONE);
		// createToolArea(toolArea);
		// toolArea.pack();
		// toolArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		minSizeComposite = new MinimumSizeComposite(p, SWT.H_SCROLL | SWT.V_SCROLL);
		minSizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		minSizeComposite.setMinSize(0, 0);
		minSizeComposite.setExpandHorizontal(true);
		minSizeComposite.setExpandVertical(true);

		glCanvas = createGLCanvas(minSizeComposite);
		parentComposite = glCanvas.asComposite();
		ViewManager.get().registerGLCanvasToAnimator(glCanvas);
		minSizeComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				// if (!PlatformUI.getWorkbench().isClosing())
				ViewManager.get().unregisterGLCanvas(glCanvas);
			}
		});

		minSizeComposite.setContent(parentComposite);

		view = new GLBiCluster(glCanvas);
		initializeView();
		createPartControlGL();
	}

	protected void createToolArea(Composite composite) {
		composite.setLayout(new RowLayout());

		Toolbar.createToolBar(composite);
	}


	@Override
	public void addToolBarContent() {
		toolBarManager.add(new Toolbar());
		toolBarManager.update(true);
	}

	@Override
	public void createDefaultSerializedView() {
		serializedView = new SerializedBiClusterView();
	}

	@Override
	public String getViewGUIID() {
		return GLBiCluster.VIEW_TYPE;
	}

}