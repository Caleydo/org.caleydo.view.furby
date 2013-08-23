/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.core.view.opengl.util.gleem.ColoredVec3f;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.MouseOverBandEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

/**
 * @author Michael Gillhofer
 *
 */
public abstract class BandElement extends PickableGLElement implements IPickingLabelProvider {

	private static final float HIGH_OPACITY_FACTPOR = 1;
	private static final float LOW_OPACITY_FACTOR = 0.15f;

	private static final float DEFAULT_Z_DELTA = -4;
	private static final float HOVERED_BACKGROUND_Z_DELTA = -3;
	private static final float SELECTED_Z_DELTA = -2;
	private static final float HOVERED_Z_DELTA = -1;

	protected final ClusterElement first, second;
	protected List<Integer> overlap;
	private boolean hasSharedElementsWithSelection;
	private boolean hasSharedElementsWithHover;

	protected final SelectionManager selectionManager;
	private final IIDTypeMapper<Integer, String> id2label;

	protected AllBandsElement root;

	protected BandFactory secondMergeArea, bandFactory;
	protected Map<List<Integer>, Band> splittedBands, nonSplittedBands;
	protected Map<Integer, List<Vec2f>> splines;
	protected List<List<Integer>> firstSubIndices, secondSubIndices;
	protected Color highlightColor, hoveredColor, defaultColor;
	protected boolean isMouseOver = false;
	protected boolean isAnyThingHovered = false;

	protected PickingPool pickingPool;
	protected IPickingListener pickingListener;
	private int currSelectedSplineID = -1;

	private float opacityFactor = 1;

	protected BandElement(GLElement first, GLElement second, List<Integer> list,
			SelectionManager selectionManager,
			AllBandsElement root, float[] defaultColor, IDType idType) {
		this.first = (ClusterElement) first;
		this.second = (ClusterElement) second;
		this.overlap = toFastOverlap(list);
		this.id2label = IDMappingManagerRegistry.get().getIDMappingManager(idType)
				.getIDTypeMapper(idType, idType.getIDCategory().getHumanReadableIDType());
		this.root = root;
		this.selectionManager = selectionManager;
		this.defaultColor = new Color(defaultColor);
		hasSharedElementsWithSelection = false;
		hasSharedElementsWithHover = false;
		highlightColor = SelectionType.SELECTION.getColor();
		hoveredColor = SelectionType.MOUSE_OVER.getColor();
		setZDeltaAccordingToState();
		initBand();
	}

	protected static ImmutableList<Integer> toFastOverlap(List<Integer> list) {
		return ImmutableSortedSet.copyOf(list).asList();
	}

	/**
	 * @return the first, see {@link #first}
	 */
	public final ClusterElement getFirst() {
		return first;
	}

	/**
	 * @return the second, see {@link #second}
	 */
	public final ClusterElement getSecond() {
		return second;
	}

