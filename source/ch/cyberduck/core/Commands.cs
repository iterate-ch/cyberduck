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
using System;
using System.Collections.Generic;
using System.Windows.Forms;

namespace Ch.Cyberduck.Core
{
    /// <summary>
    /// Group of commands which belongs to a view.
    /// </summary>
    public class Commands
    {
        private readonly List<Command> _commands = new List<Command>();

        /// <summary>
        /// Validate all commands
        /// </summary>
        public void Validate()
        {
            foreach (Command command in _commands)
            {
                command.Validate();
            }
        }

        public void Add(ToolStripItem[] items,
                        EventHandler clickDelegate,
                        ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(items, clickDelegate, validateDelegate));
        }

        public void Add(Control[] controls,
                        EventHandler clickDelegate,
                        ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(null, controls, clickDelegate, validateDelegate));
        }

        public void Add(Control control,
                        EventHandler clickDelegate,
                        ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(null, new[] {control}, clickDelegate, validateDelegate));
        }

        public void Add(Control control,
                        ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(null, new[] {control}, delegate {  }, validateDelegate));
        }

        public void Add(ToolStripItem[] items,
                        Control[] controls,
                        EventHandler clickDelegate,
                        ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(items, controls, clickDelegate, validateDelegate));
        }

        /// <summary>
        /// Wraps a main menu item, a context menu item and toolstrip button into a command. A command can
        /// be validated (enable/disable) and executed.
        /// </summary>
        private class Command
        {
            private readonly EventHandler _clickCommandDelegate;
            private readonly Control[] _controls;
            private readonly ToolStripItem[] _items;
            private readonly ValidateCommand _validateCommandDelegate;

            public Command(ToolStripItem[] items,
                           Control[] controls,
                           EventHandler clickDelegate,
                           ValidateCommand validateDelegate)
            {
                _items = items;
                _controls = controls;
                _validateCommandDelegate = validateDelegate;
                _clickCommandDelegate = clickDelegate;

                if (items != null)
                    foreach (ToolStripItem item in items)
                    {
                        item.Click += _clickCommandDelegate;
                    }

                if (controls != null)
                    foreach (Control control in controls)
                    {
                        control.Click += _clickCommandDelegate;
                    }
            }

            public Command(ToolStripItem[] items,
                           EventHandler clickDelegate,
                           ValidateCommand validateDelegate) : this(items, null, clickDelegate, validateDelegate)
            {
            }

            /// <summary>
            /// Validate this command.
            /// </summary>
            public void Validate()
            {
                bool enabled = _validateCommandDelegate();
                if (_items != null)
                    foreach (ToolStripItem item in _items)
                    {
                        item.Enabled = enabled;
                    }
                if (_controls != null)
                    foreach (Control control in _controls)
                    {
                        control.Enabled = enabled;
                    }
            }

            ~Command()
            {
                if (_items != null)
                    foreach (ToolStripItem item in _items)
                    {
                        item.Click -= _clickCommandDelegate;
                    }
                if (_controls != null)
                    foreach (Control control in _controls)
                    {
                        control.Click -= _clickCommandDelegate;
                    }
            }
        }
    }

    public delegate bool ValidateCommand();
}