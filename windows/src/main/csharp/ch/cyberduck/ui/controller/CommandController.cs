// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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

using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Core;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class CommandController : WindowController<ICommandView>, TranscriptListener
    {
        private readonly WindowController _parent;
        private readonly Session _session;

        public CommandController(WindowController parent, Session session)
            : this(parent, session, ObjectFactory.GetInstance<ICommandView>())
        {
        }

        private CommandController(WindowController parent, Session session, ICommandView view)
        {
            View = view;
            _parent = parent;
            _session = session;
            View.SendEvent += View_SendEvent;
        }

        public override void log(TranscriptListener.Type request, string message)
        {
            AsyncDelegate mainAction = delegate { View.AddTranscriptEntry(message); };
            Invoke(mainAction);
        }

        private void View_SendEvent()
        {
            string command = View.Command;
            if (Utils.IsNotBlank(command))
            {
                View.StartActivityAnimation();
                _parent.Background(new CommandControllerBackgroundAction(this, _session, PathCache.empty(), command));
            }
        }

        private class CommandControllerBackgroundAction : ControllerBackgroundAction
        {
            private readonly string _command;
            private readonly CommandController _controller;
            private readonly Session _session;

            public CommandControllerBackgroundAction(CommandController controller, Session session, PathCache cache,
                string command) : base(controller, session, cache)
            {
                _controller = controller;
                _session = session;
                _command = command;
            }

            public override bool alert()
            {
                return false;
            }

            public override object run()
            {
                Command feature = (Command) _session.getFeature(typeof (Command));
                feature.send(_command, this, _controller);
                return null;
            }

            public override void cleanup()
            {
                base.cleanup();
                _controller.View.StopActivityAnimation();
            }

            public override string getActivity()
            {
                return _command;
            }
        }
    }
}