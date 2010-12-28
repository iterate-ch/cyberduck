using System;
using System.Collections.Generic;
using System.Windows.Forms;
using ch.cyberduck.core.i18n;

namespace Ch.Cyberduck.Ui.Winforms.Taskdialog
{
    //--------------------------------------------------------------------------------

    #region PUBLIC enums

    //--------------------------------------------------------------------------------
    public enum SysIcons
    {
        Information,
        Question,
        Warning,
        Error
    } ;

    public enum TaskDialogButtons
    {
        YesNo,
        YesNoCancel,
        OKCancel,
        OK,
        Close,
        Cancel,
        None
    }

    #endregion

    //--------------------------------------------------------------------------------
    public class TaskDialog
    {
        private int _commandButtonResult = -1;
        private int _radioButtonResult = -1;
        private int EmulatedFormWidth = 450;
        private EventHandler OnTaskDialogShown = null;
        private EventHandler OnTaskDialogClosed = null;
        public Help HelpDelegate { get; set; }
        private bool _verificationChecked;

        public bool VerificationChecked
        {
            get { return _verificationChecked; }
            set { _verificationChecked = value; }
        }

        public int CommandButtonResult
        {
            get { return _commandButtonResult; }
            set { _commandButtonResult = value; }
        }

        public int RadioButtonResult
        {
            get { return _radioButtonResult; }
            set { _radioButtonResult = value; }
        }

        public bool PlaySystemSounds { get; set; }

        public delegate void Help(string url);

        public TaskDialog()
        {
            PlaySystemSounds = true;
            VerificationChecked = false;
        }

        //--------------------------------------------------------------------------------

        #region ShowTaskDialogBox

        //--------------------------------------------------------------------------------
        public DialogResult ShowTaskDialogBox(IWin32Window Owner,
                                              string Title,
                                              string MainInstruction,
                                              string Content,
                                              string ExpandedInfo,
                                              string Footer,
                                              string VerificationText,
                                              string RadioButtons,
                                              string CommandButtons,
                                              TaskDialogButtons Buttons,
                                              SysIcons MainIcon,
                                              SysIcons FooterIcon,
                                              int DefaultIndex)

