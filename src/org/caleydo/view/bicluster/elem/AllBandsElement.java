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

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author Michael Gillhofer
 *
 */
public class AllBandsElement extends GLElementContainer implements IGLLayout,
		TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback {
	@DeepScan
	private final TablePerspectiveSelectionMixin selectionMixin;

	private BandElement selection;

	/**
	 * @return the selectionMixin, see {@link #selectionMixin}
	 */
	public TablePerspectiveSelectionMixin getSelectionMixin() {
		return selectionMixin;
	}

	/**
	 * @param view
	 */
	public AllBandsElement(TablePerspective tablePerspective) {
		if (tablePerspective != null)
			this.selectionMixin = new TablePerspectiveSelectionMixin(tablePerspective, this);
		else
			this.selectionMixin = null;
		setLayout(this);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		for (GLElement b : this) {
			((BandElement) b).updatePosition();
			b.setSize(w, h);
			b.setLocation(0, 0);
		}
		relayout();
	}

	public void updateSelection() {
		if (selection == null)
			return;
		selection.recalculateSelection();
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {

	}

	@Override
	public void onVAUpdate(TablePerspective tablePerspective) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param selection
	 *            setter, see {@link selection}
	 */
	public void setSelection(BandElement selection) {
		if (this.selection != null)
			this.selection.deselect();
		this.selection = selection;
	}

	public BandElement getSelection() {
		return this.selection;
	}
}
