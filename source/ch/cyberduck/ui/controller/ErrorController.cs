// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using System.Collections.Generic;
using ch.cyberduck.core;
using ch.cyberduck.core.threading;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class ErrorController : WindowController<IErrorView>
    {
        public ErrorController(IErrorView view, IEnumerable<BackgroundException> exceptions, string transcript)
        {
            View = view;

            View.SetModel(exceptions);
            View.ModelErrorMessageGetter =
                delegate(object rowObject)
                    {
                        BackgroundException failure = (BackgroundException) rowObject;
                        return GetReadableTitle(failure) + ": " + failure.getMessage();
                    };

            View.ModelHostGetter = delegate(object rowObject)
                                       {
                                           BackgroundException failure = (BackgroundException) rowObject;
                                           if (null == failure.getPath())
                                           {
                                               return failure.getSession().getHost().toURL();
                                           }
                                           return failure.getPath().getAbsolute();
                                       };
            View.ModelDescriptionGetter =
                delegate(object rowObject) { return GetDetailedCauseMessage((BackgroundException) rowObject); };

            bool log = !string.IsNullOrEmpty(transcript);
            View.Transcript = transcript;
            View.TranscriptVisible = log &&
                                     Preferences.instance().getBoolean("alert.toggle.transcript");
            View.TranscriptEnabled = log;
            View.ToggleTranscriptEvent += View_ToggleTranscriptEvent;
        }

        private void View_ToggleTranscriptEvent()
        {
            View.TranscriptVisible = !View.TranscriptVisible;
            Preferences.instance().setProperty("alert.toggle.transcript", View.TranscriptVisible);
        }

        private string GetReadableTitle(BackgroundException e)
        {
            return e.getReadableTitle();
        }

        private string GetDetailedCauseMessage(BackgroundException e)
        {
            return e.getDetailedCauseMessage();
        }
    }
}