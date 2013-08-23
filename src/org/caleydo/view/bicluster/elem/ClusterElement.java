/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.UNBOUND_NUMBER;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getDimTopNElements;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecThreshold;
import static org.caleydo.view.bicluster.internal.prefs.MyPreferences.getRecTopNElements;
import gleem.linalg.Vec2f;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.Transitions;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IHasGLLayoutData;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.bicluster.event.ChangeMaxDistanceEvent;
import org.caleydo.view.bicluster.event.ClusterGetsHiddenEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.LZThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MinClusterSizeThresholdChangeEvent;
import org.caleydo.view.bicluster.event.MouseOverBandEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.SearchClusterEvent;
import org.caleydo.view.bicluster.event.SortingChangeEvent.SortingType;
import org.caleydo.view.bicluster.internal.prefs.MyPreferences;
import org.caleydo.view.bicluster.physics.MyDijkstra;
import org.caleydo.view.bicluster.util.ClusterRenameEvent;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Iterables;

/**
 * e.g. a class for representing a cluster
 *
 * @author Michael Gillhofer
 * @author Samuel Gratzl
 */
public abstract class ClusterElement extends AnimatedGLElementContainer implements IGLLayout, ILabeled {
	protected static final IHasGLLayoutData GROW_LEFT = GLLayoutDatas.combine(new MoveTransitions.MoveTransitionBase(
			Transitions.LINEAR, Transitions.NO, Transitions.LINEAR, Transitions.NO), DEFAULT_DURATION);
	protected static final IHasGLLayoutData GROW_UP = GLLayoutDatas.combine(new MoveTransitions.MoveTransitionBase(
			Transitions.NO, Transitions.LINEAR, Transitions.NO, Transitions.LINEAR), DEFAULT_DURATION);

	protected static final float highOpacityFactor = 1;
	protected static final float lowOpacityFactor = 0.2f;
	protected static final float DEFAULT_Z_DELTA = 0;
	protected static final float FOCUSED_Z_DELTA = 1;
	protected static final float HOVERED_NOT_FOCUSED_Z_DELTA = 2;
	protected static final float DRAGGING_Z_DELTA = 4;

	protected final TablePerspective data;
	protected final BiClustering clustering;

	protected boolean isDragged = false;
	protected boolean isHovered = false;
	protected boolean isHidden = false;
	protected boolean isLocked = false;
	protected boolean hasContent = false;
	protected boolean dimBandsEnabled, recBandsEnabled;
	protected float curOpacityFactor = 1f;
	protected float opacityfactor = 1;

	protected Map<GLElement, List<Integer>> dimOverlap, recOverlap;

	protected SortingType sortingType = SortingType.probabilitySorting;
	protected List<Integer> dimProbabilitySorting;
	protected List<Integer> recProbabilitySorting;

	protected int bcNr = -1;
	protected HeaderBar headerBar;

	protected float recThreshold = getRecThreshold();
	protected int recNumberThreshold = getRecTopNElements();
	protected float dimThreshold = getDimThreshold();
	protected int dimNumberThreshold = getDimTopNElements();
	protected double clusterSizeThreshold;
	protected double elementCountBiggestCluster;

	int dimensionOverlapSize;
	int recordOverlapSize;
	private double dimSize;
	private double recSize;
	protected double scaleFactor;
	protected double minScaleFactor;
	private double preFocusScaleFactor = -1; // -1 indicator no backup
	private boolean isFocused = false;

	private int maxDistance = MyPreferences.getMaxDistance();

	/**
	 * delayed mouse out to avoid fast in / out delays
	 */
	private int mouseOutDelay = Integer.MAX_VALUE;

