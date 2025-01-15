/*
 * Copyright (c) 2016 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.ui.pasteboard;

public interface PasteboardService {

    enum Type {
        string,
        url,
        filename
    }

    boolean add(Type type, String content);
}
