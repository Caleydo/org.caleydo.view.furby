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
package org.caleydo.view.bicluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.event.EventListenerManager;
import org.caleydo.core.event.EventListenerManagers;
import org.caleydo.core.event.view.TablePerspectivesChangedEvent;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.listener.AddTablePerspectivesEvent;
import org.caleydo.core.view.listener.AddTablePerspectivesListener;
import org.caleydo.core.view.listener.RemoveTablePerspectiveEvent;
import org.caleydo.core.view.listener.RemoveTablePerspectiveListener;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.layout.LayoutManager;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.bicluster.elem.GLBiClusterElement;
import org.caleydo.view.bicluster.renderstyle.BiClusterRenderStyle;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>
 * Sample GL2 view.
 * </p>
 * <p>
 * This Template is derived from {@link ATableBasedView}, but if the view does
 * not use a table, changing that to {@link AGLView} is necessary.
 * </p>
 * 
 * @author Marc Streit
 * @author Michael Gillhofer
 */

public class GLBiCluster extends AGLView implements
		IMultiTablePerspectiveBasedView, IGLRemoteRenderingView {
	public static final String VIEW_TYPE = "org.caleydo.view.bicluster";
	public static final String VIEW_NAME = "BiCluster Visualization";

	private final EventListenerManager listeners = EventListenerManagers
			.wrap(this);

	private List<TablePerspective> perspectives = new ArrayList<>();
	private BiClusterRenderStyle renderStyle;

	private LayoutManager layoutManager;
	private GLBiClusterElement root;

	/**
	 * Constructor.
	 * 
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 */
	public GLBiCluster(IGLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {
		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);
		this.textureManager = new TextureManager(Activator.getResourceLoader());
	}

	@Override
	public void init(GL2 gl) {
		this.renderStyle = new BiClusterRenderStyle(viewFrustum);

		layoutManager = new LayoutManager(viewFrustum, pixelGLConverter);
		root = new GLBiClusterElement(this);
		layoutManager.setBaseElementLayout(root);

		if (this.perspectives.size() >= 3) {
			findXLZ();
		}

		detailLevel = EDetailLevel.HIGH;

		layoutManager.updateLayout();
	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

		// Register keyboard listener to GL2 canvas
		glParentView.getParentComposite().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						glParentView.getParentComposite().addKeyListener(
								glKeyListener);
					}
				});

		this.glMouseListener = glMouseListener;

		init(gl);
	}

	@Override
	public void displayLocal(GL2 gl) {
		pickingManager.handlePicking(this, gl);
		display(gl);
		if (busyState != EBusyState.OFF) {
			renderBusyMode(gl);
		}

	}

	@Override
	public void displayRemote(GL2 gl) {
		display(gl);
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDataView() {
		return true;
	}

	/**
	 * determines which of the given {@link TablePerspective} is L and Z, given
	 * the known X table
	 * 
	 * @param a
	 * @param b
	 */
	private Pair<TablePerspective, TablePerspective> findLZ(TablePerspective x,
			TablePerspective a, TablePerspective b) {
		// row: gene, row: gene
		if (a.getDataDomain().getRecordIDCategory()
				.equals(x.getDataDomain().getRecordIDCategory())) {
			return Pair.make(a, b);
		} else {
			return Pair.make(b, a);
		}
	}

	/**
	 * infers the X,L,Z tables from the dimension and record ID categories, as L
	 * and Z has the same Dimension ID Category, the remaining one will be X
	 */
	private void findXLZ() {

		TablePerspective a = perspectives.get(0);
		TablePerspective b = perspectives.get(1);
		TablePerspective c = perspectives.get(2);

		IDCategory a_d = a.getDataDomain().getDimensionIDCategory();
		IDCategory b_d = b.getDataDomain().getDimensionIDCategory();
		IDCategory c_d = c.getDataDomain().getDimensionIDCategory();

		TablePerspective x;
		TablePerspective l;
		TablePerspective z;
		Pair<TablePerspective, TablePerspective> lz;
		if (a_d.equals(b_d)) {
			x = c;
			lz = findLZ(x, a, b);
		} else if (a_d.equals(c_d)) {
			x = b;
			lz = findLZ(x, a, c);
		} else {
			x = a;
			lz = findLZ(x, b, c);
		}
		l = lz.getFirst();
		z = lz.getSecond();

		createBiClusterPerspectives(x, l, z);

		root.setData(perspectives);
		layoutManager.updateLayout();
	}

	private void createBiClusterPerspectives(TablePerspective x,
			TablePerspective l, TablePerspective z) {
		float sampleThreshold = 0.3f;
		float geneThreshold = 0.2f;
		ATableBasedDataDomain xdd = x.getDataDomain();
		Table xtable = xdd.getTable();
		IDType xdimtype = xdd.getDimensionIDType();
		IDType xrectype = xdd.getRecordIDType();

		Table X = x.getDataDomain().getTable();
		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		int bcCountData = L.getColumnIDList().size(); // Nr of BCs in L & Z
		int bcCountActual = 0; // Nr of BCs to display

		// Indices for Genes and Tables of a specific BiCluster.
		Map<Integer, ArrayList<Integer>> bcDimIndices = new HashMap<>();
		Map<Integer, ArrayList<Integer>> bcRecIndices = new HashMap<>();
		for (int bcNr = 0; bcNr < bcCountData; bcNr++) {
			// Scanning samples
			ArrayList<Integer> recList = new ArrayList<Integer>();
			for (int recNr = 0; recNr < L.getRowIDList().size(); recNr++) {
				if ((float) L.getRaw(bcNr, recNr) > geneThreshold) {
					recList.add(recNr);
				}
			}

			// Scanning genes
			ArrayList<Integer> dimList = new ArrayList<Integer>();
			for (int sampleNr = 0; sampleNr < Z.getRowIDList().size(); sampleNr++) {
				if ((float) Z.getRaw(bcNr, sampleNr) > sampleThreshold) {
					dimList.add(sampleNr);
				}
			}

			// only add the cluster if data is present
			if (recList.size() > 0 && dimList.size() > 0) {
				bcRecIndices.put(bcNr, recList);
				bcDimIndices.put(bcNr, dimList);
				bcCountActual++;
			}

		}

		//Debug output
//		for (Integer i: bcDimIndices.keySet()) {
//			System.out.println("Cluster: " + i);
//			System.out.println("  Dim: " + bcDimIndices.get(i));
//			System.out.println("  Rec: " + bcRecIndices.get(i));
//		}

		perspectives = new ArrayList<TablePerspective>(); //clear perspectives
		
		//actually create the cluster perspectives
		for (Integer i: bcDimIndices.keySet()) {
			System.out.println(i);
			Perspective dim = new Perspective(xdd, xdimtype);
			Perspective rec = new Perspective(xdd, xrectype);
			PerspectiveInitializationData dim_init = new PerspectiveInitializationData();
			PerspectiveInitializationData rec_init = new PerspectiveInitializationData();
			dim_init.setData(bcDimIndices.get(i));
			rec_init.setData(bcRecIndices.get(i));
			dim.init(dim_init);
			rec.init(rec_init);
			xtable.registerDimensionPerspective(dim, false);
			xtable.registerRecordPerspective(rec, false);
			String dimKey = dim.getPerspectiveID();
			String recKey = rec.getPerspectiveID();
			TablePerspective custom = xdd.getTablePerspective(recKey, dimKey,
					false);
			perspectives.add(custom);
		}

	}

	@Override
	public void display(GL2 gl) {
		checkForHits(gl);
		layoutManager.render(gl);
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedBiClusterView serializedForm = new SerializedBiClusterView(
				this);
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "BiCluster";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		listeners.register(this);
		listeners.register(AddTablePerspectivesEvent.class,
				new AddTablePerspectivesListener().setHandler(this));
		listeners.register(RemoveTablePerspectiveEvent.class,
				new RemoveTablePerspectiveListener().setHandler(this));
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();
		listeners.unregisterAll();
	}

	@Override
	protected void destroyViewSpecificContent(GL2 gl) {
		root.destroy(gl);
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.tableBased;
	}

	@Override
	public void addTablePerspective(TablePerspective newTablePerspective) {
		if (this.perspectives.contains(newTablePerspective))
			return;
		this.perspectives.add(newTablePerspective);
		if (root != null && perspectives.size() == 3)
			findXLZ();
		fireTablePerspectiveChanged();
	}

	@Override
	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
		this.perspectives.addAll(newTablePerspectives);
		if (root != null && perspectives.size() == 3)
			findXLZ();
		fireTablePerspectiveChanged();
	}

	@Override
	public List<TablePerspective> getTablePerspectives() {
		return perspectives;
	}

	@Override
	public void removeTablePerspective(int tablePerspectiveID) {
		for (Iterator<TablePerspective> it = perspectives.iterator(); it
				.hasNext();) {
			if (it.next().getID() == tablePerspectiveID)
				it.remove();
		}
		if (root != null && this.perspectives.size() < 3) {
			root.setData(null);
		}
		fireTablePerspectiveChanged();
	}

	private void fireTablePerspectiveChanged() {
		GeneralManager
				.get()
				.getEventPublisher()
				.triggerEvent(
						new TablePerspectivesChangedEvent(this).from(this));
	}

}
