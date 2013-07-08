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
	public static final String LABEL = "Import XLZ Data";
	public static final String ICON = "resources/icons/general/save.png";

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