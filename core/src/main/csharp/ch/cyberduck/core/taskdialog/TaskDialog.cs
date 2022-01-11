using System;
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

    public sealed class TaskDialog
    {
        private readonly List<TASKDIALOG_BUTTON> buttons = new();
        private readonly List<TASKDIALOG_BUTTON> radioButtons = new();
        private bool buttonsConfigured = false;
        private TaskDialogHandler callback;
        private TASKDIALOGCONFIG config;
        private bool footerIconConfigured = false;
        private bool mainIconConfigured = false;
        private bool radioButtonsConfigured = false;

        private TaskDialog()
        {
            config.cbSize = (uint)SizeOf<TASKDIALOGCONFIG>();
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
            config.pfCallback = Handler;
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
            config.pszContent = content;
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
            config.pszExpandedInformation = expandedInformation;
            return this;
        }

        public TaskDialog ExpandedInformation(in string controlText, in string expandedInformation) => ExpandedInformation(controlText, controlText, expandedInformation);

        public TaskDialog ExpandedInformation(in string expandedControlText, in string collapsedControlText, in string expandedInformation)
        {
            config.pszExpandedControlText = expandedControlText;
            config.pszCollapsedControlText = collapsedControlText;
            config.pszExpandedInformation = expandedInformation;
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

            config.Anonymous2.hFooterIcon = (HICON)icon.Handle;
            return this;
        }

        public TaskDialog FooterIcon(PCWSTR icon)
        {
            if (footerIconConfigured)
                throw new InvalidOperationException();
            footerIconConfigured = true;

            config.Anonymous2.pszFooterIcon = icon;
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
            config.pszFooter = text;
            return this;
        }

        public TaskDialog Instance(in HINSTANCE instance)
        {
            config.hInstance = instance;
            return this;
        }

        public TaskDialog Instruction(in string instruction)
        {
            config.pszMainInstruction = instruction;
            return this;
        }

        public TaskDialog MainIcon(PCWSTR icon)
        {
            if (mainIconConfigured)
                throw new InvalidOperationException();
            mainIconConfigured = true;

            config.Anonymous1.pszMainIcon = icon;
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

        public unsafe TaskDialogResult Show()
        {
            int button = default;
            int radioButton = default;
            BOOL verificationChecked = default;
            HRESULT result;

            Span<TASKDIALOG_BUTTON> buttons = stackalloc TASKDIALOG_BUTTON[(int)config.cButtons];
            Span<TASKDIALOG_BUTTON> radioButtons = stackalloc TASKDIALOG_BUTTON[(int)config.cRadioButtons];
            for (int i = 0; i < this.buttons.Count; i++)
            {
                buttons[i] = this.buttons[i];
            }
            for (int i = 0; i < this.radioButtons.Count; i++)
            {
                radioButtons[i] = this.radioButtons[i];
            }
            config.pButtons = (TASKDIALOG_BUTTON*)AsPointer(ref GetReference(buttons));
            config.pRadioButtons = (TASKDIALOG_BUTTON*)AsPointer(ref GetReference(radioButtons));

            try
            {
                Showing?.Invoke(this, EventArgs.Empty);
                result = TaskDialogIndirect(config, &button, &radioButton, &verificationChecked);
            }
            finally
            {
                Closed?.Invoke(this, EventArgs.Empty);
            }
            if (button == 0)
                Marshal.ThrowExceptionForHR(result);

            return new(
                (MESSAGEBOX_RESULT)button,
                radioButtonsConfigured ? radioButton : default,
                config.pszVerificationText.Length > 0 ? verificationChecked : default);
        }

        public TaskDialog ShowProgressbar(bool marquee)
        {
            config.dwFlags |= marquee ? TDF_SHOW_MARQUEE_PROGRESS_BAR : TDF_SHOW_PROGRESS_BAR;
            return this;
        }

        public TaskDialog Title(in string title)
        {
            config.pszWindowTitle = title;
            return this;
        }

        public TaskDialog UseHyperlinks()
        {
            config.dwFlags |= TDF_ENABLE_HYPERLINKS;
            return this;
        }

        public TaskDialog VerificationText(in string verificationText, bool verificationChecked)
        {
            config.pszVerificationText = verificationText;
            if (verificationChecked)
            {
                config.dwFlags |= TDF_VERIFICATION_FLAG_CHECKED;
            }
            return this;
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

        private void DoButtons(Action<AddButtonCallback> configure, in int defaultButton, out uint count) => DoButtons(buttons, configure, defaultButton, out count);

        private void DoButtons(List<TASKDIALOG_BUTTON> target, Action<AddButtonCallback> configure, in int defaultButton, out uint count)
        {
            MESSAGEBOX_RESULT? value = default;
            configure((MESSAGEBOX_RESULT id, in string title, bool @default) =>
            {
                target.Add(new() { nButtonID = (int)id, pszButtonText = title });
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

        private unsafe HRESULT Handler(HWND hwnd, uint msg, WPARAM wParam, LPARAM lParam, nint lpRefData)
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
            return callback(this, args) ? S_FALSE : S_OK;
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
