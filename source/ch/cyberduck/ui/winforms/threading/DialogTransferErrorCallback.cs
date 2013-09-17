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

using System;
using System.Threading;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.transfer;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogTransferErrorCallback : TransferErrorCallback
    {
        private readonly WindowController _controller;
        private bool _option;
        private bool _supressed;

        public DialogTransferErrorCallback(WindowController controller)
        {
            _controller = controller;
        }

        public bool prompt(BackgroundException failure)
        {
            if (_supressed)
            {
                return _option;
            }
            if (_controller.Visible)
            {
                AtomicBoolean c = new AtomicBoolean(true);
                _controller.Invoke(delegate
                    {
                        _controller.View.CommandBox(LocaleFactory.localizedString("Error"),
                                                    failure.getMessage() ?? LocaleFactory.localizedString("Unknown"),
                                                    failure.getDetail() ?? LocaleFactory.localizedString("Unknown"),
                                                    null,
                                                    null,
                                                    LocaleFactory.localizedString("Always"),
                                                    String.Format("{0}|{1}",
                                                                  LocaleFactory.localizedString("Continue",
                                                                                                "Credentials"),
                                                                  LocaleFactory.localizedString("Cancel")), false,
                                                    SysIcons.Warning, SysIcons.Information,
                                                    delegate(int opt, bool verificationChecked)
                                                        {
                                                            if (opt == 1)
                                                            {
                                                                c.SetValue(false);
                                                            }

                                                            if (verificationChecked)
                                                            {
                                                                _supressed = true;
                                                                _option = c.Value;
                                                            }
                                                        });
                    }, true);
                return c.Value;
            }
            // Abort
            return false;
        }

        /// <summary>
        /// Provides non-blocking, thread-safe access to a boolean valueB.
        /// </summary>
        private class AtomicBoolean
        {
            private const int VALUE_FALSE = 0;
            private const int VALUE_TRUE = 1;

            private int _currentValue;

            public AtomicBoolean(bool initialValue)
            {
                _currentValue = BoolToInt(initialValue);
            }

            public bool Value
            {
                get
                {
                    return IntToBool(Interlocked.Add(
                        ref _currentValue, 0));
                }
            }

            private int BoolToInt(bool value)
            {
                return value ? VALUE_TRUE : VALUE_FALSE;
            }

            private bool IntToBool(int value)
            {
                return value == VALUE_TRUE;
            }

            /// <summary>
            /// Sets the boolean value.
            /// </summary>
            /// <param name="newValue"></param>
            /// <returns>The original value.</returns>
            public bool SetValue(bool newValue)
            {
                return IntToBool(
                    Interlocked.Exchange(ref _currentValue,
                                         BoolToInt(newValue)));
            }

            /// <summary>
            /// Compares with expected value and if same, assigns the new value.
            /// </summary>
            /// <param name="expectedValue"></param>
            /// <param name="newValue"></param>
            /// <returns>True if able to compare and set, otherwise false.</returns>
            public bool CompareAndSet(bool expectedValue,
                                      bool newValue)
            {
                int expectedVal = BoolToInt(expectedValue);
                int newVal = BoolToInt(newValue);
                return Interlocked.CompareExchange(
                    ref _currentValue, newVal, expectedVal) == expectedVal;
            }
        }
    }
}