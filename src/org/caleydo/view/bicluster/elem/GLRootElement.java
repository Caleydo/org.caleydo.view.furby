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

import gleem.linalg.Vec4f;

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IPopupLayer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * @author user
 *
 */
public class GLRootElement extends GLElementContainer implements IGLLayout {
	private AllBandsElement bands;
	private final AllClustersElement clusters = new AllClustersElement();

	private GlobalToolBarElement globalToolBar = new GlobalToolBarElement();
	/**
	 *
	 */
	public GLRootElement() {
		setLayout(this);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		// show the global toolbar as a popup
		context.getPopupLayer().show(globalToolBar, new Vec4f(10, 10, 200, 200),
				IPopupLayer.FLAG_BORDER | IPopupLayer.FLAG_MOVEABLE);
	}

	public void setData(List<TablePerspective> list, TablePerspective x) {
		this.clear();
		bands = new AllBandsElement(x);
		this.add(bands);
		if (clusters.size() > 0)
			clusters.clear();
		this.add(clusters);

		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(p, x, clusters);
				clusters.add(el);
			}
		}
	}

	/**
	 *
	 */
	public void createBands() {
		if (bands == null)
			return;
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
			i.relayout();
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

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement child : children)
			child.setBounds(0, 0, w, h);

	}

}
