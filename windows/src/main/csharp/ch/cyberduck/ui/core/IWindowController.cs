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

using Ch.Cyberduck.Ui.Controller;
using System.Windows.Forms;

namespace ch.cyberduck.ui.core;

public interface IWindowController
{
    bool Visible { get; }

    IWin32Window Window { get; }

    void Invoke(AsyncController.AsyncDelegate action);

    void Invoke(AsyncController.AsyncDelegate action, bool wait);
}
