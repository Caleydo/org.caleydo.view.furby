package org.caleydo.view.bicluster.elem.band;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class RecBandMergeArea extends BandMergeArea {

	protected static final float NEAREST_POINT_X_DISTANCE = 50;
	protected static final float NEAREST_POINT_Y_DISTANCE = 50;
	
	public RecBandMergeArea(ClusterElement cluster, ClusterElement other,
			List<List<Integer>> mergedIndices) {
		super(cluster, other, mergedIndices, cluster.getRecordElementSize());
		setMergingAreaPoints();
	}


	protected void setMergingAreaPoints() {
		
		//TODO WRONG
		
		Vec2f clusterCenter = cluster.getLocation().addScaled(0.5f,
				cluster.getSize());
		Vec2f otherCenter = other.getLocation()
				.addScaled(0.5f, other.getSize());
		Vec2f direction = clusterCenter.minus(otherCenter);
		direction.normalize();
		Vec2f normalDirection = new Vec2f(direction.y(), -direction.x());
		if (direction.x() >= 0) {
			points[0] = new Vec2f(0f, -NEAREST_POINT_X_DISTANCE);
		} else {
			points[0] = new Vec2f(cluster.getSize().x(),
					-NEAREST_POINT_X_DISTANCE);
		}
		points[1] = new Vec2f((float) (points[0].x() + normalDirection.x()
				* nrOfIndices * elementSize),
				(float) (points[0].y() + normalDirection.y() * nrOfIndices
						* elementSize));
		points[2] = new Vec2f(points[0].addScaled(MERGING_AREA_LENGHT,
				direction));
		points[3] = new Vec2f(points[1].addScaled(MERGING_AREA_LENGHT,
				direction));

	}

	@Override
	protected Band getBand(List<Integer> subBandIndices) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Vec2f[] getConnectionFromBand() {
		// TODO Auto-generated method stub
		return null;
	}
}
