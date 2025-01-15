//
// Copyright (c) 2015-2017 iterate GmbH. All rights reserved.
//

using System.Threading;
using org.apache.logging.log4j;
using System;
using System.Windows.Forms;
using ch.cyberduck.ui.pasteboard;

namespace Ch.Cyberduck.Ui.Pasteboard
{
    public class ClipboardService : PasteboardService
    {
        public static Logger Logger { get; } = LogManager.getLogger(typeof(ClipboardService).AssemblyQualifiedName);

        public bool add(PasteboardService.Type type, string content)
        {
            if (string.IsNullOrWhiteSpace(content))
            {
                // Content is empty, do not set clipboard.
                return false;
            }
            if (type == PasteboardService.Type.@string || type == PasteboardService.Type.url)
            {
                // needs STA threading model
                Thread thread = new Thread(() =>
                {
                    try
                    {
                        Clipboard.SetText(content);
                    }
                    catch (Exception exception)
                    {
                        Logger.error($"Failed to set clipboard to value {content}", exception);
                    }
                });
                thread.SetApartmentState(ApartmentState.STA);
                thread.Start();
                thread.Join();
                return true;
            }
            else
            {
                Logger.error($"Unsupported pasteboard type {type}");
                return false;
            }
        }
    }
}
