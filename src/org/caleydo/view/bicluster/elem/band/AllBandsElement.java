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
package org.caleydo.view.bicluster.elem.band;

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.data.SelectionCommandEvent;
import org.caleydo.core.event.data.SelectionUpdateEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;

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
	 * @param savedData
	 */
	public AllBandsElement(TablePerspective tablePerspective) {
//		setzDelta(-3);
		if (tablePerspective != null)
			this.selectionMixin = new TablePerspectiveSelectionMixin(tablePerspective, this);
		else
			this.selectionMixin = null;
		setLayout(this);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		for (GLElement b : this) {
			b.setBounds(0,0,w, h);
			((BandElement) b).updatePosition();
		}
		relayout();
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		SelectionDelta selectionDelta = manager.getDelta();
		SelectionUpdateEvent event = new SelectionUpdateEvent();
		event.setSender(selectionMixin);
		event.setEventSpace(selectionMixin.getTablePerspective().getDataDomain().getDataDomainID());
		event.setSelectionDelta(selectionDelta);
		EventPublisher.trigger(event);
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

	public void updateSelection() {
		if (selection == null)
			return;
		selection.recalculateSelection();
	}

	@ListenTo
	private void listenTo(RecalculateOverlapEvent event) {
		updateStructure();
	}

	public void updateStructure() {
		for (GLElement b : this) {
			((BandElement) b).updateStructure();
		}
		relayout();
		
	}

}