        {
            DialogResult result;
            if (OnTaskDialogShown != null)
                OnTaskDialogShown(null, EventArgs.Empty);

            if (VistaTaskDialog.IsAvailableOnThisOS)
            {
                // [OPTION 1] Show Vista TaskDialog
                VistaTaskDialog vtd = new VistaTaskDialog();

                vtd.WindowTitle = Title;
                vtd.MainInstruction = MainInstruction;
                vtd.Content = Content;
                vtd.ExpandedInformation = ExpandedInfo;
                vtd.Footer = Footer;

                // Radio Buttons
                if (RadioButtons != "")
                {
                    List<VistaTaskDialogButton> lst = new List<VistaTaskDialogButton>();
                    string[] arr = RadioButtons.Split(new char[] {'|'});
                    for (int i = 0; i < arr.Length; i++)
                    {
                        try
                        {
                            VistaTaskDialogButton button = new VistaTaskDialogButton();
                            button.ButtonId = 1000 + i;
                            button.ButtonText = arr[i];
                            lst.Add(button);
                        }
                        catch (FormatException)
                        {
                        }
                    }
                    vtd.RadioButtons = lst.ToArray();
                    vtd.NoDefaultRadioButton = (DefaultIndex == -1);
                    if (DefaultIndex >= 0)
                        vtd.DefaultRadioButton = DefaultIndex;
                }

                // Custom Buttons
                if (CommandButtons != "")
                {
                    List<VistaTaskDialogButton> lst = new List<VistaTaskDialogButton>();
                    string[] arr = CommandButtons.Split(new char[] {'|'});
                    for (int i = 0; i < arr.Length; i++)
                    {
                        try
                        {
                            VistaTaskDialogButton button = new VistaTaskDialogButton();
                            button.ButtonId = 2000 + i;
                            button.ButtonText = arr[i];
                            lst.Add(button);
                        }
                        catch (FormatException)
                        {
                        }
                    }
                    vtd.Buttons = lst.ToArray();
                    if (DefaultIndex >= 0)
                        vtd.DefaultButton = DefaultIndex;
                }

                switch (Buttons)
                {
                    case TaskDialogButtons.YesNo:
                        vtd.CommonButtons = VistaTaskDialogCommonButtons.Yes | VistaTaskDialogCommonButtons.No;
                        break;
                    case TaskDialogButtons.YesNoCancel:
                        vtd.CommonButtons = VistaTaskDialogCommonButtons.Yes | VistaTaskDialogCommonButtons.No |
                                            VistaTaskDialogCommonButtons.Cancel;
                        break;
                    case TaskDialogButtons.OKCancel:
                        vtd.CommonButtons = VistaTaskDialogCommonButtons.Ok | VistaTaskDialogCommonButtons.Cancel;
                        break;
                    case TaskDialogButtons.OK:
                        vtd.CommonButtons = VistaTaskDialogCommonButtons.Ok;
                        break;
                    case TaskDialogButtons.Close:
                        vtd.CommonButtons = VistaTaskDialogCommonButtons.Close;
                        break;
                    case TaskDialogButtons.Cancel:
                        vtd.CommonButtons = VistaTaskDialogCommonButtons.Cancel;
                        break;
                    default:
                        vtd.CommonButtons = 0;
                        break;
                }

                switch (MainIcon)
                {
                    case SysIcons.Information:
                        vtd.MainIcon = VistaTaskDialogIcon.Information;
                        break;
                    case SysIcons.Question:
                        vtd.MainIcon = VistaTaskDialogIcon.Question;
                        break;
                    case SysIcons.Warning:
                        vtd.MainIcon = VistaTaskDialogIcon.Warning;
                        break;
                    case SysIcons.Error:
                        vtd.MainIcon = VistaTaskDialogIcon.Error;
                        break;
                }

                switch (FooterIcon)
                {
                    case SysIcons.Information:
                        vtd.FooterIcon = VistaTaskDialogIcon.Information;
                        break;
                    case SysIcons.Question:
                        vtd.FooterIcon = VistaTaskDialogIcon.Question;
                        break;
                    case SysIcons.Warning:
                        vtd.FooterIcon = VistaTaskDialogIcon.Warning;
                        break;
                    case SysIcons.Error:
                        vtd.FooterIcon = VistaTaskDialogIcon.Error;
                        break;
                }

                vtd.EnableHyperlinks = true;
                vtd.ShowProgressBar = false;
                vtd.AllowDialogCancellation = (Buttons == TaskDialogButtons.Cancel ||
                                               Buttons == TaskDialogButtons.Close ||
                                               Buttons == TaskDialogButtons.OKCancel ||
                                               Buttons == TaskDialogButtons.YesNoCancel);
                vtd.CallbackTimer = false;
                vtd.ExpandedByDefault = false;
                vtd.ExpandFooterArea = false;
                vtd.PositionRelativeToWindow = true;
                vtd.RightToLeftLayout = false;
                vtd.NoDefaultRadioButton = false;
                vtd.CanBeMinimized = false;
                vtd.ShowMarqueeProgressBar = false;
                vtd.UseCommandLinks = (CommandButtons != "");
                vtd.UseCommandLinksNoIcon = false;
                vtd.VerificationText = VerificationText;
                vtd.VerificationFlagChecked = false;
                vtd.ExpandedControlText = Locale.localizedString("More Options", "Bookmark");
                vtd.CollapsedControlText = Locale.localizedString("More Options", "Bookmark");
                vtd.Callback =
                    delegate(VistaActiveTaskDialog taskDialog, VistaTaskDialogNotificationArgs args, object callbackData)
                        {
                            if (!String.IsNullOrEmpty(args.Hyperlink))
                            {
                                HelpDelegate(args.Hyperlink);
                            }
                            return false;
                        };

                // Show the Dialog
                result =
                    (DialogResult)
                    vtd.Show((vtd.CanBeMinimized ? null : Owner), out _verificationChecked, out _radioButtonResult);

                // if a command button was clicked, then change return result
                // to "DialogResult.OK" and set the CommandButtonResult
                if ((int) result >= 2000)
                {
                    CommandButtonResult = ((int) result - 2000);
                    result = DialogResult.OK;
                }
                if (RadioButtonResult >= 1000)
                    RadioButtonResult -= 1000; // deduct the ButtonID start value for radio buttons
            }
            else
            {
                // [OPTION 2] Show Emulated Form
                using (TaskDialogForm td = new TaskDialogForm())
                {
                    td.Title = Title;
                    td.MainInstruction = MainInstruction;
                    td.Content = Content;
                    td.ExpandedInfo = ExpandedInfo;
                    td.Footer = Footer;
                    td.RadioButtons = RadioButtons;
                    td.CommandButtons = CommandButtons;
                    td.PlaySystemSounds = PlaySystemSounds;
                    td.Buttons = Buttons;
                    td.MainIcon = MainIcon;
                    td.FooterIcon = FooterIcon;
                    td.VerificationText = VerificationText;
                    td.Width = EmulatedFormWidth;
                    td.DefaultButtonIndex = DefaultIndex;
                    td.BuildForm();
                    result = td.ShowDialog(Owner);

                    RadioButtonResult = td.RadioButtonIndex;
                    CommandButtonResult = td.CommandButtonClickedIndex;
                    VerificationChecked = td.VerificationCheckBoxChecked;
                }
            }
            if (OnTaskDialogClosed != null)
                OnTaskDialogClosed(null, EventArgs.Empty);
            return result;
        }

