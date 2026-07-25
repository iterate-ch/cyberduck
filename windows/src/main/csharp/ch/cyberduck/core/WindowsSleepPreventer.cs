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

using ch.cyberduck.core;
using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core;

public sealed class WindowsSleepPreventer : SleepPreventer
{
    [Flags]
    private enum EXECUTION_STATE : uint
    {
        ES_SYSTEM_REQUIRED = 0x00000001,
        ES_CONTINUOUS = 0x80000000
    }

    [DllImport("kernel32.dll", SetLastError = true)]
    private static extern EXECUTION_STATE SetThreadExecutionState(EXECUTION_STATE executionState);

    private static readonly object sync = new();
    private static readonly HashSet<string> assertions = new(StringComparer.Ordinal);

    string SleepPreventer.@lock()
    {
        lock (sync)
        {
            if (0 == assertions.Count && !SetExecutionState(EXECUTION_STATE.ES_CONTINUOUS | EXECUTION_STATE.ES_SYSTEM_REQUIRED))
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
            if (0 == assertions.Count)
            {
                SetExecutionState(EXECUTION_STATE.ES_CONTINUOUS);
            }
        }
    }

    private static bool SetExecutionState(EXECUTION_STATE executionState)
    {
        return 0 != (uint)SetThreadExecutionState(executionState);
    }
}
