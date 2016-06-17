using System;

namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// Provides safe Win32 API wrapper calls for various actions not directly
	/// supported by WPF classes out of the box.
	/// </summary>
	internal class SafeNativeMethods
	{
		/// <summary>
		/// Sets the window's close button visibility.
		/// </summary>
		/// <param name="handle">The window to set.</param>
		/// <param name="showCloseButton"><c>true</c> to show the close button; otherwise, <c>false</c></param>
		public static void SetWindowCloseButtonVisibility(IntPtr handle, bool showCloseButton)
		{
			int style = NativeMethods.GetWindowLong(handle, Win32Constants.GWL_STYLE);

			if (showCloseButton)
				NativeMethods.SetWindowLong(handle, Win32Constants.GWL_STYLE, style & Win32Constants.WS_SYSMENU);
			else
				NativeMethods.SetWindowLong(handle, Win32Constants.GWL_STYLE, style & ~Win32Constants.WS_SYSMENU);
		}
		/// <summary>
		/// Sets the window's icon visibility.
		/// </summary>
		/// <param name="window">The window to set.</param>
		/// <param name="showIcon"><c>true</c> to show the icon in the caption; otherwise, <c>false</c></param>
		public static void SetWindowIconVisibility(IntPtr handle, bool showIcon)
		{
			// For Vista/7 and higher
			if (Environment.OSVersion.Version.Major >= 6)
			{
				// Change the extended window style
				if (showIcon)
				{
					int extendedStyle = NativeMethods.GetWindowLong(handle, Win32Constants.GWL_EXSTYLE);
					NativeMethods.SetWindowLong(handle, Win32Constants.GWL_EXSTYLE, extendedStyle | ~Win32Constants.WS_EX_DLGMODALFRAME);
				}
				else
				{
					int extendedStyle = NativeMethods.GetWindowLong(handle, Win32Constants.GWL_EXSTYLE);
					NativeMethods.SetWindowLong(handle, Win32Constants.GWL_EXSTYLE, extendedStyle | Win32Constants.WS_EX_DLGMODALFRAME);
				}

				// Update the window's non-client area to reflect the changes
				NativeMethods.SetWindowPos(handle, IntPtr.Zero, 0, 0, 0, 0,
					Win32Constants.SWP_NOMOVE | Win32Constants.SWP_NOSIZE | Win32Constants.SWP_NOZORDER | Win32Constants.SWP_FRAMECHANGED);
			}
			// For XP and older
			// TODO Setting Window Icon visibility doesn't work in XP
			else
			{
				// 0 - ICON_SMALL (caption bar)
				// 1 - ICON_BIG   (alt-tab)

				if (showIcon)
					NativeMethods.SendMessage(handle, Win32Constants.WM_SETICON, new IntPtr(0),
						NativeMethods.DefWindowProc(handle, Win32Constants.WM_SETICON, new IntPtr(0), IntPtr.Zero));
				else
					NativeMethods.SendMessage(handle, Win32Constants.WM_SETICON, new IntPtr(0), IntPtr.Zero);
			}
		}
	}
}
