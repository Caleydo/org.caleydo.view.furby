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
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IPopupLayer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;

;

/**
 * @author user
 * 
 */
public class GLRootElement extends GLElementContainer implements IGLLayout {
	private AllBandsElement bands;
	private final AllClustersElement clusters = new AllClustersElement(this);

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
		context.getPopupLayer().show(globalToolBar,
				new Vec4f(Float.NaN, 0, 200, 200),
				IPopupLayer.FLAG_BORDER | IPopupLayer.FLAG_MOVEABLE);
	}

	public void setData(List<TablePerspective> list, TablePerspective x,
			TablePerspective l, TablePerspective z, ExecutorService executor) {
		this.clear();
		bands = new AllBandsElement(x);
		this.add(bands);
		if (clusters.size() > 0)
			clusters.clear();
		this.add(clusters);

		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(p, clusters, x, l,
						z, executor);
				clusters.add(el);
			}
		}
		clusters.setToolbar(globalToolBar);
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
				for (GLElement end : clusters.asList().subList(i,
						clusters.asList().size())) {
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

	int maxClusterRecSize = 150;
	int maxClusterDimSize = 150;

	private int curClusterSize = 150;

	public void setClusterSizes() {
		double maxDimClusterElements = 1;
		double maxRecClusterElements = 1;
		maxClusterDimSize = curClusterSize;
		maxClusterRecSize = curClusterSize;

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
//			if (i.getID().contains("27"))
//				System.out.println("stop");
			double recSize =  (i.getNumberOfRecElements()
					* (maxClusterRecSize) / (float)maxRecClusterElements);
			double dimSize =  (i.getNumberOfDimElements()
					* (maxClusterDimSize) / maxDimClusterElements);
			// if (recSize < 50) recSize = 50;
			// if (dimSize < 50) dimSize =50;
			i.setClusterSize(dimSize, recSize);
			i.relayout();
		}

	}

	public AllClustersElement getClusters() {
		return clusters;
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w,
			float h) {
		for (IGLLayoutElement child : children)
			child.setBounds(0, 0, w, h);

	}

	public void recalculateOverlap() {
		for (GLElement iGL : clusters) {
			((ClusterElement) iGL).calculateOverlap();
		}

	}

	@ListenTo
	public void listenTo(ClusterScaleEvent event) {
		setClusterSizes();
	}

	int bandCount = 0;

	@ListenTo
	public void listenTo(CreateBandsEvent event) {
		bandCount++;
		if (bandCount == clusters.size()) {
			createBands();
			bandCount = 0;
		}
	}

	int count = 0;

	@ListenTo
	public void listenTo(RecalculateOverlapEvent event) {
		if (event.isGlobal())
			count++;
		else {
			recalculateOverlap();
			count = 0;
			return;
		}
		if (count == clusters.size()) {
			recalculateOverlap();
			count = 0;
		}
	}

	private int smallClusterSize = 100;
	private int largeClusterSize = 150;

	@ListenTo
	public void listenTo(LZThresholdChangeEvent event) {
		if (event.isFixedClusterCount())
			curClusterSize = smallClusterSize;
		else
			curClusterSize = largeClusterSize;
		// setClusterSizes();
	}
}
