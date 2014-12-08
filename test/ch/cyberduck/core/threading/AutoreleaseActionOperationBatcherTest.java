package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class AutoreleaseActionOperationBatcherTest extends AbstractTestCase {

    @Test
    public void testOperate() throws Exception {
        this.repeat(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                new AutoreleaseActionOperationBatcher().operate();
                return null;
            }
        }, 20);
    }
}
