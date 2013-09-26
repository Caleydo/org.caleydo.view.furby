/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.events.DimensionVAUpdateEvent;
import org.caleydo.core.data.virtualarray.events.RecordVAUpdateEvent;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.gui.util.RenameNameDialog;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.Transitions;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IHasGLLayoutData;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.MouseOverBandEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.SearchClusterEvent;
import org.caleydo.view.bicluster.physics.MyDijkstra;
import org.caleydo.view.bicluster.util.ClusterRenameEvent;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * e.g. a class for representing a cluster
 *
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public abstract class ClusterElement extends AnimatedGLElementContainer implements IGLLayout, ILabeled {
	protected static final IHasGLLayoutData GROW_UP = GLLayoutDatas.combine(new MoveTransitions.MoveTransitionBase(
			Transitions.NO, Transitions.LINEAR, Transitions.NO, Transitions.LINEAR), DEFAULT_DURATION);
	private static final Logger log = Logger.create(ClusterElement.class);

	protected static final float highOpacityFactor = 1;
	protected static final float lowOpacityFactor = 0.2f;
	protected static final float DEFAULT_Z_DELTA = 0;
	protected static final float FOCUSED_Z_DELTA = 1;
	protected static final float HOVERED_NOT_FOCUSED_Z_DELTA = 2;
	protected static final float DRAGGING_Z_DELTA = 4;

	protected final int bcNr;
	protected final TablePerspective data;
	protected final BiClustering clustering;

	protected boolean isHovered = false;
	protected boolean isHidden = false;
	protected boolean isLocked = false;

	protected float actOpacityFactor = 1;
	protected float targetOpacityfactor = 1;

	protected final Map<ClusterElement, Edge> edges = new IdentityHashMap<>();
	private int totalDimOverlaps = 0;
	private int totalRecOverlaps = 0;

	protected HeaderBar headerBar;

	private double dimSize;
	private double recSize;
	protected double scaleFactor;
	protected double minScaleFactor;
	private double preFocusScaleFactor = -1; // -1 indicator no backup

	/**
	 * delayed mouse out to avoid fast in / out delays
	 */
	private int mouseOutDelay = Integer.MAX_VALUE;

	public ClusterElement(int bcNr, TablePerspective data, BiClustering clustering) {
		setLayout(this);
		setAnimateByDefault(false);

		this.bcNr = bcNr;

		this.headerBar = new HeaderBar();
		this.add(headerBar);

		this.data = data;
		this.clustering = clustering;
		minScaleFactor = 0.25;
		updateVisibility();
		setScaleFactor(1);
		this.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				onPicked(pick);
			}
		});
	}

	protected final boolean isShowAlwaysToolBar() {
		GLRootElement p = findRootElement();
		return p != null && p.isShowAlwaysToolBar();
	}

	/**
	 * @param i
	 */
	protected final void setScaleFactor(double s) {
		if (s < minScaleFactor)
			s = minScaleFactor;
		scaleFactor = s;
	}

	/**
	 * @return the bcNr, see {@link #bcNr}
	 */
	public final int getBiClusterNumber() {
		return bcNr;
	}


	public final IDType getDimensionIDType() {
		return getDimVirtualArray().getIdType();
	}

	public final IDType getRecordIDType() {
		return getRecVirtualArray().getIdType();
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public abstract String getID();

	protected final void setZValuesAccordingToState() {
		if (isDragged()) {
			setzDelta(DRAGGING_Z_DELTA);
		} else if (isFocused()) {
			setzDelta(FOCUSED_Z_DELTA);
		} else if (isHovered) {
			setzDelta(HOVERED_NOT_FOCUSED_Z_DELTA);
		} else {
			setzDelta(DEFAULT_Z_DELTA);
		}
	}

	@Override
	protected final void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.GRAY);
		if (isFocused()) {
			g.decZ().fillRect(-20, -20, w + 80 + 20, h + 80 + 20).incZ();
		} else
			g.decZ().fillRect(-10, -10, w + 20, w + 20).incZ();
		g.color(Color.DARK_BLUE);
		super.renderPickImpl(g, w, h);
	}

	public final int minimalDistanceTo(ClusterElement other, int maxDistance) {
		GLRootElement r = findRootElement();
		return MyDijkstra.minDistance(this, other, maxDistance, r.isRecBandsEnabled(), r.isDimBandsEnabled());
	}

	@Override
	public final void layout(int deltaTimeMs) {
		updateOpacity(deltaTimeMs);
		if (mouseOutDelay != Integer.MAX_VALUE) {
			mouseOutDelay -= deltaTimeMs;
			if (mouseOutDelay <= 0) {
				mouseOutDelay = Integer.MAX_VALUE;
				mouseOut();
			}
		}
		super.layout(deltaTimeMs);

	}

	private void updateOpacity(int deltaTimeMs) {
		float delta = Math.abs(actOpacityFactor - targetOpacityfactor);
		if (delta < 0.01f) // done
			return;
		final float speed = 0.002f; // [units/ms]
		float back = actOpacityFactor;

		final float change = deltaTimeMs * speed;

		if (targetOpacityfactor < actOpacityFactor)
			actOpacityFactor = Math.max(targetOpacityfactor, actOpacityFactor - change);
		else
			actOpacityFactor = Math.min(targetOpacityfactor, actOpacityFactor + change);
		if (back != actOpacityFactor) {
			repaint();
			repaintChildren();
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Color color = null;
		if (isFocused())
			color = new Color(0, 0, 0, 1.0f);
		else if (isHovered) {
			color = SelectionType.MOUSE_OVER.getColor();
		} else
			color = new Color(0, 0, 0, actOpacityFactor);
		g.color(color);
		g.drawRect(-1, -1, w + 2, h + 2);
	}

	protected final void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!pick.isAnyDragging()) {
				if (!isHovered) {
					EventPublisher.trigger(new MouseOverClusterEvent(this, true));
					EventPublisher.trigger(new DataSetSelectedEvent(data));
					relayout(); // for showing the bars
				}
				isHovered = true;
				findAllClustersElement().setHoveredElement(this);
				mouseOutDelay = Integer.MAX_VALUE;
			}
			break;
		case MOUSE_OUT:
			if (isHovered)
				EventPublisher.trigger(new DataSetSelectedEvent(data.getDataDomain()));
			mouseOutDelay = 200;
			// mouseOut();
			break;
		case MOUSE_WHEEL:
			// zoom on CTRL+mouse wheel
			IMouseEvent event = ((IMouseEvent) pick);
			int r = (event).getWheelRotation();
			if (r != 0 && event.isCtrlDown()) {
				scaleFactor = Math.max(minScaleFactor, scaleFactor * (r > 0 ? 1.1 : 1 / 1.1));
				resize();
			}
			break;
		default:
			break;

		}
	}

	protected final void mouseOut() {
		if (isHovered && !headerBar.isClicked()) {
			isHovered = false;
			if (wasResizedWhileHovered)
				setClusterSize(newDimSize, newRecSize, this);
			targetOpacityfactor = highOpacityFactor;
			repaintChildren();
			EventPublisher.trigger(new MouseOverClusterEvent(this, false));
			relayout(); // for hiding the bars
		}
	}

	protected final AllClustersElement findAllClustersElement() {
		return findParent(AllClustersElement.class);
	}

	protected final GLRootElement findRootElement() {
		return findParent(GLRootElement.class);
	}

	public final void addEdge(ClusterElement target, Edge edge) {
		this.edges.put(target, edge);
	}

	public void updateEdge(ClusterElement to, boolean dim, boolean rec) {
		if (!dim && !rec)
			return;
		Edge edge = edges.get(to);
		if (edge == null)
			return;
		if (dim) {
			edge.updateDim();
		}
		if (rec) {
			edge.updateRec();
		}
		updateVisibility();
		edge.getOpposite(this).updateVisibility();
	}

	/**
	 *
	 */
	public void updateOutgoingEdges(boolean dim, boolean rec) {
		updateEdges(dim, rec, true);
	}

	private void updateEdges(boolean dim, boolean rec, boolean justOutgoing) {
		if (!dim && !rec)
			return;
		for (Edge edge : edges.values()) {
			if (!justOutgoing || (edge.getA() == this)) {
				updateEdge(edge.getB(), dim, rec);
			}
		}
	}

	protected void updateMyEdges(boolean dim, boolean rec) {
		updateEdges(dim, rec, false);
		onEdgeUpdateDone();
		for (Edge edge : edges.values()) {
			edge.getOpposite(this).onEdgeUpdateDone();
		}
	}

	public void onEdgeUpdateDone() {
		// nothing todo
	}

	public final void setPerspectiveLabel(String dimensionName, String recordName) {
		data.getDimensionPerspective().setLabel(dimensionName);
		data.getRecordPerspective().setLabel(recordName);
	}

	protected final void fireTablePerspectiveChanged() {
		EventPublisher.trigger(new RecordVAUpdateEvent(data.getDataDomain().getDataDomainID(), data
				.getRecordPerspective().getPerspectiveID(), this));
		EventPublisher.trigger(new DimensionVAUpdateEvent(data.getDataDomain().getDataDomainID(), data
				.getDimensionPerspective().getPerspectiveID(), this));
	}

	protected abstract VirtualArray getDimVirtualArray();

	protected abstract VirtualArray getRecVirtualArray();

	public final VirtualArray getVirtualArray(EDimension dim) {
		return dim.isHorizontal() ? getDimVirtualArray() : getRecVirtualArray();
	}

	public abstract int getDimSize();

	public abstract int getRecSize();

	public final boolean isVisible() {
		return getVisibility().doRender();
	}

	public final int getDimOverlap(ClusterElement elem) {
		if (!findRootElement().isDimBandsEnabled())
			return 0;
		Edge edge = edges.get(elem);
		return edge != null ? edge.getDimOverlap() : 0;
	}

	public final int getRecOverlap(ClusterElement elem) {
		if (!findRootElement().isRecBandsEnabled())
			return 0;
		Edge edge = edges.get(elem);
		return edge != null ? edge.getRecOverlap() : 0;
	}

	public final Iterable<ClusterElement> getOverlappingNeighbors(final EDimension dim) {
		if (!findRootElement().isBandsEnabled(dim))
			return Collections.emptyList();
		return Iterables.filter(edges.keySet(), new Predicate<ClusterElement>() {
			@Override
			public boolean apply(ClusterElement input) {
				return input != null && edges.get(input).getOverlap(dim) > 0;
			}
		});
	}

	/**
	 * @return
	 */
	public Iterable<? extends ClusterElement> getAnyOverlappingNeighbors() {
		if (!findRootElement().isDimBandsEnabled() && !findRootElement().isRecBandsEnabled())
			return Collections.emptyList();
		return Iterables.filter(edges.keySet(), new Predicate<ClusterElement>() {
			@Override
			public boolean apply(ClusterElement input) {
				if (input == null)
					return false;
				final Edge edge = edges.get(input);
				return edge.getDimOverlap() > 0 || edge.getRecOverlap() > 0;
			}
		});
	}

	public final Iterable<Edge> getOverlappingEdges(final EDimension dim) {
		if (!findRootElement().isBandsEnabled(dim))
			return Collections.emptyList();
		return Iterables.filter(edges.values(), new Predicate<Edge>() {
			@Override
			public boolean apply(Edge input) {
				return input != null && input.getOverlap(dim) > 0;
			}
		});
	}

	/**
	 * @return the totalDimOverlaps, see {@link #totalDimOverlaps}
	 */
	public final int getDimTotalOverlaps() {
		if (!findRootElement().isDimBandsEnabled())
			return 0;
		return totalDimOverlaps;
	}

	/**
	 * @param delta
	 */
	final void incTotalDimOverlap(int delta) {
		this.totalDimOverlaps += delta;
	}
	/**
	 * @return the totalRecOverlaps, see {@link #totalRecOverlaps}
	 */
	public final int getRecTotalOverlaps() {
		if (!findRootElement().isRecBandsEnabled())
			return 0;
		return totalRecOverlaps;
	}

	final void incTotalRecOverlap(int delta) {
		this.totalRecOverlaps += delta;
	}

	protected final IGLLayoutElement getIGLayoutElement() {
		return GLElementAccessor.asLayoutElement(this);
	}

	protected class HeaderBar extends PickableGLElement {

		private boolean clicked = false;

		public boolean isClicked() {
			return clicked;
		}

		public HeaderBar() {
			setzDelta(DEFAULT_Z_DELTA);
			setSize(Float.NaN, 20);
			this.setLayoutData(GROW_UP);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			super.renderImpl(g, w, h);
			if (isFocused()) {
				g.color(SelectionType.SELECTION.getColor());
				g.fillRoundedRect(0, 0, w, h, 2);
				g.textColor(Color.BLACK);
			} else if (isHovered) {
				g.color(SelectionType.MOUSE_OVER.getColor());
				g.fillRoundedRect(0, 0, w, h, 2);
			} else
				g.textColor(new Color(0, 0, 0, actOpacityFactor));

			String text = getID();
			if (scaleFactor > minScaleFactor && Math.round(scaleFactor * 100) != 100)
				text += String.format(" (%d%%)", (int) (100 * scaleFactor));
			if (isHovered)
				text = " " + text;
			g.drawText(text, 0, 0, g.text.getTextWidth(text, 12) + 2, 12);
			g.textColor(Color.BLACK);
		}

		@Override
		protected void onPicked(Pick pick) {
			switch (pick.getPickingMode()) {
			case DRAGGED:
				if (!pick.isDoDragging())
					return;
				drag(pick);
				break;
			case CLICKED:
				if (!pick.isAnyDragging()) {
					pick.setDoDragging(true);
					clicked = true;
				}
				break;
			case DOUBLE_CLICKED:
				renameCluster();
				break;
			case MOUSE_RELEASED:
				pick.setDoDragging(false);
				clicked = false;
				findAllClustersElement().setDragedElement(null);
				setzDelta(DEFAULT_Z_DELTA);
				if (isClusterCollision())
					mouseOut();
				break;
			default:
				if (!pick.isDoDragging())
					return;
				findAllClustersElement().setDragedElement(null);
			}
			setZValuesAccordingToState();
		}
	}

	private boolean isClusterCollision() {
		Vec2f mySize = getSize();
		Vec2f myLoc = getLocation();
		Rectangle myRec = new Rectangle((int) myLoc.x() - 10, (int) myLoc.y() - 10, (int) mySize.x() + 20,
				(int) mySize.y() + 20);
		for (GLElement jGLE : findAllClustersElement()) {
			ClusterElement j = (ClusterElement) jGLE;
			if (j == this)
				continue;
			Vec2f jSize = j.getSize();
			Vec2f jLoc = j.getLocation();
			Rectangle jRec = new Rectangle((int) jLoc.x() - 10, (int) jLoc.y() - 10, (int) jSize.x() + 20,
					(int) jSize.y() + 20);
			if (myRec.intersects(jRec)) {
				return true;
			}
		}
		return false;
	}

	protected void upscale() {
		scaleFactor += 0.6;
	}

	public final void renameCluster() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				String label = getLabel();
				String newlabel = RenameNameDialog.show(null, "Rename Cluster: " + label, label);
				if (newlabel != null && !label.equals(newlabel))
					EventPublisher.trigger(new ClusterRenameEvent(newlabel).to(ClusterElement.this));
			}
		});
	}

	@ListenTo(sendToMe = true)
	private void listenTo(ClusterRenameEvent e) {
		setLabel(e.getNewName());
	}

	protected void reduceScaleFactor() {
		scaleFactor -= 0.6;
		if (scaleFactor <= minScaleFactor)
			scaleFactor = minScaleFactor;
	}

	private boolean wasResizedWhileHovered = false;
	private double newRecSize = 0;
	private double newDimSize = 0;
	// whether hiding is enforced
	private boolean forceHide = false;

	public void setClusterSize(double x, double y, Object causer) {
		if ((isHovered || isLocked) && (causer != this)) {
			wasResizedWhileHovered = true;
			newRecSize = y;
			newDimSize = x;
		} else {
			wasResizedWhileHovered = false;
			newRecSize = 0;
			newDimSize = 0;
			dimSize = x;
			recSize = y;
			resize();
		}
	}

	protected void resize() {
		float targetX = (float) (dimSize * scaleFactor);
		float targetY = (float) (recSize * scaleFactor);
		setLayoutData(new Vec2f(targetX, targetY));
		IGLLayoutElement layoutElement = GLElementAccessor.asLayoutElement(this);
		if (isFocused()) {
			Vec2f size = getParent().getSize();
			targetX = Math.min(targetX, size.x() * 0.7f);
			targetY = Math.min(targetY, size.y() * 0.9f);
		}
		layoutElement.setSize(targetX, targetY);
		relayoutParent();
		relayout();
	}

	/**
	 * @return the scaleFactor, see {@link #scaleFactor}
	 */
	public double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * @return the isFocused, see {@link #isFocused}
	 */
	public final boolean isFocused() {
		return findAllClustersElement().isFocussed(this);
	}

	public final boolean isDragged() {
		return findAllClustersElement().isDragged(this);
	}

	/**
	 * generate the default scale factor to use during focus
	 *
	 * @return
	 */
	private double defaultFocusScaleFactor() {
		double s = 0;
		double ws = focusSize(getDimSize()) / dimSize;
		double hs = focusSize(getRecSize()) / recSize;
		s = Math.max(ws, hs);
		return Math.max(scaleFactor, s);
	}

	/**
	 * @param dimensionElementSize
	 * @return
	 */
	private static float focusSize(int elements) {
		if (elements < 5)
			return 20 * elements;
		else if (elements < 10)
			return 16 * elements;
		return Math.min(14 * elements, 14 * 20);
	}

	protected final void hideThisCluster() {
		if (isHidden)
			return;
		if (isHovered)
			EventPublisher.trigger(new MouseOverClusterEvent(this, false));
		isHidden = true;
		isHovered = false;
		updateVisibility();
		EventPublisher.trigger(new ClusterGetsHiddenEvent(getID()));
		relayoutParent();
	}

	public void setFocus(boolean isFocused) {
		if (isFocused) {
			preFocusScaleFactor = scaleFactor;
			scaleFactor = defaultFocusScaleFactor();
			resize();
			forceHide = false;
		} else {
			setScaleFactor(preFocusScaleFactor);
			preFocusScaleFactor = -1;
			resize();
			mouseOut();
		}
		setZValuesAccordingToState();
		updateVisibility();
		relayoutParent();
	}

	public void focusChanged(ClusterElement elem) {
		assert this != elem;
		if (this.isFocused())
			setFocus(false);
		if (elem != null) {
			if (preFocusScaleFactor < 0) {// backup
				preFocusScaleFactor = scaleFactor;
			}
			int maxDistance = findRootElement().getMaxDistance();
			int distance = minimalDistanceTo(elem, maxDistance);
			if (distance > maxDistance) {
				forceHide = true;
			} else {
				float relationship = relationshipTo(elem);
				forceHide = false;
				setScaleFactor(preFocusScaleFactor * Math.min(0.8f + relationship * 10, 2));
				resize();
			}
		} else {
			if (preFocusScaleFactor > 0)
				setScaleFactor(preFocusScaleFactor);
			preFocusScaleFactor = -1;
			resize();
			forceHide = false;
		}
		updateVisibility();
	}

	/**
	 * compute the relation ship factor (0..none, 1... itself) to the given element
	 *
	 * @param other
	 * @return
	 */
	private float relationshipTo(ClusterElement other) {
		if (other == this)
			return 1;
		GLRootElement r = findRootElement();
		final boolean dimBandsEnabled = r.isDimBandsEnabled();
		final boolean recBandsEnabled = r.isRecBandsEnabled();

		if (!dimBandsEnabled && !recBandsEnabled)
			return 0;
		int dimIntersection = getDimOverlap(other);
		int recIntersection = getRecOverlap(other);
		int intersection = dimIntersection + recIntersection;
		if (intersection == 0)
			return 0;
		int dimUnion = getDimSize() + other.getDimSize() - dimIntersection;
		int recUnion = getRecSize() + other.getRecSize() - recIntersection;
		float jaccard = 0;
		// some kind of jaccard
		if (dimBandsEnabled && recBandsEnabled)
			jaccard = (dimIntersection + recIntersection) / (float) (dimUnion + recUnion);
		else if (dimBandsEnabled)
			jaccard = (dimIntersection) / (float) (dimUnion);
		else if (recBandsEnabled)
			jaccard = (recIntersection) / (float) (recUnion);
		return jaccard;
	}

	public void show() {
		if (!isHidden)
			return;
		isHidden = false;
		updateVisibility();
		relayoutParent();
	}

	@ListenTo
	private void listenTo(MouseOverClusterEvent event) {
		if (!event.isMouseOver()) {
			targetOpacityfactor = highOpacityFactor;
			return;
		}

		ClusterElement hoveredElement = (ClusterElement) event.getSender();

		final int maxDistance = findRootElement().getMaxDistance();
		if (minimalDistanceTo(hoveredElement, maxDistance) <= maxDistance || areBandsSelected()) {
			targetOpacityfactor = highOpacityFactor;
			return;
		} else
			targetOpacityfactor = lowOpacityFactor;
	}

	/**
	 *
	 * check if any of my bands is selected
	 *
	 * @return
	 */
	private boolean areBandsSelected() {
		GLRootElement root = findRootElement();
		if (root.isRecBandsEnabled()) {
			for (Edge shared : edges.values()) {
				if (shared.getRecOverlap() == 0)
					continue;
				if (root.isAnyRecSelected(shared.getRecOverlapIndices(), SelectionType.SELECTION,
						SelectionType.MOUSE_OVER))
					return true;
			}
		}
		if (root.isDimBandsEnabled()) {
			for (Edge shared : edges.values()) {
				if (shared.getDimOverlap() == 0)
					continue;
				if (root.isAnyDimSelected(shared.getDimOverlapIndices(), SelectionType.SELECTION,
						SelectionType.MOUSE_OVER))
					return true;
			}
		}
		return false;
	}

	@ListenTo
	private void listenTo(MouseOverBandEvent event) {
		if (!event.isMouseOver()) {
			targetOpacityfactor = highOpacityFactor;
			return;
		}
		if (nearEnough(event.getFirst(), event.getSecond()) || areBandsSelected()) {
			targetOpacityfactor = highOpacityFactor;
			return;
		} else
			targetOpacityfactor = lowOpacityFactor;
	}

	/**
	 * whether the the first clusterelement is near enough to the second one given the stored maxDistance
	 *
	 * @return
	 */
	public boolean nearEnough(ClusterElement first, ClusterElement second) {
		final int maxDistance = findRootElement().getMaxDistance();

		int dist = minimalDistanceTo(first, maxDistance + 1);
		if (dist > maxDistance + 1) // unbound
			return false;
		if (dist == maxDistance) // as connected just check one side and in the corner case extrem change if the other
									// side is nearer
			dist = minimalDistanceTo(second, maxDistance);
		return dist <= maxDistance;
	}

	@ListenTo
	private void onSearchClusterEvent(SearchClusterEvent event) {
		if (event.isClear()) {
			targetOpacityfactor = highOpacityFactor;
		} else if (StringUtils.containsIgnoreCase(getLabel(), event.getText())) {
			targetOpacityfactor = highOpacityFactor;
			log.debug(getLabel() + " matches " + event.getText());
		} else {
			targetOpacityfactor = lowOpacityFactor;
			log.debug(getLabel() + " not matches " + event.getText());
		}
		repaintChildren();
	}

	protected final void updateVisibility() {
		boolean should = shouldBeVisible();
		boolean v = should && !forceHide;
		final boolean bak = getVisibility() == EVisibility.PICKABLE;
		if (v == bak)
			return;
		if (v) // reset location if become visible
			setLocation(Float.NaN, Float.NaN);
		setVisibility(v ? EVisibility.PICKABLE : EVisibility.NONE);
		// System.out.println(toString() + " " + v);
	}

	public abstract boolean shouldBeVisible();

	protected abstract void setLabel(String id);

	@Override
	public String getLabel() {
		return getID();
	}

	/**
	 * @param dimension
	 * @param overlap
	 * @return
	 */
	public List<List<Integer>> getListOfContinousSequences(EDimension dim, List<Integer> overlap) {
		return getListOfContinousIDs2(overlap, getVirtualArray(dim).getIDs());
	}

	protected static List<List<Integer>> getListOfContinousIDs(List<Integer> overlap, List<Integer> indices) {
		List<List<Integer>> sequences = new ArrayList<List<Integer>>();
		if (overlap.size() == 0)
			return sequences;
		List<Integer> accu = new ArrayList<Integer>();
		for (Integer i : indices) {
			if (overlap.contains(i)) {
				accu.add(i);
			} else if (accu.size() > 0) { // don't add empty lists
				sequences.add(accu);
				accu = new ArrayList<>();
			}
		}
		if (accu.size() > 0)
			sequences.add(accu);
		List<List<Integer>> r2 = getListOfContinousIDs2(overlap, indices);
		assert r2.equals(sequences);
		return sequences;
	}

	protected static List<List<Integer>> getListOfContinousIDs2(List<Integer> overlap, List<Integer> indices) {
		if (overlap.isEmpty())
			return Collections.emptyList();
		if (overlap.size() == indices.size()) // all
			return ImmutableList.of(indices);
		List<List<Integer>> sequences = new ArrayList<List<Integer>>(1);
		int from = 0;
		for (int i = 0; i < indices.size(); ++i) {
			Integer index = indices.get(i);
			if (!overlap.contains(index)) {
				int to = i;
				if (from < to) {
					sequences.add(indices.subList(from, to));
				}
				from = i + 1;// starting with next
			}
		}
		if (from < indices.size())
			sequences.add(indices.subList(from, indices.size()));
		assert !sequences.isEmpty();
		return ImmutableList.copyOf(sequences);
	}

	public abstract float getDimPosOf(int id);

	public abstract float getRecPosOf(int id);

	public int getDimIndexOf(int value) {
		return getDimVirtualArray().indexOf(value);
	}

	public int getRecIndexOf(int value) {
		return getRecVirtualArray().indexOf(value);
	}

	public float getDimensionElementSize() {
		return getSize().x() / getDimVirtualArray().size();
	}

	public float getRecordElementSize() {
		return getSize().y() / getRecVirtualArray().size();
	}

	public int getNrOfElements(List<Integer> band) {
		return band.size();
	}

	public Vec2f getPreferredSize(float scaleX, float scaleY) {
		return new Vec2f(getDimSize() * scaleX, getRecSize() * scaleY);
	}

	private void drag(Pick pick) {
		findAllClustersElement().setDragedElement(this);
		setzDelta(DRAGGING_Z_DELTA);
		setLocation(getLocation().x() + pick.getDx(), getLocation().y() + pick.getDy());
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClusterElement [").append(getLabel()).append(']');
		return builder.toString();
	}

}
