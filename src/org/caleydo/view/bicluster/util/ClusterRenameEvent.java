package org.caleydo.view.bicluster.util;

import org.caleydo.core.event.AEvent;
import org.caleydo.view.bicluster.elem.ClusterElement;
import org.eclipse.swt.widgets.Text;

public class ClusterRenameEvent extends AEvent {

	String newName;

	public ClusterRenameEvent(ClusterElement sender, Text input) {
		setSender(sender);
		this.newName = input.getText();
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
	
	public String getNewName() {
		return newName;
	}

}
