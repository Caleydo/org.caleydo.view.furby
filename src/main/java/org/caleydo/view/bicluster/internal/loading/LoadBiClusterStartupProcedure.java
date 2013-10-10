/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.internal.loading;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.collection.table.NumericalTable;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.io.DataLoader;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.startup.IStartupProcedure;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.view.bicluster.GLBiCluster;
import org.caleydo.view.bicluster.RcpGLBiClusterView;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

final class LoadBiClusterStartupProcedure implements IStartupProcedure {
	private static final Logger log = Logger.create(LoadBiClusterStartupProcedure.class);

	private final String name;
	private final List<DataSetDescription> datasets;

	private final List<ATableBasedDataDomain> loaded = new ArrayList<>();

	public LoadBiClusterStartupProcedure(String name, List<DataSetDescription> datasets) {
		this.datasets = datasets;
		this.name = name;
	}

	@Override
	public void run() {

		// 1. load data
		for (DataSetDescription ri : datasets)
			loaded.add(DataLoader.loadData(ri, new NullProgressMonitor()));
	}

	@Override
	public boolean preWorkbenchOpen() {
		return true;
	}

	@Override
	public void postWorkbenchOpen(IWorkbenchWindowConfigurer configurer) {
		configurer.setTitle("Caleydo - " + name);

		// 2. show Bicluster View
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		RcpGLBiClusterView rbiCluster;
		try {
			rbiCluster = (RcpGLBiClusterView) activePage.showView(GLBiCluster.VIEW_TYPE);
		} catch (PartInitException e) {
			log.error("can't create Furby: ", e);
			return;
		}

		GLBiCluster bicluster = (GLBiCluster) rbiCluster.getView();
		// 3. add them to the BiCluster View
		if (bicluster == null)
			return;

		List<TablePerspective> ts = new ArrayList<>(bicluster.getTablePerspectives());
		for (TablePerspective t : ts)
			bicluster.removeTablePerspective(t);

		for (ATableBasedDataDomain d : loaded) {
			if (d.getTable() instanceof NumericalTable)
				bicluster.addTablePerspective(d.getDefaultTablePerspective());
			else if (d.getRecordIDType().getIDCategory().getCategoryName().startsWith("BICLUSTER")) {
				// thresholds
				for (TablePerspective p : d.getAllTablePerspectives())
					if (!p.getDimensionPerspective().isDefault())
						bicluster.addTablePerspective(p);
			} else {
				// chemical clusters
				for (TablePerspective p : d.getAllTablePerspectives())
					if (!p.getDimensionPerspective().isDefault())
						bicluster.addTablePerspective(p);
			}
		}
	}
}