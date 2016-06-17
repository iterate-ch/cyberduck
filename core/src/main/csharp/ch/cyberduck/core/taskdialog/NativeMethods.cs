using System;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core.TaskDialog
{
	internal class NativeMethods
	{
		[DllImport("user32.dll")]
		internal extern static int SetWindowLong(IntPtr hwnd, int index, int value);
		[DllImport("user32.dll")]
		internal extern static int GetWindowLong(IntPtr hwnd, int index);
		[DllImport("user32.dll")]
		internal static extern bool SetWindowPos(IntPtr hwnd, IntPtr hwndInsertAfter, int x, int y, int width, int height, uint flags);
		[DllImport("user32.dll")]
		internal static extern IntPtr SendMessage(IntPtr hwnd, uint msg, IntPtr wParam, IntPtr lParam);
		[DllImport("user32.dll")]
		internal static extern IntPtr DefWindowProc(IntPtr hwnd, uint msg, IntPtr wParam, IntPtr lParam);
	}
}
