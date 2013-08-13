/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.bicluster.internal.prefs;

import org.caleydo.view.bicluster.internal.Activator;
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
		store.setDefault("view.bicluster.scale.dim", 4 * 100);
		store.setDefault("view.bicluster.scale.rec", 4 * 100);
		store.setDefault("view.bicluster.threshold.dim", (int) (4.5f * 100));
		store.setDefault("view.bicluster.threshold.rec", (int) (0.08f * 100));
		store.setDefault("view.bicluster.top.dim", UNBOUND_NUMBER);
		store.setDefault("view.bicluster.top.rec", UNBOUND_NUMBER);
		store.setDefault("view.bicluster.showbands.dim", true);
		store.setDefault("view.bicluster.showbands.rec", true);
	}

	/**
	 * @return
	 */
	public static float getDimScaleFactor() {
		return prefs().getInt("view.bicluster.scale.dim") / 100.f;
	}

	/**
	 * @return
	 */
	public static float getRecScaleFactor() {
		return prefs().getInt("view.bicluster.scale.rec") / 100.f;
	}

	public static float getDimThreshold() {
		return prefs().getInt("view.bicluster.threshold.dim") / 100.f;
	}

	public static float getRecThreshold() {
		return prefs().getInt("view.bicluster.threshold.rec") / 100.f;
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

}
