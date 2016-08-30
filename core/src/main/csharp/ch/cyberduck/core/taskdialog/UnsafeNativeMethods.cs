using System;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// Class to hold native code interop declarations.
	/// </summary>
	internal static partial class UnsafeNativeMethods
	{
		/// <summary>
		/// An application-defined function used with the TaskDialogIndirect function. It receives messages from the task dialog when various events occur.
		/// </summary>
		/// <param name="hwnd">Handle to the TaskDialog window. Do not continue sending messages to hwnd after the callback procedure returns from having been called with TDN_DESTROYED.</param>
		/// <param name="uNotification">A notification of type TASKDIALOG_NOTIFICATIONS.</param>
		/// <param name="wParam">Specifies additional notification information. The contents of this parameter depend on the value of the uNotification parameter.</param>
		/// <param name="lParam">Specifies additional notification information. The contents of this parameter depend on the value of the uNotification parameter.</param>
		/// <param name="dwRefData">Pointer to application specific data. This is the data pointed to by the lpCallbackData member of structure TASKDIALOGCONFIG used to create the task dialog.</param>
		/// <returns>The return value is specific to the notification being processed. When responding to a button click, your implementation should return S_FALSE if the Task Dialog is not to close. Otherwise return S_OK. </returns>
		internal delegate int TaskDialogCallbackProc([In] IntPtr hwnd, [In] uint uNotification, [In] UIntPtr wParam, [In] IntPtr lParam, [In] IntPtr dwRefData);

		///// <summary>
		///// TaskDialog taken from commctrl.h.
		///// </summary>
		///// <param name="hwndParent">Parent window.</param>
		///// <param name="hInstance">Module instance to get resources from.</param>
		///// <param name="pszWindowTitle">Title of the Task Dialog window.</param>
		///// <param name="pszMainInstruction">The main instructions.</param>
		///// <param name="dwCommonButtons">Common push buttons to show.</param>
		///// <param name="pszIcon">The main icon.</param>
		///// <param name="pnButton">The push button pressed.</param>
		////[DllImport("ComCtl32", CharSet = CharSet.Unicode, PreserveSig = false)]
		////public static extern void TaskDialog(
		////    [In] IntPtr hwndParent,
		////    [In] IntPtr hInstance,
		////    [In] String pszWindowTitle,
		////    [In] String pszMainInstruction,
		////    [In] TaskDialogCommonButtons dwCommonButtons,
		////    [In] IntPtr pszIcon,
		////    [Out] out int pnButton);

		/// <summary>
		/// TaskDialogIndirect taken from commctl.h
		/// </summary>
		/// <param name="pTaskConfig">All the parameters about the Task Dialog to Show.</param>
		/// <param name="pnButton">The push button pressed.</param>
		/// <param name="pnRadioButton">The radio button that was selected.</param>
		/// <param name="pfVerificationFlagChecked">The state of the verification checkbox on dismiss of the Task Dialog.</param>
		[System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Interoperability", "CA1400:PInvokeEntryPointsShouldExist", Justification = "Declaration is valid and works fine.")]
		[DllImport("ComCtl32", CharSet = CharSet.Unicode, EntryPoint = "TaskDialogIndirect", ExactSpelling = true, PreserveSig = false)]
		internal static extern void TaskDialogIndirect(
			[In] ref TASKDIALOGCONFIG pTaskConfig,
			[Out] out int pnButton,
			[Out] out int pnRadioButton,
			[Out] out bool pfVerificationFlagChecked);

		/// <summary>
		/// Win32 SendMessage.
		/// </summary>
		/// <param name="hWnd">Window handle to send to.</param>
		/// <param name="Msg">The windows message to send.</param>
		/// <param name="wParam">Specifies additional message-specific information.</param>
		/// <param name="lParam">Specifies additional message-specific information.</param>
		/// <returns>The return value specifies the result of the message processing; it depends on the message sent.</returns>
		[DllImport("user32.dll")]
		internal static extern IntPtr SendMessage(IntPtr hWnd, uint Msg, IntPtr wParam, IntPtr lParam);

		/// <summary>
		/// Win32 SendMessage.
		/// </summary>
		/// <param name="hWnd">Window handle to send to.</param>
		/// <param name="Msg">The windows message to send.</param>
		/// <param name="wParam">Specifies additional message-specific information.</param>
		/// <param name="lParam">Specifies additional message-specific information as a string.</param>
		/// <returns>The return value specifies the result of the message processing; it depends on the message sent.</returns>
		[DllImport("user32.dll", EntryPoint="SendMessage")]
		internal static extern IntPtr SendMessageWithString(IntPtr hWnd, uint Msg, IntPtr wParam, [MarshalAs(UnmanagedType.LPWStr)] string lParam);

		/// <summary>
		/// Changes the text of the specified window's title bar (if it has one).
		/// </summary>
		/// <param name="hwnd">A handle to the window or control whose text is to be changed.</param>
		/// <param name="lpString">The new title or control text. </param>
		/// <returns>
		/// If the function succeeds, the return value is nonzero.
		/// If the function fails, the return value is zero.
		/// To get extended error information, call GetLastError. 
		/// </returns>
		[DllImport("user32.dll", SetLastError = true, CharSet = CharSet.Auto)]
		internal static extern bool SetWindowText(IntPtr hwnd, String lpString);
	}
}
