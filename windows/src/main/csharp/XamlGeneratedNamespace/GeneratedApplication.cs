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

using System.Threading;
using System.Threading.Tasks;
using System.Windows.Threading;
using static Windows.Win32.PInvoke;

namespace XamlGeneratedNamespace;

partial class GeneratedApplication
{
    public static GeneratedApplication OffThread()
    {
        TaskCompletionSource<GeneratedApplication> appResult = new();
        Thread thread = new(static state =>
        {
            SetThreadDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);
            GeneratedApplication app = new();
            app.InitializeComponent();
            ((TaskCompletionSource<GeneratedApplication>)state).SetResult(app);
            try
            {
                Dispatcher.Run();
            }
            catch { }
        })
        {
            IsBackground = true
        };
        thread.TrySetApartmentState(ApartmentState.STA);
        thread.Start(appResult);

        return appResult.Task.Result;
    }
}
