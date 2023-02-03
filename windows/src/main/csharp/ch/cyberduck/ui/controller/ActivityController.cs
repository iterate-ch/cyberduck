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

using System.Collections;
using Ch.Cyberduck.Ui.Controller.Threading;
using StructureMap;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.threading;
using java.lang;
using org.apache.logging.log4j;
using Exception = System.Exception;
using Object = System.Object;

namespace Ch.Cyberduck.Ui.Controller
{
    internal sealed class ActivityController : WindowController<IActivityView>
    {
        private static readonly Logger Log = LogManager.getLogger(typeof (ActivityController).FullName);

        private static readonly object SyncRoot = new Object();
        private static volatile ActivityController _instance;

        private ActivityController()
        {
            View = ObjectFactory.GetInstance<IActivityView>();
            Init();
        }

        public static ActivityController Instance
        {
            get
            {
                if (_instance == null)
                {
                    lock (SyncRoot)
                    {
                        if (_instance == null)
                            _instance = new ActivityController();
                    }
                }
                return _instance;
            }
        }

        private void Init()
        {
            View.ModelTitleGetter =
                delegate(object rowObject) { return ((AbstractBackgroundAction) rowObject).toString(); };
            View.ModelDescriptionGetter =
                delegate(object rowObject) { return ((BackgroundAction) rowObject).getActivity(); };
            View.ModelIsRunningGetter =
                delegate(object rowObject) { return ((BackgroundAction) rowObject).isRunning(); };
            View.StopActionEvent += View_StopActionEvent;

            BackgroundActionRegistry.global().addListener(new BackgroundActionListener(this));
            // Add already running background actions
            ArrayList tasks = new ArrayList();
            int size = BackgroundActionRegistry.global().size();
            for (int i = 0; i < size; i++)
            {
                try
                {
                    BackgroundAction action = (BackgroundAction) BackgroundActionRegistry.global().get(i);
                    tasks.Add(action);
                }
                catch (Exception)
                {
                    Log.debug("BackgroundActionRegistry modified while iterating");
                    // collection has been modified by another thread, continue
                }
            }
            tasks.Reverse();
            View.SetModel(tasks);
        }

        private void View_StopActionEvent()
        {
            BackgroundAction action = View.SelectedTask;
            if (null != action)
            {
                Log.debug("Cancel action:" + action);
                action.cancel();
            }
        }

        private class BackgroundActionListener : AbstractCollectionListener
        {
            private readonly Hashtable _actionListener = Hashtable.Synchronized(new Hashtable());
            private readonly ActivityController _controller;

            public BackgroundActionListener(ActivityController controller)
            {
                _controller = controller;
            }

            public override void collectionItemAdded(object item)
            {
                BackgroundAction action = item as BackgroundAction;
                TaskListener listener = new TaskListener(_controller, action);
                _actionListener.Add(action, listener);
                _controller.Invoke(new AddTaskAction(_controller, action));
            }

            public override void collectionItemRemoved(object item)
            {
                BackgroundAction action = item as BackgroundAction;
                _actionListener.Remove(action);
                _controller.Invoke(new RemoveTaskAction(_controller, action));
            }

            private class AddTaskAction : WindowMainAction
            {
                private static readonly Logger Log = LogManager.getLogger(typeof (AddTaskAction).FullName);
                private readonly BackgroundAction _action;
                private readonly ActivityController _controller;

                public AddTaskAction(ActivityController controller, BackgroundAction action) : base(controller)
                {
                    _action = action;
                    _controller = controller;
                }

                public override void run()
                {
                    Log.debug("collectionItemAdded:" + _action);
                    _controller.View.AddTask(_action);
                }
            }

            private class RemoveTaskAction : WindowMainAction
            {
                private static readonly Logger Log = LogManager.getLogger(typeof (AddTaskAction).FullName);
                private readonly BackgroundAction _action;
                private readonly ActivityController _controller;

                public RemoveTaskAction(ActivityController controller, BackgroundAction action) : base(controller)
                {
                    _action = action;
                    _controller = controller;
                }

                public override void run()
                {
                    Log.debug("collectionItemRemoved:" + _action);
                    _controller.View.RemoveTask(_action);
                }
            }

            private class TaskListener : ch.cyberduck.core.threading.BackgroundActionListener
            {
                private readonly BackgroundAction _action;
                private readonly ActivityController _controller;

                public TaskListener(ActivityController controller, BackgroundAction action)
                {
                    _action = action;
                    _controller = controller;
                }

                public void cancel(BackgroundAction ba)
                {
                    _controller.View.RefreshTask(_action);
                }

                public void start(BackgroundAction ba)
                {
                    _controller.View.RefreshTask(_action);
                }

                public void stop(BackgroundAction ba)
                {
                    _controller.View.RefreshTask(_action);
                }


                public bool alert(Host host, BackgroundException be, StringBuilder sb)
                {
                    return false;
                }
            }
        }
    }
}
