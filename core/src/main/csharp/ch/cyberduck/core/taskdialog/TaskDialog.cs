using System;
using System.Collections.Generic;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Controls;
using static System.Runtime.CompilerServices.Unsafe;
using static Windows.Win32.Constants;
using Windows.Win32.UI.WindowsAndMessaging;
using static Windows.Win32.UI.WindowsAndMessaging.MESSAGEBOX_RESULT;
using static Windows.Win32.UI.Controls.TASKDIALOG_COMMON_BUTTON_FLAGS;
using static Windows.Win32.UI.Controls.TASKDIALOG_NOTIFICATIONS;
using static Windows.Win32.UI.Controls.TASKDIALOG_FLAGS;

namespace Ch.Cyberduck.Core.TaskDialog
{
    public class TaskDialogCreatedEventArgs : EventArgs { }
    public class TaskDialogNavigatedEventArgs : EventArgs { }
    public class TaskDialogButtonClickedEventArgs : EventArgs
    {
        public int ButtonId { get; }

        public TaskDialogButtonClickedEventArgs(int buttonId)
        {
            ButtonId = buttonId;
        }
    }
    public class TaskDialogHyperlinkClickedEventArgs : EventArgs
    {
        public string Url { get; }

        public TaskDialogHyperlinkClickedEventArgs(string url)
        {
            Url = url;
        }
    }
    public class TaskDialogTimerEventArgs : EventArgs
    {
        public TimeSpan Time { get; }
        public TaskDialogTimerEventArgs(TimeSpan time)
        {
            Time = time;
        }
    }
    public class TaskDialogDestroyedEventArgs : EventArgs { }
    public class TaskDialogRadioButtonClickedEventArgs : EventArgs
    {
        public int RadioButtonId { get; }

        public TaskDialogRadioButtonClickedEventArgs(int radioButtonId)
        {
            RadioButtonId = radioButtonId;
        }
    }
    public class TaskDialogConstructedEventArgs : EventArgs { }
    public class TaskDialogVerificationClickedEventArgs : EventArgs
    {
        public bool Checked { get; }

        public TaskDialogVerificationClickedEventArgs(bool @checked)
        {
            Checked = @checked;
        }
    }
    public class TaskDialogHelpEventArgs : EventArgs { }
    public class TaskDialogExpandoButtonClickedEventArgs : EventArgs
    {
        public bool Expanded { get; }

        public TaskDialogExpandoButtonClickedEventArgs(bool expanded)
        {
            Expanded = expanded;
        }
    }

    public sealed class TaskDialog
    {
        private readonly List<TASKDIALOG_BUTTON> buttons = new();
        private readonly List<TASKDIALOG_BUTTON> radioButtons = new();
        private bool buttonsConfigured = false;
        private TASKDIALOGCONFIG config;
        private bool radioButtonsConfigured = false;
        private Func<EventArgs, bool> callback;

        public TaskDialog()
        {
            config.cbSize = (uint)SizeOf<TASKDIALOGCONFIG>();
        }

        private unsafe HRESULT Handler(HWND hwnd, uint msg, WPARAM wParam, LPARAM lParam, nint lpRefData)
        {
            var notification = (TASKDIALOG_NOTIFICATIONS)msg;

            EventArgs args = (TASKDIALOG_NOTIFICATIONS)msg switch
            {
                TDN_CREATED => new TaskDialogCreatedEventArgs(),
                TDN_NAVIGATED => new TaskDialogNavigatedEventArgs(),
                TDN_BUTTON_CLICKED => new TaskDialogButtonClickedEventArgs(wParam.Value < 10 ? (config.dwCommonButtons switch
                {
                    TDCBF_CLOSE_BUTTON => 0,
                    (TDCBF_OK_BUTTON | TDCBF_CANCEL_BUTTON) => (MESSAGEBOX_RESULT)wParam.Value switch
                    {
                        IDOK => 0,
                        IDCANCEL => 1,
                        _ => -1
                    },
                    (TDCBF_RETRY_BUTTON | TDCBF_CANCEL_BUTTON) => (MESSAGEBOX_RESULT)wParam.Value switch
                    {
                        IDRETRY => 0,
                        IDCANCEL => 1,
                        _ => -1,
                    },
                    (TDCBF_YES_BUTTON | TDCBF_NO_BUTTON) => (MESSAGEBOX_RESULT)wParam.Value switch
                    {
                        IDYES => 0,
                        IDNO => 1,
                        _ => -1
                    },
                    (TDCBF_YES_BUTTON | TDCBF_NO_BUTTON | TDCBF_CANCEL_BUTTON) => (MESSAGEBOX_RESULT)wParam.Value switch
                    {
                        IDYES => 0,
                        IDNO => 1,
                        IDCANCEL => 2,
                        _ => -1
                    },
                    _ => -1
                }) : ((int)wParam.Value - 10)),
                TDN_HYPERLINK_CLICKED => new TaskDialogHyperlinkClickedEventArgs(((PWSTR)(char*)lParam.Value).ToString()),
                TDN_TIMER => new TaskDialogTimerEventArgs(TimeSpan.FromMilliseconds(wParam.Value)),
                TDN_DESTROYED => new TaskDialogDestroyedEventArgs(),
                TDN_RADIO_BUTTON_CLICKED => new TaskDialogRadioButtonClickedEventArgs((int)wParam.Value),
                TDN_DIALOG_CONSTRUCTED => new TaskDialogConstructedEventArgs(),
                TDN_VERIFICATION_CLICKED => new TaskDialogVerificationClickedEventArgs(wParam.Value != 0),
                TDN_HELP => new TaskDialogHelpEventArgs(),
                TDN_EXPANDO_BUTTON_CLICKED => new TaskDialogExpandoButtonClickedEventArgs(wParam.Value != 0),
                _ => default
            };
            return callback(args) ? S_FALSE : S_OK;
        }

