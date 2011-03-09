// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 
using System;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Text;

namespace Ch.Cyberduck.Ui.Winforms.Commondialog
{
    internal static class InteropUtil
    {
        public delegate int SUBCLASSPROC
            (
            IntPtr hWnd,
            uint uMsg,
            IntPtr wParam,
            IntPtr lParam,
            IntPtr uIdSubclass,
            uint dwRefData
            );

        public delegate IntPtr WndProc(IntPtr hWnd, int msg, IntPtr wParam, IntPtr lParam);

        public const int BN_CLICKED = 0;
        public const uint CDM_FIRST = WM_USER + 100;
        public const uint CDM_GETFILEPATH = CDM_FIRST + 0x0001;
        public const uint CDM_GETFOLDERPATH = CDM_FIRST + 0x0002;
        public const uint CDM_HIDECONTROL = CDM_FIRST + 0x0005;
        public const uint CDN_FILEOK = (CDN_FIRST - 5);
        public const uint CDN_FIRST = unchecked(0u - 601u);
        public const uint CDN_FOLDERCHANGE = (CDN_FIRST - 2);
        public const uint CDN_INITDONE = (CDN_FIRST - 0);
        public const int DWL_MSGRESULT = 0;
        public const int IDCANCEL = 2;
        public const int IDD_CustomOpenDialog = 101;
        public const int IDOK = 1;
        public const int ID_CUSTOM_CANCEL = 1002;
        public const int ID_FileList = lst2;
        public const int ID_FileNameCombo = cmb13;
        public const int ID_FileNameLabel = stc3;
        public const int ID_FileNameTextBox = edt1;
        public const int ID_FilterCombo = cmb1;
        public const int ID_FilterLabel = stc2;
        public const int ID_SELECT = 1001;
        public const int LVIF_TEXT = 0x00000001;
        public const int LVIS_SELECTED = 0x0002;
        public const uint LVM_FIRST = 0x1000;
        public const uint LVM_GETITEMTEXT = LVM_FIRST + 115;
        public const uint LVM_GETNEXTITEM = LVM_FIRST + 12;
        public const uint LVM_GETNEXTITEMINDEX = LVM_FIRST + 211;
        public const uint LVM_GETSELECTEDCOUNT = LVM_FIRST + 50; //yla extension
        public const int LVNI_SELECTED = 0x0002;
        public const uint LVN_FIRST = unchecked(0U - 100U);
        public const uint LVN_ITEMCHANGED = (LVN_FIRST - 1);
        public const int NumberOfFileChars = 8192;
        public const int OFN_ALLOWMULTISELECT = 0x00000200;
        public const int OFN_DONTADDTORECENT = 0x02000000;
        public const int OFN_ENABLEHOOK = 0x00000020;
        public const int OFN_ENABLESIZING = 0x00800000;
        public const int OFN_ENABLETEMPLATE = 0x00000040;
        public const int OFN_EXPLORER = 0x00080000;
        public const int OFN_FILEMUSTEXIST = 0x00001000;
        public const int OFN_HIDEREADONLY = 0x00000004;
        public const int OFN_NODEREFERENCELINKS = 0x00100000;
        public const int OFN_NOTESTFILECREATE = 0x00010000;
        public const int OFN_PATHMUSTEXIST = 0x00000800;
        public const uint WM_CLOSE = 0x0010;
        public const uint WM_COMMAND = 0x0111;
        public const uint WM_CREATE = 0x0001;
        public const uint WM_GETFONT = 0x0031;
        public const uint WM_INITDIALOG = 0x0110;
        public const uint WM_NOTIFY = 0x4E;
        public const uint WM_PARENTNOTIFY = 0x0210;
        public const uint WM_SETFONT = 0x0030;
        public const uint WM_SETTEXT = 0x000C;
        public const uint WM_SIZE = 0x0005;
        public const uint WM_USER = 0x0400;
        public const int cmb1 = 0x0470;
        public const int cmb13 = 0x047c;
        public const int edt1 = 0x0480;
        public const int lst2 = 0x0461;
        public const int stc2 = 0x0441;
        public const int stc3 = 0x0442;

