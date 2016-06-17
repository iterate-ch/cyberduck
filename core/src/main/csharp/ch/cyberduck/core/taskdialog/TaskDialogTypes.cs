using System;
using System.ComponentModel;
using System.Drawing;

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
	public delegate bool TaskDialogCallback(IActiveTaskDialog dialog, TaskDialogNotificationArgs args, object callbackData);

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
		/// Contains localized strings for the emulated dialog.
		/// </summary>
		/// <remarks>
		/// Note that these are only used by the emulated dialog.
		/// The native dialog is controlled by Windows language settings.
		/// </remarks>
		public static TaskDialogStrings LocalizedStrings;

		/// <summary>
		/// Initializes the <see cref="T:TaskDialogOptions"/> type.
		/// </summary>
		static TaskDialogOptions()
		{
			LocalizedStrings = new EnglishTaskDialogStrings();
		}

		/// <summary>
		/// The owner window of the task dialog box.
		/// </summary>
		public IntPtr Owner;
		/// <summary>
		/// Caption of the window.
		/// </summary>
		public string Title;
		/// <summary>
		/// A large 32x32 icon that signifies the purpose of the dialog, using
		/// one of the built-in system icons.
		/// </summary>
		public TaskDialogIcon MainIcon;
		/// <summary>
		/// A large 32x32 icon that signifies the purpose of the dialog, using
		/// a custom Icon resource. If defined <see cref="MainIcon"/> will be
		/// ignored.
		/// </summary>
		public Icon CustomMainIcon;
		/// <summary>
		/// Principal text.
		/// </summary>
		public string MainInstruction;
		/// <summary>
		/// Supplemental text that expands on the principal text.
		/// </summary>
		public string Content;
		/// <summary>
		/// Extra text that will be hidden by default.
		/// </summary>
		public string ExpandedInfo;
		/// <summary>
		/// Indicates that the expanded info should be displayed when the
		/// dialog is initially displayed.
		/// </summary>
		public bool ExpandedByDefault;
		/// <summary>
		/// Indicates that the expanded info should be displayed at the bottom
		/// of the dialog's footer area instead of immediately after the
		/// dialog's content.
		/// </summary>
		public bool ExpandToFooter;
		/// <summary>
		/// Standard push buttons.
		/// </summary>
		public TaskDialogCommonButtons CommonButtons;
		/// <summary>
		/// Application-defined options for the user.
		/// </summary>
		public string[] RadioButtons;
		/// <summary>
		/// Buttons that are not from the set of standard buttons. Use an
		/// ampersand to denote an access key. These are ignored if CommandLinks
		/// are also defined. These will be appended after any defined common buttons.
		/// </summary>
		public string[] CustomButtons;
		/// <summary>
		/// Command links. These override any custom buttons, but can be used with
		/// radio and common buttons.
		/// </summary>
		public string[] CommandLinks;
		/// <summary>
		/// Zero-based index of the button to have focus by default.
		/// </summary>
		public int? DefaultButtonIndex;
		/// <summary>
		/// Text accompanied by a checkbox, typically for user feedback such as
		/// Do-not-show-this-dialog-again options.
		/// </summary>
		public string VerificationText;
		/// <summary>
		/// Indicates that the verification checkbox in the dialog is checked
		/// when the dialog is initially displayed.
		/// </summary>
		public bool VerificationByDefault;
		/// <summary>
		/// A small 16x16 icon that signifies the purpose of the footer text,
		/// using one of the built-in system icons.
		/// </summary>
		public TaskDialogIcon FooterIcon;
		/// <summary>
		/// A small 16x16 icon that signifies the purpose of the footer text,
		/// using a custom Icon resource. If defined <see cref="FooterIcon"/>
		/// will be ignored.
		/// </summary>
		public Icon CustomFooterIcon;
		/// <summary>
		/// Additional footer text.
		/// </summary>
		public string FooterText;
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
		/// Indicates that a Progress Bar is to be displayed.
		/// </summary>
		/// <remarks>
		/// You can set the state, whether paused, in error, etc., as well as
		/// the range and current value by setting a callback and timer to
		/// control the dialog at custom intervals.
		/// </remarks>
		public bool ShowProgressBar;
		/// <summary>
		/// Indicates that an Marquee Progress Bar is to be displayed.
		/// </summary>
		/// <remarks>
		/// You can set start and stop the animation by setting a callback and
		/// timer to control the dialog at custom intervals.
		/// </remarks>
		public bool ShowMarqueeProgressBar;
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
		/// Indicates that the task dialog's callback is to be called
		/// approximately every 200 milliseconds.
		/// </summary>
		/// <remarks>
		/// Enable this in order to do updates on the task dialog periodically,
		/// such as for a progress bar, current download speed, or estimated
		/// time to complete, etc.
		/// </remarks>
		public bool EnableCallbackTimer;
	}
	/// <summary>
	/// Provides view data for all task dialog buttons.
	/// </summary>
	public class TaskDialogButtonData : INotifyPropertyChanged
	{
		private bool _isEnabled;
		private bool _isElevationRequired;

		/// <summary>
		/// Gets the button's ID value to return when clicked.
		/// </summary>
		public int ID { get; private set; }
		/// <summary>
		/// Gets the button's text label.
		/// </summary>
		public string Text { get; private set; }
		/// <summary>
		/// Gets a value indicating whether or not the button should be the default.
		/// </summary>
		public bool IsDefault { get; private set; }
		/// <summary>
		/// Gets a value indicating whether or not the button should be a cancel.
		/// </summary>
		public bool IsCancel { get; private set; }
		/// <summary>
		/// Gets or sets a value indicating whether or not the button should be enabled.
		/// </summary>
		public bool IsEnabled
		{
			get { return _isEnabled; }
			set
			{
				_isEnabled = value;

				RaisePropertyChanged("IsEnabled");
			}
		}
		/// <summary>
		/// Gets or sets a value indicating whether or not the button requires elevation.
		/// </summary>
		public bool IsElevationRequired
		{
			get { return _isElevationRequired; }
			set
			{
				_isElevationRequired = value;

				RaisePropertyChanged("IsElevationRequired");
			}
		}

		/// <summary>
		/// Initializes a new instance of the <see cref="TaskDialogButtonData"/> class.
		/// </summary>
		public TaskDialogButtonData()
		{
			_isEnabled = true;
			_isElevationRequired = false;
		}
		/// <summary>
		/// Initializes a new instance of the <see cref="TaskDialogButtonData"/> struct.
		/// </summary>
		/// <param name="id">The id value for the button.</param>
		/// <param name="text">The text label.</param>
		/// <param name="command">The command to associate.</param>
		/// <param name="isDefault">Whether the button should be the default.</param>
		/// <param name="isCancel">Whether the button should be a cancel.</param>
		public TaskDialogButtonData(int id, string text, bool isDefault = false, bool isCancel = false)
			: this()
		{
			ID = id;
			Text = text;
			IsDefault = isDefault;
			IsCancel = isCancel;
		}

		/// <summary>
		/// Occurs when a property value changes.
		/// </summary>
		public event PropertyChangedEventHandler PropertyChanged;

		/// <summary>
		/// Raises the <see cref="E:PropertyChanged"/> event.
		/// </summary>
		/// <param name="propertyName">The property name of the property that has changed.</param>
		protected void RaisePropertyChanged(string propertyName)
		{
			OnPropertyChanged(new PropertyChangedEventArgs(propertyName));
		}

		/// <summary>
		/// Raises the <see cref="E:PropertyChanged"/> event.
		/// </summary>
		/// <param name="e">The <see cref="System.ComponentModel.PropertyChangedEventArgs"/> instance containing the event data.</param>
		protected virtual void OnPropertyChanged(PropertyChangedEventArgs e)
		{
			if (PropertyChanged != null)
				PropertyChanged(this, e);
		}
	}
	/// <summary>
	/// Defines methods for manipulating an active dialog during a callback.
	/// </summary>
	public interface IActiveTaskDialog
	{
		// TODO Support more of the methods exposed by VistaActiveTaskDialog class

		/// <summary>
		/// Simulate the action of a button click in the TaskDialog. This can be a DialogResult value 
		/// or the ButtonID set on a TaskDialogButton set on TaskDialog.Buttons.
		/// </summary>
		/// <param name="buttonId">Indicates the button ID to be selected.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool ClickButton(int buttonId);
		/// <summary>
		/// Simulate the action of a command link button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool ClickCommandButton(int index);
		/// <summary>
		/// Simulate the action of a common button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool ClickCommonButton(int index);
		/// <summary>
		/// Simulate the action of a custom button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool ClickCustomButton(int index);
		/// <summary>
		/// Simulate the action of a radio button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool ClickRadioButton(int index);
		/// <summary>
		/// Check or uncheck the verification checkbox in the TaskDialog. 
		/// </summary>
		/// <param name="checkedState">The checked state to set the verification checkbox.</param>
		/// <param name="setKeyboardFocusToCheckBox"><c>true</c> to set the keyboard focus to the checkbox; <c>false</c> to leave focus unchanged.</param>
		void ClickVerification(bool checkedState, bool setKeyboardFocusToCheckBox);
		/// <summary>
		/// Sets the state of a button to enabled or disabled.
		/// </summary>
		/// <param name="buttonId">The id of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		void SetButtonEnabledState(int buttonId, bool enabled);
		/// <summary>
		/// Sets the state of a command link button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		void SetCommandButtonEnabledState(int index, bool enabled);
		/// <summary>
		/// Sets the state of a common button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		void SetCommonButtonEnabledState(int index, bool enabled);
		/// <summary>
		/// Sets the state of a custom button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		void SetCustomButtonEnabledState(int index, bool enabled);
		/// <summary>
		/// Sets the state of a radio button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		void SetRadioButtonEnabledState(int index, bool enabled);
		/// <summary>
		/// Sets the elevation required state of a button, adding a shield icon.
		/// </summary>
		/// <param name="buttonId">The id of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		void SetButtonElevationRequiredState(int buttonId, bool elevationRequired);
		/// <summary>
		/// Sets the elevation required state of a command link button, adding a shield icon.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		void SetCommandButtonElevationRequiredState(int index, bool elevationRequired);
		/// <summary>
		/// Sets the elevation required state of a common button, adding a shield icon.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		void SetCommonButtonElevationRequiredState(int index, bool elevationRequired);
		/// <summary>
		/// Sets the elevation required state of a custom button, adding a shield icon.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to enable the button; <c>false</c> to disable</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		void SetCustomButtonElevationRequiredState(int index, bool elevationRequired);
		/// <summary>
		/// Used to indicate whether the hosted progress bar should be displayed in marquee mode or not.
		/// </summary>
		/// <param name="marquee">Specifies whether the progress bar sbould be shown in Marquee mode.
		/// A value of true turns on Marquee mode.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetMarqueeProgressBar(bool marquee);
		/// <summary>
		/// Sets the state of the progress bar.
		/// </summary>
		/// <param name="newState">The state to set the progress bar.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetProgressBarState(TaskDialogProgressBarState newState);
		/// <summary>
		/// Set the minimum and maximum values for the hosted progress bar.
		/// </summary>
		/// <param name="minRange">Minimum range value. By default, the minimum value is zero.</param>
		/// <param name="maxRange">Maximum range value.  By default, the maximum value is 100.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetProgressBarRange(Int16 minRange, Int16 maxRange);
		/// <summary>
		/// Set the current position for a progress bar.
		/// </summary>
		/// <param name="newPosition">The new position.</param>
		/// <returns>Returns the previous value if successful, or zero otherwise.</returns>
		int SetProgressBarPosition(int newPosition);
		/// <summary>
		/// Sets the animation state of the Marquee Progress Bar.
		/// </summary>
		/// <param name="startMarquee">true starts the marquee animation and false stops it.</param>
		/// <param name="speed">The time in milliseconds between refreshes.</param>
		void SetProgressBarMarquee(bool startMarquee, uint speed);
		/// <summary>
		/// Updates the window title text.
		/// </summary>
		/// <param name="title">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetWindowTitle(string title);
		/// <summary>
		/// Updates the content text.
		/// </summary>
		/// <param name="content">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetContent(string content);
		/// <summary>
		/// Updates the Expanded Information text.
		/// </summary>
		/// <param name="expandedInformation">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetExpandedInformation(string expandedInformation);
		/// <summary>
		/// Updates the Footer text.
		/// </summary>
		/// <param name="footer">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetFooter(string footer);
		/// <summary>
		/// Updates the Main Instruction.
		/// </summary>
		/// <param name="mainInstruction">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		bool SetMainInstruction(string mainInstruction);
		/// <summary>
		/// Updates the main instruction icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">Task Dialog standard icon.</param>
		void UpdateMainIcon(TaskDialogIcon icon);
		/// <summary>
		/// Updates the main instruction icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">The icon to set.</param>
		void UpdateMainIcon(Icon icon);
		/// <summary>
		/// Updates the footer icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">Task Dialog standard icon.</param>
		void UpdateFooterIcon(TaskDialogIcon icon);
		/// <summary>
		/// Updates the footer icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">The icon to set.</param>
		void UpdateFooterIcon(Icon icon);
	}
}
