//
// Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

using System;
using System.Buffers;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.InteropServices;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Controls;
using Windows.Win32.UI.WindowsAndMessaging;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Controls.TASKDIALOG_COMMON_BUTTON_FLAGS;
using static Windows.Win32.UI.Controls.TASKDIALOG_FLAGS;
using static Windows.Win32.UI.Controls.TASKDIALOG_NOTIFICATIONS;

namespace Ch.Cyberduck.Core.TaskDialog
{
    public enum TaskDialogIcon
    {
        Warning,
        Error,
        Information,
        Shield,
        Question
    }

    public readonly struct TaskDialogResult
    {
        public readonly MESSAGEBOX_RESULT Button;
        public readonly int? RadioButton;
        public readonly bool? VerificationChecked;

        public TaskDialogResult(MESSAGEBOX_RESULT button, int? radioButton, bool? verificationChecked)
        {
            Button = button;
            RadioButton = radioButton;
            VerificationChecked = verificationChecked;
        }
    }

    public sealed class TaskDialog : IDisposable
    {
        private readonly List<TASKDIALOG_BUTTON> buttons = new();
        private readonly List<TASKDIALOG_BUTTON> radioButtons = new();
        private readonly GCRegistry registry = new();
        private bool buttonsConfigured = false;
        private TaskDialogHandler callback;
        private TASKDIALOGCONFIG config;
        private bool disposedValue;
        private bool footerIconConfigured = false;
        private bool hasVerification = false;
        private bool mainIconConfigured = false;
        private bool radioButtonsConfigured = false;

        private static readonly PFTASKDIALOGCALLBACK s_callbackProcDelegate;

        static TaskDialog()
        {
            s_callbackProcDelegate = (hwnd, msg, wParam, lParam, lpRefData) =>
            ((TaskDialog)GCHandle.FromIntPtr(lpRefData).Target).Handler(hwnd, msg, wParam, lParam);
        }

        private TaskDialog()
        {
            config.cbSize = (uint)SizeOf<TASKDIALOGCONFIG>();
        }

        ~TaskDialog()
        {
            Dispose(disposing: false);
        }

        public delegate void AddButtonCallback(MESSAGEBOX_RESULT id, in string title, bool @default);

        public delegate bool TaskDialogHandler(object sender, EventArgs e);

        public static event EventHandler Closed;

        public static event EventHandler Showing;

        internal interface ITaskDialog
        {
            HWND HWND { get; }
        }

        public static TaskDialog Create() => new();

        public TaskDialog AllowCancellation()
        {
            config.dwFlags |= TDF_ALLOW_DIALOG_CANCELLATION;
            return this;
        }

        public TaskDialog Callback(TaskDialogHandler handler)
        {
            callback = handler;
            return this;
        }

        public TaskDialog CommandLinks(Action<AddButtonCallback> configure, bool noIcon = false)
        {
            if (buttonsConfigured)
                throw new InvalidOperationException();
            buttonsConfigured = true;
            config.dwFlags |= noIcon ? TDF_USE_COMMAND_LINKS_NO_ICON : TDF_USE_COMMAND_LINKS;
            DoButtons(configure, config.nDefaultButton, out config.cButtons);
            return this;
        }

        public TaskDialog CommonButtons(TASKDIALOG_COMMON_BUTTON_FLAGS commonButtons, MESSAGEBOX_RESULT? optionalDefaultButton = default)
        {
            config.dwCommonButtons = commonButtons;
            if ((commonButtons & (TDCBF_CLOSE_BUTTON | TDCBF_CANCEL_BUTTON)) > 0)
            {
                config.dwFlags |= TDF_ALLOW_DIALOG_CANCELLATION;
            }
            if (optionalDefaultButton is MESSAGEBOX_RESULT defaultButton)
            {
                config.nDefaultButton = (int)defaultButton;
            }
            return this;
        }

        public TaskDialog Content(in string content)
        {
            PinValue(out config.pszContent, content, registry);
            return this;
        }

        public TaskDialog CustomButtons(Action<AddButtonCallback> configure)
        {
            if (buttonsConfigured)
                throw new InvalidOperationException();
            buttonsConfigured = true;
            DoButtons(configure, config.nDefaultButton, out config.cButtons);
            return this;
        }

        public void Dispose()
        {
            Dispose(disposing: true);
            GC.SuppressFinalize(this);
        }

        public TaskDialog EnableCallbackTimer()
        {
            config.dwFlags |= TDF_CALLBACK_TIMER;
            return this;
        }

