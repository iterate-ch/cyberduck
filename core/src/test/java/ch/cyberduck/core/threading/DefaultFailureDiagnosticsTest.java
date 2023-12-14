package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.ResolveCanceledException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.exception.TransferCanceledException;
import ch.cyberduck.core.exception.TransferStatusCanceledException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultFailureDiagnosticsTest {

    @Test
    public void testDetermine() {
        assertEquals(FailureDiagnostics.Type.application, new DefaultFailureDiagnostics().determine(null));
        assertEquals(FailureDiagnostics.Type.network, new DefaultFailureDiagnostics().determine(new ResolveFailedException("d", null)));
        assertEquals(FailureDiagnostics.Type.cancel, new DefaultFailureDiagnostics().determine(new ResolveCanceledException()));
        assertEquals(FailureDiagnostics.Type.login, new DefaultFailureDiagnostics().determine(new LoginFailureException("d")));
        assertEquals(FailureDiagnostics.Type.cancel, new DefaultFailureDiagnostics().determine(new ConnectionCanceledException()));
        // By user
        assertEquals(FailureDiagnostics.Type.skip, new DefaultFailureDiagnostics().determine(new TransferCanceledException()));
        assertEquals(FailureDiagnostics.Type.application, new DefaultFailureDiagnostics().determine(new BackgroundException(new ConnectionCanceledException())));
        // By transfer status
        assertEquals(FailureDiagnostics.Type.cancel, new DefaultFailureDiagnostics().determine(new TransferStatusCanceledException()));
    }
}
