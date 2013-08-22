/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;

public final class ChemicalClusterElement extends ASpecialClusterElement {

	private static float TEXT_SIZE = 10f;
	private VirtualArray elements;

	private List<String> clusterList;
	private Map<Integer, String> elementToClusterMap;

	public ChemicalClusterElement(TablePerspective data,
 BiClustering clustering, List<String> clusterList,
			Map<Integer, String> elementToClusterMap) {
		super(data, clustering);
		this.clusterList = clusterList;
		this.elementToClusterMap = elementToClusterMap;
		List<Integer> elements = new ArrayList<>(elementToClusterMap.size());
		for (Integer i : elementToClusterMap.keySet()) {
			elements.add(i);
		}
		setHasContent(elements, null);

		this.add(new SpecialClusterContent().setzDelta(0.5f));
		setLabel("Chemical clusters");
	}

	@Override
	public void setClusterSize(double x, double y, double maxClusterSize, Object causer) {
		y = 70f / scaleFactor;
		x = TEXT_SIZE * elements.size() / scaleFactor / 2;
		super.setClusterSize(x, y, maxClusterSize, causer);
	}

	@Override
	protected void setHasContent(List<Integer> dimIndices,
			List<Integer> recIndices) {
		if (dimIndices.size() > 0) {
			hasContent = true;
			recreateVirtualArrays(dimIndices, recIndices);
		} else {
			hasContent = false;
		}
	}

	@Override
	protected void recreateVirtualArrays(List<Integer> dimIndices,
			List<Integer> recIndices) {
		this.elements = new VirtualArray(clustering.getXDataDomain()
				.getDimensionGroupIDType(), dimIndices);
		((SpecialClusterContent) get(2)).update();
	}

	@Override
	protected VirtualArray getDimensionVirtualArray() {
		return elements;
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return new VirtualArray(clustering.getXDataDomain().getRecordIDType());
	}

	@Override
	public int getNumberOfDimElements() {
		return elements.size();
	}

	@Override
	public int getNumberOfRecElements() {
		return 0;
	}

	@Override
	protected void rebuildMyData(boolean isGlobal) {
		if (isLocked)
			return;
		setData(elements.getIDs(), elements.getIDs(), getID(), bcNr, -1, -1, -1, -1);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		if (!isGlobal)
			EventPublisher.trigger(new MouseOverClusterEvent(this, true));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal,
				dimBandsEnabled, recBandsEnabled));
		EventPublisher.trigger(new CreateBandsEvent(this));

	}

	private class SpecialClusterContent extends AnimatedGLElementContainer {

		List<String> chemicalClusterNames;

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			int i = chemicalClusterNames.size();
			float[] color = { 0, 0, 0, curOpacityFactor };
			g.textColor(color);
			g.save().gl.glRotatef(90, 0, 0, 1);

			float size = (i+1) * TEXT_SIZE;
			for (String s : chemicalClusterNames) {
				g.drawText(s, 1, i * TEXT_SIZE - 2 - size, h, TEXT_SIZE);
				i--;
			}
			g.restore();
			g.textColor(Color.BLACK);
			super.renderImpl(g, w, h);
		}

		void update() {
			chemicalClusterNames = clusterList;
		}

		@Override
		public String toString() {
			return "special clusterContent";
		}
	}

	@Override
	public float getDimPosOf(int id) {
//		return clusterList.indexOf(elementToClusterMap.get(index)) * getSize().x()*(float)scaleFactor
//				/ getDimensionVirtualArray().size();
		return getDimIndexOf(id)*TEXT_SIZE;
	}

	@Override
	public float getRecPosOf(int index) {
		return getRecIndexOf(index) * getSize().y() / getRecordVirtualArray().size();
	}

	@Override
	public List<List<Integer>> getListOfContinousDimSequences(
			List<Integer> overlap) {
		List<List<Integer>> sequences = new ArrayList<List<Integer>>();
		sequences.add(overlap);
		return sequences;
	}

	@Override
	public float getDimensionElementSize() {
		return getSize().x() / clusterList.size();
	}

	@Override
	public int getDimIndexOf(int value) {
		return clusterList.indexOf(elementToClusterMap.get(value));
	}

	@Override
	public int getNrOfElements(List<Integer> band){
		Set<String> usedElements = new HashSet<>();
		for (Integer i: band) {
			usedElements.add(elementToClusterMap.get(i));
		}
		return usedElements.size();
	}
}
