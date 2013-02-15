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
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.view.TablePerspectivesChangedEvent;
import org.caleydo.core.id.IDCategory;
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
import org.caleydo.core.view.opengl.layout2.AGLElementGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.bicluster.elem.GLBiClusterElement;
import org.caleydo.view.bicluster.event.ToolbarEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * <p>
 * Sample GL2 view.
 * </p>
 * <p>
 * This Template is derived from {@link ATableBasedView}, but if the view does not use a table, changing that to
 * {@link AGLView} is necessary.
 * </p>
 *
 * @author Michael Gillhofer
 * @author Marc Streit
 */

public class GLBiCluster extends AGLElementGLView implements IMultiTablePerspectiveBasedView, IGLRemoteRenderingView {
	public static final String VIEW_TYPE = "org.caleydo.view.bicluster";
	public static final String VIEW_NAME = "BiCluster Visualization";

	private final List<TablePerspective> perspectives = new ArrayList<>();

	private Model model;

	private TablePerspective x;
	private TablePerspective l;
	private TablePerspective z;

	/**
	 * Constructor.
	 *
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 *
	 */
	public GLBiCluster(IGLCanvas glCanvas, Composite parentComposite, ViewFrustum viewFrustum) {
		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);
		this.textureManager = new TextureManager(Activator.getResourceLoader());
		this.model = Model.getModel();
	}

	@Override
	public void init(GL2 gl) {
		super.init(gl);

		if (this.perspectives.size() >= 3) {
			findXLZ();
		}

		detailLevel = EDetailLevel.HIGH;
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
	 * determines which of the given {@link TablePerspective} is L and Z, given the known X table
	 *
	 * @param a
	 * @param b
	 */
	private Pair<TablePerspective, TablePerspective> findLZ(TablePerspective x, TablePerspective a, TablePerspective b) {
		// row: gene, row: gene
		if (a.getDataDomain().getRecordIDCategory().equals(x.getDataDomain().getRecordIDCategory())) {
			return Pair.make(a, b);
		} else {
			return Pair.make(b, a);
		}
	}

	/**
	 * infers the X,L,Z tables from the dimension and record ID categories, as L and Z has the same Dimension ID
	 * Category, the remaining one will be X
	 */
	private void findXLZ() {

		TablePerspective a = perspectives.get(0);
		TablePerspective b = perspectives.get(1);
		TablePerspective c = perspectives.get(2);

		IDCategory a_d = a.getDataDomain().getDimensionIDCategory();
		IDCategory b_d = b.getDataDomain().getDimensionIDCategory();
		IDCategory c_d = c.getDataDomain().getDimensionIDCategory();

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

		getRoot().setData(model.createBiClusterPerspectives(x, l, z));
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedBiClusterView serializedForm = new SerializedBiClusterView(this);
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
		eventListeners.register(AddTablePerspectivesEvent.class, new AddTablePerspectivesListener().setHandler(this));
		eventListeners.register(RemoveTablePerspectiveEvent.class,
				new RemoveTablePerspectiveListener().setHandler(this));
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
		if (getRoot() != null && perspectives.size() == 3)
			findXLZ();
		fireTablePerspectiveChanged();
	}

	@Override
	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
		this.perspectives.addAll(newTablePerspectives);
		if (getRoot() != null && perspectives.size() == 3)
			findXLZ();
		fireTablePerspectiveChanged();
	}

	@Override
	public List<TablePerspective> getTablePerspectives() {
		return perspectives;
	}

	@Override
	protected GLBiClusterElement getRoot() {
		return (GLBiClusterElement) super.getRoot();
	}

	@Override
	protected GLElement createRoot() {
		return new GLBiClusterElement(this);
	}

	@Override
	public void removeTablePerspective(int tablePerspectiveID) {
		for (Iterator<TablePerspective> it = perspectives.iterator(); it.hasNext();) {
			if (it.next().getID() == tablePerspectiveID)
				it.remove();
		}
		if (getRoot() != null && this.perspectives.size() < 3) {
			getRoot().setData(null);
		}
		fireTablePerspectiveChanged();
	}

	private void fireTablePerspectiveChanged() {
		GeneralManager.get().getEventPublisher().triggerEvent(new TablePerspectivesChangedEvent(this).from(this));
	}

	@ListenTo
	private void handleUpdate(ToolbarEvent event) {
		model.setGeneThreshold(event.getGeneThreshold());
		model.setSampleThreshold(event.getSampleThreshold());
		if (perspectives.size() >= 3) {
			long time = System.currentTimeMillis();
			findXLZ();
			System.out.println("Updated in: " + (System.currentTimeMillis() - time) / 1000. + "s");
			// detailLevel = EDetailLevel.HIGH;
		}
	}
}
