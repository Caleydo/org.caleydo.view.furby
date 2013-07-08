package org.caleydo.view.bicluster.util;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.util.spline.Band;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;

/**
 * @author Samuel Gratzl
 *
 */
public class SimpleBandTest extends GLElement {

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		List<Pair<Vec3f, Vec3f>> points0 = new ArrayList<>();
		// point pairs defining line segments
		points0.add(pair(180, 150, 185, 150));
		points0.add(pair(180, 190, 185, 190));
		points0.add(pair(260, 210, 260, 205));
		points0.add(pair(300, 210, 300, 205));


		// can be stored :)
		Band band0 = TesselatedPolygons.band(points0);
		band0.setDrawBandBordersOnly(true).setDrawBandBordersOnFill(false);
		
		
		
		
		
		List<Pair<Vec3f, Vec3f>> points = new ArrayList<>();
		// point pairs defining line segments
		points.add(pair(200, 150, 205, 150));
		points.add(pair(200, 190, 205, 190));
		points.add(pair(260, 205, 260, 200));
		points.add(pair(300, 205, 300, 200));


		// can be stored :)
		Band band = TesselatedPolygons.band(points);
		band.setDrawBandBordersOnly(true).setDrawBandBordersOnFill(false);

		List<Pair<Vec3f, Vec3f>> points2 = new ArrayList<>();
		// point pairs defining line segments
		points2.add(pair(250, 150, 255, 150));
		points2.add(pair(250, 190, 255, 190));
		points2.add(pair(260, 200, 260, 195));
		points2.add(pair(300, 200, 300, 195));


		// can be stored :)
		Band band2 = TesselatedPolygons.band(points2);
		band2.setDrawBandBordersOnly(true).setDrawBandBordersOnFill(false);

		List<Pair<Vec3f, Vec3f>> points3 = new ArrayList<>();
		// point pairs defining line segments
		
		points3.add(pair(300, 195, 300, 210));
		points3.add(pair(350, 195, 350, 210));
		points3.add(pair(415, 260, 400, 260));
		points3.add(pair(415, 300, 400, 300));


		// can be stored :)
		Band band3 = TesselatedPolygons.band(points3);
		band3.setDrawBandBordersOnly(true).setDrawBandBordersOnFill(false);
		
		
		List<Vec2f> anchorPoints = new ArrayList<>();
		anchorPoints.add(new Vec2f(300, 300));
		anchorPoints.add(new Vec2f(300, 350));
		anchorPoints.add(new Vec2f(340, 400));
		anchorPoints.add(new Vec2f(380, 400));
		
		Band band4 = TesselatedPolygons.band(anchorPoints, 1, 10, 10);
		
		
		g.color(0, 0, 1, 1.f);
		g.drawPath(band);
		g.drawPath(band2);
		g.drawPath(band3);
		g.drawPath(band0);
		g.drawPath(band4);
		g.color(0, 0, 1, 0.5f);
		g.fillPolygon(band);
		g.fillPolygon(band2);
		g.fillPolygon(band3);
		g.fillPolygon(band0);
		g.fillPolygon(band4);
	}

	private Pair<Vec3f, Vec3f> pair(float x1, float y1, float x2, float y2) {
		Vec3f _1 = new Vec3f(x1, y1, 0);
		Vec3f _2 = new Vec3f(x2, y2, 0);
		return Pair.make(_1, _2);
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new SimpleBandTest(), new GLPadding(10));
	}
}
