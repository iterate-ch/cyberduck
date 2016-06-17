using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// Provides static methods for showing dialog boxes that can be used to display information and receive simple input from the user.
	/// </summary>
	/// <remarks>
	/// To use, call one of the various Show methods. If you are using this in an MVVM pattern, you may want to create
	/// a simple TaskDialogService or something to provide a bit of decoupling from my specific implementation.
	/// </remarks>
	public static class TaskDialog
	{
		private const string HtmlHyperlinkPattern = "<a href=\"[^>]+\">[^<]+<\\/a>";
		private const string HtmlHyperlinkCapturePattern = "<a href=\"(?<link>[^>]+)\">(?<text>[^<]+)<\\/a>";

		internal static readonly Regex HyperlinkRegex = new Regex(HtmlHyperlinkPattern, RegexOptions.IgnoreCase);
		internal static readonly Regex HyperlinkCaptureRegex = new Regex(HtmlHyperlinkCapturePattern, RegexOptions.IgnoreCase);

		internal const int CommandButtonIDOffset = 2000;
		internal const int RadioButtonIDOffset = 1000;
		internal const int CustomButtonIDOffset = 500;
		
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
		/// Occurs when a task dialog has been closed.
		/// </summary>
		public static event TaskDialogClosedEventHandler Closed;

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
			TaskDialogResult result;

			// Make a copy since we'll let Showing event possibly modify them
			TaskDialogOptions configOptions = options;

			OnShowing(new TaskDialogShowingEventArgs(ref configOptions));

			if (NativeTaskDialog.IsAvailableOnThisOS)
			{
				try
				{
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
			}
			else
			{
				throw new Exception("Old System");
			}

			OnClosed(new TaskDialogClosedEventArgs(result));

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
		public static int GetButtonIdForCommandButton(int index)
		{
			return CommandButtonIDOffset + index;
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
		public static int GetButtonIdForRadioButton(int index)
		{
			return RadioButtonIDOffset + index;
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
		public static int GetButtonIdForCustomButton(int index)
		{
			return CustomButtonIDOffset + index;
		}

		/// <summary>
		/// Raises the <see cref="E:Showing"/> event.
		/// </summary>
		/// <param name="e">The <see cref="TaskDialogInterop.TaskDialogShowingEventArgs"/> instance containing the event data.</param>
		private static void OnShowing(TaskDialogShowingEventArgs e)
		{
			if (Showing != null)
			{
				Showing(null, e);
			}
		}
		/// <summary>
		/// Raises the <see cref="E:Closed"/> event.
		/// </summary>
		/// <param name="e">The <see cref="TaskDialogInterop.TaskDialogClosedEventArgs"/> instance containing the event data.</param>
		private static void OnClosed(TaskDialogClosedEventArgs e)
		{
			if (Closed != null)
			{
				Closed(null, e);
			}
		}
		private static TaskDialogResult ShowTaskDialog(TaskDialogOptions options)
		{
			var td = new NativeTaskDialog();

			td.WindowTitle = options.Title;
			td.MainInstruction = options.MainInstruction;
			td.Content = options.Content;
			td.ExpandedInformation = options.ExpandedInfo;
			td.Footer = options.FooterText;

			bool hasCustomCancel = false;

			// Use of Command Links overrides any custom defined buttons
			if (options.CommandLinks != null && options.CommandLinks.Length > 0)
			{
				List<TaskDialogButton> lst = new List<TaskDialogButton>();
				for (int i = 0; i < options.CommandLinks.Length; i++)
				{
					try
					{
						TaskDialogButton button = new TaskDialogButton();
						button.ButtonId = GetButtonIdForCommandButton(i);
						button.ButtonText = options.CommandLinks[i];
						lst.Add(button);
					}
					catch (FormatException)
					{
					}
				}
				td.Buttons = lst.ToArray();
				if (options.DefaultButtonIndex.HasValue
					&& options.DefaultButtonIndex >= 0
					&& options.DefaultButtonIndex.Value < td.Buttons.Length)
					td.DefaultButton = td.Buttons[options.DefaultButtonIndex.Value].ButtonId;
			}
			else if (options.CustomButtons != null && options.CustomButtons.Length > 0)
			{
				List<TaskDialogButton> lst = new List<TaskDialogButton>();
				for (int i = 0; i < options.CustomButtons.Length; i++)
				{
					try
					{
						TaskDialogButton button = new TaskDialogButton();
						button.ButtonId = GetButtonIdForCustomButton(i);
						button.ButtonText = options.CustomButtons[i];

						if (!hasCustomCancel)
						{
							hasCustomCancel =
								(button.ButtonText == TaskDialogOptions.LocalizedStrings.CommonButton_Close
								|| button.ButtonText == TaskDialogOptions.LocalizedStrings.CommonButton_Cancel);
						}

						lst.Add(button);
					}
					catch (FormatException)
					{
					}
				}

				td.Buttons = lst.ToArray();
				if (options.DefaultButtonIndex.HasValue
					&& options.DefaultButtonIndex.Value >= 0
					&& options.DefaultButtonIndex.Value < td.Buttons.Length)
					td.DefaultButton = td.Buttons[options.DefaultButtonIndex.Value].ButtonId;
				td.CommonButtons = TaskDialogCommonButtons.None;
			}
			
			if (options.RadioButtons != null && options.RadioButtons.Length > 0)
			{
				List<TaskDialogButton> lst = new List<TaskDialogButton>();
				for (int i = 0; i < options.RadioButtons.Length; i++)
				{
					try
					{
						TaskDialogButton button = new TaskDialogButton();
						button.ButtonId = GetButtonIdForRadioButton(i);
						button.ButtonText = options.RadioButtons[i];
						lst.Add(button);
					}
					catch (FormatException)
					{
					}
				}
				td.RadioButtons = lst.ToArray();
				td.NoDefaultRadioButton = (!options.DefaultButtonIndex.HasValue || options.DefaultButtonIndex.Value == -1);
				if (options.DefaultButtonIndex.HasValue
					&& options.DefaultButtonIndex >= 0
					&& options.DefaultButtonIndex.Value < td.RadioButtons.Length)
					td.DefaultButton = td.RadioButtons[options.DefaultButtonIndex.Value].ButtonId;
			}

			if (options.CommonButtons != TaskDialogCommonButtons.None)
			{
				td.CommonButtons = options.CommonButtons;

				if (options.DefaultButtonIndex.HasValue
					&& options.DefaultButtonIndex >= 0)
					td.DefaultButton = GetButtonIdForCommonButton(options.CommonButtons, options.DefaultButtonIndex.Value);
			}

			td.MainIcon = options.MainIcon;
			td.CustomMainIcon = options.CustomMainIcon;
			td.FooterIcon = options.FooterIcon;
			td.CustomFooterIcon = options.CustomFooterIcon;
			td.EnableHyperlinks = DetectHyperlinks(options.Content, options.ExpandedInfo, options.FooterText);
			td.AllowDialogCancellation =
				(options.AllowDialogCancellation
				|| hasCustomCancel
				|| options.CommonButtons.HasFlag(TaskDialogCommonButtons.Close)
				|| options.CommonButtons.HasFlag(TaskDialogCommonButtons.Cancel));
			td.CallbackTimer = options.EnableCallbackTimer;
			td.ExpandedByDefault = options.ExpandedByDefault;
			td.ExpandFooterArea = options.ExpandToFooter;
			td.PositionRelativeToWindow = true;
			td.RightToLeftLayout = false;
			td.NoDefaultRadioButton = false;
			td.CanBeMinimized = false;
			td.ShowProgressBar = options.ShowProgressBar;
			td.ShowMarqueeProgressBar = options.ShowMarqueeProgressBar;
			td.UseCommandLinks = (options.CommandLinks != null && options.CommandLinks.Length > 0);
			td.UseCommandLinksNoIcon = false;
			td.VerificationText = options.VerificationText;
			td.VerificationFlagChecked = options.VerificationByDefault;
			td.ExpandedControlText = "Hide details";
			td.CollapsedControlText = "Show details";
			td.Callback = options.Callback;
			td.CallbackData = options.CallbackData;
			td.Config = options;

			TaskDialogResult result;
			int diagResult = 0;
			TaskDialogSimpleResult simpResult = TaskDialogSimpleResult.None;
			bool verificationChecked = false;
			int radioButtonResult = -1;
			int? commandButtonResult = null;
			int? customButtonResult = null;

			diagResult = td.Show(options.Owner, out verificationChecked, out radioButtonResult);

			if (radioButtonResult >= RadioButtonIDOffset)
			{
				simpResult = (TaskDialogSimpleResult)diagResult;
				radioButtonResult -= RadioButtonIDOffset;
			}

			if (diagResult >= CommandButtonIDOffset)
			{
				simpResult = TaskDialogSimpleResult.Command;
				commandButtonResult = diagResult - CommandButtonIDOffset;
			}
			else if (diagResult >= CustomButtonIDOffset)
			{
				simpResult = TaskDialogSimpleResult.Custom;
				customButtonResult = diagResult - CustomButtonIDOffset;
			}
			else
			{
				simpResult = (TaskDialogSimpleResult)diagResult;
			}

			result = new TaskDialogResult(
				simpResult,
				(String.IsNullOrEmpty(options.VerificationText) ? null : (bool?)verificationChecked),
				((options.RadioButtons == null || options.RadioButtons.Length == 0) ? null : (int?)radioButtonResult),
				((options.CommandLinks == null || options.CommandLinks.Length == 0) ? null : commandButtonResult),
				((options.CustomButtons == null || options.CustomButtons.Length == 0) ? null : customButtonResult));

			return result;
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
	}
}
