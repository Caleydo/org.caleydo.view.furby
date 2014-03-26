/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.internal.prefs;

import org.caleydo.view.bicluster.internal.Activator;
import org.caleydo.view.bicluster.sorting.EThresholdMode;
import org.caleydo.view.heatmap.v2.EScalingMode;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Samuel Gratzl
 *
 */
public class MyPreferences extends AbstractPreferenceInitializer {
	public static final float MIN_SCALE_FACTOR = 0.25f;
	public static final float MAX_SCALE_FACTOR = 32f;
	public static final int UNBOUND_NUMBER = 0;



	private static IPreferenceStore prefs() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = prefs();
		store.setDefault("view.bicluster.aspectratio", 100);
		store.setDefault("view.bicluster.maxdistance", 1);

		store.setDefault("view.bicluster.threshold.dim", (int) (0.7f * 100));
		store.setDefault("view.bicluster.threshold.rec", (int) (0.08f * 100));
		store.setDefault("view.bicluster.threshold.mode.dim", EThresholdMode.ABS.name());
		store.setDefault("view.bicluster.threshold.mode.rec", EThresholdMode.ABS.name());
		store.setDefault("view.bicluster.top.dim", UNBOUND_NUMBER);
		store.setDefault("view.bicluster.top.rec", UNBOUND_NUMBER);
		store.setDefault("view.bicluster.showbands.dim", false);
		store.setDefault("view.bicluster.showbands.rec", true);

		store.setDefault("view.bicluster.bar.scaling", EScalingMode.LOCAL.name());
		store.setDefault("view.bicluster.groupingHints", false);

		store.setDefault("view.bicluster.go.maxgos", 5);
		store.setDefault("view.bicluster.go.maxp", 100);
	}

	public static float getDimThreshold() {
		return prefs().getInt("view.bicluster.threshold.dim") / 100.f;
	}

	public static float getRecThreshold() {
		return prefs().getInt("view.bicluster.threshold.rec") / 100.f;
	}

	public static EThresholdMode getDimThresholdMode() {
		return EThresholdMode.valueOf(prefs().getString("view.bicluster.threshold.mode.dim"));
	}

	public static EThresholdMode getRecThresholdMode() {
		return EThresholdMode.valueOf(prefs().getString("view.bicluster.threshold.mode.rec"));
	}

	public static int getDimTopNElements() {
		return prefs().getInt("view.bicluster.top.dim");
	}

	public static int getRecTopNElements() {
		return prefs().getInt("view.bicluster.top.rec");
	}

	public static boolean isShowDimBands() {
		return prefs().getBoolean("view.bicluster.showbands.dim");
	}

	public static boolean isShowRecBands() {
		return prefs().getBoolean("view.bicluster.showbands.rec");
	}

	/**
	 * @return
	 */
	public static int getMaxDistance() {
		return prefs().getInt("view.bicluster.maxdistance");
	}

	/**
	 * @return
	 */
	public static double getAspectRatio() {
		return prefs().getInt("view.bicluster.aspectratio") / 100.f;
	}

	/**
	 * @return
	 */
	public static EScalingMode getBarPlotScalingMode() {
		return EScalingMode.valueOf(prefs().getString("view.bicluster.bar.scaling"));
	}

	/**
	 * @return
	 */
	public static boolean isShowGroupingHints() {
		return prefs().getBoolean("view.bicluster.groupingHints");
	}

	public static float getMaximalGOPValue() {
		return prefs().getInt("view.bicluster.go.maxp") / 100.f;
	}

	public static int getMaxNumberofGOs() {
		return prefs().getInt("view.bicluster.go.maxgos");
	}
}
