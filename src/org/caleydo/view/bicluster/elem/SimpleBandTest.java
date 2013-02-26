package org.caleydo.view.bicluster.elem;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLContext;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Colors;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;

/**
 * @author Samuel Gratzl
 *
 */
public class SimpleBandTest extends GLElement {
	private ConnectionBandRenderer r = new ConnectionBandRenderer();

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		r = new ConnectionBandRenderer();
		r.init(GLContext.getCurrentGL().getGL2());
	}

	@Override
	protected void takeDown() {
		r = null;
		super.takeDown();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		boolean highlight = false;
		float[] color = Colors.BLUE.getRGBA();
		List<Pair<Point2D, Point2D>> points = new ArrayList<>();
		// point pairs defining line segments
		points.add(pair(100, 0, 120, 10));
		points.add(pair(100 + 70, 200, 120 + 70, 200));
		points.add(pair(100 + 70, 300, 120 + 70, 300));
		points.add(pair(100 + 20, 410, 120 + 20, 400));
		r.renderComplexBand(g.gl, points, highlight, color, .5f);
	}

	private Pair<Point2D, Point2D> pair(float x1, float y1, float x2, float y2) {
		Point2D _1 = new Point2D.Float(x1, y1);
		Point2D _2 = new Point2D.Float(x2, y2);
		return Pair.make(_1, _2);
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new SimpleBandTest(), new GLPadding(10));
	}
}
