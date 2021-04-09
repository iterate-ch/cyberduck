/*
 * Copyright (c) 2015 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.core;

import ch.cyberduck.core.exception.LocalAccessDeniedException;

public interface UniqueIdService {
    String getUUID() throws LocalAccessDeniedException;
}