        [DllImport("comctl32.dll", EntryPoint = "SetWindowSubclass")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetWindowSubclass
            (IntPtr hWnd,
             SUBCLASSPROC pfnSubclass,
             uint uIdSubclass,
             uint dwRefData
            );


        [DllImport("comctl32.dll", EntryPoint = "DefSubclassProc")]
        public static extern int DefSubclassProc
            (IntPtr hWnd,
             uint uMsg,
             IntPtr wParam,
             IntPtr lParam
            );

        [DllImport("comdlg32.dll", EntryPoint = "GetOpenFileNameW")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool GetOpenFileNameW(ref OpenFileName openFileNameInfo);

        [DllImport("kernel32.dll", EntryPoint = "GetLastError")]
        public static extern uint GetLastError();

        [DllImport("user32.dll", EntryPoint = "SendMessageW", CharSet=CharSet.Unicode)]
        public static extern uint SendMessage
            (
            [In] IntPtr hWnd,
            uint Msg,
            uint wParam,
            uint lParam
            );

        [DllImport("user32.dll", EntryPoint = "SendMessageW", CharSet=CharSet.Unicode)]
        public static extern int SendMessageInt
            (
            [In] IntPtr hWnd,
            uint Msg,
            int wParam,
            int lParam
            );

        [DllImport("user32.dll", EntryPoint = "SendMessageW")]
        public static extern uint SendListViewMessage
            (
            [In] IntPtr hWnd,
            uint Msg,
            uint wParam,
            ref LVITEM lParam
            );

        [DllImport("user32.dll", EntryPoint = "SendMessageW")]
        public static extern uint SendMessageString
            (
            [In] IntPtr hWnd,
            uint Msg,
            uint wParam,
            [MarshalAs(UnmanagedType.LPWStr)] string str
            );

        [DllImport("user32.dll", EntryPoint = "GetDlgItem")]
        public static extern IntPtr GetDlgItem
            (
            [In] IntPtr hDlg,
            int nIDDlgItem
            );

        [DllImport("user32.dll", EntryPoint = "GetWindowTextW")]
        public static extern int GetWindowTextW
            (
            [In] IntPtr hWnd,
            [
                Out,
                MarshalAs(UnmanagedType.LPWStr)
            ] StringBuilder lpString,
            int nMaxCount
            );

        public static string GetWindowTextW(IntPtr hWnd)
        {
            var builder = new StringBuilder(NumberOfFileChars);
            if (GetWindowTextW(hWnd, builder, builder.Capacity) > 0)
            {
                return builder.ToString();
            }
            return "";
        }

        [DllImport("user32.dll", EntryPoint = "GetParent")]
        public static extern IntPtr GetParent([In] IntPtr hWnd);

        [DllImport("user32.dll", EntryPoint = "EnableWindow")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool EnableWindow([In] IntPtr hWnd, [MarshalAs(UnmanagedType.Bool)] bool bEnable);

        [DllImport("user32.dll", EntryPoint = "SetWindowLongW")]
        public static extern int SetWindowLongW
            (
            [In] IntPtr hWnd,
            int nIndex,
            int dwNewLong
            );

        [DllImport("user32.dll", EntryPoint = "SetWindowTextW")]
        public static extern bool SetWindowTextW
            (
            [In] IntPtr hWnd,
            [In] [MarshalAs(UnmanagedType.LPWStr)] string lpString
            );

        [DllImport("user32.dll", EntryPoint = "GetFocus")]
        public static extern IntPtr GetFocus();

        [DllImport("user32.dll", EntryPoint = "GetDlgCtrlID")]
        public static extern int GetDlgCtrlID([In] IntPtr hWnd);

        [DllImport("user32.dll", EntryPoint = "SetFocus")]
        public static extern IntPtr SetFocus([In] IntPtr hWnd);

        [DllImport("user32.dll", EntryPoint = "SetWindowPlacement")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetWindowPlacement
            (
            [In] IntPtr hWnd,
            [In] ref WINDOWPLACEMENT lpwndpl
            );

        [DllImport("user32.dll", EntryPoint = "InvalidateRect")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool InvalidateRect
            (
            [In] IntPtr hWnd,
            IntPtr lpRect,
            [MarshalAs(UnmanagedType.Bool)] bool bErase
            );


        public static void LoadFontFrom(IntPtr hWndDest, IntPtr hWndSrc)
        {
            AssumeNonZero(hWndDest);
            AssumeNonZero(hWndSrc);

            var hFont = (IntPtr) unchecked((int) SendMessage(hWndSrc, WM_GETFONT, 0, 0));
            AssumeNonZero(hFont);

            SendMessage(hWndDest, WM_SETFONT, (uint) hFont, 0);
        }

        public static WINDOWPLACEMENT GetWindowPlacement(IntPtr hwnd)
        {
            WINDOWPLACEMENT ret = WINDOWPLACEMENT.New();

            if (!GetWindowPlacement(hwnd, ref ret))
            {
                CheckForWin32Error();
            }
            return ret;
        }

        [DllImport("user32.dll", EntryPoint = "GetWindowPlacement")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool GetWindowPlacement
            (
            [In] IntPtr hWnd,
            ref WINDOWPLACEMENT lpwndpl
            );

        public static void CheckForWin32Error()
        {
            var error = GetLastError();
            if (error != 0)
            {
                throw new Win32Exception(unchecked((int) error));
            }
        }

        [DllImport("gdi32.dll", EntryPoint = "CreateRectRgnIndirect")]
        public static extern IntPtr CreateRectRgnIndirect
            (
            [In] ref RECT lpRect
            );

        [DllImport("gdi32.dll", EntryPoint = "DeleteObject")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool DeleteObject
            (
            [In] IntPtr hObject
            );

        [DllImport("user32.dll", EntryPoint = "SetWindowRgn")]
        public static extern int SetWindowRgn
            (
            [In] IntPtr hWnd,
            [In] IntPtr hRgn,
            [MarshalAs(UnmanagedType.Bool)] bool bRedraw
            );

        //What follows here are a bunch of magic constants needed for
        //windows message passing. One could make an argument that it would be
        //more "C#ish" to package these all up into enum types, and then to
        //create appropriate overloads of the "extern" functions that take in 
        //enum types.
        //
        //However, I wanted to keep the calls to API functions as close to
        //the documentation as possible. That way if you look at the stuff and wonder 
        //"what the hell is this" you can just look it up in MSDN and get an answer. 
        //I did make some things extension methods, to improve readability, but otherwise 
        //the code pretty much looks like it would if it were written in C. 

        //===========================================================
        //Open File Name flag constants

        public static void Assume
            (
            bool condition,
            string message
            )
        {
            if (!condition)
            {
                throw new InternalErrorException(message);
            }
        }


        public static T AssumeNotNull<T>
            (T item
            ) where T : class
        {
            Assume(item != null, "Unexpected null value!");
            return item;
        }

        public static void Assume
            (
            bool condition
            )
        {
            Assume(condition, "The condition should not be false!");
        }

        public static IntPtr AssumeNonZero
            (IntPtr item
            )
        {
            Assume(item != IntPtr.Zero);
            return item;
        }

        public static bool PathContains(string parent, string child)
        {
            AssumeNotNull(parent);
            AssumeNotNull(child);

            if (!parent.EndsWith("\\"))
            {
                parent = parent + "\\";
            }

            return
                child.StartsWith(parent, StringComparison.OrdinalIgnoreCase) ||
                parent.TrimEnd('\\').Equals(child, StringComparison.OrdinalIgnoreCase);
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct LVITEM
        {
            public uint mask;
            public int iItem;
            public int iSubItem;
            public uint state;
            public uint stateMask;
            public IntPtr pszText;
            public int cchTextMax;
            public int iImage;
            public int lParam;
            public int iIndent;
            public int iGroupId;
            public uint cColumns;
            public IntPtr puColumns;
            public IntPtr piColFmt;
            public int iGroup;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct NMHDR
        {
            public IntPtr hwndFrom;
            public uint idFrom;
            public uint code;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct NMLISTVIEW
        {
            public NMHDR hdr;
            public int iItem;
            public int iSubItem;
            public uint uNewState;
            public uint uOldState;
            public uint uChanged;
            public Point ptAction;
            public int lParam;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct OFNOTIFY
        {
            public IntPtr hdr_hwndFrom;
            public IntPtr hdr_idFrom;
            public uint hdr_code;
            public IntPtr lpOFN;
            public IntPtr pszFile;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct OpenFileName
        {
            public int lStructSize;
            public IntPtr hwndOwner;
            public IntPtr hInstance;
            public IntPtr lpstrFilter;
            public IntPtr lpstrCustomFilter;
            public int nMaxCustFilter;
            public int nFilterIndex;
            public IntPtr lpstrFile;
            public int nMaxFile;
            public IntPtr lpstrFileTitle;
            public int nMaxFileTitle;
            [MarshalAs(UnmanagedType.LPWStr)] public string lpstrInitialDir;
            [MarshalAs(UnmanagedType.LPWStr)] public string lpstrTitle;
            public int Flags;
            public short nFileOffset;
            public short nFileExtension;
            public string lpstrDefExt;
            public IntPtr lCustData;
            public WndProc lpfnHook;
            public int templateID;
            public IntPtr pvReserved;
            public int dwReserved;
            public int FlagsEx;

            public void Initialize()
            {
                lStructSize = Marshal.SizeOf(typeof (OpenFileName));
                lpstrCustomFilter = IntPtr.Zero;
                lpstrFileTitle = IntPtr.Zero;
                lCustData = IntPtr.Zero;
                pvReserved = IntPtr.Zero;
            }
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct Point
        {
            public int x;
            public int y;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct RECT
        {
            public int left;
            public int top;
            public int right;
            public int bottom;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct WINDOWPLACEMENT
        {
            public static WINDOWPLACEMENT New()
            {
                return
                    new WINDOWPLACEMENT
                        {
                            length = ((uint) Marshal.SizeOf(typeof (WINDOWPLACEMENT)))
                        };
            }

            public uint length;
            public uint flags;
            public uint showCmd;
            public Point ptMinPosition;
            public Point ptMaxPosition;
            public RECT rcNormalPosition;

            public int Left
            {
                get { return rcNormalPosition.left; }
                set { rcNormalPosition.left = value; }
            }

            public int Top
            {
                get { return rcNormalPosition.top; }
                set { rcNormalPosition.top = value; }
            }

            public int Right
            {
                get { return rcNormalPosition.right; }
                set { rcNormalPosition.right = value; }
            }

            public int Bottom
            {
                get { return rcNormalPosition.bottom; }
                set { rcNormalPosition.bottom = value; }
            }
        }
    }
}

// ReSharper restore InconsistentNaming
// ReSharper restore MemberCanBePrivate.Global