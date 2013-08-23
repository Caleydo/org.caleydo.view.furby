/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.band;

import java.util.HashMap;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;

/**
 * @author Michael Gillhofer
 *
 */
public class RecordBandElement extends BandElement {

//	private static float[] recBandColor = {0.6f,0.1f,0.1f,1f};
	private static float[] recBandColor = Color.LIGHT_GRAY.getRGBA();

	public RecordBandElement(GLElement first, GLElement second,
			AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getRecOverlap(second),
				root.getSelectionMixin().getRecordSelectionManager(), root,
 recBandColor, ((ClusterElement) first).getRecordIDType());
	}

	@Override
	protected void fireSelectionChanged() {
		root.getSelectionMixin().fireRecordSelectionDelta();
	}

	@Override
	protected void initBand() {
		updateStructure();
	}

	@Override
	public void updateStructure() {
		if (!isVisible())
			return;
		overlap = first.getRecOverlap(second);
		updateVisibilityByOverlap();
		firstSubIndices = first.getListOfContinousRecSequenzes(overlap);
		secondSubIndices = second.getListOfContinousRecSequenzes(overlap);
		if (firstSubIndices.size() == 0)
			return;
		bandFactory = new RecordBandFactory(first, second, firstSubIndices,
				secondSubIndices, overlap);
		nonSplittedBands = bandFactory.getNonSplitableBands();
//		splittedBands = bandFactory.getSplitableBands();

		// splitted bands are not looking really helpfull for records
		splittedBands= nonSplittedBands;

//		splines = bandFactory.getConnectionsSplines();
		splines = new HashMap<>(); // create empty hashmap .. splines are not looking very good
		if (pickingPool != null) {
			pickingPool.clear();
		}
		repaintAll();
	}

	@Override
	public void updatePosition() {
		updateStructure();
	}

	@Override
	public void updateSelection() {
		// TODO Auto-generated method stub

	}

	@ListenTo
	private void listenTo(SpecialClusterRemoveEvent e) {
		if (e.isDimCluster())
			return;
		if (e.getSender() == first || e.getSender() == second) {
			setVisibility(EVisibility.NONE);
			overlap = null;
			root.remove(this);
		}
	}


}