	@Override
	protected void init(IGLElementContext context) {
		pickingListener = new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onSplinePicked(pick);
			}
		};
		pickingPool = new PickingPool(context, PickingListenerComposite.concat(pickingListener, context.getSWTLayer()
				.createTooltip(this)));
		super.init(context);
	}
	@Override
	protected void takeDown() {
		pickingPool.clear();
		super.takeDown();
	}

	private void setZDeltaAccordingToState() {
		float oldZ = getzDelta();
		float z = DEFAULT_Z_DELTA;
		if (hasSharedElementsWithHoveredBand())
			z = HOVERED_BACKGROUND_Z_DELTA;
		if (hasSharedElementsWithSelectedBand())
			z = SELECTED_Z_DELTA;
		if (isMouseOver)
			z = HOVERED_Z_DELTA;
		if (oldZ != z) {
			setzDelta(z);
			root.triggerResort();
		}
	}

	@Override
	public String getLabel(Pick pick) {
		int id = pick.getObjectID() - 1;
		Set<String> labels = id2label.apply(id);
		return StringUtils.join(labels, ",");
	}

	protected void onSplinePicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case CLICKED:
			selectElement(SelectionType.SELECTION, pick.getObjectID(), true);
			repaint();
			break;
		case MOUSE_OVER:
			this.currSelectedSplineID = pick.getObjectID();
			repaint();
			break;
		case MOUSE_OUT:
			this.currSelectedSplineID = -1;
			repaint();
			break;
		default:
			break;
		}
	}
	@Override
	protected void onPicked(Pick pick) {
		switch (pick.getPickingMode()) {
		case CLICKED:
			onClicked(pick);
			break;
		case MOUSE_OUT:
			onMouseOut(pick);
			break;
		case MOUSE_OVER:
			onMouseOver(pick);
			break;
		default:
			return;
		}
		setZDeltaAccordingToState();
	}

	protected abstract void initBand();

	@Override
	public void layout(int deltaTimeMs) {
		updateOpacticy(deltaTimeMs);
		super.layout(deltaTimeMs);
	}

	private void updateOpacticy(int deltaTimeMs) {
		float delta = Math.abs(curOpacityFactor - opacityFactor);
		if (delta < 0.01f) // done
			return;
		final float speed = 0.003f; // [units/ms]
		float back = curOpacityFactor;

		final float change = deltaTimeMs * speed;

		if (opacityFactor < curOpacityFactor)
			curOpacityFactor = Math.max(opacityFactor, curOpacityFactor - change);
		else
			curOpacityFactor = Math.min(opacityFactor, curOpacityFactor + change);
		if (back != curOpacityFactor) {
			repaint();
		}
	}

	protected final void updateVisibilityByOverlap() {
		if (overlap.size() > 0)
			setVisibility(EVisibility.PICKABLE);
		else
			setVisibility(EVisibility.NONE);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		Color bandColor;
		if (isVisible()) {
			if (hasSharedElementsWithSelectedBand())
				bandColor = highlightColor;
			else if (hasSharedElementsWithHoveredBand())
				bandColor = hoveredColor;
			else
				bandColor = defaultColor;
			if (isMouseOver) {
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.8f * curOpacityFactor);
				for (Band b : splittedBands.values()) {
					g.drawPath(b);
				}
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.5f * curOpacityFactor);
				for (Band b : splittedBands.values()) {
					g.fillPolygon(b);
				}

				g.color(bandColor.r, bandColor.g, bandColor.b, 0.25f * curOpacityFactor);
				List<Vec2f> currSelectedSpline = splines.get(currSelectedSplineID - 1);
				for (List<Vec2f> b : splines.values()) {
					if (b == currSelectedSpline)
						continue;
					g.drawPath(b, false);
				}
				if (currSelectedSpline != null) {
					g.color(0, 0, 0, 0.85f * curOpacityFactor).lineWidth(2);
					g.drawPath(currSelectedSpline, false);
					g.lineWidth(1);
				}
			} else {
				Color col = bandColor.clone();
				col.a = 0.8f;
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.8f * curOpacityFactor);
				Collection<Band> stubBands;
				if (!hasSelections())
					// stub only if we haven't any highlights
					stubBands = stubify(nonSplittedBands.values(), col, curOpacityFactor, HIGH_OPACITY_FACTPOR);
				else {
					stubBands = nonSplittedBands.values();
				}
				for (Band b : stubBands) {
					g.drawPath(b);
				}
				g.color(bandColor.r, bandColor.g, bandColor.b, 0.5f * curOpacityFactor);
				for (Band b : stubBands) {
					g.fillPolygon(b);
				}
			}
		}
	}


	protected boolean isVisible() {
		return first.isVisible() && second.isVisible() && overlap != null;
	}

	protected boolean hasSharedElementsWithSelectedBand() {
		return hasSharedElementsWithSelection;
	}

	protected boolean hasSharedElementsWithHoveredBand() {
		return hasSharedElementsWithHover;
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (getVisibility() == EVisibility.PICKABLE && !isAnyThingHovered && isVisible()) {
			g.color(defaultColor);
			if (isMouseOver) {
				for (Band b : splittedBands.values())
					g.fillPolygon(b);
				g.incZ();
				for (Integer elementIndex : splines.keySet()) {
					g.pushName(pickingPool.get(elementIndex + 1));
					g.fillPolygon(splines.get(elementIndex));
					g.popName();
				}
				g.decZ();
			} else {
				if (nonSplittedBands != null)
					for (Band b : nonSplittedBands.values())
						g.fillPolygon(b);
			}
		}
	}

	@Override
	protected void onClicked(Pick pick) {
		if (hasSharedElementsWithSelectedBand()) { // disable the selection again
			hasSharedElementsWithSelection = false;
			root.setSelection(null);
			selectElement(SelectionType.SELECTION, -1, true);
		} else {
			hasSharedElementsWithSelection = true;
			root.setSelection(this);
			selectElement(SelectionType.SELECTION, -1, true);
		}
	}

	protected void selectElement(SelectionType type, int objectId, boolean select) {
		if (selectionManager == null)
			return;
		root.clearAll(type);
		if (select) {
			if (objectId <= 0)
				selectionManager.addToType(type, overlap);
			else
				selectionManager.addToType(type, objectId - 1);
		}
		root.fireAllSelections(this);
	}

	public void deselect() {
		hasSharedElementsWithSelection = false;
		setZDeltaAccordingToState();
	}

	@Override
	protected void onMouseOver(Pick pick) {
		isMouseOver = true;
		selectElement(SelectionType.MOUSE_OVER, -1, true);
		hasSharedElementsWithHover = true;
		EventPublisher.trigger(new MouseOverBandEvent(this, true));
		repaintAll();
	}

	@Override
	protected void onMouseOut(Pick pick) {
		isMouseOver = false;
		hasSharedElementsWithHover = false;
		this.currSelectedSplineID = -1;
		EventPublisher.trigger(new MouseOverBandEvent(this, false));
		selectElement(SelectionType.MOUSE_OVER, -1, false);
		repaintAll();
	}

	public abstract void updateStructure();

	public abstract void updatePosition();

	public void onSelectionUpdate(SelectionManager manager) {
		if (manager != selectionManager)
			return;
		hasSharedElementsWithHover = containsAny(selectionManager.getElements(SelectionType.MOUSE_OVER));
		hasSharedElementsWithSelection = containsAny(selectionManager.getElements(SelectionType.SELECTION));
		setZDeltaAccordingToState();
	}

	/**
	 * @param elements
	 * @return
	 */
	private boolean containsAny(Iterable<Integer> elements) {
		for (Integer elem : elements)
			if (overlap.contains(elem))
				return true;
		return false;
	}

	protected float curOpacityFactor = 1;

	@ListenTo
	private void listenTo(MouseOverClusterEvent event) {
		isAnyThingHovered = event.isMouseOver();
		if (event.getSender() == first || event.getSender() == second)
			return;
		else if (event.isMouseOver() && !hasSelections())
			opacityFactor = LOW_OPACITY_FACTOR;
		else
			opacityFactor = HIGH_OPACITY_FACTPOR;
		setZDeltaAccordingToState();
	}

	@ListenTo
	private void listenTo(MouseOverBandEvent event) {
		if (event.getSender() == this)
			return;
		isAnyThingHovered = event.isMouseOver();
		boolean fadeOut = event.isMouseOver() && !hasSelections();
		if (fadeOut) {
			opacityFactor = LOW_OPACITY_FACTOR;
			// if (isOtherType(event.getBand()))
			// setVisibility(EVisibility.NONE);
		} else {
			opacityFactor = HIGH_OPACITY_FACTPOR;
			// updateVisibilityByOverlap();
		}
		setZDeltaAccordingToState();
	}

	// /**
	// * whether the bands are from different types
	// *
	// * @param band
	// * @return
	// */
	// private boolean isOtherType(BandElement band) {
	// return !band.getClass().equals(this.getClass());
	// }

	private boolean hasSelections() {
		return hasSharedElementsWithHoveredBand() || hasSharedElementsWithSelectedBand();
	}

	protected Pair<Vec3f, Vec3f> pair(float x1, float y1, float x2, float y2) {
		Vec3f _1 = new Vec3f(x1, y1, 0);
		Vec3f _2 = new Vec3f(x2, y2, 0);
		return Pair.make(_1, _2);
	}

	/**
	 * @param values
	 * @param bandColor
	 * @param curOpacityFactor2
	 * @return
	 */
	private static Collection<Band> stubify(Collection<Band> bands, Color color, float centerAlpha, float maxAlpha) {
		if (bands.isEmpty() || centerAlpha >= 1)
			return bands;

		Collection<Band> result = new ArrayList<>(bands.size());

		for (Band band : bands) {
			stubify(result, band, color, centerAlpha, maxAlpha);
		}
		return result;
	}

	private static void stubify(Collection<Band> result, Band band, Color color, float centerAlpha, float maxAlpha) {
		float r = color.r;
		float g = color.g;
		float b = color.b;
		float a = color.a;

		List<Vec3f> curveTop = band.getCurveTop();
		List<Vec3f> curveBottom = band.getCurveBottom();
		assert curveTop.size() == curveBottom.size();

		final int size = curveTop.size();

		List<Vec3f> cOut = new ArrayList<>(size);
		List<Vec3f> bOut = new ArrayList<>(size);

		boolean even = size % 2 == 0;
		int center = size % 2 == 0 ? (size / 2 - 1) : size / 2;

		float delta_a = (centerAlpha - maxAlpha) / (center * 0.3f); // artificial enlarge delta for better fading effect
		float act_a = 1;
		int firstAlpha = center;
		for (int i = 0; i < size; ++i) {
			Vec3f top = curveTop.get(i);
			Vec3f bottom = curveBottom.get(i);

			float a_i = Math.max(a * act_a, 0);
			if (a_i <= 0)
				firstAlpha = Math.min(firstAlpha, i);
			Color act = new Color(r, g, b, a_i);
			// System.out.println(i + " " + act_a);
			// manipulate act
			if (even && i == center) {
				// nothing
			} else if (i <= center)
				act_a += delta_a;
			else
				act_a -= delta_a;
			cOut.add(new ColoredVec3f(top, act));
			bOut.add(new ColoredVec3f(bottom, act));
		}

		// split the band into two to avoid tesselation effects
		result.add(new Band(cOut.subList(0, firstAlpha + 2), bOut.subList(0, firstAlpha + 2)));
		result.add(new Band(cOut.subList(size - firstAlpha - 1, size), bOut.subList(size - firstAlpha - 1, size)));
	}

	public enum BandType {
		recordBand, dimensionBand;
	}

	public static void main(String[] args) {

		GLSandBox.main(args, new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				renderBand(g, new Vec2f(0, 0), new Vec2f(2, -2), new Vec2f(4, -4), new Vec2f(5, -4),
						new Vec2f(5.5f, -2));

			}

			private void renderBand(GLGraphics g, Vec2f... vecs) {
				final Band band = TesselatedPolygons.band(Arrays.asList(vecs), 0, 1, 10);
				Collection<Band> r = new ArrayList<>();
				stubify(r, band, new Color(1, 0, 0), 0.0f, 1);

				g.move(100, 100);
				g.save().gl.glScalef(10, 10, 10);
				g.color(1, 0, 0, 0.5f);
				for (Band ri : r)
					g.fillPolygon(ri);
				g.color(0, 1, 0, 0.5f);
				for (Band ri : r)
					g.drawPath(ri);
				g.restore();
			}
		});

	}
}
