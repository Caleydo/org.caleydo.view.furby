/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.elem;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.util.Vec2d;

/**
 * @author Samuel Gratzl
 * @author Michael Gillhofer
 */
public class AllClustersElement extends GLElementContainer implements IGLLayout {

	private float repulsion = 100000f;
	private float attractionFactor = 100f;
	private float borderForceFactor = 200f;
	private float iterationFactor = 500;

	private int deltaToLastFrame = 0;

	double damping = 1f;

	public Integer fixedElementsCount = 15;

	/**
	 * @return the fixedElementsCount, see {@link #fixedElementsCount}
	 */
	public Integer getFixedElementsCount() {
		return fixedElementsCount;
	}

	/**
	 * @param fixedElementsCount
	 *            setter, see {@link fixedElementsCount}
	 */
	public void setFixedElementsCount(Integer fixedElementsCount) {
		this.fixedElementsCount = fixedElementsCount;
	}

	public AllClustersElement(GLRootElement glRootElement) {
		setLayout(this);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
	}

	public void setData(List<TablePerspective> list, TablePerspective x, TablePerspective l, TablePerspective z,
			ExecutorService executor, GLRootElement biclusterRoot) {
		this.clear();
		this.setzDelta(1);
		if (list != null) {
			System.out.println("List size: " + list.size());
			for (TablePerspective p : list) {
				final ClusterElement el = new ClusterElement(p, this, x, l, z, executor, biclusterRoot);
				this.add(el);
			}
		}
	}

	private boolean isInitLayoutDone = false;
	float lastW, lastH;

	@Override
	public void layout(int deltaTimeMs) {
		deltaToLastFrame += deltaTimeMs;
		super.layout(deltaTimeMs);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		if (!isInitLayoutDone && !children.isEmpty()) {
			initialLayout(children, w, h);
			isInitLayoutDone = true;
		} else {
			if (lastW > w || lastH > h)
				scaleView(children, w, h);
			lastW = w;
			lastH = h;
			if (focusedElement != null) {
				setLocation((ClusterElement) focusedElement, w / 2, h / 2, w, h);
			}
			bringClustersBackToFrame(children, w, h);
			clearClusterCollisions(children, w, h);
			int iterations = (int) ((float) 1 / deltaToLastFrame * iterationFactor) + 1;
			deltaToLastFrame = 0;
			for (int i = 0; i < iterations; i++)
				forceDirectedLayout(children, w, h);

		}
		for (IGLLayoutElement child : children) {
			child.setSize(child.getSetWidth(), child.getSetHeight());
		}
		relayout();
	}

