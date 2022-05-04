// 
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
// http://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
// 

using System.Collections.Generic;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using Ch.Cyberduck.Core;
using org.apache.logging.log4j;
using org.apache.commons.lang3;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class DefaultBookmarkController : BookmarkController<IBookmarkView>
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(DefaultBookmarkController).FullName);

        public DefaultBookmarkController(Host bookmark) : base(bookmark)
        {
            View.ChangedPasswordEvent += View_ChangedPasswordEvent;
        }

        private void View_ChangedPasswordEvent()
        {
            if (_options.keychain() && _options.password())
            {
                if (string.IsNullOrWhiteSpace(_host.getHostname()))
                {
                    return;
                }
                if (string.IsNullOrWhiteSpace(_host.getCredentials().getUsername()))
                {
                    return;
                }
                if (string.IsNullOrWhiteSpace(View.Password))
                {
                    return;
                }
                try
                {
                    _host.getCredentials().setPassword(View.Password);
                    PasswordStoreFactory.get().save(_host);
                }
                catch (LocalAccessDeniedException e)
                {
                    Log.error($"Failure saving credentials for ${_host} in keychain. ${e.getDetail()}");
                }
            }
        }

        public static class Factory
        {
            private static readonly IDictionary<Host, DefaultBookmarkController> Open =
                new Dictionary<Host, DefaultBookmarkController>();

            public static DefaultBookmarkController Create(Host host)
            {
                DefaultBookmarkController c;
                if (Open.TryGetValue(host, out c))
                {
                    c.Update();
                    return c;
                }

                c = new DefaultBookmarkController(host);
                c.View.ViewClosedEvent += () => Open.Remove(host);
                Open.Add(host, c);
                return c;
            }
        }
    }
}
