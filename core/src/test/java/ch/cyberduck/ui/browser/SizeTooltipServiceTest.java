package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Ignore;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Ignore
public class SizeTooltipServiceTest {

    @Test
    public void testGetTooltip() {
        final SizeTooltipService s = new SizeTooltipService();
        final PathAttributes attr = new PathAttributes();
        attr.setSize(-1L);
        assertEquals("--", s.getTooltip(new Path("/p", EnumSet.of(Path.Type.file), attr)));
        attr.setSize(0L);
        assertEquals("0 B", s.getTooltip(new Path("/p", EnumSet.of(Path.Type.file), attr)));
        attr.setSize(213457865421L);
        assertEquals("198.8 GiB (213,457,865,421 bytes)", s.getTooltip(new Path("/p", EnumSet.of(Path.Type.file), attr)));
    }
}