        //--------------------------------------------------------------------------------
        // Overloaded versions...
        //--------------------------------------------------------------------------------
        public DialogResult ShowTaskDialogBox(IWin32Window Owner,
                                              string Title,
                                              string MainInstruction,
                                              string Content,
                                              string ExpandedInfo,
                                              string Footer,
                                              string VerificationText,
                                              string RadioButtons,
                                              string CommandButtons,
                                              TaskDialogButtons Buttons,
                                              SysIcons MainIcon,
                                              SysIcons FooterIcon)
        {
            return ShowTaskDialogBox(Owner, Title, MainInstruction, Content, ExpandedInfo, Footer, VerificationText,
                                     RadioButtons, CommandButtons, Buttons, MainIcon, FooterIcon, 0);
        }

        public DialogResult ShowTaskDialogBox(string Title,
                                              string MainInstruction,
                                              string Content,
                                              string ExpandedInfo,
                                              string Footer,
                                              string VerificationText,
                                              string RadioButtons,
                                              string CommandButtons,
                                              TaskDialogButtons Buttons,
                                              SysIcons MainIcon,
                                              SysIcons FooterIcon)
        {
            return ShowTaskDialogBox(null, Title, MainInstruction, Content, ExpandedInfo, Footer, VerificationText,
                                     RadioButtons, CommandButtons, Buttons, MainIcon, FooterIcon, 0);
        }

        #endregion

        //--------------------------------------------------------------------------------

        #region MessageBox

        //--------------------------------------------------------------------------------
        public DialogResult MessageBox(IWin32Window Owner,
                                       string Title,
                                       string MainInstruction,
                                       string Content,
                                       string ExpandedInfo,
                                       string Footer,
                                       string VerificationText,
                                       TaskDialogButtons Buttons,
                                       SysIcons MainIcon,
                                       SysIcons FooterIcon)
        {
            return ShowTaskDialogBox(Owner, Title, MainInstruction, Content, ExpandedInfo, Footer, VerificationText, "",
                                     "", Buttons, MainIcon, FooterIcon);
        }

        //--------------------------------------------------------------------------------
        // Overloaded versions...
        //--------------------------------------------------------------------------------
        public DialogResult MessageBox(string Title,
                                       string MainInstruction,
                                       string Content,
                                       string ExpandedInfo,
                                       string Footer,
                                       string VerificationText,
                                       TaskDialogButtons Buttons,
                                       SysIcons MainIcon,
                                       SysIcons FooterIcon)
        {
            return ShowTaskDialogBox(null, Title, MainInstruction, Content, ExpandedInfo, Footer, VerificationText, "",
                                     "", Buttons, MainIcon, FooterIcon);
        }

        public DialogResult MessageBox(IWin32Window Owner,
                                       string Title,
                                       string MainInstruction,
                                       string Content,
                                       TaskDialogButtons Buttons,
                                       SysIcons MainIcon)
        {
            return MessageBox(Owner, Title, MainInstruction, Content, "", "", "", Buttons, MainIcon,
                              SysIcons.Information);
        }

        public DialogResult MessageBox(string Title,
                                       string MainInstruction,
                                       string Content,
                                       TaskDialogButtons Buttons,
                                       SysIcons MainIcon)
        {
            return MessageBox(null, Title, MainInstruction, Content, "", "", "", Buttons, MainIcon, SysIcons.Information);
        }

        //--------------------------------------------------------------------------------

        #endregion

        //--------------------------------------------------------------------------------

        #region ShowRadioBox

        //--------------------------------------------------------------------------------
        public DialogResult ShowRadioBox(IWin32Window Owner,
                                         string Title,
                                         string MainInstruction,
                                         string Content,
                                         string ExpandedInfo,
                                         string Footer,
                                         string VerificationText,
                                         string RadioButtons,
                                         SysIcons MainIcon,
                                         SysIcons FooterIcon,
                                         int DefaultIndex)
        {
            return ShowTaskDialogBox(Owner, Title, MainInstruction, Content, ExpandedInfo, Footer,
                                     VerificationText,
                                     RadioButtons, "", TaskDialogButtons.OKCancel, MainIcon, FooterIcon,
                                     DefaultIndex);
        }

