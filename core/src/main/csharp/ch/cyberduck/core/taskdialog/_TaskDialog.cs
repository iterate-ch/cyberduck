using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Controls;
using Windows.Win32.UI.WindowsAndMessaging;
using System;
using System.Buffers;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;
using static Windows.Win32.UI.Controls.TASKDIALOG_FLAGS;
using static Windows.Win32.UI.Controls.TASKDIALOG_NOTIFICATIONS;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core.TaskDialog
{
    /// <summary>
    /// The signature of the callback that recieves notificaitons from a Task Dialog.
    /// </summary>
    /// <param name="dialog">The active dialog. Use this to manipulate various properties of the dialog as it is displayed.</param>
    /// <param name="args">The notification arguments including the type of notification and information for the notification.</param>
    /// <param name="callbackData">The value set on TaskDialog.CallbackData</param>
    /// <returns>
    /// <para>Return value meaning varies depending on the Notification member of args.
    /// In all cases, returning <c>false</c> ensures default behavior.
    /// You can override certain behaviors by returning <c>true</c> in a few cases.</para>
    /// <para>For ButtonClicked: <c>true</c> will keep the dialog open, <c>false</c> will cause the dialog to close and return as normal.</para>
    /// <para>For HyperlinkClicked: <c>true</c> will prevent ShellExecute being called on the hyperlink, <c>false</c> will allow ShellExecute to be called as normal.</para>
    /// <para>For Timer: <c>true</c> will reset the timer tick count; otherwise, <c>false</c> will do nothing.</para>
    /// <para>For all other notifications, the return value is ignored.</para>
    /// </returns>
    public delegate bool TaskDialogCallback(TaskDialogNotificationArgs args, object callbackData);

    public interface ITaskDialog { }

    /// <summary>
    /// Defines configuration options for showing a task dialog.
    /// </summary>
    public struct TaskDialogOptions
    {
        /// <summary>
        /// The default <see cref="T:TaskDialogOptions"/> to be used
        /// by all new <see cref="T:TaskDialog"/>s.
        /// </summary>
        /// <remarks>
        /// Use this to make application-wide defaults, such as for
        /// the caption.
        /// </remarks>
        public static TaskDialogOptions Default;

        /// <summary>
        /// Indicates that the dialog should be able to be closed using Alt-F4,
        /// Escape, and the title bar's close button even if no cancel button
        /// is specified the CommonButtons.
        /// </summary>
        /// <remarks>
        /// You'll want to set this to true if you use CustomButtons and have
        /// a Cancel-like button in it.
        /// </remarks>
        public bool AllowDialogCancellation;

        /// <summary>
        /// A callback that receives messages from the Task Dialog when
        /// various events occur.
        /// </summary>
        public TaskDialogCallback Callback;

        /// <summary>
        /// Reference object that is passed to the callback.
        /// </summary>
        public object CallbackData;

        /// <summary>
        /// Command links. These override any custom buttons, but can be used with
        /// radio and common buttons.
        /// </summary>
        public string[] CommandLinks;

        /// <summary>
        /// Standard push buttons.
        /// </summary>
        public TaskDialogCommonButtons CommonButtons;

        /// <summary>
        /// Supplemental text that expands on the principal text.
        /// </summary>
        public string Content;

        /// <summary>
        /// Buttons that are not from the set of standard buttons. Use an
        /// ampersand to denote an access key. These are ignored if CommandLinks
        /// are also defined. These will be appended after any defined common buttons.
        /// </summary>
        public string[] CustomButtons;

        /// <summary>
        /// A small 16x16 icon that signifies the purpose of the footer text,
        /// using a custom Icon resource. If defined <see cref="FooterIcon"/>
        /// will be ignored.
        /// </summary>
        public Icon CustomFooterIcon;

        /// <summary>
        /// A large 32x32 icon that signifies the purpose of the dialog, using
        /// a custom Icon resource. If defined <see cref="MainIcon"/> will be
        /// ignored.
        /// </summary>
        public Icon CustomMainIcon;

        /// <summary>
        /// Zero-based index of the button to have focus by default.
        /// </summary>
        public int? DefaultButtonIndex;

        /// <summary>
        /// Indicates that the task dialog's callback is to be called
        /// approximately every 200 milliseconds.
        /// </summary>
        /// <remarks>
        /// Enable this in order to do updates on the task dialog periodically,
        /// such as for a progress bar, current download speed, or estimated
        /// time to complete, etc.
        /// </remarks>
        public bool EnableCallbackTimer;

        /// <summary>
        /// Indicates that the expanded info should be displayed when the
        /// dialog is initially displayed.
        /// </summary>
        public bool ExpandedByDefault;

        /// <summary>
        /// Extra text that will be hidden by default.
        /// </summary>
        public string ExpandedInfo;

        /// <summary>
        /// Indicates that the expanded info should be displayed at the bottom
        /// of the dialog's footer area instead of immediately after the
        /// dialog's content.
        /// </summary>
        public bool ExpandToFooter;

        /// <summary>
        /// A small 16x16 icon that signifies the purpose of the footer text,
        /// using one of the built-in system icons.
        /// </summary>
        public TaskDialogIcon FooterIcon;

        /// <summary>
        /// Additional footer text.
        /// </summary>
        public string FooterText;

        /// <summary>
        /// A large 32x32 icon that signifies the purpose of the dialog, using
        /// one of the built-in system icons.
        /// </summary>
        public TaskDialogIcon MainIcon;

        /// <summary>
        /// Principal text.
        /// </summary>
        public string MainInstruction;

        /// <summary>
        /// The owner window of the task dialog box.
        /// </summary>
        public IntPtr Owner;

        /// <summary>
        /// Application-defined options for the user.
        /// </summary>
        public string[] RadioButtons;

        /// <summary>
        /// Indicates that an Marquee Progress Bar is to be displayed.
        /// </summary>
        /// <remarks>
        /// You can set start and stop the animation by setting a callback and
        /// timer to control the dialog at custom intervals.
        /// </remarks>
        public bool ShowMarqueeProgressBar;

        /// <summary>
        /// Indicates that a Progress Bar is to be displayed.
        /// </summary>
        /// <remarks>
        /// You can set the state, whether paused, in error, etc., as well as
        /// the range and current value by setting a callback and timer to
        /// control the dialog at custom intervals.
        /// </remarks>
        public bool ShowProgressBar;

        /// <summary>
        /// Caption of the window.
        /// </summary>
        public string Title;

        /// <summary>
        /// Indicates that the verification checkbox in the dialog is checked
        /// when the dialog is initially displayed.
        /// </summary>
        public bool VerificationByDefault;

        /// <summary>
        /// Text accompanied by a checkbox, typically for user feedback such as
        /// Do-not-show-this-dialog-again options.
        /// </summary>
        public string VerificationText;
    }

    /// <summary>
    /// Provides static methods for showing dialog boxes that can be used to display information and receive simple input from the user.
    /// </summary>
    /// <remarks>
    /// To use, call one of the various Show methods. If you are using this in an MVVM pattern, you may want to create
    /// a simple TaskDialogService or something to provide a bit of decoupling from my specific implementation.
    /// </remarks>
    public static partial class TaskDialog
    {
        internal const int CommandButtonIDOffset = 2000;

        internal const int CustomButtonIDOffset = 500;

        internal const int RadioButtonIDOffset = 1000;

        internal static readonly Regex HyperlinkCaptureRegex = new Regex(HtmlHyperlinkCapturePattern, RegexOptions.IgnoreCase);

        internal static readonly Regex HyperlinkRegex = new Regex(HtmlHyperlinkPattern, RegexOptions.IgnoreCase);

        private const string HtmlHyperlinkCapturePattern = "<a href=\"(?<link>[^>]+)\">(?<text>[^<]+)<\\/a>";

        private const string HtmlHyperlinkPattern = "<a href=\"[^>]+\">[^<]+<\\/a>";

        /// <summary>
        /// Occurs when a task dialog has been closed.
        /// </summary>
        public static event TaskDialogClosedEventHandler Closed;

        /// <summary>
        /// Occurs when a task dialog is about to show.
        /// </summary>
        /// <remarks>
        /// Use this event for both notification and modification of all task
        /// dialog showings. Changes made to the configuration options will be
        /// persisted.
        /// </remarks>
        public static event TaskDialogShowingEventHandler Showing;

        /// <summary>
        /// Gets the buttonId for a command button.
        /// </summary>
        /// <param name="index">The zero-based index into the array of command buttons.</param>
        /// <returns>An integer representing the button, used for example with callbacks and the ClickButton method.</returns>
        /// <remarks>
        /// When creating the config options for the dialog and specifying command buttons,
        /// typically you pass in an array of button label strings. The index specifies which
        /// button to get an id for. If you passed in Save, Don't Save, and Cancel, then index 2
        /// specifies the Cancel button.
        /// </remarks>
        public static nuint GetButtonIdForCommandButton(uint index)
        {
            return CommandButtonIDOffset + index;
        }

        public static int GetButtonIdForCommandButton(int index)
        {
            return CommandButtonIDOffset + index;
        }

        /// <summary>
        /// Gets the buttonId for a common button. If the common button set includes more than
        /// one button, the index number specifies which.
        /// </summary>
        /// <param name="commonButtons">The common button set to use.</param>
        /// <param name="index">The zero-based index into the button set.</param>
        /// <returns>An integer representing the button, used for example with callbacks and the ClickButton method.</returns>
        public static int GetButtonIdForCommonButton(TaskDialogCommonButtons commonButtons, int index)
        {
            int buttonId = 0;

            switch (commonButtons)
            {
                default:
                case TaskDialogCommonButtons.None:
                case TaskDialogCommonButtons.Close:
                    // We'll set to 0 even for Close, as it doesn't matter that we
                    //get the value right since there is only one button anyway
                    buttonId = 0;
                    break;

                case TaskDialogCommonButtons.OKCancel:
                    if (index == 0)
                        buttonId = (int)TaskDialogCommonButtons.OK;
                    else if (index == 1)
                        buttonId = (int)TaskDialogCommonButtons.Cancel;
                    else
                        buttonId = 0;
                    break;

                case TaskDialogCommonButtons.RetryCancel:
                    if (index == 0)
                        buttonId = (int)TaskDialogCommonButtons.Retry;
                    else if (index == 1)
                        buttonId = (int)TaskDialogCommonButtons.Cancel;
                    else
                        buttonId = 0;
                    break;

                case TaskDialogCommonButtons.YesNo:
                    if (index == 0)
                        buttonId = (int)TaskDialogCommonButtons.Yes;
                    else if (index == 1)
                        buttonId = (int)TaskDialogCommonButtons.No;
                    else
                        buttonId = 0;
                    break;

                case TaskDialogCommonButtons.YesNoCancel:
                    if (index == 0)
                        buttonId = (int)TaskDialogCommonButtons.Yes;
                    else if (index == 1)
                        buttonId = (int)TaskDialogCommonButtons.No;
                    else if (index == 2)
                        buttonId = (int)TaskDialogCommonButtons.Cancel;
                    else
                        buttonId = 0;
                    break;
            }

            return buttonId;
        }

        /// <summary>
        /// Gets the buttonId for a custom button.
        /// </summary>
        /// <param name="index">The zero-based index into the array of custom buttons.</param>
        /// <returns>An integer representing the button, used for example with callbacks and the ClickButton method.</returns>
        /// <remarks>
        /// When creating the config options for the dialog and specifying custom buttons,
        /// typically you pass in an array of button label strings. The index specifies which
        /// button to get an id for. If you passed in Save, Don't Save, and Cancel, then index 2
        /// specifies the Cancel custom button.
        /// </remarks>
        public static nuint GetButtonIdForCustomButton(uint index)
        {
            return CustomButtonIDOffset + index;
        }

        public static int GetButtonIdForCustomButton(int index)
        {
            return CustomButtonIDOffset + index;
        }

        /// <summary>
        /// Gets the buttonId for a radio button.
        /// </summary>
        /// <param name="index">The zero-based index into the array of radio buttons.</param>
        /// <returns>An integer representing the button, used for example with callbacks and the ClickButton method.</returns>
        /// <remarks>
        /// When creating the config options for the dialog and specifying radio buttons,
        /// typically you pass in an array of radio label strings. The index specifies which
        /// button to get an id for. If you passed in Automatic, Manual, and Disabled, then index 1
        /// specifies the Manual radio button.
        /// </remarks>
        public static nuint GetButtonIdForRadioButton(uint index)
        {
            return RadioButtonIDOffset + index;
        }

        public static int GetButtonIdForRadioButton(int index)
        {
            return RadioButtonIDOffset + index;
        }

        /// <summary>
        /// Gets the zero-based index for a common button.
        /// </summary>
        /// <param name="commonButtons">The common button set to use.</param>
        /// <param name="buttonId">The button's id.</param>
        /// <returns>An integer representing the button index, or -1 if not found.</returns>
        /// <remarks>
        /// When Alt+F4, Esc, and other non-button close commands are issued, the dialog
        /// will simulate a Cancel button click. In this case, -1 for index and a buttonid
        /// of Cancel will let you know how the user closed the dialog.
        /// </remarks>
        public static int GetButtonIndexForCommonButton(TaskDialogCommonButtons commonButtons, int buttonId)
        {
            int index = -1;

            switch (commonButtons)
            {
                default:
                case TaskDialogCommonButtons.None:
                    index = -1;
                    break;

                case TaskDialogCommonButtons.Close:
                    index = 0;
                    break;

                case TaskDialogCommonButtons.OKCancel:
                    if (buttonId == (int)TaskDialogSimpleResult.Ok
                        || buttonId == (int)TaskDialogCommonButtons.OK)
                        index = 0;
                    else if (buttonId == (int)TaskDialogSimpleResult.Cancel
                        || buttonId == (int)TaskDialogCommonButtons.Cancel)
                        index = 1;
                    break;

                case TaskDialogCommonButtons.RetryCancel:
                    if (buttonId == (int)TaskDialogSimpleResult.Retry
                        || buttonId == (int)TaskDialogCommonButtons.Retry)
                        index = 0;
                    else if (buttonId == (int)TaskDialogSimpleResult.Cancel
                        || buttonId == (int)TaskDialogCommonButtons.Cancel)
                        index = 1;
                    break;

                case TaskDialogCommonButtons.YesNo:
                    if (buttonId == (int)TaskDialogSimpleResult.Yes
                        || buttonId == (int)TaskDialogCommonButtons.Yes)
                        index = 0;
                    else if (buttonId == (int)TaskDialogSimpleResult.No
                        || buttonId == (int)TaskDialogCommonButtons.No)
                        index = 1;
                    break;

                case TaskDialogCommonButtons.YesNoCancel:
                    if (buttonId == (int)TaskDialogSimpleResult.Yes
                        || buttonId == (int)TaskDialogCommonButtons.Yes)
                        index = 0;
                    else if (buttonId == (int)TaskDialogSimpleResult.No
                        || buttonId == (int)TaskDialogCommonButtons.No)
                        index = 1;
                    else if (buttonId == (int)TaskDialogSimpleResult.Cancel
                        || buttonId == (int)TaskDialogCommonButtons.Cancel)
                        index = 2;
                    break;
            }

            return index;
        }

        /// <summary>
        /// Displays a task dialog with the given configuration options.
        /// </summary>
        /// <param name="allowDialogCancellation">Indicates that the dialog should be able to be closed using Alt-F4,
        /// Escape, and the title bar's close button even if no cancel button
        /// is specified the CommonButtons.</param>
        /// <param name="callback">A callback that receives messages from the Task Dialog when
        /// various events occur.</param>
        /// <param name="callbackData">Reference object that is passed to the callback.</param>
        /// <param name="commandLinks">Command links.</param>
        /// <param name="commonButtons">Standard push buttons.</param>
        /// <param name="content">Supplemental text that expands on the principal text.</param>
        /// <param name="customButtons">Buttons that are not from the set of standard buttons. Use an
        /// ampersand to denote an access key.</param>
        /// <param name="customFooterIcon">A small 16x16 icon that signifies the purpose of the footer text,
        /// using a custom Icon resource. If defined <paramref name="footerIcon"/>
        /// will be ignored.</param>
        /// <param name="customMainIcon">A large 32x32 icon that signifies the purpose of the dialog, using
        /// a custom Icon resource. If defined <paramref name="mainIcon"/> will be
        /// ignored.</param>
        /// <param name="defaultButtonIndex">Zero-based index of the button to have focus by default.</param>
        /// <param name="enableCallbackTimer">Indicates that the task dialog's callback is to be called
        /// approximately every 200 milliseconds.</param>
        /// <param name="expandedByDefault">Indicates that the expanded info should be displayed when the
        /// dialog is initially displayed.</param>
        /// <param name="expandedInfo">Extra text that will be hidden by default.</param>
        /// <param name="expandToFooter">Indicates that the expanded info should be displayed at the bottom
        /// of the dialog's footer area instead of immediately after the
        /// dialog's content.</param>
        /// <param name="footerIcon">A small 16x16 icon that signifies the purpose of the footer text,
        /// using one of the built-in system icons.</param>
        /// <param name="footerText">Additional footer text.</param>
        /// <param name="mainIcon">A large 32x32 icon that signifies the purpose of the dialog, using
        /// one of the built-in system icons.</param>
        /// <param name="mainInstruction">Principal text.</param>
        /// <param name="owner">The owner window of the task dialog box.</param>
        /// <param name="radioButtons">Application-defined options for the user.</param>
        /// <param name="showMarqueeProgressBar">Indicates that an Marquee Progress Bar is to be displayed.</param>
        /// <param name="showProgressBar">Indicates that a Progress Bar is to be displayed.</param>
        /// <param name="title">Caption of the window.</param>
        /// <param name="verificationByDefault">Indicates that the verification checkbox in the dialog is checked
        /// when the dialog is initially displayed.</param>
        /// <param name="verificationText">Text accompanied by a checkbox, typically for user feedback such as
        /// Do-not-show-this-dialog-again options.</param>
        /// <returns>
        /// A <see cref="T:TaskDialogInterop.TaskDialogResult"/> value that specifies
        /// which button is clicked by the user.
        /// </returns>
        /// <remarks>
        /// Use of this method will ignore any TaskDialogOptions.Default settings.
        /// If you want to make use of defaults, create your own TaskDialogOptions starting with TaskDialogOptions.Default
        /// and pass it into the Show method.
        /// </remarks>
        public static TaskDialogResult Show(
            bool allowDialogCancellation = false,
            TaskDialogCallback callback = null,
            object callbackData = null,
            string[] commandLinks = null,
            TaskDialogCommonButtons commonButtons = TaskDialogCommonButtons.None,
            string content = null,
            string[] customButtons = null,
            System.Drawing.Icon customFooterIcon = null,
            System.Drawing.Icon customMainIcon = null,
            int? defaultButtonIndex = null,
            bool enableCallbackTimer = false,
            bool expandedByDefault = false,
            string expandedInfo = null,
            bool expandToFooter = false,
            TaskDialogIcon footerIcon = TaskDialogIcon.None,
            string footerText = null,
            TaskDialogIcon mainIcon = TaskDialogIcon.None,
            string mainInstruction = null,
            IntPtr owner = default(IntPtr),
            string[] radioButtons = null,
            bool showMarqueeProgressBar = false,
            bool showProgressBar = false,
            string title = null,
            bool verificationByDefault = false,
            string verificationText = null)
        {
            TaskDialogOptions options = new TaskDialogOptions()
            {
                AllowDialogCancellation = allowDialogCancellation,
                Callback = callback,
                CallbackData = callbackData,
                CommandLinks = commandLinks,
                CommonButtons = commonButtons,
                Content = content,
                CustomButtons = customButtons,
                CustomFooterIcon = customFooterIcon,
                CustomMainIcon = customMainIcon,
                DefaultButtonIndex = defaultButtonIndex,
                EnableCallbackTimer = enableCallbackTimer,
                ExpandedByDefault = expandedByDefault,
                ExpandedInfo = expandedInfo,
                ExpandToFooter = expandToFooter,
                FooterIcon = footerIcon,
                FooterText = footerText,
                MainIcon = mainIcon,
                MainInstruction = mainInstruction,
                Owner = owner,
                RadioButtons = radioButtons,
                ShowMarqueeProgressBar = showMarqueeProgressBar,
                ShowProgressBar = showProgressBar,
                Title = title,
                VerificationByDefault = verificationByDefault,
                VerificationText = verificationText
            };

            return TaskDialog.Show(options);
        }

        /// <summary>
        /// Displays a task dialog with the given configuration options.
        /// </summary>
        /// <param name="options">
        /// A <see cref="T:TaskDialogInterop.TaskDialogOptions"/> that specifies the
        /// configuration options for the dialog.
        /// </param>
        /// <returns>
        /// A <see cref="T:TaskDialogInterop.TaskDialogResult"/> value that specifies
        /// which button is clicked by the user.
        /// </returns>
        public static TaskDialogResult Show(TaskDialogOptions options)
        {
            TaskDialogResult result = TaskDialogResult.Empty;

            // Make a copy since we'll let Showing event possibly modify them
            TaskDialogOptions configOptions = options;

            try
            {
                OnShowing(new TaskDialogShowingEventArgs(ref configOptions));
                result = ShowTaskDialog(configOptions);
            }
            catch (EntryPointNotFoundException)
            {
                // HACK: Not available? Just crash. Need investigation.
                // HACK: Removed every Emulated-Task-Dialog Thingy.
                throw;
                //// This can happen on some machines, usually when running Vista/7 x64
                //// When it does, we'll work around the issue by forcing emulated mode
                //// http://www.codeproject.com/Messages/3257715/How-to-get-it-to-work-on-Windows-7-64-bit.aspx
                //ForceEmulationMode = true;
                //result = ShowEmulatedTaskDialog(configOptions);
            }
            finally
            {
                OnClosed(new TaskDialogClosedEventArgs(result));
            }

            return result;
        }

        /// <summary>
        /// Displays a task dialog that has a message and that returns a result.
        /// </summary>
        /// <param name="owner">
        /// The <see cref="T:System.Windows.Window"/> that owns this dialog.
        /// </param>
        /// <param name="messageText">
        /// A <see cref="T:System.String"/> that specifies the text to display.
        /// </param>
        /// <returns>
        /// A <see cref="T:TaskDialogInterop.TaskDialogSimpleResult"/> value that
        /// specifies which button is clicked by the user.
        /// </returns>
        public static TaskDialogSimpleResult ShowMessage(IntPtr owner, string messageText)
        {
            TaskDialogOptions options = TaskDialogOptions.Default;

            options.Owner = owner;
            options.Content = messageText;
            options.CommonButtons = TaskDialogCommonButtons.Close;

            return Show(options).Result;
        }

        /// <summary>
        /// Displays a task dialog that has a message and that returns a result.
        /// </summary>
        /// <param name="owner">
        /// The <see cref="T:System.Windows.Window"/> that owns this dialog.
        /// </param>
        /// <param name="messageText">
        /// A <see cref="T:System.String"/> that specifies the text to display.
        /// </param>
        /// <param name="caption">
        /// A <see cref="T:System.String"/> that specifies the title bar
        /// caption to display.
        /// </param>
        /// <returns>
        /// A <see cref="T:TaskDialogInterop.TaskDialogSimpleResult"/> value that
        /// specifies which button is clicked by the user.
        /// </returns>
        public static TaskDialogSimpleResult ShowMessage(IntPtr owner, string messageText, string caption)
        {
            return ShowMessage(owner, messageText, caption, TaskDialogCommonButtons.Close);
        }

        /// <summary>
        /// Displays a task dialog that has a message and that returns a result.
        /// </summary>
        /// <param name="owner">
        /// The <see cref="T:System.Windows.Window"/> that owns this dialog.
        /// </param>
        /// <param name="messageText">
        /// A <see cref="T:System.String"/> that specifies the text to display.
        /// </param>
        /// <param name="caption">
        /// A <see cref="T:System.String"/> that specifies the title bar
        /// caption to display.
        /// </param>
        /// <param name="buttons">
        /// A <see cref="T:TaskDialogInterop.TaskDialogCommonButtons"/> value that
        /// specifies which button or buttons to display.
        /// </param>
        /// <returns>
        /// A <see cref="T:TaskDialogInterop.TaskDialogSimpleResult"/> value that
        /// specifies which button is clicked by the user.
        /// </returns>
        public static TaskDialogSimpleResult ShowMessage(IntPtr owner, string messageText, string caption, TaskDialogCommonButtons buttons)
        {
            return ShowMessage(owner, messageText, caption, buttons, TaskDialogIcon.None);
        }

        /// <summary>
        /// Displays a task dialog that has a message and that returns a result.
        /// </summary>
        /// <param name="owner">
        /// The <see cref="T:System.Windows.Window"/> that owns this dialog.
        /// </param>
        /// <param name="messageText">
        /// A <see cref="T:System.String"/> that specifies the text to display.
        /// </param>
        /// <param name="caption">
        /// A <see cref="T:System.String"/> that specifies the title bar
        /// caption to display.
        /// </param>
        /// <param name="buttons">
        /// A <see cref="T:TaskDialogInterop.TaskDialogCommonButtons"/> value that
        /// specifies which button or buttons to display.
        /// </param>
        /// <param name="icon">
        /// A <see cref="T:TaskDialogInterop.VistaTaskDialogIcon"/> that specifies the
        /// icon to display.
        /// </param>
        /// <returns>
        /// A <see cref="T:TaskDialogInterop.TaskDialogSimpleResult"/> value that
        /// specifies which button is clicked by the user.
        /// </returns>
        public static TaskDialogSimpleResult ShowMessage(IntPtr owner, string messageText, string caption, TaskDialogCommonButtons buttons, TaskDialogIcon icon)
        {
            TaskDialogOptions options = TaskDialogOptions.Default;

            options.Owner = owner;
            options.Title = caption;
            options.Content = messageText;
            options.CommonButtons = buttons;
            options.MainIcon = icon;

            return Show(options).Result;
        }

        /// <summary>
        /// Displays a task dialog that has a message and that returns a result.
        /// </summary>
        /// <param name="owner">
        /// The <see cref="T:System.Windows.Window"/> that owns this dialog.
        /// </param>
        /// <param name="title">
        /// A <see cref="T:System.String"/> that specifies the title bar
        /// caption to display.
        /// </param>
        /// <param name="mainInstruction">
        /// A <see cref="T:System.String"/> that specifies the main text to display.
        /// </param>
        /// <param name="content">
        /// A <see cref="T:System.String"/> that specifies the body text to display.
        /// </param>
        /// <param name="expandedInfo">
        /// A <see cref="T:System.String"/> that specifies the expanded text to display when toggled.
        /// </param>
        /// <param name="verificationText">
        /// A <see cref="T:System.String"/> that specifies the text to display next to a checkbox.
        /// </param>
        /// <param name="footerText">
        /// A <see cref="T:System.String"/> that specifies the footer text to display.
        /// </param>
        /// <param name="buttons">
        /// A <see cref="T:TaskDialogInterop.TaskDialogCommonButtons"/> value that
        /// specifies which button or buttons to display.
        /// </param>
        /// <param name="mainIcon">
        /// A <see cref="T:TaskDialogInterop.VistaTaskDialogIcon"/> that specifies the
        /// main icon to display.
        /// </param>
        /// <param name="footerIcon">
        /// A <see cref="T:TaskDialogInterop.VistaTaskDialogIcon"/> that specifies the
        /// footer icon to display.
        /// </param>
        /// <returns></returns>
        public static TaskDialogSimpleResult ShowMessage(IntPtr owner, string title, string mainInstruction, string content, string expandedInfo, string verificationText, string footerText, TaskDialogCommonButtons buttons, TaskDialogIcon mainIcon, TaskDialogIcon footerIcon)
        {
            TaskDialogOptions options = TaskDialogOptions.Default;

            if (owner != IntPtr.Zero)
                options.Owner = owner;
            if (!String.IsNullOrEmpty(title))
                options.Title = title;
            if (!String.IsNullOrEmpty(mainInstruction))
                options.MainInstruction = mainInstruction;
            if (!String.IsNullOrEmpty(content))
                options.Content = content;
            if (!String.IsNullOrEmpty(expandedInfo))
                options.ExpandedInfo = expandedInfo;
            if (!String.IsNullOrEmpty(verificationText))
                options.VerificationText = verificationText;
            if (!String.IsNullOrEmpty(footerText))
                options.FooterText = footerText;
            options.CommonButtons = buttons;
            options.MainIcon = mainIcon;
            options.FooterIcon = footerIcon;

            return Show(options).Result;
        }

        private static bool DetectHyperlinks(string content, string expandedInfo, string footerText)
        {
            return DetectHyperlinks(content) || DetectHyperlinks(expandedInfo) || DetectHyperlinks(footerText);
        }

        private static bool DetectHyperlinks(string text)
        {
            if (String.IsNullOrEmpty(text))
                return false;
            return HyperlinkRegex.IsMatch(text);
        }

        private unsafe static HRESULT Handler(HWND hwnd, uint msg, WPARAM wParam, LPARAM lParam, nint lpRefData)
        {
            var notification = (TASKDIALOG_NOTIFICATIONS)msg;
            ref var contextData = ref Unsafe.AsRef<CallbackContext>((void*)lpRefData);
            TaskDialogNotificationArgs args = new()
            {
                TaskDialog = new TaskDialogData() { HWND = hwnd },
                Notification = notification
            };
            switch (notification)
            {
                case TDN_BUTTON_CLICKED:
                case TDN_RADIO_BUTTON_CLICKED:
                    if (wParam.Value > 100)
                        args.ButtonIndex = (int)wParam.Value % CustomButtonIDOffset;
                    else
                        args.ButtonIndex = GetButtonIndexForCommonButton(contextData.CommonButtons, (int)wParam.Value);
                    break;

                case TDN_HYPERLINK_CLICKED:
                    // TODO
                    break;

                case TDN_TIMER:
                    args.TimerTickCount = (uint)wParam.Value;
                    break;

                case TDN_VERIFICATION_CLICKED:
                    args.VerificationFlagChecked = wParam.Value != 0;
                    break;

                case TDN_EXPANDO_BUTTON_CLICKED:
                    args.Expanded = wParam.Value != 0;
                    break;
            }
            return (HRESULT)(contextData.Callback(args, null) ? 1 : 0);
        }

        /// <summary>
        /// Raises the <see cref="E:Closed"/> event.
        /// </summary>
        /// <param name="e">The <see cref="TaskDialogInterop.TaskDialogClosedEventArgs"/> instance containing the event data.</param>
        private static void OnClosed(TaskDialogClosedEventArgs e) => Closed?.Invoke(null, e);

        /// <summary>
        /// Raises the <see cref="E:Showing"/> event.
        /// </summary>
        /// <param name="e">The <see cref="TaskDialogInterop.TaskDialogShowingEventArgs"/> instance containing the event data.</param>
        private static void OnShowing(TaskDialogShowingEventArgs e) => Showing?.Invoke(null, e);

        private unsafe static TaskDialogResult ShowTaskDialog(TaskDialogOptions options)
        {
            static void SetIfNotEmpty(in PCWSTR field, string value)
            {
                if (!string.IsNullOrWhiteSpace(value))
                {
                    Unsafe.AsRef(field) = value;
                }
            }

            static IDisposable CreateRadioButtons(
                string[] radioButtons,
                in int nDefaultRadioButton,
                out TASKDIALOG_BUTTON* pRadioButtons, out uint cRadioButtons)
            {
                if (!(radioButtons?.Length > 0))
                {
                    pRadioButtons = default;
                    cRadioButtons = default;
                    return default;
                }

                var count = radioButtons.Length;
                cRadioButtons = (uint)count;
                var pool = MemoryPool<TASKDIALOG_BUTTON>.Shared.Rent(count);
                var data = pool.Memory.Span;
                for (int i = 0; i < count; i++)
                {
                    ref var button = ref data[i];
                    button.nButtonID = GetButtonIdForRadioButton(i);
                    button.pszButtonText = radioButtons[i];
                }
                pRadioButtons = (TASKDIALOG_BUTTON*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(pool.Memory.Span));

                return pool;
            }
            static IDisposable CreateButtons(
                string[] commandLinks,
                string[] customButtons,
                int? defaultButtonIndex,
                in int defaultButton,
                in TASKDIALOG_COMMON_BUTTON_FLAGS commonButtons,
                out TASKDIALOG_BUTTON* pButtons, out uint cButtons)
            {
                string[] strings;
                Func<int, int> idSelector;
                if (commandLinks?.Length > 0)
                {
                    strings = commandLinks;
                    idSelector = GetButtonIdForCommandButton;
                }
                else if (customButtons?.Length > 0)
                {
                    strings = customButtons;
                    idSelector = GetButtonIdForCustomButton;
                    Unsafe.AsRef(commonButtons) = 0;
                }
                else
                {
                    pButtons = default;
                    cButtons = default;
                    return default;
                }
                var count = strings.Length;
                cButtons = (uint)count;
                var pool = MemoryPool<TASKDIALOG_BUTTON>.Shared.Rent(count);
                var data = pool.Memory.Span;
                for (int i = 0; i < count; i++)
                {
                    ref var button = ref data[i];
                    button.nButtonID = idSelector(i);
                    button.pszButtonText = strings[i];
                }
                pButtons = (TASKDIALOG_BUTTON*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(pool.Memory.Span));

                if (defaultButtonIndex is int localIndex && localIndex >= 0 && localIndex < count)
                {
                    Unsafe.AsRef(defaultButton) = idSelector(localIndex);
                }

                return pool;
            }

            TASKDIALOGCONFIG tdc = new() { cbSize = (uint)Marshal.SizeOf<TASKDIALOGCONFIG>() };
            SetIfNotEmpty(tdc.pszWindowTitle, options.Title);
            SetIfNotEmpty(tdc.pszMainInstruction, options.MainInstruction);
            SetIfNotEmpty(tdc.pszContent, options.Content);
            SetIfNotEmpty(tdc.pszExpandedInformation, options.ExpandedInfo);
            SetIfNotEmpty(tdc.pszFooter, options.FooterText);
            SetIfNotEmpty(tdc.pszVerificationText, options.VerificationText);
            SetIfNotEmpty(tdc.pszExpandedControlText, "Hide details");
            SetIfNotEmpty(tdc.pszCollapsedControlText, "Show details");

            TASKDIALOG_FLAGS flags = default;
            using var buttonMemory = CreateButtons(
                options.CommandLinks,
                options.CustomButtons,
                options.DefaultButtonIndex,
                tdc.nDefaultButton,
                tdc.dwCommonButtons,
                out tdc.pButtons, out tdc.cButtons);

            using var radioButtons = CreateRadioButtons(
                options.RadioButtons,
                tdc.nDefaultRadioButton,
                out tdc.pRadioButtons, out tdc.cRadioButtons);

            if (options.CommonButtons != TaskDialogCommonButtons.None)
            {
                tdc.dwCommonButtons = (TASKDIALOG_COMMON_BUTTON_FLAGS)options.CommonButtons;
                if (options.DefaultButtonIndex is int localButton && localButton >= 0)
                {
                    tdc.nDefaultButton = GetButtonIdForCommonButton(options.CommonButtons, localButton);
                }
            }

            tdc.Anonymous1.pszMainIcon = (char*)(int)options.MainIcon;
            if (options.CustomMainIcon != null)
            {
                tdc.Anonymous1.hMainIcon = (HICON)options.CustomMainIcon.Handle;
                flags |= TDF_USE_HICON_MAIN;
            }
            tdc.Anonymous2.pszFooterIcon = (char*)(int)options.FooterIcon;
            if (options.CustomFooterIcon != null)
            {
                tdc.Anonymous2.hFooterIcon = (HICON)options.CustomMainIcon.Handle;
                flags |= TDF_USE_HICON_FOOTER;
            }
            if (DetectHyperlinks(options.Content, options.ExpandedInfo, options.FooterText))
            {
                flags |= TDF_ENABLE_HYPERLINKS;
            }
            if (options.AllowDialogCancellation || (options.CommonButtons & (TaskDialogCommonButtons.Close | TaskDialogCommonButtons.Cancel)) > 0)
            {
                flags |= TDF_ALLOW_DIALOG_CANCELLATION;
            }
            if (options.EnableCallbackTimer)
            {
                flags |= TDF_CALLBACK_TIMER;
            }
            if (options.ExpandedByDefault)
            {
                flags |= TDF_EXPANDED_BY_DEFAULT;
            }
            if (options.ExpandToFooter)
            {
                flags |= TDF_EXPAND_FOOTER_AREA;
            }
            flags |= TDF_POSITION_RELATIVE_TO_WINDOW;

            if (options.ShowProgressBar)
            {
                flags |= TDF_SHOW_PROGRESS_BAR;
            }
            if (options.ShowMarqueeProgressBar)
            {
                flags |= TDF_SHOW_MARQUEE_PROGRESS_BAR;
            }
            if (options.VerificationByDefault)
            {
                flags |= TDF_VERIFICATION_FLAG_CHECKED;
            }
            tdc.dwFlags = flags;

            PFTASKDIALOGCALLBACK callback;
            CallbackContext context;
            if (options.Callback != null)
            {
                callback = Handler;
                context = new CallbackContext()
                {
                    CommonButtons = options.CommonButtons,
                    Callback = options.Callback
                };
                tdc.pfCallback = callback;
                tdc.lpCallbackData = (nint)Unsafe.AsPointer(ref context);
            }

            int pnButton;
            int pnRadioButton;
            BOOL pfVerificationFlagChecked;
            TaskDialogIndirect(tdc, &pnButton, &pnRadioButton, &pfVerificationFlagChecked).ThrowOnFailure();

            TaskDialogSimpleResult simpleResult = (TaskDialogSimpleResult)pnButton;
            int? radioButtonResult = default;
            int? commandButtonResult = default;
            int? customButtonResult = default;

            if (pnRadioButton >= RadioButtonIDOffset)
            {
                radioButtonResult = pnRadioButton - RadioButtonIDOffset;
            }

            if (pnButton >= CommandButtonIDOffset)
            {
                simpleResult = TaskDialogSimpleResult.Command;
                commandButtonResult = pnButton - CommandButtonIDOffset;
            }
            else if (pnButton >= CustomButtonIDOffset)
            {
                simpleResult = TaskDialogSimpleResult.Custom;
                customButtonResult = pnButton - CustomButtonIDOffset;
            }

            return new TaskDialogResult(simpleResult, pfVerificationFlagChecked, radioButtonResult, commandButtonResult, customButtonResult);
        }

        private struct CallbackContext
        {
            public TaskDialogCallback Callback;
            public TaskDialogCommonButtons CommonButtons;
        }

        private class TaskDialogData : ITaskDialog
        {
            public HWND HWND { get; set; }
        }
    }

    public class TaskDialogNotificationArgs
    {
        /// <summary>
        /// Gets the button ID if the notification is about a button. This a DialogResult
        /// value or the ButtonID member of a TaskDialogButton set in the
        /// TaskDialog.Buttons member.
        /// </summary>
        public int ButtonId { get; internal set; }

        /// <summary>
        /// Gets the button index if the notification is about a button.
        /// </summary>
        public int ButtonIndex { get; internal set; }

        /// <summary>
        /// Gets the configuration options for the dialog.
        /// </summary>
        /// <remarks>
        /// Changes to any of the options will be ignored.
        /// </remarks>
        public TaskDialogOptions Config { get; internal set; }

        /// <summary>
        /// Gets the state of the dialog expando when the notification is about the expando.
        /// </summary>
        public bool Expanded { get; internal set; }

        /// <summary>
        /// Gets the HREF string of the hyperlink the notification is about.
        /// </summary>
        public string Hyperlink { get; internal set; }

        /// <summary>
        /// Gets what the TaskDialog callback is a notification of.
        /// </summary>
        public TASKDIALOG_NOTIFICATIONS Notification { get; internal set; }

        public ITaskDialog TaskDialog { get; internal set; }

        /// <summary>
        /// Gets the number of milliseconds since the dialog was opened or the last time the
        /// callback for a timer notification reset the value by returning true.
        /// </summary>
        public uint TimerTickCount { get; internal set; }

        /// <summary>
        /// Gets the state of the verification flag when the notification is about the verification flag.
        /// </summary>
        public bool VerificationFlagChecked { get; internal set; }
    }
}
