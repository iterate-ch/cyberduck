//
// Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
// https://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

using System;
using System.Collections.Generic;
using ch.cyberduck.core;
using Windows.Win32.System.Power;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core;

public sealed class WindowsSleepPreventer : SleepPreventer
{
    private readonly HashSet<string> assertions = new(StringComparer.Ordinal);
    private readonly object sync = new();

    string SleepPreventer.@lock()
    {
        lock (sync)
        {
            if (assertions.Count == 0 && !TrySetThreadExecutionState(EXECUTION_STATE.ES_CONTINUOUS | EXECUTION_STATE.ES_SYSTEM_REQUIRED))
            {
                return null;
            }

            string id = Guid.NewGuid().ToString("N");
            assertions.Add(id);
            return id;
        }
    }

    void SleepPreventer.release(string id)
    {
        if (null == id)
        {
            return;
        }

        lock (sync)
        {
            if (!assertions.Remove(id))
            {
                return;
            }

            if (assertions.Count == 0)
            {
                _ = TrySetThreadExecutionState(EXECUTION_STATE.ES_CONTINUOUS);
            }
        }
    }

    private static bool TrySetThreadExecutionState(EXECUTION_STATE executionState)
    {
        return SetThreadExecutionState(executionState) != 0;
    }
}
