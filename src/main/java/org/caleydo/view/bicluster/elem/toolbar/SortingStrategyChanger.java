/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.elem.toolbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.bicluster.event.SortingChangeEvent;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy.IComposeAbleSortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.BandSortingStrategy;
import org.caleydo.view.bicluster.sorting.CenterMembershipSortingStrategy;
import org.caleydo.view.bicluster.sorting.ComposedSortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.DefaultSortingStrategy;
import org.caleydo.view.bicluster.sorting.ISortingStrategyFactory;
import org.caleydo.view.bicluster.sorting.MembershipSortingStrategy;

/**
 * @author Samuel Gratzl
 *
 */
public class SortingStrategyChanger implements GLComboBox.ISelectionCallback<ISortingStrategyFactory> {
	private static final ISortingStrategyFactory DEFAULT_PRIMARY_SORTING_MODE = MembershipSortingStrategy.FACTORY_INC;
	private static final ISortingStrategyFactory DEFAULT_SECONDARY_SORTING_MODE = DefaultSortingStrategy.FACTORY;

	private final GLComboBox<ISortingStrategyFactory> sorterPrimary;
	private final GLComboBox<ISortingStrategyFactory> sorterSecondary;
	private List<ISortingStrategyFactory> sortingModelPrimary;
	private List<ISortingStrategyFactory> sortingModelSecondary;

	private final EDimension dimension;

	public SortingStrategyChanger(GLElementContainer parent, EDimension dimension) {
		this.dimension = dimension;

		this.sortingModelPrimary = createSortingModel(true);
		this.sorterPrimary = new GLComboBox<ISortingStrategyFactory>(sortingModelPrimary, GLComboBox.DEFAULT,
				GLRenderers.fillRect(Color.WHITE));
		this.sorterPrimary.setSelectedItem(DEFAULT_PRIMARY_SORTING_MODE);
		this.sorterPrimary.setCallback(this);
		this.sorterPrimary.setSize(Float.NaN, AToolBarElement.BUTTON_WIDTH);
		this.sorterPrimary.setzDeltaList(0.5f);
		parent.add(sorterPrimary);

		this.sortingModelSecondary = new ArrayList<>();
		this.sorterSecondary = new GLComboBox<ISortingStrategyFactory>(sortingModelSecondary, GLComboBox.DEFAULT,
				GLRenderers.fillRect(Color.WHITE));
		this.sorterSecondary.setCallback(this);
		this.sorterSecondary.setSize(Float.NaN, AToolBarElement.BUTTON_WIDTH);
		this.sorterSecondary.setzDeltaList(0.5f);
		parent.add(sorterSecondary);

		updateSecondary(DEFAULT_PRIMARY_SORTING_MODE);
	}

	/**
	 * @param all
	 * @return
	 */
	private List<ISortingStrategyFactory> createSortingModel(boolean all) {
		List<ISortingStrategyFactory> r = new ArrayList<>();
		r.add(MembershipSortingStrategy.FACTORY_INC);
		r.add(MembershipSortingStrategy.FACTORY_INC_ABS);
		r.add(MembershipSortingStrategy.FACTORY_DEC);
		r.add(MembershipSortingStrategy.FACTORY_DEC_ABS);
		r.add(DefaultSortingStrategy.FACTORY);
		r.add(BandSortingStrategy.FACTORY);
		r.add(CenterMembershipSortingStrategy.FACTORY);
		if (!all)
			for (Iterator<ISortingStrategyFactory> it = r.iterator(); it.hasNext();)
				if (!(it.next() instanceof AComposeAbleSortingStrategy))
					it.remove();
		return r;
	}

	public void reset() {

		this.sorterPrimary.setCallback(null).setSelectedItem(DEFAULT_PRIMARY_SORTING_MODE).setCallback(this);
		updateSecondary(DEFAULT_PRIMARY_SORTING_MODE);
	}

	private void updateSecondary(ISortingStrategyFactory primary) {
		this.sorterSecondary.setCallback(null);
		this.sortingModelSecondary.clear();
		if (!(primary instanceof IComposeAbleSortingStrategyFactory)) {
			this.sorterSecondary.setVisibility(EVisibility.VISIBLE);
			this.sorterSecondary.setSelected(-1);
		} else {
			this.sortingModelSecondary.clear();
			this.sortingModelSecondary.addAll(sortingModelPrimary);
			for (Iterator<ISortingStrategyFactory> it = this.sortingModelSecondary.iterator(); it.hasNext();) {
				ISortingStrategyFactory act = it.next();
				if (act == primary || !(act instanceof IComposeAbleSortingStrategyFactory))
					it.remove();
			}
			this.sorterSecondary
					.setSelectedItem(primary == DEFAULT_SECONDARY_SORTING_MODE ? DEFAULT_PRIMARY_SORTING_MODE
							: DEFAULT_SECONDARY_SORTING_MODE);
			this.sorterSecondary.setVisibility(EVisibility.PICKABLE);
		}
		this.sorterSecondary.setCallback(this);
	}

	@Override
	public void onSelectionChanged(GLComboBox<? extends ISortingStrategyFactory> widget, ISortingStrategyFactory item) {
		if (widget == sorterPrimary && item != null) {
			updateSecondary(item);
		}
		ISortingStrategyFactory primary = sorterPrimary.getSelectedItem();
		ISortingStrategyFactory secondary = sorterSecondary.getSelectedItem();
		if (primary == null)
			return;
		ISortingStrategyFactory r;
		if (primary instanceof IComposeAbleSortingStrategyFactory
				&& secondary instanceof IComposeAbleSortingStrategyFactory) {
			r = new ComposedSortingStrategyFactory((IComposeAbleSortingStrategyFactory) primary,
					(IComposeAbleSortingStrategyFactory) secondary);
		} else
			r = primary;
		EventPublisher.trigger(new SortingChangeEvent(r, dimension));
	}

	/**
	 * @param factory
	 */
	public void addSortingMode(ISortingStrategyFactory factory) {
		this.sortingModelPrimary.add(factory);
		if (this.sorterPrimary.getSelectedItem() instanceof IComposeAbleSortingStrategyFactory
				&& factory instanceof IComposeAbleSortingStrategyFactory)
			this.sortingModelSecondary.add(factory);
		this.sorterPrimary.repaint();
	}

	/**
	 * @param data
	 */
	public void removeSortingMode(ISortingStrategyFactory data) {
		if (sorterPrimary.getSelectedItem() == data)
			sorterPrimary.setSelectedItem(DEFAULT_PRIMARY_SORTING_MODE);
		if (this.sortingModelPrimary.remove(data))
			this.sorterPrimary.repaint();
	}

}