	private void bringClustersBackToFrame(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement i : children) {
			Vec4f bounds = i.asElement().getBounds();
			Rectangle frame = new Rectangle(0, 0, (int) w, (int) h);
			if (!frame.intersects(bounds.x(), bounds.y(), bounds.z(), bounds.w()))
				i.setLocation((float) (Math.random() * w), (float) (Math.random() * h));
		}
	}

	private void clearClusterCollisions(List<? extends IGLLayoutElement> children, float w, float h) {
		for (IGLLayoutElement i : children) {
			Vec2f iSize = i.asElement().getSize();
			Vec2f iLoc = i.asElement().getLocation();
			Rectangle iRec = new Rectangle((int) iLoc.x() - 10, (int) iLoc.y() - 10, (int) iSize.x() + 20,
					(int) iSize.y() + 20);
			for (IGLLayoutElement j : children) {
				if (j == i)
					continue;
				if ((j.asElement() == dragedElement || j.asElement() == focusedElement))
					continue;
				Vec2f jSize = j.asElement().getSize();
				Vec2f jLoc = j.asElement().getLocation();
				Rectangle jRec = new Rectangle((int) jLoc.x() - 10, (int) jLoc.y() - 10, (int) jSize.x() + 20,
						(int) jSize.y() + 20);
				if (iRec.intersects(jRec)) {
					setLocation((ClusterElement) j.asElement(), (jLoc.x() + 200) % w, (jLoc.y() + 200) % h, w, h);
				}
			}
			Vec2f toolsLoc = toolbar.getAbsoluteLocation();
			Vec2f toolsSiz = toolbar.getSize();
			Rectangle toolRec = new Rectangle((int) toolsLoc.x(), (int) toolsLoc.y(), (int) toolsSiz.x(),
					(int) toolsSiz.y());
			if (toolRec.intersects(iRec)) {
				setLocation((ClusterElement) i.asElement(), (iLoc.x() - 200) % w, (iLoc.y() - 200) % h, w, h);
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
		xOverlapSize *= 2;
		yOverlapSize /= 3;

		// layout begin
		double xForce = 0, yForce = 0;
		for (IGLLayoutElement iGLE : children) { // Loop through Vertices
			GLElement vGL = iGLE.asElement();
			ClusterElement i = (ClusterElement) vGL;
			i.setRepForce(new Vec2d(0, 0));
			i.setAttForce(new Vec2d(0, 0));
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
				xForce += repulsion * distVec.x() / rsq;
				yForce += repulsion * distVec.y() / rsq;
			}
			i.setRepForce(checkPlausibility(new Vec2d(xForce, yForce)));

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
			i.setAttForce(checkPlausibility(new Vec2d(xForce, yForce)));

			// Border Force
			Vec2d distFromTopLeft = getDistanceFromTopLeft(i, w, h);
			Vec2d distFromBottomRight = getDistanceFromBottomRight(i, w, h);
			xForce = Math.exp(borderForceFactor / Math.abs(distFromTopLeft.x()));
			xForce -= Math.exp(borderForceFactor / Math.abs(distFromBottomRight.x()));
			yForce = Math.exp(borderForceFactor / Math.abs(distFromTopLeft.y()));
			yForce -= Math.exp(borderForceFactor / Math.abs(distFromBottomRight.y()));
			i.setFrameForce(checkPlausibility(new Vec2d(xForce, yForce)));

			// Toolbar force
			Vec2d distVec = getDistance(i, toolbar);
			double rsq = distVec.lengthSquared();
			rsq *= distVec.length();
			xForce = 3 * repulsion * distVec.x() / rsq;
			yForce = 3 * repulsion * distVec.y() / rsq;
			xForce += i.getRepForce().x();
			yForce += i.getRepForce().y();
			i.setRepForce(checkPlausibility(new Vec2d(xForce, yForce)));

		}

		for (IGLLayoutElement iGLL : children) {
			ClusterElement i = (ClusterElement) iGLL.asElement();
			Vec2d attForce = i.getAttForce();
			if (xOverlapSize < 3)
				attForce = attForce.times(0.2);
			Vec2d force = attForce.plus(i.getRepForce()).plus(i.getFrameForce());
			while (force.length() > 20)
				force.scale(0.1);
			Vec2d pos = getCenter(i);
			pos = force.times(damping).plus(pos);
			// System.out.println(i.getId() + ": ");
			// System.out.println("  Att: " + i.getAttForce());
			// System.out.println("  Rep: " + i.getRepForce());
			// System.out.println("  Fra: " + i.getCenterForce());
			// System.out.println("  Sum: " + force);
			if (i != dragedElement && i != hoveredElement && i != focusedElement)
				setLocation(i, (float) pos.x(), (float) pos.y(), w, h);

		}

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

	private Vec2d getDistance(ClusterElement i, GlobalToolBarElement tools) {
		Vec2f toolsPos = tools.getAbsoluteLocation();
		Vec2f toolsSize = tools.getSize();
		Vec2d toolsCenter = new Vec2d(toolsPos.x() + toolsSize.x(), toolsPos.y() + toolsSize.y());
		Vec2d distVec = getCenter(i).minus(toolsCenter);
		double distance = distVec.length();
		Vec2f iSize = i.getSize();
		Vec2f jSize = toolsSize;
		double r1 = iSize.x() > iSize.y() ? iSize.x() / 2 : iSize.y() / 2;
		double r2 = jSize.x() > jSize.y() ? jSize.x() / 2 : jSize.y() / 2;
		distance -= Math.abs(r1) + Math.abs(r2);
		distVec.normalize();
		distVec.scale(distance);
		return distVec;
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
		Vec2d distVec = getCenter(i).minus(getCenter(j));
		double distance = distVec.length();
		Vec2f iSize = i.getSize();
		Vec2f jSize = j.getSize();
		double r1 = iSize.x() > iSize.y() ? iSize.x() / 2 : iSize.y() / 2;
		double r2 = jSize.x() > jSize.y() ? jSize.x() / 2 : jSize.y() / 2;
		distance -= Math.abs(r1) + Math.abs(r2);
		distVec.normalize();
		distVec.scale(distance);
		return distVec;
	}

	private void setLocation(ClusterElement v, double xPos, double yPos, float w, float h) {
		if (xPos > w || xPos < 0 || yPos > h || yPos < 0)
			System.out.println(v.getID() + ": " + xPos + "/" + yPos);
		v.setLocation((float) (xPos - v.getSize().x() / 2), (float) (yPos - v.getSize().y() / 2));
		v.repaintPick();
	}

	private void initialLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		int rowCount = ((int) (Math.sqrt(children.size())) + 1);
		int i = 0;
		for (GLElement child : asList()) {
			Vec2d pos = new Vec2d(i / rowCount * 250 + 200, (i % rowCount) * 180 + 100);
			setLocation((ClusterElement) child, pos.x(), pos.y(), w, h);
			i++;
		}
		EventPublisher.trigger(new CreateBandsEvent(this));
	}

	/**
	 * @return the fixLayout, see {@link #fixLayout}
	 */
	public boolean isLayoutFixed() {
		return dragedElement == null;
	}

	private ClusterElement hoveredElement = null;

	private GLElement dragedElement = null;

	private GLElement focusedElement = null;

	/**
	 * @param fixLayout
	 *            setter, see {@link fixLayout}
	 */
	public void setDragedLayoutElement(ClusterElement element) {
		this.dragedElement = element;
	}

	public void setHooveredElement(ClusterElement hooveredElement) {
		this.hoveredElement = hooveredElement;
	}

	GlobalToolBarElement toolbar;

	public void setToolbar(GlobalToolBarElement globalToolBar) {
		this.toolbar = globalToolBar;

	}

	@ListenTo
	public void ListenTo(FocusChangeEvent e) {
		if (focusedElement == e.getSender())
			focusedElement = null;
		else
			focusedElement = (GLElement) e.getSender();
		// if (focusedElement == null)
		// return;
	}
}
