/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.bicluster.sorting;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.bicluster.elem.NormalClusterElement;
import org.caleydo.view.bicluster.sorting.AComposeAbleSortingStrategy.IComposeAbleSortingStrategyFactory;

/**
 * categorical sorting strategy
 *
 * @author Samuel Gratzl
 *
 */
public class CategoricalSortingStrategyFactory implements IComposeAbleSortingStrategyFactory {

	private final EDimension dim;
	private final Integer oppositeID;
	private final Table table;
	private final IIDTypeMapper<Integer, Integer> id2colorId;
	private final boolean isDimension;

	public CategoricalSortingStrategyFactory(EDimension dim, Integer oppositeID, Table table,
			IIDTypeMapper<Integer, Integer> mapper) {
		this.dim = dim;
		this.oppositeID = oppositeID;
		this.table = table;
		this.id2colorId = mapper;
		this.isDimension = table.getDataDomain().getDimensionIDType().equals(id2colorId.getTarget());
	}

	public IDType getTarget() {
		return id2colorId.getTarget();
	}

	@Override
	public String getLabel() {
		return WordUtils.capitalizeFully(table.getDataDomain().getLabel());
	}

	@Override
	public AComposeAbleSortingStrategy create(NormalClusterElement cluster, EDimension dim) {
		if (this.dim == dim)
			return new CategoricalSortingStrategy(this);
		return AllEqualsSortringStrategy.INSTANCE;
	}

	public String getLabel(Integer id) {
		CategoryProperty<?> property = getProperty(id);
		return property == null ? null : property.getCategoryName();
	}

	public CategoryProperty<?> getProperty(Integer id) {
		id = map(id);
		if (id == null)
			return null;
		Object category;
		Object desc;
		if (isDimension) {
			category = table.getRaw(id, oppositeID);
			desc = table.getDataClassSpecificDescription(id, oppositeID);
		} else {
			category = table.getRaw(oppositeID, id);
			desc = table.getDataClassSpecificDescription(oppositeID, id);
		}
		if (category == null)
			return null;
		if (!(desc instanceof CategoricalClassDescription<?>))
			return null;
		return ((CategoricalClassDescription<?>) desc).getCategoryProperty(category);
	}

	public int getOrder(Integer id) {
		id = map(id);
		if (id == null)
			return Integer.MAX_VALUE;
		Object category;
		Object desc;
		if (isDimension) {
			category = table.getRaw(id, oppositeID);
			desc = table.getDataClassSpecificDescription(id, oppositeID);
		} else {
			category = table.getRaw(oppositeID, id);
			desc = table.getDataClassSpecificDescription(oppositeID, id);
		}
		if (category == null)
			return 0;
		if (!(desc instanceof CategoricalClassDescription<?>))
			return 0;
		return ((CategoricalClassDescription<?>) desc).indexOf(category);
	}

	public float[] getColor(Integer id) {
		id = map(id);
		if (id == null)
			return Color.NOT_A_NUMBER_COLOR.getRGBA();
		if (isDimension)
			return table.getColor(id, oppositeID);
		else
			return table.getColor(oppositeID, id);
	}

	private Integer map(Integer id) {
		Set<Integer> r = id2colorId.apply(id);
		return r == null || r.isEmpty() ? null : r.iterator().next();
	}

	private static class CategoricalSortingStrategy extends AComposeAbleSortingStrategy implements
			IGroupingStrategy {
		private final CategoricalSortingStrategyFactory factory;

		/**
		 * @param factory
		 */
		public CategoricalSortingStrategy(CategoricalSortingStrategyFactory factory) {
			this.factory = factory;
		}

		@Override
		public GroupList getGrouping(List<IntFloat> sortedList) {
			GroupList l = new GroupList();
			int from = 0;
			int size = 0;
			CategoryProperty<?> last = null;
			for (IntFloat i : sortedList) {
				CategoryProperty<?> prop = factory.getProperty(i.getIndex());
				if (!Objects.equals(prop, last) && size > 0 && last != null) {
					Group g = new Group(size, -1);
					g.setLabel(last.getCategoryName(), false);
					g.setStartIndex(from);
					l.append(g);
					from += size;
					size = 0;
				}
				size++;
				last = prop;
			}
			if (size > 0 && last != null) {
				Group g = new Group(size, -1);
				g.setLabel(last.getCategoryName(), false);
				g.setStartIndex(from);
				l.append(g);
				from += size;
				size = 0;
			}
			return l;
		}

		@Override
		public int compare(IntFloat o1, IntFloat o2) {
			int a = factory.getOrder(o1.getIndex());
			int b = factory.getOrder(o2.getIndex());
			return a - b;
		}
	}

}
