/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.band;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.SpecialClusterRemoveEvent;

/**
 * @author Michael Gillhofer
 *
 */
public class DimensionBandElement extends BandElement {

	private static float[] dimBandColor = Color.NEUTRAL_GREY.getRGBA();

	public DimensionBandElement(GLElement first, GLElement second,
			AllBandsElement root) {
		super(first, second, ((ClusterElement) first).getDimOverlap(second),
 root.getDimensionSelectionManager(), root,
 dimBandColor, ((ClusterElement) first).getDimensionIDType());
	}

	@Override
	protected void initBand() {
		updateStructure();
	}

	@Override
	public void updateStructure() {
		if (!isVisible())
			return;
		overlap = toFastOverlap(first.getDimOverlap(second));
		updateVisibilityByOverlap();
		firstSubIndices = first.getListOfContinousDimSequences(overlap);
		secondSubIndices = second.getListOfContinousDimSequences(overlap);
		if (firstSubIndices.size() == 0)
			return;
		bandFactory = new DimensionBandFactory(first, second, firstSubIndices,
				secondSubIndices, overlap);
		splittedBands = bandFactory.getSplitableBands();
		nonSplittedBands = bandFactory.getNonSplitableBands();
		splines = bandFactory.getConnectionsSplines();
		if (pickingPool != null) {
			pickingPool.clear();
		}
		repaintAll();
	}

	@Override
	public void updatePosition() {
		updateStructure();
	}

	@ListenTo
	private void listenTo(SpecialClusterRemoveEvent e) {
		if (!e.isDimCluster())
			return;
		if (e.getSender() == first || e.getSender() == second) {
			setVisibility(EVisibility.NONE);
			overlap = null;
			root.remove(this);
		}
	}

}
