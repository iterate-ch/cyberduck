using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    using static Constants;

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
