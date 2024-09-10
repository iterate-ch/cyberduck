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

using System;
using System.Collections.Generic;
using System.Windows.Forms;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Ui.Core
{
    /// <summary>
    /// Group of commands which belongs to a view.
    /// </summary>
    public class Commands
    {
        public static readonly ValidateCommand True = () => true;

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

        // this maps default mappings to not hit on clicked.
        private static ValidateCommand2 Map(ValidateCommand command) => clicked => !clicked && command();

        public void Add(ToolStripItem[] toolStripItems, MenuItem[] menuItems, EventHandler clickDelegate,
            ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(toolStripItems, menuItems, clickDelegate, Map(validateDelegate)));
        }

        public void Add(ToolStripItem[] toolStripItems, MenuItem[] menuItems, EventHandler clickDelegate,
            ValidateCommand2 validateDelegate)
        {
            _commands.Add(new Command(toolStripItems, menuItems, clickDelegate, validateDelegate));
        }

        /*
        public void Add(Control[] controls,
                        EventHandler clickDelegate,
                        ValidateCommand validateDelegate)
        {
            _commands.Add(new Command(null, controls, clickDelegate, validateDelegate));
        }
         */

        public void Add(Control control, EventHandler clickDelegate, ValidateCommand validateDelegate) => Add(control, clickDelegate, Map(validateDelegate));

        public void Add(Control control, EventHandler clickDelegate, ValidateCommand2 validateDelegate)
        {
            _commands.Add(new Command(null, null, new[] { control }, clickDelegate, validateDelegate));
        }

        public void Add(Control control, ValidateCommand validateDelegate) => Add(control, Map(validateDelegate));

        public void Add(Control control, ValidateCommand2 validateDelegate)
        {
            _commands.Add(new Command(null, null, new[] { control }, delegate { }, validateDelegate));
        }

        public void Add(ToolStripItem[] items, Control[] controls, MenuItem[] menuItems, EventHandler clickDelegate,
            ValidateCommand validateDelegate) => Add(items, controls, menuItems, clickDelegate, Map(validateDelegate));

        public void Add(ToolStripItem[] items, Control[] controls, MenuItem[] menuItems, EventHandler clickDelegate,
            ValidateCommand2 validateDelegate)
        {
            _commands.Add(new Command(items, menuItems, controls, clickDelegate, validateDelegate));
        }

        /// <summary>
        /// Wraps a main menu item, a context menu item and toolstrip button into a command. A command can
        /// be validated (enable/disable) and executed.
        /// </summary>
        private class Command
        {
            private static readonly Logger Log = LogManager.getLogger(typeof(Command).FullName);

            private readonly EventHandler _clickCommandDelegate;
            private readonly Control[] _controls;
            private readonly MenuItem[] _menuItems;
            private readonly ToolStripItem[] _toolStripItems;
            private readonly ValidateCommand2 _validateCommandDelegate;
            private bool clicked = false;

            public Command(ToolStripItem[] toolStripItems, MenuItem[] menuItems, Control[] controls,
                EventHandler clickDelegate, ValidateCommand2 validateDelegate)
            {
                _toolStripItems = toolStripItems;
                _menuItems = menuItems;
                _controls = controls;
                _validateCommandDelegate = validateDelegate;
                _clickCommandDelegate = (s, e) =>
                {
                    clicked = true;
                    Validate();
                    try
                    {
                        clickDelegate(s, e);
                    }
                    finally
                    {
                        clicked = false;
                    }
                };

                if (toolStripItems != null)
                    foreach (ToolStripItem item in toolStripItems)
                    {
                        item.Click += _clickCommandDelegate;
                    }

                if (controls != null)
                    foreach (Control control in controls)
                    {
                        control.Click += _clickCommandDelegate;
                    }
                if (menuItems != null)
                {
                    foreach (MenuItem item in menuItems)
                    {
                        item.Click += _clickCommandDelegate;
                    }
                }
            }

            public Command(ToolStripItem[] toolStripItems, MenuItem[] menuItems, EventHandler clickDelegate,
                ValidateCommand2 validateDelegate)
                : this(toolStripItems, menuItems, null, clickDelegate, validateDelegate)
            {
            }

            /// <summary>
            /// Validate this command.
            /// </summary>
            public void Validate()
            {
                bool enabled = TryValidate();
                if (_toolStripItems != null)
                    foreach (ToolStripItem item in _toolStripItems)
                    {
                        try
                        {
                            item.Enabled = enabled;
                        }
                        catch
                        {
                            /* NOP */
                        }
                    }
                if (_controls != null)
                    foreach (Control control in _controls)
                    {
                        try
                        {
                            control.Enabled = enabled;
                        }
                        catch
                        {
                            /* NOP */
                        }
                    }
                if (_menuItems != null)
                {
                    foreach (var item in _menuItems)
                    {
                        try
                        {
                            item.Enabled = enabled;
                        }
                        catch
                        {
                            /* NOP */
                        }
                    }
                }
            }

            ~Command()
            {
                if (_toolStripItems != null)
                    foreach (ToolStripItem item in _toolStripItems)
                    {
                        item.Click -= _clickCommandDelegate;
                    }
                if (_controls != null)
                    foreach (Control control in _controls)
                    {
                        control.Click -= _clickCommandDelegate;
                    }
                //apparently we do not need to remove the click handler from any MenuItem. Seems
                //already removed at this point.
            }

            private bool TryValidate()
            {
                try
                {
                    return _validateCommandDelegate(clicked);
                }
                catch (Exception ex)
                {
                    if (Log.isDebugEnabled())
                    {
                        Log.info("Failure validating", ex);
                    }
                }

                return false;
            }
        }
    }

    public delegate bool ValidateCommand();

    public delegate bool ValidateCommand2(bool clicked);
}
