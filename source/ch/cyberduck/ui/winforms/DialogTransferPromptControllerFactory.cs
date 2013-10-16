// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 

using Ch.Cyberduck.Ui.Controller;
using ch.cyberduck.core;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui;

namespace Ch.Cyberduck.Ui.Winforms
{
    internal class DialogTransferPromptControllerFactory : TransferPromptControllerFactory
    {
        private DialogTransferPromptControllerFactory()
        {
        }

        public static void Register()
        {
            addFactory(NATIVE_PLATFORM, new DialogTransferPromptControllerFactory());
        }

        protected override object create()
        {
            return null;
        }

        public override TransferPrompt create(ch.cyberduck.ui.Controller controller, Transfer transfer, Session session)
        {
            if (transfer.getType() == Transfer.Type.download)
            {
                return new DownloadPromptController((WindowController) controller, transfer, session);
            }
            if (transfer.getType() == Transfer.Type.upload)
            {
                return new UploadPromptController((WindowController) controller, transfer, session);
            }
            if (transfer.getType() == Transfer.Type.sync)
            {
                return new SyncPromptController((WindowController) controller, transfer, session);
            }
            return new DisabledTransferPrompt();
        }
    }
}