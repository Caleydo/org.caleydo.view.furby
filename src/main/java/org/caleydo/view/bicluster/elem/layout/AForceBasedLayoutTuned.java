/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.toolbar.AToolBarElement;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * tuned version of {@link ForceBasedLayout}
 *
 * @author Samuel Gratzl
 *
 */
public abstract class AForceBasedLayoutTuned extends AForceBasedLayout {
	private boolean isInitLayoutDone = false;

	/**
	 * counts the number of continuous "another round" calls, used for damping
	 */
	private int continousLayoutDuration = 0;

	public AForceBasedLayoutTuned(AllClustersElement parent) {
		super(parent);
	}
	@Override
	public boolean forceBasedLayout(List<? extends IGLLayoutElement> children, float w, float h, int deltaTimeMs) {
		if (children.isEmpty())
			return false;

		List<ForcedBody> bodies = toForcedBodies(children);
		List<ForcedBody> toolBars = toToolBarBodies(parent.getToolbars());

		int iterations;
		if (!isInitLayoutDone) {
			initialLayout(bodies, w, h);
			isInitLayoutDone = true;
			iterations = 20;
			// System.out.println("init all");
		} else {
			iterations = Math.max(1, computeNumberOfIterations(deltaTimeMs));

			// init uninitialized bodies
			for (ForcedBody body : bodies) {
				if (body.isInvalid() && body.isVisible()) {
					// System.out.println("init " + body);
					initialPosition(body, w, h, bodies);
				}
			}
		}

		forcedBasedLayout(bodies, toolBars, iterations, w, h);

		double totalDistanceSquared = 0;
		double damping = time2damping(continousLayoutDuration);
		for (ForcedBody body : bodies)
			totalDistanceSquared += body.apply(damping);

		final boolean anotherRound = totalDistanceSquared > 5 * 5;
		if (anotherRound)
			continousLayoutDuration += deltaTimeMs;
		else
			continousLayoutDuration = 0;
		return anotherRound;
	}

	protected abstract void initialPosition(ForcedBody body, float w, float h, List<ForcedBody> bodies);

	protected abstract void initialLayout(List<ForcedBody> bodies, float w, float h);

	protected abstract void forcedBasedLayout(List<ForcedBody> bodies, List<ForcedBody> toolBars, final int iterations,
			float w, float h);
	/**
	 * @param duration
	 * @return
	 */
	private static double time2damping(int duration) {
		if (duration == 0)
			return 1;
		final double maxTime = 5000; // ms
		final double minDamping = 0.00;
		final double maxDamping = 1;
		double factor = Math.max(minDamping, Math.min(maxDamping, 1 - (duration / maxTime)));
		return factor;
	}

	/**
	 * @param toolbars
	 * @return
	 */
	private List<ForcedBody> toToolBarBodies(List<AToolBarElement> toolbars) {
		List<ForcedBody> b = new ArrayList<>(toolbars.size());
		for (AToolBarElement elem : toolbars) {
			if (!elem.isVisible())
				continue;
			ForcedBody body = new ForcedBody(GLElementAccessor.asLayoutElement(elem), ForcedBody.FLAG_TOOLBAR);
			if (body.isInvalid()) // no position yet, skip it
				continue;
			b.add(body);
		}
		return b;
	}

	/**
	 * @param children
	 * @return
	 */
	private List<ForcedBody> toForcedBodies(List<? extends IGLLayoutElement> children) {
		List<ForcedBody> b =new ArrayList<>(children.size());
		for(IGLLayoutElement elem : children) {
			b.add(toForcedBody(elem));
		}
		return b;
	}

	private ForcedBody toForcedBody(IGLLayoutElement elem) {
		int flags = 0;
		GLElement glelem = elem.asElement();
		if (parent.getFocussedElement() == glelem)
			flags |= ForcedBody.FLAG_FOCUSSED;
		if (parent.getDraggedElement() == glelem)
			flags |= ForcedBody.FLAG_DRAGGED;
		if (parent.getHoveredElement() == glelem)
			flags |= ForcedBody.FLAG_HOVERED;
		if (!isInitLayoutDone)
			flags |= ForcedBody.FLAG_INITIAL;
		return new ForcedBody(elem, flags);
	}

	protected static Vec2d getDistanceFromTopLeft(ForcedBody body, float w, float h) {
		return new Vec2d(body.x0(), body.y0());
	}

	protected static Vec2d getDistanceFromBottomRight(ForcedBody body, float w, float h) {
		return new Vec2d(body.getCenterX() + body.radiusX - w, body.getCenterY() + body.radiusY - h);
	}

}
