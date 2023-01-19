package ch.cyberduck.core.diagnostics;

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

import ch.cyberduck.core.Factory;

public class ReachabilityDiagnosticsFactory extends Factory<Reachability.Diagnostics> {

    public ReachabilityDiagnosticsFactory() {
        super("factory.reachability.diagnostics.class");
    }

    /**
     * @return Null if no implementation available
     */
    public static Reachability.Diagnostics get() {
        return new ReachabilityDiagnosticsFactory().create();
    }
}
