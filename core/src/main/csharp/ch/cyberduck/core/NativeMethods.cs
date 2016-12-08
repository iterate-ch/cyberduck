// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core
{
    [StructLayout(LayoutKind.Sequential)]
    public struct APPBARDATA
    {
        public uint cbSize;
        public IntPtr hWnd;
        public uint uCallbackMessage;
        public AppBarEdge uEdge;
        public RECT rc;
        public int lParam;
    }

    internal enum MousePositionCodes
    {
        HTERROR = (-2),
        HTTRANSPARENT = (-1),
        HTNOWHERE = 0,
        HTCLIENT = 1,
        HTCAPTION = 2,
        HTSYSMENU = 3,
        HTGROWBOX = 4,
        HTSIZE = HTGROWBOX,
        HTMENU = 5,
        HTHSCROLL = 6,
        HTVSCROLL = 7,
        HTMINBUTTON = 8,
        HTMAXBUTTON = 9,
        HTLEFT = 10,
        HTRIGHT = 11,
        HTTOP = 12,
        HTTOPLEFT = 13,
        HTTOPRIGHT = 14,
        HTBOTTOM = 15,
        HTBOTTOMLEFT = 16,
        HTBOTTOMRIGHT = 17,
        HTBORDER = 18,
        HTREDUCE = HTMINBUTTON,
        HTZOOM = HTMAXBUTTON,
        HTSIZEFIRST = HTLEFT,
        HTSIZELAST = HTBOTTOMRIGHT,
        HTOBJECT = 19,
        HTCLOSE = 20,
        HTHELP = 21
    }

    public enum AppBarEdge : uint
    {
        NotDocked = UInt32.MaxValue,
        ScreenLeft = 0, // ABE_LEFT
        ScreenTop = 1, // ABE_TOP
        ScreenRight = 2, // ABE_RIGHT
        ScreenBottom = 3, // ABE_BOTTOM
    }

    public enum AppBarMessage : uint
    {
        ABM_NEW = 0x00,
        ABM_REMOVE = 0x01,
        ABM_QUERYPOS = 0x02,
        ABM_SETPOS = 0x03,
        ABM_GETSTATE = 0x04,
        ABM_GETTASKBARPOS = 0x05,
        ABM_ACTIVATE = 0x06,
        ABM_GETAUTOHIDEBAR = 0x07,
        ABM_SETAUTOHIDEBAR = 0x08,
        ABM_WINDOWPOSCHANGED = 0x09,
        ABM_SETSTATE = 0x0a,
    }

    internal enum AppBarState
    {
        ABS_MANUAL = 0,
        ABS_AUTOHIDE = 1,
        ABS_ALWAYSONTOP = 2,
        ABS_AUTOHIDEANDONTOP = 3,
    }

    internal enum AppBarNotification
    {
        ABN_STATECHANGE = 0,
        ABN_POSCHANGED,
        ABN_FULLSCREENAPP,
        ABN_WINDOWARRANGE,
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct RECT
    {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public RECT(int left, int top, int right, int bottom)
        {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public static implicit operator RECT(Rectangle r)
        {
            return new RECT(r.Left, r.Top, r.Right, r.Bottom);
        }

        public static explicit operator Rectangle(RECT r)
        {
            return new Rectangle(r.left, r.top, r.right - r.left, r.bottom - r.top);
        }
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct POINT
    {
        public int X;
        public int Y;

        public POINT(int x, int y)
        {
            X = x;
            Y = y;
        }

        public static implicit operator Point(POINT p)
        {
            return new Point(p.X, p.Y);
        }

        public static implicit operator POINT(Point p)
        {
            return new POINT(p.X, p.Y);
        }
    }

    public static class NativeMethods
    {
        [DllImport("shell32.dll", CallingConvention = CallingConvention.StdCall)]
        public static extern uint SHAppBarMessage(AppBarMessage dwMessage, ref APPBARDATA pData);

        [DllImport("user32.dll", ExactSpelling = true)]
        public static extern bool MoveWindow(IntPtr hWnd, int x, int y, int nWidth, int nHeight, bool bRepaint);

        [DllImport("user32.dll")]
        public static extern uint RegisterWindowMessage([MarshalAs(UnmanagedType.LPTStr)] string lpString);

        [DllImport("user32.dll", EntryPoint = "ReleaseCapture")]
        public static extern bool StopMouseCapture();

        [DllImport("user32.dll", EntryPoint = "SetCapture")]
        public static extern IntPtr StartMouseCapture(IntPtr hWnd);

        [DllImport("user32.dll", EntryPoint = "GetCapture")]
        public static extern IntPtr GetMouseCapture();

        [DllImport("user32.dll")]
        public static extern bool DragDetect(IntPtr hwnd, POINT pt);

        [DllImport("user32.dll")]
        public static extern void SendMessage(IntPtr hWnd, int msg, int wParam, int lParam);

        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        public static extern Int32 SendMessage(IntPtr hWnd, int msg, int wParam,
            [MarshalAs(UnmanagedType.LPWStr)] string lParam);

        [DllImport("user32.dll", EntryPoint = "ShowCaret")]
        public static extern long ShowCaret(IntPtr hwnd);

        [DllImport("user32.dll", EntryPoint = "HideCaret")]
        public static extern long HideCaret(IntPtr hwnd);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern int IsValidLocale(int locale, int dwFlags);
    }
}