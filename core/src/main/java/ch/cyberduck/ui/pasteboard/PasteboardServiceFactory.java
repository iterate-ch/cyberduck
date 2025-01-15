/*
 * Copyright (c) 2016 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.ui.pasteboard;

import ch.cyberduck.core.Factory;

public class PasteboardServiceFactory extends Factory<PasteboardService> {
    protected PasteboardServiceFactory() {
        super("factory.pasteboardservice.class");
    }

    public static PasteboardService get() {
        return new PasteboardServiceFactory().create();
    }
}
