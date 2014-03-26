package org.caleydo.view.bicluster.internal;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.gui.SimpleAction;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
import org.caleydo.view.bicluster.event.SearchClusterEvent;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

/**
 * @author Samuel Gratzl
 *
 */
public final class SearchAction extends SimpleAction implements IGLKeyListener {

	public SearchAction() {
		super("&Find Cluster", BiClusterRenderStyle.ICON_FIND, Activator.getResourceLoader());
	}

	@Override
	public void run() {
		InputDialog d = new InputDialog(null, "Find Cluster", "Find Cluster (at least 2 characters)", "",
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						newText = newText.trim();
						if (newText.length() >= 2)
							EventPublisher.trigger(new SearchClusterEvent(newText));
						return null;
					}
				});
		if (d.open() == Window.OK) {
			EventPublisher.trigger(new SearchClusterEvent(d.getValue()));
		} else {
			EventPublisher.trigger(new SearchClusterEvent(null));
		}
	}

	@Override
	public void keyPressed(IKeyEvent e) {

	}

	@Override
	public void keyReleased(IKeyEvent e) {
		if (e.isControlDown() && e.isKey('f')) {
			run();
		}
	}
}