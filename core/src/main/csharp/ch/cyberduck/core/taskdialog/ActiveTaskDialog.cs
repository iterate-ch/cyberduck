using System;
using System.Drawing;
using System.Diagnostics.CodeAnalysis;

namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// The active Task Dialog window. Provides several methods for acting on the active TaskDialog.
	/// You should not use this object after the TaskDialog Destroy notification callback. Doing so
	/// will result in undefined behavior and likely crash.
	/// </summary>
	internal class ActiveTaskDialog : IActiveTaskDialog
	{
		/// <summary>
		/// The Task Dialog's window handle.
		/// </summary>
		[SuppressMessage("Microsoft.Reliability", "CA2006:UseSafeHandleToEncapsulateNativeResources")] // We don't own the window.
		private IntPtr handle;

		/// <summary>
		/// Creates a ActiveTaskDialog.
		/// </summary>
		/// <param name="handle">The Task Dialog's window handle.</param>
		internal ActiveTaskDialog(IntPtr handle)
		{
			if (handle == IntPtr.Zero)
			{
				throw new ArgumentNullException("handle");
			}

			this.handle = handle;
		}

		/// <summary>
		/// The Task Dialog's window handle.
		/// </summary>
		public IntPtr Handle
		{
			get { return this.handle; }
		}

		//// Not supported. Task Dialog Spec does not indicate what this is for.
		////public void NavigatePage()
		////{
		////    // TDM_NAVIGATE_PAGE                   = WM_USER+101,
		////    UnsafeNativeMethods.SendMessage(
		////        this.windowHandle,
		////        (uint)UnsafeNativeMethods.TASKDIALOG_MESSAGES.TDM_NAVIGATE_PAGE,
		////        IntPtr.Zero,
		////        //a UnsafeNativeMethods.TASKDIALOGCONFIG value);
		////}

		/// <summary>
		/// Simulate the action of a button click in the TaskDialog. This can be a DialogResult value 
		/// or the ButtonID set on a TaskDialogButton set on TaskDialog.Buttons.
		/// </summary>
		/// <param name="buttonId">Indicates the button ID to be selected.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool ClickButton(int buttonId)
		{
			if (buttonId >= TaskDialog.RadioButtonIDOffset && buttonId < TaskDialog.CommandButtonIDOffset)
			{
				// TDM_CLICK_RADIO_BUTTON = WM_USER+110, // wParam = Radio Button ID
				return UnsafeNativeMethods.SendMessage(
					this.handle,
					(uint)TASKDIALOG_MESSAGES.TDM_CLICK_RADIO_BUTTON,
					(IntPtr)buttonId,
					IntPtr.Zero) != IntPtr.Zero;
			}
			else
			{
				// TDM_CLICK_BUTTON = WM_USER+102, // wParam = Button ID
				return UnsafeNativeMethods.SendMessage(
					this.handle,
					(uint)TASKDIALOG_MESSAGES.TDM_CLICK_BUTTON,
					(IntPtr)buttonId,
					IntPtr.Zero) != IntPtr.Zero;
			}
		}
		/// <summary>
		/// Simulate the action of a command link button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>
		/// If the function succeeds the return value is true.
		/// </returns>
		public bool ClickCommandButton(int index)
		{
			return ClickButton(TaskDialog.GetButtonIdForCommandButton(index));
		}
		/// <summary>
		/// Simulate the action of a common button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>
		/// If the function succeeds the return value is true.
		/// </returns>
		public bool ClickCommonButton(int index)
		{
			return ClickButton(index);
		}
		/// <summary>
		/// Simulate the action of a custom button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>
		/// If the function succeeds the return value is true.
		/// </returns>
		public bool ClickCustomButton(int index)
		{
			return ClickButton(TaskDialog.GetButtonIdForCustomButton(index));
		}
		/// <summary>
		/// Simulate the action of a radio button click in the TaskDialog.
		/// </summary>
		/// <param name="index">The zero-based index into the button set.</param>
		/// <returns>
		/// If the function succeeds the return value is true.
		/// </returns>
		public bool ClickRadioButton(int index)
		{
			return ClickButton(TaskDialog.GetButtonIdForRadioButton(index));
		}

		/// <summary>
		/// Check or uncheck the verification checkbox in the TaskDialog. 
		/// </summary>
		/// <param name="checkedState">The checked state to set the verification checkbox.</param>
		/// <param name="setKeyboardFocusToCheckBox">True to set the keyboard focus to the checkbox, and fasle otherwise.</param>
		public void ClickVerification(bool checkedState, bool setKeyboardFocusToCheckBox)
		{
			// TDM_CLICK_VERIFICATION = WM_USER+113, // wParam = 0 (unchecked), 1 (checked), lParam = 1 (set key focus)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_CLICK_VERIFICATION,
				(checkedState ? new IntPtr(1) : IntPtr.Zero),
				(setKeyboardFocusToCheckBox ? new IntPtr(1) : IntPtr.Zero));
		}

		/// <summary>
		/// Enable or disable a button in the TaskDialog. 
		/// The passed buttonID is the ButtonID set on a TaskDialogButton set on TaskDialog.Buttons
		/// or a common button ID.
		/// </summary>
		/// <param name="buttonId">Indicates the button ID to be enabled or diabled.</param>
		/// <param name="enabled">Enambe the button if true. Disable the button if false.</param>
		public void SetButtonEnabledState(int buttonId, bool enabled)
		{
			if (buttonId >= TaskDialog.RadioButtonIDOffset && buttonId < TaskDialog.CommandButtonIDOffset)
			{
				// TDM_ENABLE_RADIO_BUTTON = WM_USER+112, // lParam = 0 (disable), lParam != 0 (enable), wParam = Radio Button ID
				UnsafeNativeMethods.SendMessage(
					this.handle,
					(uint)TASKDIALOG_MESSAGES.TDM_ENABLE_RADIO_BUTTON,
					(IntPtr)buttonId,
					(IntPtr)(enabled ? 1 : 0));
			}
			else
			{
				// TDM_ENABLE_BUTTON = WM_USER+111, // lParam = 0 (disable), lParam != 0 (enable), wParam = Button ID
				UnsafeNativeMethods.SendMessage(
					this.handle,
					(uint)TASKDIALOG_MESSAGES.TDM_ENABLE_BUTTON,
					(IntPtr)buttonId,
					(IntPtr)(enabled ? 1 : 0));
			}
		}
		/// <summary>
		/// Sets the state of a command link button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		public void SetCommandButtonEnabledState(int index, bool enabled)
		{
			SetButtonEnabledState(TaskDialog.GetButtonIdForCommandButton(index), enabled);
		}
		/// <summary>
		/// Sets the state of a common button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		public void SetCommonButtonEnabledState(int index, bool enabled)
		{
			SetButtonEnabledState(index, enabled);
		}
		/// <summary>
		/// Sets the state of a custom button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		public void SetCustomButtonEnabledState(int index, bool enabled)
		{
			SetButtonEnabledState(TaskDialog.GetButtonIdForCustomButton(index), enabled);
		}
		/// <summary>
		/// Sets the state of a radio button to enabled or disabled.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
		public void SetRadioButtonEnabledState(int index, bool enabled)
		{
			SetButtonEnabledState(TaskDialog.GetButtonIdForRadioButton(index), enabled);
		}

		/// <summary>
		/// Designate whether a given Task Dialog button or command link should have a User Account Control (UAC) shield icon.
		/// </summary>
		/// <param name="buttonId">ID of the push button or command link to be updated.</param>
		/// <param name="elevationRequired">False to designate that the action invoked by the button does not require elevation;
		/// true to designate that the action does require elevation.</param>
		public void SetButtonElevationRequiredState(int buttonId, bool elevationRequired)
		{
			// TDM_SET_BUTTON_ELEVATION_REQUIRED_STATE = WM_USER+115, // wParam = Button ID, lParam = 0 (elevation not required), lParam != 0 (elevation required)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_BUTTON_ELEVATION_REQUIRED_STATE,
				(IntPtr)buttonId,
				(IntPtr)(elevationRequired ? new IntPtr(1) : IntPtr.Zero));
		}
		/// <summary>
		/// Sets the elevation required state of a command link button, adding a shield icon.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		public void SetCommandButtonElevationRequiredState(int index, bool elevationRequired)
		{
			SetButtonElevationRequiredState(TaskDialog.GetButtonIdForCommandButton(index), elevationRequired);
		}
		/// <summary>
		/// Sets the elevation required state of a common button, adding a shield icon.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		public void SetCommonButtonElevationRequiredState(int index, bool elevationRequired)
		{
			SetButtonElevationRequiredState(index, elevationRequired);
		}
		/// <summary>
		/// Sets the elevation required state of a custom button, adding a shield icon.
		/// </summary>
		/// <param name="index">The zero-based index of the button to set.</param>
		/// <param name="elevationRequired"><c>true</c> to enable the button; <c>false</c> to disable</param>
		/// <remarks>
		/// Note that this is purely for visual effect. You will still need to perform
		/// the necessary code to trigger a UAC prompt for the user.
		/// </remarks>
		public void SetCustomButtonElevationRequiredState(int index, bool elevationRequired)
		{
			SetButtonElevationRequiredState(TaskDialog.GetButtonIdForCustomButton(index), elevationRequired);
		}

		/// <summary>
		/// Used to indicate whether the hosted progress bar should be displayed in marquee mode or not.
		/// </summary>
		/// <param name="marquee">Specifies whether the progress bar sbould be shown in Marquee mode.
		/// A value of true turns on Marquee mode.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetMarqueeProgressBar(bool marquee)
		{
			// TDM_SET_MARQUEE_PROGRESS_BAR        = WM_USER+103, // wParam = 0 (nonMarque) wParam != 0 (Marquee)
			return UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_MARQUEE_PROGRESS_BAR,
				(marquee ? (IntPtr)1 : IntPtr.Zero),
				IntPtr.Zero) != IntPtr.Zero;

			// Future: get more detailed error from and throw.
		}

		/// <summary>
		/// Sets the state of the progress bar.
		/// </summary>
		/// <param name="newState">The state to set the progress bar.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetProgressBarState(TaskDialogProgressBarState newState)
		{
			// TDM_SET_PROGRESS_BAR_STATE          = WM_USER+104, // wParam = new progress state
			return UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_PROGRESS_BAR_STATE,
				(IntPtr)newState,
				IntPtr.Zero) != IntPtr.Zero;

			// Future: get more detailed error from and throw.
		}

		/// <summary>
		/// Set the minimum and maximum values for the hosted progress bar.
		/// </summary>
		/// <param name="minRange">Minimum range value. By default, the minimum value is zero.</param>
		/// <param name="maxRange">Maximum range value.  By default, the maximum value is 100.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetProgressBarRange(Int16 minRange, Int16 maxRange)
		{
			// TDM_SET_PROGRESS_BAR_RANGE          = WM_USER+105, // lParam = MAKELPARAM(nMinRange, nMaxRange)
			// #define MAKELPARAM(l, h)      ((LPARAM)(DWORD)MAKELONG(l, h))
			// #define MAKELONG(a, b)      ((LONG)(((WORD)(((DWORD_PTR)(a)) & 0xffff)) | ((DWORD)((WORD)(((DWORD_PTR)(b)) & 0xffff))) << 16))
			IntPtr lparam = (IntPtr)((((Int32)minRange) & 0xffff) | ((((Int32)maxRange) & 0xffff) << 16));
			return UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_PROGRESS_BAR_RANGE,
				IntPtr.Zero,
				lparam) != IntPtr.Zero;

			// Return value is actually prior range.
		}

		/// <summary>
		/// Set the current position for a progress bar.
		/// </summary>
		/// <param name="newPosition">The new position.</param>
		/// <returns>Returns the previous value if successful, or zero otherwise.</returns>
		public int SetProgressBarPosition(int newPosition)
		{
			// TDM_SET_PROGRESS_BAR_POS            = WM_USER+106, // wParam = new position
			return (int)UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_PROGRESS_BAR_POS,
				(IntPtr)newPosition,
				IntPtr.Zero);
		}

		/// <summary>
		/// Sets the animation state of the Marquee Progress Bar.
		/// </summary>
		/// <param name="startMarquee">true starts the marquee animation and false stops it.</param>
		/// <param name="speed">The time in milliseconds between refreshes.</param>
		public void SetProgressBarMarquee(bool startMarquee, uint speed)
		{
			// TDM_SET_PROGRESS_BAR_MARQUEE        = WM_USER+107, // wParam = 0 (stop marquee), wParam != 0 (start marquee), lparam = speed (milliseconds between repaints)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_PROGRESS_BAR_MARQUEE,
				(startMarquee ? new IntPtr(1) : IntPtr.Zero),
				(IntPtr)speed);
		}

		/// <summary>
		/// Updates the window title text.
		/// </summary>
		/// <param name="title">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetWindowTitle(string title)
		{
			return UnsafeNativeMethods.SetWindowText(
				this.handle,
				title);
		}

		/// <summary>
		/// Updates the content text.
		/// </summary>
		/// <param name="content">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetContent(string content)
		{
			// TDE_CONTENT,
			// TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			return UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_CONTENT,
				content) != IntPtr.Zero;
		}

		/// <summary>
		/// Updates the Expanded Information text.
		/// </summary>
		/// <param name="expandedInformation">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetExpandedInformation(string expandedInformation)
		{
			// TDE_EXPANDED_INFORMATION,
			// TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			return UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_EXPANDED_INFORMATION,
				expandedInformation) != IntPtr.Zero;
		}

		/// <summary>
		/// Updates the Footer text.
		/// </summary>
		/// <param name="footer">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetFooter(string footer)
		{
			// TDE_FOOTER,
			// TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			return UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_FOOTER,
				footer) != IntPtr.Zero;
		}

		/// <summary>
		/// Updates the Main Instruction.
		/// </summary>
		/// <param name="mainInstruction">The new value.</param>
		/// <returns>If the function succeeds the return value is true.</returns>
		public bool SetMainInstruction(string mainInstruction)
		{
			// TDE_MAIN_INSTRUCTION
			// TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			return UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_SET_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_MAIN_INSTRUCTION,
				mainInstruction) != IntPtr.Zero;
		}

		/// <summary>
		/// Updates the content text.
		/// </summary>
		/// <param name="content">The new value.</param>
		public void UpdateContent(string content)
		{
			// TDE_CONTENT,
			// TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_CONTENT,
				content);
		}

		/// <summary>
		/// Updates the Expanded Information text. No effect if it was previously set to null.
		/// </summary>
		/// <param name="expandedInformation">The new value.</param>
		public void UpdateExpandedInformation(string expandedInformation)
		{
			// TDE_EXPANDED_INFORMATION,
			// TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_EXPANDED_INFORMATION,
				expandedInformation);
		}

		/// <summary>
		/// Updates the Footer text. No Effect if it was perviously set to null.
		/// </summary>
		/// <param name="footer">The new value.</param>
		public void UpdateFooter(string footer)
		{
			// TDE_FOOTER,
			// TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_FOOTER,
				footer);
		}

		/// <summary>
		/// Updates the Main Instruction.
		/// </summary>
		/// <param name="mainInstruction">The new value.</param>
		public void UpdateMainInstruction(string mainInstruction)
		{
			// TDE_MAIN_INSTRUCTION
			// TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
			UnsafeNativeMethods.SendMessageWithString(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ELEMENT_TEXT,
				(IntPtr)TASKDIALOG_ELEMENTS.TDE_MAIN_INSTRUCTION,
				mainInstruction);
		}

		/// <summary>
		/// Updates the main instruction icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">Task Dialog standard icon.</param>
		public void UpdateMainIcon(TaskDialogIcon icon)
		{
			// TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ICON,
				(IntPtr)TASKDIALOG_ICON_ELEMENTS.TDIE_ICON_MAIN,
				(IntPtr)icon);
		}

		/// <summary>
		/// Updates the main instruction icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">The icon to set.</param>
		public void UpdateMainIcon(Icon icon)
		{
			// TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ICON,
				(IntPtr)TASKDIALOG_ICON_ELEMENTS.TDIE_ICON_MAIN,
				(icon == null ? IntPtr.Zero : icon.Handle));
		}

		/// <summary>
		/// Updates the footer icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">Task Dialog standard icon.</param>
		public void UpdateFooterIcon(TaskDialogIcon icon)
		{
			// TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ICON,
				(IntPtr)TASKDIALOG_ICON_ELEMENTS.TDIE_ICON_FOOTER,
				(IntPtr)icon);
		}

		/// <summary>
		/// Updates the footer icon. Note the type (standard via enum or
		/// custom via Icon type) must be used when upating the icon.
		/// </summary>
		/// <param name="icon">The icon to set.</param>
		public void UpdateFooterIcon(Icon icon)
		{
			// TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
			UnsafeNativeMethods.SendMessage(
				this.handle,
				(uint)TASKDIALOG_MESSAGES.TDM_UPDATE_ICON,
				(IntPtr)TASKDIALOG_ICON_ELEMENTS.TDIE_ICON_FOOTER,
				(icon == null ? IntPtr.Zero : icon.Handle));
		}
	}
}
