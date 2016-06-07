package ch.cyberduck.ui.cocoa;

import ch.cyberduck.binding.application.NSToolbarItem;

import org.rococoa.Selector;

public interface ToolbarValidator {

    boolean validate(NSToolbarItem item);

    /**
     * Validates menu and toolbar items
     *
     * @param action Method target
     * @return true if the item with the identifier should be selectable
     */
    boolean validate(Selector action);
}