        public delegate void AddButtonCallback(in string title, bool @default);

        public TaskDialog AllowCancellation()
        {
            config.dwFlags |= TDF_ALLOW_DIALOG_CANCELLATION;
            return this;
        }

        public TaskDialog Callback()
        {
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

        public TaskDialog CommonButtons(TASKDIALOG_COMMON_BUTTON_FLAGS commonButtons, int? optionalDefaultButton = default)
        {
            config.dwCommonButtons = commonButtons;
            if ((commonButtons & (TDCBF_CLOSE_BUTTON | TDCBF_CANCEL_BUTTON)) > 0)
            {
                config.dwFlags |= TDF_ALLOW_DIALOG_CANCELLATION;
            }
            if (optionalDefaultButton is int defaultButton)
            {
                config.nDefaultButton = (int)(commonButtons switch
                {
                    TDCBF_CLOSE_BUTTON => default,
                    (TDCBF_OK_BUTTON | TDCBF_CANCEL_BUTTON) => defaultButton switch
                    {
                        0 => IDOK,
                        1 => IDCANCEL,
                        _ => default
                    },
                    (TDCBF_RETRY_BUTTON | TDCBF_CANCEL_BUTTON) => defaultButton switch
                    {
                        0 => IDRETRY,
                        1 => IDCANCEL,
                        _ => default
                    },
                    (TDCBF_YES_BUTTON | TDCBF_NO_BUTTON) => defaultButton switch
                    {
                        0 => IDYES,
                        1 => IDNO,
                        _ => default
                    },
                    (TDCBF_YES_BUTTON | TDCBF_NO_BUTTON | TDCBF_CANCEL_BUTTON) => defaultButton switch
                    {
                        0 => IDYES,
                        1 => IDNO,
                        2 => IDCANCEL,
                        _ => default
                    },
                    _ => default,
                });
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

            DoButtons(0, radioButtons, configure, config.nDefaultRadioButton, out config.cRadioButtons);
            return this;
        }

        public void Show()
        {
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

        private void DoButtons(Action<AddButtonCallback> configure, in int defaultButton, out uint count) => DoButtons(10, buttons, configure, defaultButton, out count);

        private void DoButtons(int offset, List<TASKDIALOG_BUTTON> target, Action<AddButtonCallback> configure, in int defaultButton, out uint count)
        {
            bool configured = false;
            int? value = default;
            configure((in string title, bool @default) =>
            {
                var id = offset + target.Count;
                target.Add(new() { nButtonID = id, pszButtonText = title });
                if (@default)
                {
                    value = !configured ? id : default;
                    configured = true;
                }
            });
            count = (uint)target.Count;
            if (value is int button)
            {
                AsRef(defaultButton) = button;
            }
        }

        private readonly struct ButtonConfig
        {
            public readonly TASKDIALOG_BUTTON Button;
            public readonly bool Default;

            public ButtonConfig(TASKDIALOG_BUTTON button, bool @default)
            {
                Button = button;
                Default = @default;
            }
        }
    }
}
