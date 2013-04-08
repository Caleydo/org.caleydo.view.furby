package org.caleydo.view.bicluster.util;

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

		List<Pair<Vec3f, Vec3f>> points = new ArrayList<>();
		// point pairs defining line segments
		points.add(pair(100, 0, 120, 10));
		points.add(pair(100 + 70, 200, 120 + 70, 200));
		points.add(pair(100 + 70, 300, 120 + 70, 300));
		points.add(pair(100 + 20, 410, 120 + 20, 400));

		// can be stored :)
		Band band = TesselatedPolygons.band(points);
		band.setDrawBandBordersOnly(true).setDrawBandBordersOnFill(false);

		g.color(0, 0, 1, 1.f);
		g.drawPath(band);
		g.color(0, 0, 1, 0.5f);
		g.fillPolygon(band);
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
