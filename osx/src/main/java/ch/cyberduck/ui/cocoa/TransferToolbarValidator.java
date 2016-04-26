package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;

import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSUInteger;

public class TransferToolbarValidator implements ToolbarValidator {

    private TransferController controller;

    public TransferToolbarValidator(final TransferController controller) {
        this.controller = controller;
    }

    @Override
    public boolean validate(final NSToolbarItem item) {
        return this.validate(item.action());
    }

    @Override
    public boolean validate(final Selector action) {
        if(action.equals(Foundation.selector("paste:"))) {
            return !PathPasteboardFactory.allPasteboards().isEmpty();
        }
        if(action.equals(Foundation.selector("stopButtonClicked:"))) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("reloadButtonClicked:"))) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return transfer.getType().isReloadable() && !transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("deleteButtonClicked:"))) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return !transfer.isRunning();
                }
            });
        }
        if(action.equals(Foundation.selector("resumeButtonClicked:"))) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    if(transfer.isRunning()) {
                        return false;
                    }
                    return !transfer.isComplete();
                }
            });
        }
        if(action.equals(Foundation.selector("openButtonClicked:"))
                || action.equals(Foundation.selector("trashButtonClicked:"))) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    if(transfer.getLocal() != null) {
                        if(!transfer.isComplete()) {
                            return false;
                        }
                        if(!transfer.isRunning()) {
                            for(TransferItem l : transfer.getRoots()) {
                                if(l.local.exists()) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }
        if(action.equals(Foundation.selector("revealButtonClicked:"))) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    if(transfer.getLocal() != null) {
                        for(TransferItem l : transfer.getRoots()) {
                            if(l.local.exists()) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        if(action.equals(Foundation.selector("clearButtonClicked:"))) {
            return controller.getTransferTable().numberOfRows().intValue() > 0;
        }
        return true;
    }


    /**
     * Validates the selected items in the transfer window against the toolbar validator
     *
     * @param validator The validator to use
     * @return True if one or more of the selected items passes the validation test
     */
    private boolean validate(final InnerTransferValidator validator) {
        final NSIndexSet iterator = controller.getTransferTable().selectedRowIndexes();
        final Collection<Transfer> transfers = controller.getTransferTableModel().getSource();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Transfer transfer = transfers.get(index.intValue());
            if(validator.validate(transfer)) {
                return true;
            }
        }
        return false;
    }

    private interface InnerTransferValidator {
        boolean validate(Transfer transfer);
    }
}