	public ClusterElement(TablePerspective data, BiClustering clustering) {
		setLayout(this);
		setAnimateByDefault(false);

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
		AllClustersElement p = findAllClustersElement();
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

	public final IDCategory getRecordIDCategory() {
		return data.getDataDomain().getRecordIDCategory();
	}

	public final IDCategory getDimensionIDCategory() {
		return data.getDataDomain().getDimensionIDCategory();
	}

	public final IDType getDimensionIDType() {
		return getDimensionVirtualArray().getIdType();
	}

	public final IDType getRecordIDType() {
		return getRecordVirtualArray().getIdType();
	}

	public final String getDataDomainID() {
		return data.getDataDomain().getDataDomainID();
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public abstract String getID();

	protected final void setZValuesAccordingToState() {
		if (isDragged) {
			setzDelta(DRAGGING_Z_DELTA);
		} else if (isFocused) {
			setzDelta(FOCUSED_Z_DELTA);
		} else if (isHovered) {
			setzDelta(HOVERED_NOT_FOCUSED_Z_DELTA);
		} else {
			setzDelta(DEFAULT_Z_DELTA);
		}
	}

	@Override
	protected final void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.BLACK);
		if (isFocused) {
			g.fillRect(-20, -20, w + 80 + 20, h + 80 + 20);
		} else
			g.fillRect(-10, -10, w + 20, w + 20);
		super.renderPickImpl(g, w, h);
	}

	public int minimalDistanceTo(ClusterElement other, int maxDistance) {
		return MyDijkstra.minDistance(this, other, maxDistance, this.recBandsEnabled, this.dimBandsEnabled);
	}

	@Override
	public final void layout(int deltaTimeMs) {
		updateOpacticy(deltaTimeMs);
		if (mouseOutDelay != Integer.MAX_VALUE) {
			mouseOutDelay -= deltaTimeMs;
			if (mouseOutDelay <= 0) {
				mouseOutDelay = Integer.MAX_VALUE;
				mouseOut();
			}
		}
		super.layout(deltaTimeMs);

	}

	private void updateOpacticy(int deltaTimeMs) {
		float delta = Math.abs(curOpacityFactor - opacityfactor);
		if (delta < 0.01f) // done
			return;
		final float speed = 0.002f; // [units/ms]
		float back = curOpacityFactor;

		final float change = deltaTimeMs * speed;

		if (opacityfactor < curOpacityFactor)
			curOpacityFactor = Math.max(opacityfactor, curOpacityFactor - change);
		else
			curOpacityFactor = Math.min(opacityfactor, curOpacityFactor + change);
		if (back != curOpacityFactor) {
			repaint();
			repaintChildren();
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Color color = null;
		if (isFocused)
			color = new Color(0, 0, 0, 1.0f);
		else if (isHovered) {
			color = SelectionType.MOUSE_OVER.getColor();
		} else
			color = new Color(0, 0, 0, curOpacityFactor);
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
				setClusterSize(newDimSize, newRecSize, elementCountBiggestCluster, this);
			opacityfactor = highOpacityFactor;
			repaintChildren();
			EventPublisher.trigger(new MouseOverClusterEvent(this, false));
			relayout(); // for hiding the bars
		}
	}

	protected abstract void recreateVirtualArrays(List<Integer> dimIndices, List<Integer> recIndices);

	private AllClustersElement findAllClustersElement() {
		return findParent(AllClustersElement.class);
	}

