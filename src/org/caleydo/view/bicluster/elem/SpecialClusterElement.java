package org.caleydo.view.bicluster.elem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.bicluster.concurrent.ScanProbabilityMatrix;
import org.caleydo.view.bicluster.concurrent.ScanResult;
import org.caleydo.view.bicluster.event.ClusterScaleEvent;
import org.caleydo.view.bicluster.event.CreateBandsEvent;
import org.caleydo.view.bicluster.event.FocusChangeEvent;
import org.caleydo.view.bicluster.event.MouseOverClusterEvent;
import org.caleydo.view.bicluster.event.RecalculateOverlapEvent;
import org.caleydo.view.bicluster.sorting.ASortingStrategy;
import org.caleydo.view.bicluster.sorting.ProbabilityStrategy;
import org.caleydo.view.heatmap.v2.HeatMapElement;
import org.caleydo.view.heatmap.v2.HeatMapElement.EShowLabels;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

public final class SpecialClusterElement extends ClusterElement {

	public SpecialClusterElement(TablePerspective data,
			AllClustersElement root, TablePerspective x, TablePerspective l,
			TablePerspective z, ExecutorService executor) {
		super(data, root, x, l, z, executor);

	}

	@Override
	public IDType getDimensionIDType() {
		// TODO
		return getDimensionVirtualArray().getIdType();
	}

	@Override
	public IDType getRecordIDType() {
		// TODO
		return getRecordVirtualArray().getIdType();
	}

	@Override
	public String getID() {
		// TODO
		return data.getLabel();
	}

	@Override
	protected void recreateVirtualArrays(List<Integer> dimIndices,
			List<Integer> recIndices) {
		// TODO
		VirtualArray dimArray = getDimensionVirtualArray();
		VirtualArray recArray = getRecordVirtualArray();
		dimArray.clear();
		int count = 0;
		for (Integer i : dimIndices) {
			if (setOnlyShowXElements
					&& allClusters.getFixedElementsCount() <= count)
				break;
			dimArray.append(i);
			count++;
		}
		count = 0;
		recArray.clear();
		for (Integer i : recIndices) {
			if (setOnlyShowXElements
					&& allClusters.getFixedElementsCount() <= count)
				break;
			recArray.append(i);
			count++;
		}
	}

