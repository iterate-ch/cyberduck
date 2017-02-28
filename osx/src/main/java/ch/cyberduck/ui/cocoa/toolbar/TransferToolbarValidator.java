package ch.cyberduck.ui.cocoa.toolbar;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.ui.cocoa.controller.TransferController;

import org.rococoa.Foundation;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSUInteger;

import static ch.cyberduck.ui.cocoa.toolbar.TransferToolbarFactory.TransferToolbarItem.*;

public class TransferToolbarValidator implements ToolbarValidator {

    private final TransferController controller;

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
        if(action.equals(stop.action())) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return transfer.isRunning();
                }
            });
        }
        if(action.equals(reload.action())) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return transfer.getType().isReloadable() && !transfer.isRunning();
                }
            });
        }
        if(action.equals(remove.action())) {
            return this.validate(new InnerTransferValidator() {
                @Override
                public boolean validate(final Transfer transfer) {
                    return !transfer.isRunning();
                }
            });
        }
        if(action.equals(resume.action())) {
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
        if(action.equals(open.action()) || action.equals(trash.action())) {
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
        if(action.equals(reveal.action())) {
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
        if(action.equals(cleanup.action())) {
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