        public TaskDialog ExpandedByDefault()
        {
            config.dwFlags |= TDF_EXPANDED_BY_DEFAULT;
            return this;
        }

        public TaskDialog ExpandedInformation(in string expandedInformation)
        {
            PinValue(out config.pszExpandedInformation, expandedInformation, registry);
            return this;
        }

        public TaskDialog ExpandedInformation(in string controlText, in string expandedInformation) => ExpandedInformation(controlText, controlText, expandedInformation);

        public TaskDialog ExpandedInformation(in string expandedControlText, in string collapsedControlText, in string expandedInformation)
        {
            PinValue(out config.pszExpandedControlText, expandedControlText, registry);
            PinValue(out config.pszCollapsedControlText, collapsedControlText, registry);
            PinValue(out config.pszExpandedInformation, expandedInformation, registry);
            return this;
        }

        public TaskDialog ExpandToFooter()
        {
            config.dwFlags |= TDF_EXPAND_FOOTER_AREA;
            return this;
        }

        public TaskDialog FooterIcon(Icon icon)
        {
            if (footerIconConfigured)
                throw new InvalidOperationException();
            footerIconConfigured = true;

            config.dwFlags |= TDF_USE_HICON_FOOTER;
            config.Anonymous2.hFooterIcon = (HICON)icon.Handle;
            return this;
        }

        public TaskDialog FooterIcon(TaskDialogIcon icon)
        {
            if (footerIconConfigured)
                throw new InvalidOperationException();
            footerIconConfigured = true;

            config.Anonymous2.pszFooterIcon = ToIcon(icon);
            return this;
        }

        public TaskDialog FooterText(string text)
        {
            PinValue(out config.pszFooter, text, registry);
            return this;
        }

        public TaskDialog Instance(in HINSTANCE instance)
        {
            config.hInstance = instance;
            return this;
        }

        public TaskDialog Instruction(in string instruction)
        {
            PinValue(out config.pszMainInstruction, instruction, registry);
            return this;
        }

        public TaskDialog MainIcon(TaskDialogIcon icon)
        {
            if (mainIconConfigured)
                throw new InvalidOperationException();
            mainIconConfigured = true;

            config.Anonymous1.pszMainIcon = ToIcon(icon);
            return this;
        }

        public TaskDialog MainIcon(Icon icon)
        {
            if (mainIconConfigured)
                throw new InvalidOperationException();
            mainIconConfigured = true;

            config.dwFlags |= TDF_USE_HICON_MAIN;
            config.Anonymous1.hMainIcon = (HICON)icon.Handle;
            return this;
        }

        public TaskDialog Parent(in HWND parent)
        {
            config.hwndParent = parent;
            return this;
        }

        public TaskDialog RadioButtons(Action<AddButtonCallback> configure)
        {
            if (radioButtonsConfigured)
                throw new InvalidOperationException();
            radioButtonsConfigured = true;

            DoButtons(radioButtons, configure, config.nDefaultRadioButton, out config.cRadioButtons);
            return this;
        }

        /// <summary>
        /// Shows this TaskDialog instance, and disposes any allocated resources immediately
        /// </summary>
        public unsafe TaskDialogResult Show()
        {
            int button = default;
            int radioButton = default;
            BOOL verificationChecked = default;
            HRESULT result;

            unsafe static void CopyButtons(ref TASKDIALOG_BUTTON target, List<TASKDIALOG_BUTTON> buttons, out TASKDIALOG_BUTTON* first)
            {
                first = (TASKDIALOG_BUTTON*)AsPointer(ref target);
                ref TASKDIALOG_BUTTON next = ref target;
                foreach (var item in buttons)
                {
                    next = item;
                    next = ref Add(ref next, 1);
                }
            }

            int buttonCount = buttons.Count;
            int radioButtonCount = radioButtons.Count;
            using var buttonMemory = MemoryPool<TASKDIALOG_BUTTON>.Shared.Rent(buttonCount + radioButtonCount);
            registry.Register(buttonMemory.Memory.Pin());
            CopyButtons(ref GetReference(buttonMemory.Memory.Slice(0, buttonCount).Span), buttons, out config.pButtons);
            CopyButtons(ref GetReference(buttonMemory.Memory.Slice(buttonCount, radioButtonCount).Span), radioButtons, out config.pRadioButtons);

            GCHandle handle = default;
            try
            {
                Showing?.Invoke(this, EventArgs.Empty);
                TASKDIALOGCONFIG copy = config;
                config = default;
                copy.pfCallback = s_callbackProcDelegate;

                handle = GCHandle.Alloc(this, GCHandleType.Normal);
                copy.lpCallbackData = GCHandle.ToIntPtr(handle);

                result = TaskDialogIndirect(copy, &button, &radioButton, &verificationChecked);
            }
            finally
            {
                if (handle.IsAllocated)
                {
                    handle.Free();
                }
                Closed?.Invoke(this, EventArgs.Empty);
                this.Dispose();
            }
            if (button == 0)
                Marshal.ThrowExceptionForHR(result);

            return new(
                (MESSAGEBOX_RESULT)button,
                radioButtonsConfigured ? radioButton : default,
                hasVerification ? verificationChecked : default);
        }

