package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.TRAVERSEPERMISSION;
import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.WRITEPERMISSION;
import static org.junit.Assert.assertThrows;

@Category(IntegrationTest.class)
public class CteraListServiceTest extends AbstractCteraTest {

    @Test
    public void testPreflightFileMissingCustomProps() throws BackgroundException {
        final Path directory = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.setAttributes(directory.attributes().withAcl(Acl.EMPTY));
        new CteraListService(session).preflight(directory);
    }

    @Test
    public void testPreflightFiledAccessDeniedCustomProps() throws BackgroundException {
        final Path directory = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.setAttributes(directory.attributes().withAcl(new Acl(new Acl.CanonicalUser(), WRITEPERMISSION)));
        assertThrows(AccessDeniedException.class, () -> new CteraListService(session).preflight(directory));
    }

    @Test
    public void testPreflightFileAccessGrantedMinimalCustomProps() throws BackgroundException {
        final Path directory = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.setAttributes(directory.attributes().withAcl(new Acl(new Acl.CanonicalUser(), TRAVERSEPERMISSION)));
        new CteraListService(session).preflight(directory);
        // assert no fail
    }
}