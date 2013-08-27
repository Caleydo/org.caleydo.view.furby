/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.layout;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.elem.AToolBarElement;
import org.caleydo.view.bicluster.elem.AllClustersElement;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.physics.Physics;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * @author Samuel Gratzl
 *
 */
public class ForceBasedLayout extends AForceBasedLayout {
	private boolean isInitLayoutDone = false;
	float lastW, lastH;

	public ForceBasedLayout(AllClustersElement parent) {
		super(parent);
	}

	@Override
	public boolean forceBasedLayout(List<? extends IGLLayoutElement> children, float w, float h, int deltaTimeMs) {
		if (!isInitLayoutDone && !children.isEmpty()) {
			initialLayout(children, w, h);
			isInitLayoutDone = true;

			return true;
		}
		if (lastW > w || lastH > h)
			scaleView(children, w, h);
		lastW = w;
		lastH = h;
		if (focusedElement != null) {
			setLocation(focusedElement, w / 2, h / 2, w, h);
		}
		bringClustersBackToFrame(children, w, h);
		clearClusterCollisions(children, w, h);

		int iterations = computeNumberOfIterations(deltaTimeMs);
		for (int i = 0; i < iterations; i++)
			forceDirectedLayout(children, w, h);

		return true;
	}

	private void bringClustersBackToFrame(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement i : children) {
			Vec4f bounds = i.getBounds();
			Rectangle2D frame = new Rectangle2D.Float(0, 0, (int) w, (int) h);
			if (!frame.intersects(bounds.x(), bounds.y(), bounds.z(), bounds.w()))
				i.setLocation((float) (Math.random() * w), (float) (Math.random() * h));
		}
	}

	private void clearClusterCollisions(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement iIGL : children) {
			ClusterElement i = (ClusterElement) iIGL.asElement();
			if (!i.isVisible())
				continue;
			Vec2f iSize = iIGL.getSetSize();
			Vec2f iLoc = iIGL.getLocation();
			Rectangle2D iRec = new Rectangle2D.Float(iLoc.x() - 10, iLoc.y() - 10, iSize.x() + 20, iSize.y() + 20);
			for (IGLLayoutElement jIGL : children) {
				ClusterElement j = (ClusterElement) jIGL.asElement();
				if (j == i || !j.isVisible() || (j == parent.getDragedElement() || j == focusedElement))
					continue;

				Vec2f jSize = j.getSize();
				Vec2f jLoc = j.getLocation();
				Rectangle2D jRec = new Rectangle2D.Float(jLoc.x() - 10, jLoc.y() - 10, jSize.x() + 20, jSize.y() + 20);
				if (iRec.intersects(jRec)) {
					setLocation(j, (jLoc.x() + 200) % w, (jLoc.y() + 200) % h, w, h);
				}
			}
			for (AToolBarElement toolbar : parent.getToolbars()) {
				if (!toolbar.isVisible()) // no parent not visible
					continue;
				Vec2f toolsLoc = toolbar.getAbsoluteLocation();
				Vec2f toolsSiz = toolbar.getSize();
				Rectangle2D toolRec = new Rectangle2D.Float(toolsLoc.x(), toolsLoc.y(), toolsSiz.x(), toolsSiz.y());
				if (toolRec.intersects(iRec)) {
					setLocation(i, (iLoc.x() - 200) % w, (iLoc.y() - 200) % h, w, h);
				}
			}
		}

	}

	private void scaleView(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement igllChild : children) {
			GLElement child = igllChild.asElement();
			Vec2f loc = child.getLocation();
			child.setLocation(loc.x() * w / lastW, loc.y() * h / lastH);
		}

	}

	/**
	 * @param children2
	 * @param w
	 * @param h
	 */
	private void forceDirectedLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		// calculate the attraction based on the size of all overlaps
		double xOverlapSize = 0, yOverlapSize = 0;
		for (IGLLayoutElement iGLE : children) {
			GLElement vGL = iGLE.asElement();
			ClusterElement v = (ClusterElement) vGL;
			xOverlapSize += v.getDimensionOverlapSize();
			yOverlapSize += v.getRecordOverlapSize();
		}
		double attractionX = 1;
		double attractionY = 1;

		attractionX = attractionFactor / (xOverlapSize + yOverlapSize);
		attractionY = attractionFactor / (yOverlapSize + xOverlapSize);
		// -> attractionX = attrationY
		// TODO: magic numbers?
		xOverlapSize *= 2;
		yOverlapSize /= 3;

		List<ForceHelper> helpers = new ArrayList<>(children.size());

		// layout begin
		double xForce = 0, yForce = 0;
		for (IGLLayoutElement iGLE : children) { // Loop through Vertices
			GLElement vGL = iGLE.asElement();
			ClusterElement i = (ClusterElement) vGL;
			ForceHelper f = new ForceHelper();
			helpers.add(f);
			f.setRepForce(new Vec2d(0, 0));
			f.setAttForce(new Vec2d(0, 0));
			// repulsion
			xForce = 0;
			yForce = 0;
			for (IGLLayoutElement jGLL : children) { // loop through other
														// vertices
				GLElement jElement = jGLL.asElement();
				ClusterElement j = (ClusterElement) jElement;
				if (j == i)
					continue;
				// squared distance between "u" and "v" in 2D space
				// calculate the repulsion between two vertices
				Vec2d distVec = getDistance(i, j);
				double rsq = distVec.lengthSquared();
				rsq *= distVec.length();
				// why: rsq = |distVec| * (|distVec| * |distVec|) = cubic
				xForce += repulsion * distVec.x() / rsq;
				yForce += repulsion * distVec.y() / rsq;
			}
			f.setRepForce(checkPlausibility(new Vec2d(xForce, yForce)));

			// attraction force calculation
			xForce = 0;
			yForce = 0;
			for (IGLLayoutElement jGLL : children) {
				GLElement jElement = jGLL.asElement();
				ClusterElement j = (ClusterElement) jElement;
				if (i == j)
					continue;
				List<Integer> xOverlap = i.getDimOverlap(j);
				List<Integer> yOverlap = i.getRecOverlap(j);
				if (xOverlap.size() == 0 && yOverlap.size() == 0)
					continue;
				int overlapSizeX = xOverlap.size();
				int overlapSizeY = yOverlap.size();
				Vec2d distVec = getDistance(j, i);
				double dist = distVec.length/* Squared */();
				// counting the attraction
				xForce += attractionX * distVec.x() * (overlapSizeX + overlapSizeY) / dist;
				yForce += attractionY * distVec.y() * (overlapSizeY + overlapSizeX) / dist;
			}
			f.setAttForce(checkPlausibility(new Vec2d(xForce, yForce)));

			// Border Force
			Vec2d distFromTopLeft = getDistanceFromTopLeft(i, w, h);
			Vec2d distFromBottomRight = getDistanceFromBottomRight(i, w, h);
			xForce = Math.exp(borderForceFactor / Math.abs(distFromTopLeft.x()));
			xForce -= Math.exp(borderForceFactor / Math.abs(distFromBottomRight.x()));
			yForce = Math.exp(borderForceFactor / Math.abs(distFromTopLeft.y()));
			yForce -= Math.exp(borderForceFactor / Math.abs(distFromBottomRight.y()));
			f.setFrameForce(checkPlausibility(new Vec2d(xForce, yForce)));

			// Toolbar force
			xForce = 0;
			yForce = 0;
			for (AToolBarElement toolbar : parent.getToolbars()) {
				if (!toolbar.isVisible()) // no parent not visible
					continue;
				if (Physics.isApproximateRects()) {
					Vec2d distVec = getDistance(i, toolbar, true);
					double rsq = distVec.lengthSquared();
					rsq *= distVec.length();
					xForce += 1.5f * repulsion * distVec.x() / rsq;
					yForce += 1.5f * repulsion * distVec.y() / rsq;

					distVec = getDistance(i, toolbar, false);
					rsq = distVec.lengthSquared();
					rsq *= distVec.length();
					xForce += 1.5f * repulsion * distVec.x() / rsq;
					yForce += 1.5f * repulsion * distVec.y() / rsq;
				} else {
					Vec2d distVec = getDistance(i, toolbar, true);
					double rsq = distVec.lengthSquared();
					rsq *= distVec.length();
					xForce += 1.5f * repulsion * distVec.x() / rsq;
					yForce += 1.5f * repulsion * distVec.y() / rsq;
				}
			}
			xForce += f.getRepForce().x();
			yForce += f.getRepForce().y();
			f.setRepForce(checkPlausibility(new Vec2d(xForce, yForce)));

		}

		int ii = 0;
		for (IGLLayoutElement iGLL : children) {
			ClusterElement i = (ClusterElement) iGLL.asElement();
			ForceHelper f = helpers.get(ii++);
			Vec2d attForce = f.getAttForce();
			if (xOverlapSize < 3)
				attForce = attForce.times(0.2);
			Vec2d force = attForce.plus(f.getRepForce()).plus(f.getFrameForce());
			while (force.length() > 20)
				force.scale(0.1);
			Vec2d pos = getCenter(i);
			pos = force.times(damping).plus(pos);
			// System.out.println(i.getId() + ": ");
			// System.out.println("  Att: " + i.getAttForce());
			// System.out.println("  Rep: " + i.getRepForce());
			// System.out.println("  Fra: " + i.getCenterForce());
			// System.out.println("  Sum: " + force);
			if (!isActiveCluster(i))
				setLocation(iGLL, (float) pos.x(), (float) pos.y(), w, h);

		}

	}

	/**
	 * @param i
	 * @return
	 */
	private boolean isActiveCluster(ClusterElement i) {
		return i == parent.getDragedElement() || i == hoveredElement || i == focusedElement;
	}

	private Vec2d checkPlausibility(Vec2d vec2d) {
		if (vec2d.x() > 1e4)
			vec2d.setX(1e4);
		if (vec2d.x() < -1e4)
			vec2d.setX(-1e4);
		if (vec2d.y() > 1e4)
			vec2d.setY(1e4);
		if (vec2d.y() < -1e4)
			vec2d.setY(-1e4);

		return vec2d;
	}

	private Vec2d getDistance(ClusterElement i, AToolBarElement tools, boolean isTop) {
		Vec2f toolsPos = tools.getAbsoluteLocation();
		Vec2f toolsSize = tools.getSize();
		if (Physics.isApproximateRects()) {
			// FIXME: idea is to approximate long rects by two circles
			Vec2f toolsCenter = new Vec2f(toolsPos.x() + toolsSize.x(), toolsPos.y() + toolsSize.y());
			if (isTop) {
				toolsCenter.add(new Vec2f(0, -toolsSize.y() / 4));
			} else {
				toolsCenter.add(new Vec2f(0, toolsSize.y() / 4));
			}
			Rectangle2D toolBounds = new Rectangle2D.Float(toolsCenter.x() - toolsSize.x() * 0.5f, toolsCenter.y()
					- toolsSize.y() * 0.5f, toolsSize.x(), toolsSize.y());
			return Physics.distance(i.getRectangleBounds(), toolBounds);
		} else {
			Rectangle2D toolBounds = new Rectangle2D.Float(toolsPos.x(), toolsPos.y(), toolsSize.x(), toolsSize.y());
			return Physics.distance(i.getRectangleBounds(), toolBounds);
		}

	}

	private Vec2d getDistanceFromTopLeft(ClusterElement i, float w, float h) {
		Vec2d pos = getCenter(i);
		Vec2f size = i.getSize();
		pos.setX(pos.x() - size.x() * 0.5);
		pos.setY(pos.y() - size.y() * 0.5);
		return pos;
	}

	private Vec2d getDistanceFromBottomRight(ClusterElement i, float w, float h) {
		Vec2d dist = getDistanceFromTopLeft(i, w, h);
		dist.setX(-(w - dist.x()));
		dist.setY(-(h - dist.y()));
		Vec2f size = i.getSize();
		dist.setX(dist.x() + size.x() * 0.5 + 30);
		dist.setY(dist.y() + size.y() * 0.5 + 30);

		return dist;
	}

	private Vec2d getCenter(ClusterElement i) {
		Vec2f vec = i.getLocation().addScaled(0.5f, i.getSize());
		return new Vec2d(vec.x(), vec.y());
	}

	private Vec2d getDistance(ClusterElement i, ClusterElement j) {
		return Physics.distance(i.getRectangleBounds(), j.getRectangleBounds());
	}

	private void setLocation(GLElement v, double xPos, double yPos, float w, float h) {
		setLocation(GLElementAccessor.asLayoutElement(v), xPos, yPos, w, h);
	}

	private void setLocation(IGLLayoutElement v, double xPos, double yPos, float w, float h) {
		if (xPos > w || xPos < 0 || yPos > h || yPos < 0)
			System.out.println(v.asElement() + ": " + xPos + "/" + yPos);
		v.setLocation((float) (xPos - v.getSetSize().x() / 2), (float) (yPos - v.getSetSize().y() / 2));
	}

	private void initialLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		final int rowCount = (int) (Math.sqrt(children.size())) + 1;
		int i = 0;

		float hFactor = (h - 200) / rowCount;
		float wFactor = (w - 300) / (children.size() / rowCount + 1);
		for (IGLLayoutElement child : children) {
			int row = i / rowCount;
			int col = (i % rowCount);
			Vec2d pos = new Vec2d(row * wFactor + 200, col * hFactor + 100);
			setLocation(child, pos.x(), pos.y(), w, h);
			i++;
		}
		EventPublisher.trigger(new CreateBandsEvent(parent));
	}

	private final static class ForceHelper {
		private Vec2d attForce;
		private Vec2d repForce;
		private Vec2d frameForce;

		public Vec2d getAttForce() {
			return attForce;
		}

		public void setAttForce(Vec2d attForce) {
			this.attForce = attForce;
		}

		public Vec2d getRepForce() {
			return repForce;
		}

		public void setRepForce(Vec2d repForce) {
			this.repForce = repForce;
		}

		public Vec2d getFrameForce() {
			return frameForce;
		}

		public void setFrameForce(Vec2d frameForce) {
			this.frameForce = frameForce;
		}

	}
}