        public TaskDialog ShowProgressbar(bool marquee)
        {
            config.dwFlags |= marquee ? TDF_SHOW_MARQUEE_PROGRESS_BAR : TDF_SHOW_PROGRESS_BAR;
            return this;
        }

        public TaskDialog Title(in string title)
        {
            PinValue(out config.pszWindowTitle, title, registry);
            return this;
        }

        public TaskDialog UseHyperlinks()
        {
            config.dwFlags |= TDF_ENABLE_HYPERLINKS;
            return this;
        }

        public TaskDialog VerificationText(in string verificationText, bool verificationChecked)
        {
            hasVerification = true;
            PinValue(out config.pszVerificationText, verificationText, registry);
            if (verificationChecked)
            {
                config.dwFlags |= TDF_VERIFICATION_FLAG_CHECKED;
            }
            return this;
        }

        private static unsafe void PinValue(out PCWSTR pcwstr, string value, in GCRegistry registry)
        {
            PinValue(out char* ptr, value, registry);
            pcwstr = ptr;
        }

        private static unsafe void PinValue<TIn, TOut>(out TOut* ptr, TIn value, in GCRegistry registry)
            where TIn : class
            where TOut : unmanaged
        {
            PinValue(out void* temp, value, registry);
            ptr = (TOut*)temp;
        }

        private static unsafe void PinValue<TIn>(out nint ptr, TIn value, in GCRegistry registry)
            where TIn : class
        {
            PinValue(out void* temp, value, registry);
            ptr = (nint)temp;
        }

        private static unsafe void PinValue<TIn>(out void* ptr, TIn value, in GCRegistry registry)
            where TIn : class
        {
            var alloc = GCHandle.Alloc(value, GCHandleType.Pinned);
            var handle = new MemoryHandle((void*)alloc.AddrOfPinnedObject(), alloc);
            registry.Register(handle);
            ptr = handle.Pointer;
        }

        private static PCWSTR ToIcon(TaskDialogIcon icon) => icon switch
        {
            TaskDialogIcon.Warning => TD_WARNING_ICON,
            TaskDialogIcon.Error => TD_ERROR_ICON,
            TaskDialogIcon.Information => TD_INFORMATION_ICON,
            TaskDialogIcon.Shield => TD_SHIELD_ICON,
            TaskDialogIcon.Question => TD_QUESTION_ICON,
            _ => throw new ArgumentOutOfRangeException(nameof(icon))
        };

        private void Dispose(bool disposing)
        {
            if (!disposedValue)
            {
                disposedValue = true;
                registry.Dispose();
            }
        }

        private void DoButtons(Action<AddButtonCallback> configure, in int defaultButton, out uint count) => DoButtons(buttons, configure, defaultButton, out count);

        private unsafe void DoButtons(List<TASKDIALOG_BUTTON> target, Action<AddButtonCallback> configure, in int defaultButton, out uint count)
        {
            MESSAGEBOX_RESULT? value = default;
            configure((MESSAGEBOX_RESULT id, in string title, bool @default) =>
            {
                PinValue(out char* pinned, title, registry);
                target.Add(new() { nButtonID = (int)id, pszButtonText = pinned });
                if (value.HasValue)
                {
                    value = id;
                }
                else
                {
                    value = 0;
                }
            });
            count = (uint)target.Count;
            if (value is MESSAGEBOX_RESULT button && button != 0)
            {
                AsRef(defaultButton) = (int)button;
            }
        }

