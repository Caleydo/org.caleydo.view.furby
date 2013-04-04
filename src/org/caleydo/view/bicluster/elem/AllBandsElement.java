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
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author user
 *
 */
public class AllBandsElement extends GLElementContainer implements IGLLayout,
		TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback {

	BandElement selection;
	TablePerspectiveSelectionMixin selectionMixin;

	/**
	 * @return the selectionMixin, see {@link #selectionMixin}
	 */
	public TablePerspectiveSelectionMixin getSelectionMixin() {
		return selectionMixin;
	}

	/**
	 * @param view
	 */
	public AllBandsElement() {
		setLayout(this);
	}


	public void setSelection(BandElement b) {
		if (selection != null) {
			selection.deselect();
			for (GLElement i : this) {
				((BandElement) i).updatePosition();
			}
		}
		selection = b;
		for (GLElement i : this) {
			((BandElement) i).highlightOverlapWith(b);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.view.opengl.layout2.layout.IGLLayout#doLayout(java.util.List, float, float)
	 */
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
		for (GLElement i : this) {
			((BandElement) i).highlightOverlapWith(selection);
			if (i == selection)
				((BandElement) i).selectElements();
		}
		selectionMixin.fireDimensionSelectionDelta();
		selectionMixin.fireRecordSelectionDelta();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.data.selection.TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback#onSelectionUpdate
	 * (org.caleydo.core.data.selection.SelectionManager)
	 */
	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		updateSelection();
		repaintAll();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.caleydo.core.data.selection.TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback#onVAUpdate(org.
	 * caleydo.core.data.perspective.table.TablePerspective)
	 */
	@Override
	public void onVAUpdate(TablePerspective tablePerspective) {
		// TODO Auto-generated method stub

	}

	public void setData(TablePerspective x) {
		selectionMixin = new TablePerspectiveSelectionMixin(x, this);
	}

}


