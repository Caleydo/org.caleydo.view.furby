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
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author user
 *
 */
public class GLRootElement extends GLElementContainer implements IGLLayout {

	private final AGLView view;
	private AllBandsElement bands;
	private AllClustersElement clusters;

	/**
	 *
	 */
	public GLRootElement(AGLView view) {
		this.view = view;
		bands = new AllBandsElement();
		clusters = new AllClustersElement(view);
		this.add(bands);
		this.add(clusters);
		setLayout(this);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
	}

	public void setData(List<TablePerspective> list) {
		if (bands.size() > 0)
			bands.clear();
		if (clusters.size() > 0)
			clusters.clear();
		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(view, p, clusters);
				clusters.add(el);
			}
		}
	}

	/**
	 *
	 */
	public void createBands() {
		if (bands.size() == 0) {
			int i = 1;
			for (GLElement start : clusters) {
				for (GLElement end : clusters.asList().subList(i, clusters.asList().size())) {
					if (start == end)
						continue;
					bands.add(new RecBandElement(start, end, bands));
					bands.add(new DimBandElement(start, end, bands));
				}
				i++;
			}
		}
		bands.updateSelection();
	}

	int maxDimClusterElements = 0;
	int maxRecClusterElements = 0;
	int maxClusterRecSize = 150;
	int maxClusterDimSize = 150;

	/**
	 *
	 */
	public void setClusterSizes() {
		double maxDimClusterElements = 1;
		double maxRecClusterElements = 1;
		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			if (!i.isVisible())
				continue;
			if (maxDimClusterElements < i.getNumberOfDimElements()) {
				maxDimClusterElements = i.getNumberOfDimElements();
			}
			if (maxRecClusterElements < i.getNumberOfRecElements()) {
				maxRecClusterElements = i.getNumberOfRecElements();
			}
		}
		for (GLElement iGL : clusters) {
			ClusterElement i = (ClusterElement) iGL;
			int recSize = (int) ((i.getNumberOfRecElements() * (maxClusterRecSize) / maxRecClusterElements));
			int dimSize = (int) ((i.getNumberOfDimElements() * (maxClusterDimSize) / maxDimClusterElements));
			i.setSize(dimSize, recSize);
		}

	}

	/**
	 *
	 */
	public void resetDamping() {
		clusters.resetDamping();

	}

	public AllClustersElement getClusters() {
		return clusters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.view.opengl.layout2.layout.IGLLayout#doLayout(java.util.List, float, float)
	 */
	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		bands.setSize(w, h);
		bands.setLocation(0, 0);
		clusters.setSize(w, h);
		clusters.setLocation(0, 0);

	}

}
