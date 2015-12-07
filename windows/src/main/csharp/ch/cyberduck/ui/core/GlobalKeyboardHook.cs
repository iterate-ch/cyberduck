// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Core
{
    /// <summary>
    /// Helper class for global (system-wide) keyboard hooks.
    /// </summary>        
    public class GlobalKeyboardHook
    {
        private const byte VK_CAPITAL = 0x14;
        private const byte VK_LALT = 0xA4;
        private const byte VK_LCONTROL = 0xA2;
        private const byte VK_LSHIFT = 0xA0;
        private const byte VK_NUMLOCK = 0x90;
        private const byte VK_RALT = 0xA5;
        private const byte VK_RCONTROL = 0x3;
        private const byte VK_RSHIFT = 0xA1;
        private const byte VK_SHIFT = 0x10;
        private const int WH_KEYBOARD = 2;
        private const int WH_KEYBOARD_LL = 13;
        private const int WM_KEYDOWN = 0x100;
        private const int WM_KEYUP = 0x101;
        private const int WM_SYSKEYDOWN = 0x104;
        private const int WM_SYSKEYUP = 0x105;
        // const byte LLKHF_ALTDOWN = 0x20; // not used

        /// <summary>
        /// Value indicating if hook is active.
        /// </summary>
        private bool m_bHookActive;

        /// <summary>
        /// Stored reference to the HookProc delegate (to prevent delegate from beeing collected by GC!)
        /// </summary>
        protected HookProc m_hookproc;

        /// <summary>
        /// Stored hook handle returned by SetWindowsHookEx
        /// </summary>
        private int m_iHandleToHook;

        /// <summary>
        /// Gets a value indicating if hook is active.
        /// </summary>
        public bool HookActive
        {
            get { return m_bHookActive; }
        }

        /// <summary>
        /// Occurs when a key is pressed.
        /// </summary>
        public event KeyEventHandler KeyDown;

        /// <summary>
        /// Occurs when a key is released.
        /// </summary>
        public event KeyEventHandler KeyUp;

        /// <summary>
        /// Occurs when a character key is pressed.
        /// </summary>
        public event KeyPressEventHandler KeyPress;

        /// <summary>
        /// Dtor.
        /// </summary>
        ~GlobalKeyboardHook()
        {
            Unhook();
        }

        /// <summary>
        /// Install the global hook. 
        /// </summary>
        /// <returns> True if hook was successful, otherwise false. </returns>
        public bool Hook()
        {
            if (!m_bHookActive)
            {
                m_hookproc = new HookProc(HookCallbackProcedure);

                IntPtr hInstance = GetModuleHandle(Process.GetCurrentProcess().MainModule.ModuleName);
                m_iHandleToHook = SetWindowsHookEx(WH_KEYBOARD_LL, m_hookproc, hInstance, 0);

                if (m_iHandleToHook != 0)
                {
                    m_bHookActive = true;
                }
            }
            return m_bHookActive;
        }

        /// <summary>
        /// Uninstall the global hook.
        /// </summary>
        public void Unhook()
        {
            if (m_bHookActive)
            {
                UnhookWindowsHookEx(m_iHandleToHook);
                m_bHookActive = false;
            }
        }

        /// <summary>
        /// Raises the KeyDown event.
        /// </summary>
        /// <param name="kea"> KeyEventArgs </param>
        protected virtual void OnKeyDown(KeyEventArgs kea)
        {
            if (KeyDown != null)
                KeyDown(this, kea);
        }

        /// <summary>
        /// Raises the KeyUp event.
        /// </summary>
        /// <param name="kea"> KeyEventArgs </param>
        protected virtual void OnKeyUp(KeyEventArgs kea)
        {
            if (KeyUp != null)
                KeyUp(this, kea);
        }

        /// <summary>
        /// Raises the KeyPress event.
        /// </summary>
        /// <param name="kea"> KeyEventArgs </param>
        protected virtual void OnKeyPress(KeyPressEventArgs kpea)
        {
            if (KeyPress != null)
                KeyPress(this, kpea);
        }

        /// <summary>
        /// Called when hook is active and a key was pressed.
        /// </summary>
        private int HookCallbackProcedure(int nCode, int wParam, IntPtr lParam)
        {
            bool bHandled = false;

            if (nCode > -1 && (KeyDown != null || KeyUp != null || KeyPress != null))
            {
                // Get keyboard data
                KeyboardHookStruct khs =
                    (KeyboardHookStruct) Marshal.PtrToStructure(lParam, typeof (KeyboardHookStruct));

                // Get key states
                bool bControl = ((GetKeyState(VK_LCONTROL) & 0x80) != 0) || ((GetKeyState(VK_RCONTROL) & 0x80) != 0);
                bool bShift = ((GetKeyState(VK_LSHIFT) & 0x80) != 0) || ((GetKeyState(VK_RSHIFT) & 0x80) != 0);
                bool bAlt = ((GetKeyState(VK_LALT) & 0x80) != 0) || ((GetKeyState(VK_RALT) & 0x80) != 0);
                bool bCapslock = (GetKeyState(VK_CAPITAL) != 0);

                // Create KeyEventArgs 
                KeyEventArgs kea =
                    new KeyEventArgs(
                        (Keys)
                            (khs.vkCode | (bControl ? (int) Keys.Control : 0) | (bShift ? (int) Keys.Shift : 0) |
                             (bAlt ? (int) Keys.Alt : 0)));

                // Raise KeyDown/KeyUp events
                if (wParam == WM_KEYDOWN || wParam == WM_SYSKEYDOWN)
                {
                    OnKeyDown(kea);
                    bHandled = kea.Handled;
                }
                else if (wParam == WM_KEYUP || wParam == WM_SYSKEYUP)
                {
                    OnKeyUp(kea);
                    bHandled = kea.Handled;
                }

                // Raise KeyPress event
                if (wParam == WM_KEYDOWN && !bHandled && !kea.SuppressKeyPress)
                {
                    byte[] abyKeyState = new byte[256];
                    byte[] abyInBuffer = new byte[2];
                    GetKeyboardState(abyKeyState);

                    if (ToAscii(khs.vkCode, khs.scanCode, abyKeyState, abyInBuffer, khs.flags) == 1)
                    {
                        char chKey = (char) abyInBuffer[0];
                        if ((bCapslock ^ bShift) && Char.IsLetter(chKey))
                            chKey = Char.ToUpper(chKey);
                        KeyPressEventArgs kpea = new KeyPressEventArgs(chKey);
                        OnKeyPress(kpea);
                        bHandled = kea.Handled;
                    }
                }
            }

            if (bHandled)
                return 1;
            else
                return CallNextHookEx(m_iHandleToHook, nCode, wParam, lParam);
        }

        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall,
            SetLastError = true)]
        private static extern int SetWindowsHookEx(int idHook, HookProc lpfn, IntPtr hMod, int dwThreadId);

        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall,
            SetLastError = true)]
        private static extern int UnhookWindowsHookEx(int idHook);

        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        private static extern int CallNextHookEx(int idHook, int nCode, int wParam, IntPtr lParam);

        [DllImport("user32.dll")]
        private static extern int GetKeyboardState(byte[] pbKeyState);

        [DllImport("user32.dll", CharSet = CharSet.Auto, CallingConvention = CallingConvention.StdCall)]
        private static extern short GetKeyState(int vKey);

        [DllImport("user32.dll")]
        private static extern int ToAscii(int uVirtKey, int uScanCode, byte[] lpbKeyState, byte[] lpwTransKey,
            int fuState);

        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        public static extern IntPtr GetModuleHandle(string lpModuleName);

        /// <summary>
        /// Represents the method called when a hook catches a monitored event.
        protected delegate int HookProc(int nCode, int wParam, IntPtr lParam);

        /// <summary>
        /// Marshalling of the Windows-API KBDLLHOOKSTRUCT structure.#
        /// Contains information about a low-level keyboard input event.
        /// This is named "struct" to be consistent with the Windows API name,
        /// but it must be a class since it is passed as a pointer in SetWindowsHookEx.
        /// </summary>
        [StructLayout(LayoutKind.Sequential)]
        private class KeyboardHookStruct
        {
            public int vkCode;
            public int scanCode;
            public int flags;
            public int time;
            public int dwExtraInfo;
        }
    }
}