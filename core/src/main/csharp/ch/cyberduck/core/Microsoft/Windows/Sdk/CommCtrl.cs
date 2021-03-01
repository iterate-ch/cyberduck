using System;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    using static Constants;

    [UnmanagedFunctionPointer(CallingConvention.Winapi)]
    public delegate HRESULT PFTASKDIALOGCALLBACK(HWND hwnd, uint msg, WPARAM wParam, LPARAM lParam, nint lpRefData);

    [Flags]
    public enum TaskDialogCommonButtons
    {
        None = 0,
        OK = 0x0001,
        Yes = 0x0002,
        No = 0x0004,
        Cancel = 0x0008,
        Retry = 0x0010,
        Close = 0x0020,
        YesNo = Yes | No,
        YesNoCancel = Yes | No | Cancel,
        OKCancel = OK | Cancel,
        RetryCancel = Retry | Cancel
    }

    [Flags]
    public enum TaskDialogFlags : uint
    {
        AllowDialogCancellation = TDF_ALLOW_DIALOG_CANCELLATION,
        CallbackTimer = TDF_CALLBACK_TIMER,
        CanBeMinimized = TDF_CAN_BE_MINIMIZED,
        EnableHyperlinks = TDF_ENABLE_HYPERLINKS,
        ExpandFooterArea = TDF_EXPAND_FOOTER_AREA,
        ExpandedByDefault = TDF_EXPANDED_BY_DEFAULT,
        NoDefaultRadioButton = TDF_NO_DEFAULT_RADIO_BUTTON,
        PositionRelativeToWindow = TDF_POSITION_RELATIVE_TO_WINDOW,
        RTLLayout = TDF_RTL_LAYOUT,
        ShowMarqueeProgressBar = TDF_SHOW_MARQUEE_PROGRESS_BAR,
        ShowProgressBar = TDF_SHOW_PROGRESS_BAR,
        UseCommandLinks = TDF_USE_COMMAND_LINKS,
        UseCommandLinksNoIcon = TDF_USE_COMMAND_LINKS_NO_ICON,
        UseHIconFooter = TDF_USE_HICON_FOOTER,
        UseHIconMain = TDF_USE_HICON_MAIN,
        VerificationFlagChecked = TDF_VERIFICATION_FLAG_CHECKED
    }

    public enum TaskDialogIcon : uint
    {
        None = 0,
        Warning = 0xFFFF, // MAKEINTRESOURCEW(-1)
        Error = 0xFFFE, // MAKEINTRESOURCEW(-2)
        Information = 0xFFFD, // MAKEINTRESOURCEW(-3)
        Shield = 0xFFFC, // MAKEINTRESOURCEW(-4)
        Question = 104
    }

    public enum TaskDialogNotification
    {
        /// <inheritdoc cref="TDN_CREATED"/>
        Created = TDN_CREATED,
        /// <inheritdoc cref="TDN_BUTTON_CLICKED"/>
        ButtonClicked = TDN_BUTTON_CLICKED,
        /// <inheritdoc cref="TDN_HYPERLINK_CLICKED"/>
        HyperlinkClicked = TDN_HYPERLINK_CLICKED,
        /// <inheritdoc cref="TDN_TIMER"/>
        Timer = TDN_TIMER,
        /// <inheritdoc cref="TDN_DESTROYED"/>
        Destroyed = TDN_DESTROYED,
        /// <inheritdoc cref="TDN_RADIO_BUTTON_CLICKED"/>
        RadioButtonClicked = TDN_RADIO_BUTTON_CLICKED,
        /// <inheritdoc cref="TDN_DIALOG_CONSTRUCTED"/>
        DialogConstructed = TDN_DIALOG_CONSTRUCTED,
        /// <inheritdoc cref="TDN_VERIFICATION_CLICKED"/>
        VerificationClicked = TDN_VERIFICATION_CLICKED,
        /// <inheritdoc cref="TDN_HELP"/>
        Help = TDN_HELP,
        /// <inheritdoc cref="TDN_EXPANDO_BUTTON_CLICKED"/>
        ExpandoButtonClicked = TDN_EXPANDO_BUTTON_CLICKED
    }

    public enum TaskDialogProgressBarState
    {
        /// <inheritdoc cref="PBST_NORMAL"/>
        Normal = PBST_NORMAL,
        /// <inheritdoc cref="PBST_ERROR"/>
        Error = PBST_ERROR,
        /// <inheritdoc cref="PBST_PAUSE"/>
        Paused = PBST_PAUSE
    }

    [StructLayout(LayoutKind.Sequential, Pack = 1)]
    public partial struct TASKDIALOGCONFIG
    {
        public uint cbSize;
        public HWND hwndParent;
        public HINSTANCE hInstance;
        public int dwFlags;
        public int dwCommonButtons;
        public PWSTR pszWindowTitle;
        public _Anonymous1_e__Union Anonymous1;
        public PWSTR pszMainInstruction;
        public PWSTR pszContent;
        public uint cButtons;
        public unsafe TASKDIALOG_BUTTON* pButtons;
        public int nDefaultButton;
        public uint cRadioButtons;
        public unsafe TASKDIALOG_BUTTON* pRadioButtons;
        public int nDefaultRadioButton;
        public PWSTR pszVerificationText;
        public PWSTR pszExpandedInformation;
        public PWSTR pszExpandedControlText;
        public PWSTR pszCollapsedControlText;
        public _Anonymous2_e__Union Anonymous2;
        public PWSTR pszFooter;
        public nint pfCallback;
        public nint lpCallbackData;
        public uint cxWidth;

        [StructLayout(LayoutKind.Explicit, Pack = 1)]
        public partial struct _Anonymous1_e__Union
        {
            [FieldOffset(0)]
            public HICON hMainIcon;
            [FieldOffset(0)]
            public PWSTR pszMainIcon;
        }

        [StructLayout(LayoutKind.Explicit, Pack = 1)]
        public partial struct _Anonymous2_e__Union
        {
            [FieldOffset(0)]
            public HICON hFooterIcon;
            [FieldOffset(0)]
            public PWSTR pszFooterIcon;
        }
    }

    partial class Constants
    {
        public const int PBST_ERROR = 0x0003;
        public const int PBST_NORMAL = 0x0001;
        public const int PBST_PAUSE = 0x0002;
        public const int TDE_CONTENT = 0;
        public const int TDE_EXPANDED_INFORMATION = 1;
        public const int TDE_FOOTER = 2;
        public const int TDE_MAIN_INSTRUCTION = 3;
        public const uint TDF_ALLOW_DIALOG_CANCELLATION = 0x0008;
        public const uint TDF_CALLBACK_TIMER = 0x0800;
        public const uint TDF_CAN_BE_MINIMIZED = 0x8000;
        public const uint TDF_ENABLE_HYPERLINKS = 0x0001;
        public const uint TDF_EXPAND_FOOTER_AREA = 0x0040;
        public const uint TDF_EXPANDED_BY_DEFAULT = 0x0080;
        public const uint TDF_NO_DEFAULT_RADIO_BUTTON = 0x4000;
        public const uint TDF_POSITION_RELATIVE_TO_WINDOW = 0x1000;
        public const uint TDF_RTL_LAYOUT = 0x2000;
        public const uint TDF_SHOW_MARQUEE_PROGRESS_BAR = 0x0400;
        public const uint TDF_SHOW_PROGRESS_BAR = 0x0200;
        public const uint TDF_USE_COMMAND_LINKS = 0x0010;
        public const uint TDF_USE_COMMAND_LINKS_NO_ICON = 0x0020;
        public const uint TDF_USE_HICON_FOOTER = 0x0004;
        public const uint TDF_USE_HICON_MAIN = 0x0002;
        public const uint TDF_VERIFICATION_FLAG_CHECKED = 0x0100;
        public const int TDIE_ICON_FOOTER = 1;
        public const int TDIE_ICON_MAIN = 0;
        public const uint TDM_CLICK_BUTTON = WM_USER + 102;
        public const uint TDM_CLICK_RADIO_BUTTON = WM_USER + 110;
        public const uint TDM_CLICK_VERIFICATION = WM_USER + 113;
        public const uint TDM_ENABLE_BUTTON = WM_USER + 111;
        public const uint TDM_ENABLE_RADIO_BUTTON = WM_USER + 112;
        public const uint TDM_SET_BUTTON_ELEVATION_REQUIRED_STATE = WM_USER + 115;
        public const uint TDM_SET_ELEMENT_TEXT = WM_USER + 108;
        public const uint TDM_SET_MARQUEE_PROGRESS_BAR = WM_USER + 103;
        public const uint TDM_SET_PROGRESS_BAR_MARQUEE = WM_USER + 107;
        public const uint TDM_SET_PROGRESS_BAR_POS = WM_USER + 106;
        public const uint TDM_SET_PROGRESS_BAR_RANGE = WM_USER + 105;
        public const uint TDM_SET_PROGRESS_BAR_STATE = WM_USER + 104;
        public const uint TDM_UPDATE_ELEMENT_TEXT = WM_USER + 114;
        public const uint TDM_UPDATE_ICON = WM_USER + 116;
        public const int TDN_BUTTON_CLICKED = 2;
        public const int TDN_CREATED = 0;
        public const int TDN_DESTROYED = 5;
        public const int TDN_DIALOG_CONSTRUCTED = 7;
        public const int TDN_EXPANDO_BUTTON_CLICKED = 10;
        public const int TDN_HELP = 9;
        public const int TDN_HYPERLINK_CLICKED = 3;
        public const int TDN_NAVIGATED = 1;
        public const int TDN_RADIO_BUTTON_CLICKED = 6;
        public const int TDN_TIMER = 4;
        public const int TDN_VERIFICATION_CLICKED = 8;
    }

    partial class PInvoke
    {
        public static unsafe HRESULT TaskDialogIndirect(in TASKDIALOGCONFIG pTaskConfig, int* pnButton, int* pnRadioButton, bool* pfVerificationFlagChecked)
        {
            fixed (TASKDIALOGCONFIG* pTaskConfigLocal = &pTaskConfig)
            {
                HRESULT __result = TaskDialogIndirect(pTaskConfigLocal, pnButton, pnRadioButton, pfVerificationFlagChecked);
                return __result;
            }
        }

        [DllImport("ComCtl32", ExactSpelling = true)]
        public static extern unsafe HRESULT TaskDialogIndirect([In] TASKDIALOGCONFIG* pTaskConfig, [Out, Optional] int* pnButton, [Out, Optional] int* pnRadioButton, [Out, Optional] bool* pfVerificationFlagChecked);
    }
}
