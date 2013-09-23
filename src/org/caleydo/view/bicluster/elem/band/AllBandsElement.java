/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.band;

import java.util.Comparator;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.GLRootElement;
import org.caleydo.view.bicluster.event.UpdateBandsEvent;

import com.google.common.collect.Iterables;

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
	 * @param savedData
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
		GLRootElement root = findParent(GLRootElement.class);
		for (IGLLayoutElement child : children) {
			BandElement b = (BandElement) child.asElement();
			if (!root.isBandsEnabled(b.getDimension()))
				child.hide();
			else {
				child.setBounds(0, 0, w, h);
				b.updatePosition();
			}
		}
		if (resortOnNextRun) {
			sortBy(new Comparator<GLElement>() {
				@Override
				public int compare(GLElement o1, GLElement o2) {
					return Float.compare(o1.getzDelta(), o2.getzDelta());
				}
			});
			resortOnNextRun = false;
		}
	}

	@ListenTo
	private void onUpdateBandsEvent(UpdateBandsEvent event) {
		relayout();
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		for (BandElement band : allBands())
			band.onSelectionUpdate(manager);
	}

	private Iterable<BandElement> allBands() {
		return Iterables.filter(this, BandElement.class);
	}

	@Override
	public void onVAUpdate(TablePerspective tablePerspective) {

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

	/**
	 *
	 */
	void fireAllSelections(BandElement sender) {
		for (SelectionManager m : selectionMixin) {
			if (selectionMixin.fireSelectionDelta(m)) {
				// fire internal selection update
				for (BandElement band : allBands())
					if (band != sender)
						band.onSelectionUpdate(m);
			}
		}
	}

	/**
	 * @param selection
	 */
	void clearAll(SelectionType selection) {
		for (SelectionManager m : selectionMixin)
			m.clearSelection(selection);
	}

	public BandElement getSelection() {
		return this.selection;
	}

	public void updateSelection() {
		if (selection == null)
			return;
		selection.onSelectionUpdate(getDimensionSelectionManager());
		selection.onSelectionUpdate(getRecordSelectionManager());
	}

	public void updateStructure() {
		relayout();
	}

	boolean resortOnNextRun = true;

	public void triggerResort() {
		resortOnNextRun = true;
		relayout();
	}

	/**
	 * @return
	 */
	public SelectionManager getDimensionSelectionManager() {
		return selectionMixin.getDimensionSelectionManager();
	}

	public SelectionManager getRecordSelectionManager() {
		return selectionMixin.getRecordSelectionManager();
	}

}
