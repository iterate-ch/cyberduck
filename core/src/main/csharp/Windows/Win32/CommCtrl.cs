using System;

namespace Windows.Win32
{
    using static UI.Controls.TASKDIALOG_COMMON_BUTTON_FLAGS;
    using static Constants;

    [Flags]
    public enum TaskDialogCommonButtons
    {
        None = 0,
        OK = TDCBF_OK_BUTTON,
        Yes = TDCBF_YES_BUTTON,
        No = TDCBF_NO_BUTTON,
        Cancel = TDCBF_CANCEL_BUTTON,
        Retry = TDCBF_RETRY_BUTTON,
        Close = TDCBF_CLOSE_BUTTON,
        YesNo = Yes | No,
        YesNoCancel = Yes | No | Cancel,
        OKCancel = OK | Cancel,
        RetryCancel = Retry | Cancel
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

    public enum TaskDialogProgressBarState : uint
    {
        /// <inheritdoc cref="PBST_NORMAL"/>
        Normal = PBST_NORMAL,

        /// <inheritdoc cref="PBST_ERROR"/>
        Error = PBST_ERROR,

        /// <inheritdoc cref="PBST_PAUSE"/>
        Paused = PBST_PAUSED
    }
}
