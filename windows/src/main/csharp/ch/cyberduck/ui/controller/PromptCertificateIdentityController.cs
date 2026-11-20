// Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
// https://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

using Ch.Cyberduck.Core.Refresh.Interactivity;
using ch.cyberduck.ui.core;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <summary>
    /// Prompt to select a client certificate shown modal to the window of the controller. Created by
    /// <c>CertificateIdentityCallbackFactory</c> with the controller of the browser window.
    /// </summary>
    public class PromptCertificateIdentityController : DialogPromptCertificateIdentityCallback
    {
        public PromptCertificateIdentityController(IWindowController c)
            // Handle of the owner window is resolved on the main thread when the prompt is shown
            : base((ch.cyberduck.core.Controller)c, () => c.Window?.Handle)
        {
        }
    }
}