        //--------------------------------------------------------------------------------
        // Overloaded versions...
        //--------------------------------------------------------------------------------
        public int ShowRadioBox(string Title,
                                string MainInstruction,
                                string Content,
                                string ExpandedInfo,
                                string Footer,
                                string VerificationText,
                                string RadioButtons,
                                SysIcons MainIcon,
                                SysIcons FooterIcon,
                                int DefaultIndex)
        {
            DialogResult res = ShowTaskDialogBox(null, Title, MainInstruction, Content, ExpandedInfo, Footer,
                                                 VerificationText,
                                                 RadioButtons, "", TaskDialogButtons.OKCancel, MainIcon, FooterIcon,
                                                 DefaultIndex);
            if (res == DialogResult.OK)
                return _radioButtonResult;
            else
                return -1;
        }

        public DialogResult ShowRadioBox(IWin32Window Owner,
                                         string Title,
                                         string MainInstruction,
                                         string Content,
                                         string ExpandedInfo,
                                         string Footer,
                                         string VerificationText,
                                         string RadioButtons,
                                         SysIcons MainIcon,
                                         SysIcons FooterIcon)
        {
            return ShowRadioBox(Owner, Title, MainInstruction, Content, ExpandedInfo, Footer, VerificationText,
                                RadioButtons, SysIcons.Question, SysIcons.Information, 0);
        }

        public DialogResult ShowRadioBox(IWin32Window Owner,
                                         string Title,
                                         string MainInstruction,
                                         string Content,
                                         string RadioButtons,
                                         int DefaultIndex)
        {
            return ShowRadioBox(Owner, Title, MainInstruction, Content, "", "", "", RadioButtons, SysIcons.Question,
                                SysIcons.Information, DefaultIndex);
        }

        public DialogResult ShowRadioBox(IWin32Window Owner,
                                         string Title,
                                         string MainInstruction,
                                         string Content,
                                         string RadioButtons)
        {
            return ShowRadioBox(Owner, Title, MainInstruction, Content, "", "", "", RadioButtons, SysIcons.Question,
                                SysIcons.Information, 0);
        }

        public DialogResult ShowRadioBox(string Title,
                                         string MainInstruction,
                                         string Content,
                                         string RadioButtons)
        {
            return ShowRadioBox(null, Title, MainInstruction, Content, "", "", "", RadioButtons, SysIcons.Question,
                                SysIcons.Information, 0);
        }

        #endregion

        //--------------------------------------------------------------------------------

        #region ShowCommandBox

        //--------------------------------------------------------------------------------
        public DialogResult ShowCommandBox(IWin32Window Owner,
                                           string Title,
                                           string MainInstruction,
                                           string Content,
                                           string ExpandedInfo,
                                           string Footer,
                                           string VerificationText,
                                           string CommandButtons,
                                           bool ShowCancelButton,
                                           SysIcons MainIcon,
                                           SysIcons FooterIcon)
        {
            return ShowTaskDialogBox(Owner, Title, MainInstruction, Content, ExpandedInfo, Footer,
                                     VerificationText,
                                     "", CommandButtons,
                                     (ShowCancelButton ? TaskDialogButtons.Cancel : TaskDialogButtons.None),
                                     MainIcon, FooterIcon);
        }

        //--------------------------------------------------------------------------------
        // Overloaded versions...
        //--------------------------------------------------------------------------------
        public DialogResult ShowCommandBox(string Title,
                                           string MainInstruction,
                                           string Content,
                                           string ExpandedInfo,
                                           string Footer,
                                           string VerificationText,
                                           string CommandButtons,
                                           bool ShowCancelButton,
                                           SysIcons MainIcon,
                                           SysIcons FooterIcon)
        {
            return ShowTaskDialogBox(null, Title, MainInstruction, Content, ExpandedInfo, Footer,
                                     VerificationText,
                                     "", CommandButtons,
                                     (ShowCancelButton ? TaskDialogButtons.Cancel : TaskDialogButtons.None),
                                     MainIcon, FooterIcon);
        }

        public DialogResult ShowCommandBox(string Title, string MainInstruction, string Content, string CommandButtons,
                                           bool ShowCancelButton, SysIcons MainIcon)
        {
            return ShowCommandBox(null, Title, MainInstruction, Content, CommandButtons, ShowCancelButton, MainIcon);
        }

        public DialogResult ShowCommandBox(IWin32Window Owner, string Title, string MainInstruction, string Content,
                                           string CommandButtons, bool ShowCancelButton, SysIcons MainIcon)
        {
            return ShowCommandBox(Owner, Title, MainInstruction, Content, "", "", "", CommandButtons, ShowCancelButton,
                                  MainIcon, SysIcons.Information);
        }

        #endregion

        //--------------------------------------------------------------------------------
    }
}