	final void calculateOverlap(boolean dimBandsEnabled, boolean recBandsEnabled) {
		this.dimBandsEnabled = dimBandsEnabled;
		this.recBandsEnabled = recBandsEnabled;
		dimOverlap = new HashMap<>();
		recOverlap = new HashMap<>();
		List<Integer> myDimIndizes = getDimensionVirtualArray().getIDs();
		List<Integer> myRecIndizes = getRecordVirtualArray().getIDs();
		dimensionOverlapSize = 0;
		recordOverlapSize = 0;
		for (GLElement element : findAllClustersElement()) {
			if (element == this)
				continue;
			ClusterElement e = (ClusterElement) element;
			List<Integer> eIndizes = null;
			if (dimBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myDimIndizes);
				eIndizes.retainAll(e.getDimensionVirtualArray().getIDs());
				if (!eIndizes.isEmpty())
					dimOverlap.put(element, eIndizes);
				dimensionOverlapSize += eIndizes.size();
			}
			if (recBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myRecIndizes);
				eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
				if (!eIndizes.isEmpty())
					recOverlap.put(element, eIndizes);
				recordOverlapSize += eIndizes.size();
			}
		}
		if (getVisibility() == EVisibility.PICKABLE)
			sort(sortingType);
		fireTablePerspectiveChanged();
		updateVisibility();
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

	protected abstract VirtualArray getDimensionVirtualArray();

	protected abstract VirtualArray getRecordVirtualArray();

	public abstract int getNumberOfDimElements();

	public abstract int getNumberOfRecElements();

	public final boolean isDragged() {
		return isDragged;
	}

	public final boolean isVisible() {
		return getVisibility().doRender();
	}

	public final List<Integer> getDimOverlap(GLElement jElement) {
		if (dimOverlap.containsKey(jElement))
			return dimOverlap.get(jElement);
		return Collections.emptyList();
	}

	/**
	 * @return
	 */
	public Iterable<ClusterElement> getDimOverlappingNeighbors() {
		return Iterables.filter(dimOverlap.keySet(), ClusterElement.class);
	}

	/**
	 * @return
	 */
	public Iterable<ClusterElement> getRecOverlappingNeighbors() {
		return Iterables.filter(recOverlap.keySet(), ClusterElement.class);
	}

	public final List<Integer> getRecOverlap(GLElement jElement) {
		if (recOverlap.containsKey(jElement))
			return recOverlap.get(jElement);
		return Collections.emptyList();
	}

	public final int getDimensionOverlapSize() {
		return dimensionOverlapSize;
	}

	public final int getRecordOverlapSize() {
		return recordOverlapSize;
	}

	protected IGLLayoutElement getIGLayoutElement() {
		return GLElementAccessor.asLayoutElement(this);
	}

	protected class HeaderBar extends GLButton implements ISelectionCallback {

		private boolean clicked = false;

		public boolean isClicked() {
			return clicked;
		}

		public HeaderBar() {
			setzDelta(DEFAULT_Z_DELTA);
			createButtons();
			setSize(Float.NaN, 20);
			this.setLayoutData(GROW_UP);
		}

		protected void createButtons() {
			setRenderer(new IGLRenderer() {

				@Override
				public void render(GLGraphics g, float w, float h, GLElement parent) {
					if (isFocused) {
						g.color(SelectionType.SELECTION.getColor());
						g.fillRoundedRect(0, 0, w, h, 2);
						g.textColor(Color.BLACK);
					} else if (isHovered) {
						g.color(SelectionType.MOUSE_OVER.getColor());
						g.fillRoundedRect(0, 0, w, h, 2);
					} else
						g.textColor(new Color(0, 0, 0, curOpacityFactor));

					String text = getID();
					if (scaleFactor > minScaleFactor && Math.round(scaleFactor * 100) != 100)
						text += String.format(" (%d%%)", (int) (100 * scaleFactor));
					if (isHovered)
						text = " " + text;
					g.drawText(text, 0, 0, g.text.getTextWidth(text, 12) + 2, 12);
					g.textColor(Color.BLACK);

				}
			});
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
				isDragged = false;
				findAllClustersElement().setDragedLayoutElement(null);
				setzDelta(DEFAULT_Z_DELTA);
				if (isClusterCollision())
					mouseOut();
				break;
			default:
				if (!pick.isDoDragging())
					return;
				isDragged = false;
				findAllClustersElement().setDragedLayoutElement(null);
			}
			setZValuesAccordingToState();
		}

		@Override
		public void onSelectionChanged(GLButton button, boolean selected) {
			// TODO Auto-generated method stub

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

	public void setClusterSize(double x, double y, double maxClusterSize, Object causer) {
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
		elementCountBiggestCluster = maxClusterSize;
	}

	protected void resize() {
		setSize((float) (dimSize * scaleFactor), (float) (recSize * scaleFactor));
		relayout();
	}

	/**
	 * @return the isFocused, see {@link #isFocused}
	 */
	public boolean isFocused() {
		return isFocused;
	}

	protected void handleFocus(boolean isFocused) {
		if (isFocused) {
			preFocusScaleFactor = scaleFactor;
			scaleFactor = defaultFocusScaleFactor();
			resize();
		} else {
			setScaleFactor(preFocusScaleFactor);
			preFocusScaleFactor = -1;
			resize();
			mouseOut();
		}
	}

	/**
	 * generate the default scale factor to use during focus
	 *
	 * @return
	 */
	private double defaultFocusScaleFactor() {
		double s = 0;
		double ws = focusSize(getNumberOfDimElements()) / dimSize;
		double hs = focusSize(getNumberOfRecElements()) / recSize;
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
		isHidden = true;
		isHovered = false;
		updateVisibility();
		EventPublisher.trigger(new ClusterGetsHiddenEvent(getID()));
		EventPublisher.trigger(new MouseOverClusterEvent(this, false));
		relayout();
	}

	@ListenTo
	private void listenTo(FocusChangeEvent e) {
		if (e.getSender() == this) {
			this.isFocused = e.gotFocus();
			handleFocus(this.isFocused);
		} else {
			if (this.isFocused) {
				this.isFocused = false;
				handleFocus(false);
			}
			if (e.gotFocus()) {
				if (preFocusScaleFactor < 0) {// backup
					preFocusScaleFactor = scaleFactor;
				}
				ClusterElement other = (ClusterElement) e.getSender();
				int distance = minimalDistanceTo(other, maxDistance);
				if (distance > maxDistance)
					setVisibility(EVisibility.NONE);
				else {
					float relationship = relationshipTo(other);
					updateVisibility();
					setScaleFactor(preFocusScaleFactor * Math.min(0.8f + relationship * 10, 2));
					resize();
				}
			} else {
				updateVisibility();
				setScaleFactor(preFocusScaleFactor);
				preFocusScaleFactor = -1;
				resize();
			}
		}
	}

	private boolean anyFocussed() {
		return preFocusScaleFactor >= 0;
	}

	@ListenTo
	private void onChangeMaxDistanceEvent(ChangeMaxDistanceEvent event) {
		this.maxDistance = event.getMaxDistance();
		if (anyFocussed() && !isFocused) {
			ClusterElement focussed = findAllClustersElement().findFocused();
			listenTo(new FocusChangeEvent(focussed, true));
		}
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
		if (!dimBandsEnabled && !recBandsEnabled)
			return 0;
		int dimIntersection = getDimOverlap(other).size();
		int recIntersection = getRecOverlap(other).size();
		int intersection = dimIntersection + recIntersection;
		if (intersection == 0)
			return 0;
		int dimUnion = getNumberOfDimElements() + other.getNumberOfDimElements() - dimIntersection;
		int recUnion = getNumberOfRecElements() + other.getNumberOfRecElements() - recIntersection;
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
	}

	@ListenTo
	private void listenTo(MouseOverClusterEvent event) {
		if (!event.isMouseOver()) {
			opacityfactor = highOpacityFactor;
			return;
		}

		ClusterElement hoveredElement = (ClusterElement) event.getSender();

		if (minimalDistanceTo(hoveredElement, maxDistance) <= maxDistance || areBandsSelected()) {
			opacityfactor = highOpacityFactor;
			return;
		} else
			opacityfactor = lowOpacityFactor;
	}

	/**
	 *
	 * check if any of my bands is selected
	 * 
	 * @return
	 */
	private boolean areBandsSelected() {
		GLRootElement root = findParent(GLRootElement.class);

		if (recBandsEnabled) {
			for (List<Integer> shared : recOverlap.values()) {
				if (root.isAnyRecSelected(shared, SelectionType.SELECTION, SelectionType.MOUSE_OVER))
					return true;
			}
		}
		if (dimBandsEnabled) {
			for (List<Integer> shared : dimOverlap.values()) {
				if (root.isAnyDimSelected(shared, SelectionType.SELECTION, SelectionType.MOUSE_OVER))
					return true;
			}
		}
		return false;
	}

	@ListenTo
	private void listenTo(MouseOverBandEvent event) {
		if (!event.isMouseOver()) {
			opacityfactor = highOpacityFactor;
			return;
		}
		if (nearEnough(event.getFirst(), event.getSecond()) || areBandsSelected()) {
			opacityfactor = highOpacityFactor;
			return;
		} else
			opacityfactor = lowOpacityFactor;
	}

	/**
	 * @param first
	 * @param second
	 * @return
	 */
	public boolean nearEnough(ClusterElement first, ClusterElement second) {
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
			opacityfactor = highOpacityFactor;
		} else if (StringUtils.containsIgnoreCase(getLabel(), event.getText())) {
			opacityfactor = highOpacityFactor;
			System.out.println(getLabel() + " matches " + event.getText());
		} else {
			opacityfactor = lowOpacityFactor;
			System.out.println(getLabel() + " not matches " + event.getText());
		}
		repaintChildren();
	}

	@ListenTo
	private void listenTo(LZThresholdChangeEvent event) {
		if (getID().contains("Special")) {
			System.out.println("Threshold Change");
		}
		if (!event.isGlobalEvent()) {
			return;
		}
		if (bcNr == 0) {
			System.out.println(recThreshold + " " + dimThreshold + " " + recNumberThreshold + " " + dimNumberThreshold);
		}
		recThreshold = event.getRecordThreshold();
		dimThreshold = event.getDimensionThreshold();
		recNumberThreshold = event.getRecordNumberThreshold();
		dimNumberThreshold = event.getDimensionNumberThreshold();
		if (bcNr == 0) {
			System.out.println(recThreshold + " " + dimThreshold + " " + recNumberThreshold + " " + dimNumberThreshold);
		}
		rebuildMyData(event.isGlobalEvent());

	}

	@ListenTo
	private void listenTo(MinClusterSizeThresholdChangeEvent event) {
		this.clusterSizeThreshold = event.getMinClusterSize();
		updateVisibility();
	}

	/**
	 *
	 */
	protected final void updateVisibility() {
		setVisibility(shouldBeVisible() ? EVisibility.PICKABLE : EVisibility.NONE);
	}

	public abstract boolean shouldBeVisible();

	public abstract void setData(List<Integer> dimIndices, List<Integer> recIndices, String id, int bcNr,
			double maxDim, double maxRec, double minDim, double minRec);

	protected abstract void setLabel(String id);

	@Override
	public String getLabel() {
		return getID();
	}

	protected abstract void setHasContent(List<Integer> dimIndices, List<Integer> recIndices);

	protected abstract void sort(SortingType type);

	public List<List<Integer>> getListOfContinousRecSequenzes(List<Integer> overlap) {
		return getListOfContinousIDs(overlap, getRecordVirtualArray().getIDs());
	}

	public List<List<Integer>> getListOfContinousDimSequences(List<Integer> overlap) {
		return getListOfContinousIDs(overlap, getDimensionVirtualArray().getIDs());
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
		return sequences;
	}

	public abstract float getDimPosOf(int index);

	public abstract float getRecPosOf(int index);

	protected abstract void rebuildMyData(boolean isGlobal);

	public int getDimIndexOf(int value) {
		return getDimensionVirtualArray().indexOf(value);
	}

	public int getRecIndexOf(int value) {
		return getRecordVirtualArray().indexOf(value);
	}

	public float getDimensionElementSize() {
		return getSize().x() / getDimensionVirtualArray().size();
	}

	public float getRecordElementSize() {
		return getSize().y() / getRecordVirtualArray().size();
	}

	public int getNrOfElements(List<Integer> band) {
		return band.size();
	}

	public final TablePerspective getTablePerspective() {
		return data;
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClusterElement [").append(getLabel());
		builder.append("]");
		return builder.toString();
	}
	public Vec2f getPreferredSize(float scaleX, float scaleY) {
		return new Vec2f(getNumberOfDimElements() * scaleX, getNumberOfRecElements() * scaleY);
	}

	private void drag(Pick pick) {
		if (isDragged == false) {
			findAllClustersElement().setDragedLayoutElement(this);
		}
		isDragged = true;
		setzDelta(DRAGGING_Z_DELTA);
		setLocation(getLocation().x() + pick.getDx(), getLocation().y() + pick.getDy());
		relayout();
		repaintPick();
	}

	protected static void addAll(VirtualArray array, List<Integer> indices, int treshold) {
		array.clear();
		if (treshold == UNBOUND_NUMBER) // unbound flush all
			array.addAll(indices);
		else
			// sublist of the real elements
			array.addAll(indices.subList(0, Math.min(indices.size(), treshold)));
	}
}