        private unsafe HRESULT Handler(HWND hwnd, uint msg, WPARAM wParam, LPARAM lParam)
        {
            var notification = (TASKDIALOG_NOTIFICATIONS)msg;

            EventArgs args = (TASKDIALOG_NOTIFICATIONS)msg switch
            {
                TDN_CREATED => new TaskDialogCreatedEventArgs(hwnd),
                TDN_NAVIGATED => new TaskDialogNavigatedEventArgs(hwnd),
                TDN_BUTTON_CLICKED => new TaskDialogButtonClickedEventArgs(hwnd, (int)wParam.Value),
                TDN_HYPERLINK_CLICKED => new TaskDialogHyperlinkClickedEventArgs(hwnd, ((PWSTR)(char*)lParam.Value).ToString()),
                TDN_TIMER => new TaskDialogTimerEventArgs(hwnd, TimeSpan.FromMilliseconds(wParam.Value)),
                TDN_DESTROYED => new TaskDialogDestroyedEventArgs(hwnd),
                TDN_RADIO_BUTTON_CLICKED => new TaskDialogRadioButtonClickedEventArgs(hwnd, (int)wParam.Value),
                TDN_DIALOG_CONSTRUCTED => new TaskDialogConstructedEventArgs(hwnd),
                TDN_VERIFICATION_CLICKED => new TaskDialogVerificationClickedEventArgs(hwnd, wParam.Value != 0),
                TDN_HELP => new TaskDialogHelpEventArgs(hwnd),
                TDN_EXPANDO_BUTTON_CLICKED => new TaskDialogExpandoButtonClickedEventArgs(hwnd, wParam.Value != 0),
                _ => default
            };
            return callback?.Invoke(this, args) == true ? S_FALSE : S_OK;
        }

        private struct GCRegistry : IDisposable
        {
            private readonly LinkedList<MemoryHandle> disposables = new();

            public GCRegistry()
            { }

            public void Dispose()
            {
                while (disposables.Last is LinkedListNode<MemoryHandle> node)
                {
                    node.Value.Dispose();
                    disposables.Remove(node);
                }
            }

            public void Register(in MemoryHandle handle) => disposables.AddLast(handle);
        }
    }

    public class TaskDialogButtonClickedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogButtonClickedEventArgs(HWND hwnd, int buttonId) : base(hwnd)
        {
            ButtonId = buttonId;
        }

        public int ButtonId { get; }
    }

    public class TaskDialogConstructedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogConstructedEventArgs(HWND hwnd) : base(hwnd)
        {
        }
    }

    public class TaskDialogCreatedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogCreatedEventArgs(HWND hwnd) : base(hwnd)
        {
        }
    }

    public class TaskDialogDestroyedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogDestroyedEventArgs(HWND hwnd) : base(hwnd)
        {
        }
    }

    public class TaskDialogEventArgs : EventArgs, TaskDialog.ITaskDialog
    {
        private readonly HWND hwnd;

        public TaskDialogEventArgs(HWND hwnd)
        {
            this.hwnd = hwnd;
        }

        HWND TaskDialog.ITaskDialog.HWND => hwnd;
    }

    public class TaskDialogExpandoButtonClickedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogExpandoButtonClickedEventArgs(HWND hwnd, bool expanded) : base(hwnd)
        {
            Expanded = expanded;
        }

        public bool Expanded { get; }
    }

    public class TaskDialogHelpEventArgs : TaskDialogEventArgs
    {
        public TaskDialogHelpEventArgs(HWND hwnd) : base(hwnd)
        {
        }
    }

    public class TaskDialogHyperlinkClickedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogHyperlinkClickedEventArgs(HWND hwnd, string url) : base(hwnd)
        {
            Url = url;
        }

        public string Url { get; }
    }

    public class TaskDialogNavigatedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogNavigatedEventArgs(HWND hwnd) : base(hwnd)
        {
        }
    }

    public class TaskDialogRadioButtonClickedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogRadioButtonClickedEventArgs(HWND hwnd, int radioButtonId) : base(hwnd)
        {
            RadioButtonId = radioButtonId;
        }

        public int RadioButtonId { get; }
    }

    public class TaskDialogTimerEventArgs : TaskDialogEventArgs
    {
        public TaskDialogTimerEventArgs(HWND hwnd, TimeSpan time) : base(hwnd)
        {
            Time = time;
        }

        public TimeSpan Time { get; }
    }

    public class TaskDialogVerificationClickedEventArgs : TaskDialogEventArgs
    {
        public TaskDialogVerificationClickedEventArgs(HWND hwnd, bool @checked) : base(hwnd)
        {
            Checked = @checked;
        }

        public bool Checked { get; }
    }
}