	@Override
	void calculateOverlap(boolean dimBandsEnabled, boolean recBandsEnabled) {
		// TODO

		this.dimBandsEnabled = dimBandsEnabled;
		this.recBandsEnabled = recBandsEnabled;
		dimOverlap = new HashMap<>();
		recOverlap = new HashMap<>();
		List<Integer> myDimIndizes = getDimensionVirtualArray().getIDs();
		List<Integer> myRecIndizes = getRecordVirtualArray().getIDs();
		dimensionOverlapSize = 0;
		recordOverlapSize = 0;
		for (GLElement element : allClusters.asList()) {
			if (element == this)
				continue;
			ClusterElement e = (ClusterElement) element;
			List<Integer> eIndizes = null;
			if (dimBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myDimIndizes);
				eIndizes.retainAll(e.getDimensionVirtualArray().getIDs());
				dimOverlap.put(element, eIndizes);
				dimensionOverlapSize += eIndizes.size();
			} else {
				dimOverlap.put(element, new ArrayList<Integer>());
			}
			if (recBandsEnabled) {
				eIndizes = new ArrayList<Integer>(myRecIndizes);
				eIndizes.retainAll(e.getRecordVirtualArray().getIDs());
				recOverlap.put(element, eIndizes);
				recordOverlapSize += eIndizes.size();
			} else {
				recOverlap.put(element, new ArrayList<Integer>());
			}
		}
		if (getVisibility() == EVisibility.PICKABLE)
			sort(sortingType);
		fireTablePerspectiveChanged();
	}

	@Override
	public void setPerspectiveLabel(String dimensionName, String recordName) {
		// TODO
		data.getDimensionPerspective().setLabel(dimensionName);
		data.getRecordPerspective().setLabel(recordName);
	}

	@Override
	protected VirtualArray getDimensionVirtualArray() {
		return null; // Never called
	}

	@Override
	protected VirtualArray getRecordVirtualArray() {
		return null; // Should never be called.
	}

	@Override
	public int getNumberOfDimElements() {
		// TODO
		return getDimensionVirtualArray().size();
	}

	@Override
	public int getNumberOfRecElements() {
		// TODO
		return getRecordVirtualArray().size();
	}

	@Override
	protected void focusThisCluster() {
		// TODO
		this.isFocused = !this.isFocused;
		HeatMapElement hm = (HeatMapElement) heatmap;
		if (isFocused) {
			scaleFactor = scaleFactor >= 4 ? 4 : 3;
			hm.setDimensionLabels(EShowLabels.RIGHT);
			hm.setRecordLabels(EShowLabels.RIGHT);
			hm.setRecordSpacingStrategy(SpacingStrategies.fishEye(18));
			hm.setDimensionSpacingStrategy(SpacingStrategies.fishEye(18));
			resize();
			EventPublisher.trigger(new FocusChangeEvent(this));
		} else {
			scaleFactor = 1;
			hm.setDimensionLabels(EShowLabels.NONE);
			hm.setRecordLabels(EShowLabels.NONE);
			hm.setRecordSpacingStrategy(SpacingStrategies.UNIFORM);
			hm.setDimensionSpacingStrategy(SpacingStrategies.UNIFORM);
			resize();
			EventPublisher.trigger(new FocusChangeEvent(null));
			mouseOut();
		}
		repaintAll();
	}

	@Override
	public void setData(List<Integer> dimIndices, List<Integer> recIndices,
			boolean setXElements, String id, int bcNr, double maxDim,
			double maxRec, double minDim, double minRec) {
		// TODO
		data.setLabel(id);
		if (maxDim >= 0 && maxRec >= 0) {
			dimThreshBar.updateSliders(maxDim, minDim);
			recThreshBar.updateSliders(maxRec, minRec);
		}
		dimProbabilitySorting = new ArrayList<Integer>(dimIndices);
		recProbabilitySorting = new ArrayList<Integer>(recIndices);
		this.bcNr = bcNr;
		this.setOnlyShowXElements = setXElements;
		if (dimIndices.size() > 0 && recIndices.size() > 0) {
			hasContent = true;
			if (!isHidden)
				setVisibility(EVisibility.PICKABLE);
			recreateVirtualArrays(dimIndices, recIndices);
		} else {
			setVisibility(EVisibility.NONE);
			hasContent = false;
		}
	}

	@Override
	public boolean isContinuousRecSequenze(List<Integer> overlap) {
		// TODO
		List<Integer> recordArray = getRecordVirtualArray().getIDs();
		int index = 0;
		for (Integer i : recordArray) {
			if (overlap.contains(i))
				break;
			index++;
		}
		if (index > recordArray.size() - overlap.size())
			return false;
		int done = 1;
		for (Integer i : recordArray.subList(index, recordArray.size() - 1)) {
			if (done++ >= overlap.size())
				break;
			if (!overlap.contains(i))
				return false;
		}
		return true;
	}

	@Override
	public boolean isContinuousDimSequenze(List<Integer> overlap) {
		// TODO
		List<Integer> recordArray = getDimensionVirtualArray().getIDs();
		int index = 0;
		for (Integer i : recordArray) {
			if (overlap.contains(i))
				break;
			index++;
		}
		if (index > recordArray.size() - overlap.size())
			return false;
		int done = 1;
		for (Integer i : recordArray.subList(index, recordArray.size() - 1)) {
			if (done++ >= overlap.size())
				break;
			if (!overlap.contains(i))
				return false;
		}
		return true;
	}

	@Override
	public int getDimIndexOf(int value) {
		// TODO
		return getDimensionVirtualArray().indexOf(value);
	}

	@Override
	public int getRecIndexOf(int value) {
		// TODO
		return getRecordVirtualArray().indexOf(value);
	}

	@Override
	protected void rebuildMyData(boolean isGlobal) {
		// TODO
		if (isLocked)
			return;
		Table L = l.getDataDomain().getTable();
		Table Z = z.getDataDomain().getTable();
		Future<ScanResult> recList = null, dimList = null;
		ASortingStrategy strategy = new ProbabilityStrategy(L, bcNr);
		recList = executor.submit(new ScanProbabilityMatrix(recThreshold, L,
				bcNr, strategy));
		strategy = new ProbabilityStrategy(Z, bcNr);
		dimList = executor.submit(new ScanProbabilityMatrix(dimThreshold, Z,
				bcNr, strategy));
		List<Integer> dimIndices = null, recIndices = null;
		try {
			dimIndices = dimList.get().getIndices();
			recIndices = recList.get().getIndices();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		setData(dimIndices, recIndices, setOnlyShowXElements, getID(), bcNr,
				-1, -1, -1, -1);
		EventPublisher.trigger(new ClusterScaleEvent(this));
		if (!isGlobal)
			EventPublisher.trigger(new MouseOverClusterEvent(this, true));
		EventPublisher.trigger(new RecalculateOverlapEvent(this, isGlobal,
				dimBandsEnabled, recBandsEnabled));
		EventPublisher.trigger(new CreateBandsEvent(this));

	}

}
