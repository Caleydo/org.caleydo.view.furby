/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;

public final class SpecialRecordClusterElement extends ASpecialClusterElement {

	private VirtualArray elements;
	private float width = 10f;

	public SpecialRecordClusterElement(TablePerspective data, BiClustering clustering, List<Integer> elements) {
		super(data, clustering);

		this.add(new SpecialClusterContent().setzDelta(0.5f));
		setLabel("Special " + clustering.getXDataDomain().getRecordIDCategory().getDenominationPlural().toString());

		this.elements = new VirtualArray(getXDataDomain().getRecordIDType(), elements);
		((SpecialClusterContent) get(2)).update();
	}

	@Override
	public void setClusterSize(double x, double y, double maxClusterSize, Object causer) {
		x = 100f/scaleFactor;
		y = width*elements.size()/scaleFactor;
		super.setClusterSize(x, y, maxClusterSize, causer);
	}

	private ATableBasedDataDomain getXDataDomain() {
		return clustering.getXDataDomain();
	}

	@Override
	protected VirtualArray getDimensionVirtualArray() {
		return new VirtualArray(getXDataDomain().getDimensionIDType());
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return elements;
	}

	@Override
	public int getNumberOfDimElements() {
		return 0;
	}

	@Override
	public int getNumberOfRecElements() {
		return elements.size();
	}

	@Override
	public boolean shouldBeVisible() {
		return !isHidden && elements.size() > 0;
	}


	private class SpecialClusterContent extends AnimatedGLElementContainer {

		List<String> recordNames;


		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			int i = 0;
			float[] color = { 0, 0, 0, curOpacityFactor };
			g.textColor(color);
			for (String s: recordNames) {
				g.drawText(s, 1, i*width-2, w, width);
				i++;
			}
			g.textColor(Color.BLACK);
			super.renderImpl(g, w, h);
		}

		void update() {
			recordNames = new ArrayList<String>();
			for (Integer i: elements) {
				recordNames.add(getXDataDomain().getRecordLabel(i));
			}
		}

		@Override
		public String toString() {
			return "special clusterContent";
		}
	}

	@Override
	public float getDimPosOf(int index) {
		return getDimIndexOf(index) * getSize().x() / getDimensionVirtualArray().size();
	}

	@Override
	public float getRecPosOf(int index) {
		return getRecIndexOf(index) * getSize().y() / getRecordVirtualArray().size();
	}

}
