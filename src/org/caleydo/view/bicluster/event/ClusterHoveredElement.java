package org.caleydo.view.bicluster.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;

public class ClusterHoveredElement extends AEvent {

	private boolean in;

	public ClusterHoveredElement(ClusterElement hoveredElement, boolean b) {
		setSender(hoveredElement);
		in = b;
	}

	public ClusterElement getElement() {
		return (ClusterElement) getSender();
	}
	
	public boolean isMouseOver(){
		return in;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

}
