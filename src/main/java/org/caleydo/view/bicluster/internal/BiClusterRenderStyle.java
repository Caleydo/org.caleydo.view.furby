/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.internal;

import java.net.URL;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;

/**
 * Render style.
 *
 * @author <INSERT_YOUR_NAME>
 */
public class BiClusterRenderStyle {
	private static final URL icon(String icon) {
		return BiClusterRenderStyle.class.getResource("/org/caleydo/view/bicluster/internal/icons/" + icon);
	}

	private static final String ICON_PREFIX = "/resources/icons/";

	public static final URL ICON_CLOSE = icon("dialog_close.png");
	public static final URL ICON_FOCUS = icon("target.png");
	public static final URL ICON_FOCUS_OUT = icon("target_out.png");
	public static final URL ICON_UNLOCK = icon("lock_open.png");
	public static final URL ICON_LOCK = icon("lock.png");
	public static final URL ICON_ZOOM_IN = icon("zoom_in.png");
	public static final URL ICON_ZOOM_OUT = icon("zoom_out.png");
	public static final URL ICON_ZOOM_RESET = icon("zoom_one.png");

	public static final String ICON_TOOLS = ICON_PREFIX + "setting_tools.png";
	public static final String ICON_LAYOUT = ICON_PREFIX + "gear_in.png";

	public static final String ICON_FIND = ICON_PREFIX + "find.png";

	public static final String ICON_TEXT_LARGE = ICON_PREFIX + "text_large_cap.png";

	public static Color getBandColor(EDimension dim) {
		switch (dim) {
		case DIMENSION:
			return Color.NEUTRAL_GREY;
		case RECORD:
			return Color.LIGHT_GRAY;
		}
		throw new IllegalStateException();

	}
}
