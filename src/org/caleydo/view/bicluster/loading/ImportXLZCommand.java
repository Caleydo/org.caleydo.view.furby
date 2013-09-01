/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.loading;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.gui.SimpleAction;
import org.caleydo.core.io.DataLoader;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.view.bicluster.GLBiCluster;
import org.caleydo.view.bicluster.RcpGLBiClusterView;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Samuel Gratzl
 *
 */
public class ImportXLZCommand extends SimpleAction {
	private static final String LABEL = "Import XLZ Data";
	private static final String ICON = "resources/icons/general/save.png";

	/**
	 * Constructor.
	 */
	public ImportXLZCommand() {
		super(LABEL, ICON);
	}

	@Override
	public void run() {
		ImportXLZDialog dialog = new ImportXLZDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		if (dialog.open() != Window.OK) {
			return;
		}
		List<DataSetDescription> r = dialog.getDataSetDescriptions();

		// 1. load data
		List<ATableBasedDataDomain> dd = new ArrayList<>();
		for (DataSetDescription ri : r)
			dd.add(DataLoader.loadData(ri, new NullProgressMonitor()));

		// 2. show Bicluster View
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		RcpGLBiClusterView rbiCluster;
		try {
			rbiCluster = (RcpGLBiClusterView) activePage.showView(GLBiCluster.VIEW_TYPE);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		GLBiCluster bicluster = (GLBiCluster) rbiCluster.getView();
		// 3. add them to the BiCluster View
		if (bicluster == null)
			return;

		List<TablePerspective> ts = new ArrayList<>(bicluster.getTablePerspectives());
		for(TablePerspective t : ts)
			bicluster.removeTablePerspective(t);

		for (ATableBasedDataDomain d : dd) {
			bicluster.addTablePerspective(d.getDefaultTablePerspective());
		}
	}
}