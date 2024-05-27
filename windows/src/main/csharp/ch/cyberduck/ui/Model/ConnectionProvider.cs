// Copyright(c) 2002 - 2024 iterate GmbH. All rights reserved.
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

using ch.cyberduck.core.i18n;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.transfer;
using System;
using System.Collections.Generic;
using System.Linq;

namespace ch.cyberduck.ui.Model;

public class ConnectionProvider
{
    private readonly string connectionFormat;

    public IReadOnlyList<ConnectionModel> Connections { get; }

    public ConnectionProvider(Preferences preferences, Locale locale)
    {
        connectionFormat = locale.localize("{0} Connections", "Transfer");
        Connections = preferences.getProperty("queue.connections.options")
            .Split([','], StringSplitOptions.RemoveEmptyEntries)
            .Select(int.Parse)
            .Select(v => new ConnectionModel(v, v == TransferConnectionLimiter.AUTO
                ? locale.localize("Auto", "Localizable")
                : Format(v))).ToArray();
    }

    public string Format(int value) => string.Format(connectionFormat, value);
}
