/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.preferences;

import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Assert;
import org.junit.Test;

public class BundleApplicationResourcesFinderTest {

    @Test
    public void testBundle() throws Exception {
        Assert.assertNotNull(new BundleApplicationResourcesFinder().bundle());
    }

    @Test
    public void testSymbolicLink() throws Exception {
        final NSBundle bundle = new BundleApplicationResourcesFinder().bundle(NSBundle.bundleWithPath("."), new Local("/usr/bin/java"));
        Assert.assertNotNull(bundle);
        Assert.assertEquals(NSBundle.bundleWithPath("/System/Library/Frameworks/JavaVM.framework/Versions/A"), bundle);
    }

    @Test
    public void testAccessDenied() throws Exception {
        final NSBundle bundle = new BundleApplicationResourcesFinder().bundle(NSBundle.bundleWithPath("."), new Local("/usr/bin/java") {
            @Override
            public Local getSymlinkTarget() throws NotfoundException {
                throw new NotfoundException("f");
            }
        });
        Assert.assertNotNull(bundle);
        Assert.assertEquals(NSBundle.bundleWithPath("."), bundle);
    }
}