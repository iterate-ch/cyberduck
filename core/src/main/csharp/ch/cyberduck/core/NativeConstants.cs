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
namespace Ch.Cyberduck.Core
{
    public static class NativeConstants
    {
        public const int ACM_OPENA = (0x0400 + 100),
                         ACM_OPENW = (0x0400 + 103),
                         ADVF_NODATA = 1,
                         ADVF_ONLYONCE = 2,
                         ADVF_PRIMEFIRST = 4;

        public const int ARW_BOTTOMLEFT = 0x0000,
                         ARW_BOTTOMRIGHT = 0x0001;

        public const int ARW_DOWN = 0x0004,
                         ARW_HIDE = 0x0008;

        public const int ARW_LEFT = 0x0000,
                         ARW_RIGHT = 0x0000;

        public const int ARW_TOPLEFT = 0x0002,
                         ARW_TOPRIGHT = 0x0003;

        public const int ARW_UP = 0x0004;

        public const int AUTOAPPEND = 0x40000000,
                         AUTOAPPEND_OFF = (unchecked((int) 0x80000000));

        public const int AUTOSUGGEST = 0x10000000,
                         AUTOSUGGEST_OFF = 0x20000000;

        public const int BCM_GETIDEALSIZE = 0x1601;

        public const int BDR_RAISED = 0x0005;

        public const int BDR_RAISEDINNER = 0x0004;
        public const int BDR_RAISEDOUTER = 0x0001;

        public const int BDR_SUNKEN = 0x000a;

        public const int BDR_SUNKENINNER = 0x0008;
        public const int BDR_SUNKENOUTER = 0x0002;
        public const int BFFM_ENABLEOK = 0x400 + 101;

        public const int BFFM_INITIALIZED = 1,
                         BFFM_SELCHANGED = 2,
                         BFFM_SETSELECTION = 0x400 + 103;

        public const int BF_ADJUST = 0x2000;
        public const int BF_BOTTOM = 0x0008;
        public const int BF_FLAT = 0x4000;
        public const int BF_LEFT = 0x0001;
        public const int BF_MIDDLE = 0x0800;
        public const int BF_RIGHT = 0x0004;
        public const int BF_TOP = 0x0002;
        public const int BITSPIXEL = 12;
        public const int BI_RGB = 0;
        public const int BM_CLICK = 0x00F5;

        public const int BM_SETCHECK = 0x00F1,
                         BM_SETSTATE = 0x00F3;

        public const int BN_CLICKED = 0;
        public const int BS_3STATE = 0x00000005;
        public const int BS_BOTTOM = 0x00000800;
        public const int BS_CENTER = 0x00000300;

        public const int BS_DEFPUSHBUTTON = 0x00000001;

        public const int BS_GROUPBOX = 0x00000007,
                         BS_LEFT = 0x00000100;

        public const int BS_MULTILINE = 0x00002000;

        public const int BS_OWNERDRAW = 0x0000000B;

        public const int BS_PATTERN = 3;
        public const int BS_PUSHBUTTON = 0x00000000;
        public const int BS_PUSHLIKE = 0x00001000;

        public const int BS_RADIOBUTTON = 0x00000004;

        public const int BS_RIGHT = 0x00000200;

        public const int BS_RIGHTBUTTON = 0x00000020;

        public const int BS_TOP = 0x00000400;

        public const int BS_VCENTER = 0x00000C00;

        public const int CBEM_GETITEMA = (0x0400 + 4); // C:\Program Files\Common
        public const int CBEM_GETITEMW = (0x0400 + 13); // C:\Program Files\Common
        public const int CBEM_INSERTITEMA = (0x0400 + 1); // C:\Program Files\Common
        public const int CBEM_INSERTITEMW = (0x0400 + 11); // C:\Program Files\Common
        public const int CBEM_SETITEMA = (0x0400 + 5); // C:\Program Files\Common
        public const int CBEM_SETITEMW = (0x0400 + 12); // C:\Program Files\Common

        public const int CBEN_ENDEDITA = ((0 - 800) - 5),
                         CBEN_ENDEDITW = ((0 - 800) - 6); // C:\Program Files\Common

        public const int CBN_CLOSEUP = 8; // C:\Program Files\Common

        public const int CBN_DBLCLK = 2; // C:\Program Files\Common

        public const int CBN_DROPDOWN = 7; // C:\Program Files\Common

        public const int CBN_EDITCHANGE = 5,
                         CBN_EDITUPDATE = 6; // C:\Program Files\Common

        public const int CBN_SELCHANGE = 1; // C:\Program Files\Common

        public const int CBN_SELENDOK = 9; // C:\Program Files\Common

        public const int CBS_AUTOHSCROLL = 0x0040; // C:\Program Files\Common

        public const int CBS_DROPDOWN = 0x0002,
                         CBS_DROPDOWNLIST = 0x0003; // C:\Program Files\Common

        public const int CBS_HASSTRINGS = 0x0200,
                         CBS_NOINTEGRALHEIGHT = 0x0400; // C:\Program Files\Common

        public const int CBS_OWNERDRAWFIXED = 0x0010,
                         CBS_OWNERDRAWVARIABLE = 0x0020; // C:\Program Files\Common

        public const int CBS_SIMPLE = 0x0001; // C:\Program Files\Common

        public const int CB_ADDSTRING = 0x0143,
                         CB_DELETESTRING = 0x0144; // C:\Program Files\Common

        public const int CB_ERR = (-1); // C:\Program Files\Common
        public const int CB_FINDSTRING = 0x014C; // C:\Program Files\Common
        public const int CB_FINDSTRINGEXACT = 0x0158; // C:\Program Files\Common

        public const int CB_GETCURSEL = 0x0147; // C:\Program Files\Common

        public const int CB_GETDROPPEDSTATE = 0x0157; // C:\Program Files\Common
        public const int CB_GETEDITSEL = 0x0140; // C:\Program Files\Common
        public const int CB_GETITEMDATA = 0x0150; // C:\Program Files\Common
        public const int CB_GETITEMHEIGHT = 0x0154; // C:\Program Files\Common

        public const int CB_INSERTSTRING = 0x014A; // C:\Program Files\Common

        public const int CB_LIMITTEXT = 0x0141; // C:\Program Files\Common

        public const int CB_RESETCONTENT = 0x014B; // C:\Program Files\Common

        public const int CB_SETCURSEL = 0x014E; // C:\Program Files\Common

        public const int CB_SETDROPPEDWIDTH = 0x0160; // C:\Program Files\Common

        public const int CB_SETEDITSEL = 0x0142; // C:\Program Files\Common
        public const int CB_SETITEMHEIGHT = 0x0153; // C:\Program Files\Common
        public const int CB_SHOWDROPDOWN = 0x014F; // C:\Program Files\Common
        public const int CCS_NODIVIDER = 0x00000040; // C:\Program Files\Common
        public const int CCS_NOPARENTALIGN = 0x00000008; // C:\Program Files\Common
        public const int CCS_NORESIZE = 0x00000004; // C:\Program Files\Common
        public const int CC_ANYCOLOR = 0x00000100;
        public const int CC_ENABLEHOOK = 0x00000010;

        public const int CC_FULLOPEN = 0x00000002,
                         CC_PREVENTFULLOPEN = 0x00000004;

        public const int CC_RGBINIT = 0x00000001;
        public const int CC_SHOWHELP = 0x00000008;
        public const int CC_SOLIDCOLOR = 0x00000080;

        public const int CDDS_ITEM = 0x00010000; // C:\Program Files\Common

        public const int CDDS_ITEMPOSTPAINT = (0x00010000 | 0x00000002); // C:\Program Files\Common

        public const int CDDS_ITEMPREPAINT = (0x00010000 | 0x00000001); // C:\Program Files\Common
        public const int CDDS_POSTPAINT = 0x00000002; // C:\Program Files\Common
        public const int CDDS_PREPAINT = 0x00000001; // C:\Program Files\Common
        public const int CDDS_SUBITEM = 0x00020000; // C:\Program Files\Common
        public const int CDERR_DIALOGFAILURE = 0xFFFF;
        public const int CDERR_FINDRESFAILURE = 0x0006;
        public const int CDERR_INITIALIZATION = 0x0002;
        public const int CDERR_LOADRESFAILURE = 0x0007;
        public const int CDERR_LOADSTRFAILURE = 0x0005;

        public const int CDERR_LOCKRESFAILURE = 0x0008,
                         CDERR_MEMALLOCFAILURE = 0x0009,
                         CDERR_MEMLOCKFAILURE = 0x000A;

        public const int CDERR_NOHINSTANCE = 0x0004;
        public const int CDERR_NOHOOK = 0x000B;
        public const int CDERR_NOTEMPLATE = 0x0003;
        public const int CDERR_REGISTERMSGFAIL = 0x000C;
        public const int CDERR_STRUCTSIZE = 0x0001;

        public const int CDIS_CHECKED = 0x0008; // C:\Program Files\Common

        public const int CDIS_DEFAULT = 0x0020; // C:\Program Files\Common

        public const int CDIS_DISABLED = 0x0004; // C:\Program Files\Common
        public const int CDIS_FOCUS = 0x0010; // C:\Program Files\Common
        public const int CDIS_GRAYED = 0x0002; // C:\Program Files\Common

        public const int CDIS_HOT = 0x0040; // C:\Program Files\Common

        public const int CDIS_INDETERMINATE = 0x0100; // C:\Program Files\Common

        public const int CDIS_MARKED = 0x0080; // C:\Program Files\Common
        public const int CDIS_SELECTED = 0x0001; // C:\Program Files\Common

        public const int CDIS_SHOWKEYBOARDCUES = 0x0200; // C:\Program Files\Common

        public const int CDRF_DODEFAULT = 0x00000000,
                         CDRF_NEWFONT = 0x00000002; // C:\Program Files\Common

        public const int CDRF_NOTIFYITEMDRAW = 0x00000020; // C:\Program Files\Common
        public const int CDRF_NOTIFYPOSTPAINT = 0x00000010; // C:\Program Files\Common
        public const int CDRF_NOTIFYSUBITEMDRAW = CDRF_NOTIFYITEMDRAW; // C:\Program Files\Common
        public const int CDRF_SKIPDEFAULT = 0x00000004; // C:\Program Files\Common
        public const int CFERR_MAXLESSTHANMIN = 0x2002;
        public const int CFERR_NOFONTS = 0x2001;
        public const int CF_APPLY = 0x00000200;
        public const int CF_BITMAP = 2; // C:\Program Files\Common
        public const int CF_DIB = 8; // C:\Program Files\Common
        public const int CF_DIF = 5; // C:\Program Files\Common
        public const int CF_EFFECTS = 0x00000100;
        public const int CF_ENABLEHOOK = 0x00000008;
        public const int CF_ENHMETAFILE = 14; // C:\Program Files\Common

        public const int CF_FIXEDPITCHONLY = 0x00004000,
                         CF_FORCEFONTEXIST = 0x00010000;

        public const int CF_HDROP = 15; // C:\Program Files\Common
        public const int CF_INITTOLOGFONTSTRUCT = 0x00000040;
        public const int CF_LIMITSIZE = 0x00002000;
        public const int CF_LOCALE = 16; // C:\Program Files\Common
        public const int CF_METAFILEPICT = 3; // C:\Program Files\Common
        public const int CF_NOSIMULATIONS = 0x00001000;
        public const int CF_NOVECTORFONTS = 0x00000800;
        public const int CF_NOVERTFONTS = 0x01000000;
        public const int CF_OEMTEXT = 7; // C:\Program Files\Common

        public const int CF_PALETTE = 9,
                         CF_PENDATA = 10,
                         CF_RIFF = 11; // C:\Program Files\Common

        public const int CF_SCREENFONTS = 0x00000001;
        public const int CF_SCRIPTSONLY = 0x00000400;
        public const int CF_SELECTSCRIPT = 0x00400000;
        public const int CF_SHOWHELP = 0x00000004;
        public const int CF_SYLK = 4; // C:\Program Files\Common
        public const int CF_TEXT = 1; // C:\Program Files\Common
        public const int CF_TIFF = 6; // C:\Program Files\Common
        public const int CF_TTONLY = 0x00040000;
        public const int CF_UNICODETEXT = 13; // C:\Program Files\Common
        public const int CF_WAVE = 12; // C:\Program Files\Common

        public const int CLR_DEFAULT = unchecked((int) 0xFF000000); // C:\Program Files\Common

        public const int CLR_NONE = unchecked((int) 0xFFFFFFFF); // C:\Program Files\Common

        public const int CLSCTX_INPROC_SERVER = 0x1,
                         CLSCTX_LOCAL_SERVER = 0x4; // C:\Program Files\Common

        public const int COLOR_WINDOW = 5; // C:\Program Files\Common

        public const int CONNECT_E_CANNOTCONNECT = unchecked((int) 0x80040202); // C:\Program Files\Common

        public const int CONNECT_E_NOCONNECTION = unchecked((int) 0x80040200); // C:\Program Files\Common
        public const int CP_WINANSI = 1004;
        public const int CSC_NAVIGATEBACK = 0x00000002;
        public const int CSC_NAVIGATEFORWARD = 0x00000001;
        public const int CSIDL_APPDATA = 0x001a; // C:\Program Files\Common
        public const int CSIDL_COMMON_APPDATA = 0x0023; // C:\Program Files\Common
        public const int CSIDL_COOKIES = 0x0021; // C:\Program Files\Common

        public const int CSIDL_DESKTOP = 0x0000; // C:\Program Files\Common

        public const int // Start Menu\Programs\Startup
            CSIDL_DESKTOPDIRECTORY = 0x0010; // C:\Program Files\Common

        public const int // Start Menu\Programs
            CSIDL_FAVORITES = 0x0006; // C:\Program Files\Common

        public const int CSIDL_HISTORY = 0x0022; // C:\Program Files\Common

        public const int // <desktop>
                         CSIDL_INTERNET = 0x0001; // C:\Program Files\Common

        public const int CSIDL_INTERNET_CACHE = 0x0020; // C:\Program Files\Common
        public const int CSIDL_LOCAL_APPDATA = 0x001c; // C:\Program Files\Common

        public const int // Start Menu\Programs
                         CSIDL_PERSONAL = 0x0005; // C:\Program Files\Common

        public const int CSIDL_PROGRAMS = 0x0002; // C:\Program Files\Common

        public const int CSIDL_PROGRAM_FILES = 0x0026,
                         // C:\Program Files
                         CSIDL_PROGRAM_FILES_COMMON = 0x002b; // C:\Program Files\Common

        public const int // Start Menu\Programs\Startup
                         CSIDL_RECENT = 0x0008,
                         // <user name>\Recent
                         CSIDL_SENDTO = 0x0009,
                         // <user name>\SendTo
                         CSIDL_STARTMENU = 0x000b; // C:\Program Files\Common

        public const int CSIDL_STARTUP = 0x0007; // C:\Program Files\Common

        public const int // All Users\Application Data
                         CSIDL_SYSTEM = 0x0025; // C:\Program Files\Common

        public const int CSIDL_TEMPLATES = 0x0015; // C:\Program Files\Common

        public const int CS_DBLCLKS = 0x0008,
                         CS_DROPSHADOW = 0x00020000; // C:\Program Files\Common

        public const int CTRLINFO_EATS_ESCAPE = 2; // C:\Program Files\Common
        public const int CTRLINFO_EATS_RETURN = 1; // C:\Program Files\Common
        public const int CWP_SKIPINVISIBLE = 0x0001; // C:\Program Files\Common
        public const int CW_USEDEFAULT = (unchecked((int) 0x80000000)); // C:\Program Files\Common

        public const int DCX_CACHE = 0x00000002,
                         DCX_LOCKWINDOWUPDATE = 0x00000400;

        public const int DCX_WINDOW = 0x00000001;

        public const int DEFAULT_GUI_FONT = 17;

        public const int DFCS_BUTTON3STATE = 0x0008;
        public const int DFCS_BUTTONCHECK = 0x0000;
        public const int DFCS_BUTTONPUSH = 0x0010;
        public const int DFCS_BUTTONRADIO = 0x0004;

        public const int DFCS_CAPTIONCLOSE = 0x0000;

        public const int DFCS_CAPTIONHELP = 0x0004;

        public const int DFCS_CAPTIONMAX = 0x0002;

        public const int DFCS_CAPTIONMIN = 0x0001;

        public const int DFCS_CAPTIONRESTORE = 0x0003;

        public const int DFCS_CHECKED = 0x0400,
                         DFCS_FLAT = 0x4000;

        public const int DFCS_INACTIVE = 0x0100;

        public const int DFCS_MENUARROW = 0x0000;

        public const int DFCS_MENUBULLET = 0x0002;

        public const int DFCS_MENUCHECK = 0x0001;
        public const int DFCS_PUSHED = 0x0200;
        public const int DFCS_SCROLLCOMBOBOX = 0x0005;

        public const int DFCS_SCROLLDOWN = 0x0001,
                         DFCS_SCROLLLEFT = 0x0002,
                         DFCS_SCROLLRIGHT = 0x0003;

        public const int DFCS_SCROLLUP = 0x0000;
        public const int DFC_BUTTON = 4;

        public const int DFC_CAPTION = 1,
                         DFC_MENU = 2,
                         DFC_SCROLL = 3;

        public const int DIB_RGB_COLORS = 0;

        public const int DISPATCH_METHOD = 0x1,
                         DISPATCH_PROPERTYGET = 0x2,
                         DISPATCH_PROPERTYPUT = 0x4;

        public const int DISPID_PROPERTYPUT = (-3);
        public const int DISPID_UNKNOWN = (-1);
        public const int DISP_E_EXCEPTION = unchecked((int) 0x80020009);

        public const int DISP_E_MEMBERNOTFOUND = unchecked((int) 0x80020003),
                         DISP_E_PARAMNOTFOUND = unchecked((int) 0x80020004);

        public const int DI_NORMAL = 0x0003;

        public const int DLGC_WANTALLKEYS = 0x0004;

        public const int DLGC_WANTARROWS = 0x0001;

        public const int DLGC_WANTCHARS = 0x0080;

        public const int DLGC_WANTTAB = 0x0002;
        public const int DRAGDROP_E_ALREADYREGISTERED = unchecked((int) 0x80040101);
        public const int DRAGDROP_E_NOTREGISTERED = unchecked((int) 0x80040100);

        public const int DTM_SETFORMATA = (0x1000 + 5),
                         DTM_SETFORMATW = (0x1000 + 50),
                         DTM_SETMCCOLOR = (0x1000 + 6),
                         DTM_SETMCFONT = (0x1000 + 9);

        public const int DTM_SETRANGE = (0x1000 + 4);
        public const int DTM_SETSYSTEMTIME = (0x1000 + 2);
        public const int DTN_CLOSEUP = ((0 - 760) + 7);

        public const int DTN_DATETIMECHANGE = ((0 - 760) + 1);

        public const int DTN_DROPDOWN = ((0 - 760) + 6);
        public const int DTN_FORMATA = ((0 - 760) + 4);

        public const int DTN_FORMATQUERYA = ((0 - 760) + 5),
                         DTN_FORMATQUERYW = ((0 - 760) + 18);

        public const int DTN_FORMATW = ((0 - 760) + 17);

        public const int DTN_USERSTRINGA = ((0 - 760) + 2),
                         DTN_USERSTRINGW = ((0 - 760) + 15),
                         DTN_WMKEYDOWNA = ((0 - 760) + 3),
                         DTN_WMKEYDOWNW = ((0 - 760) + 16);

        public const int DTS_LONGDATEFORMAT = 0x0004;
        public const int DTS_RIGHTALIGN = 0x0020;
        public const int DTS_SHOWNONE = 0x0002;
        public const int DTS_TIMEFORMAT = 0x0009;
        public const int DTS_UPDOWN = 0x0001;
        public const int DT_CALCRECT = 0x00000400;
        public const int DT_EDITCONTROL = 0x00002000;
        public const int DT_END_ELLIPSIS = 0x00008000;
        public const int DT_EXPANDTABS = 0x00000040;
        public const int DT_LEFT = 0x00000000;
        public const int DT_NOCLIP = 0x00000100;
        public const int DT_NOPREFIX = 0x00000800;
        public const int DT_RIGHT = 0x00000002;
        public const int DT_RTLREADING = 0x00020000;
        public const int DT_SINGLELINE = 0x00000020;
        public const int DT_VCENTER = 0x00000004;
        public const int DT_WORDBREAK = 0x00000010;
        public const int DUPLICATE = 0x06;
        public const int DUPLICATE_SAME_ACCESS = 0x00000002;

        public const int DVASPECT_CONTENT = 1;

        public const int DVASPECT_OPAQUE = 16;

        public const int DVASPECT_TRANSPARENT = 32;
        public const int DV_E_DVASPECT = unchecked((int) 0x8004006B);

        public const int EC_LEFTMARGIN = 0x0001,
                         EC_RIGHTMARGIN = 0x0002;

        public const int EDGE_BUMP = (0x0001 | 0x0008);
        public const int EDGE_ETCHED = (0x0002 | 0x0004);

        public const int EDGE_RAISED = (0x0001 | 0x0004),
                         EDGE_SUNKEN = (0x0002 | 0x0008);

        public const int EMR_POLYTEXTOUTA = 96,
                         EMR_POLYTEXTOUTW = 97;

        public const int EM_CANUNDO = 0x00C6;
        public const int EM_CHARFROMPOS = 0x00D7;
        public const int EM_EMPTYUNDOBUFFER = 0x00CD;
        public const int EM_GETLINE = 0x00C4;
        public const int EM_GETLINECOUNT = 0x00BA;
        public const int EM_GETMODIFY = 0x00B8;
        public const int EM_GETPASSWORDCHAR = 0x00D2;

        public const int EM_GETSEL = 0x00B0;

        public const int EM_LIMITTEXT = 0x00C5;

        public const int EM_LINEFROMCHAR = 0x00C9,
                         EM_LINEINDEX = 0x00BB;

        public const int EM_POSFROMCHAR = 0x00D6;
        public const int EM_REPLACESEL = 0x00C2;

        public const int EM_SCROLL = 0x00B5,
                         EM_SCROLLCARET = 0x00B7;

        public const int EM_SETMARGINS = 0x00D3;

        public const int EM_SETMODIFY = 0x00B9;

        public const int EM_SETPASSWORDCHAR = 0x00CC;

        public const int EM_SETREADONLY = 0x00CF;
        public const int EM_SETTYPOGRAPHYOPTIONS = WM_USER + 202;

        public const int EM_SETSEL = 0x00B1;
        public const int EM_UNDO = 0x00C7;

        public const int EM_SETCUEBANNER = 0x1501;

        public const int EN_ALIGN_LTR_EC = 0x0700,
                         EN_ALIGN_RTL_EC = 0x0701;

        public const int EN_CHANGE = 0x0300,
                         EN_HSCROLL = 0x0601,
                         EN_VSCROLL = 0x0602;

        public const int ES_AUTOHSCROLL = 0x0080;
        public const int ES_AUTOVSCROLL = 0x0040;
        public const int ES_CENTER = 0x0001;
        public const int ES_LEFT = 0x0000;
        public const int ES_LOWERCASE = 0x0010;
        public const int ES_MULTILINE = 0x0004;
        public const int ES_NOHIDESEL = 0x0100;
        public const int ES_PASSWORD = 0x0020;
        public const int ES_READONLY = 0x0800;
        public const int ES_RIGHT = 0x0002;
        public const int ES_UPPERCASE = 0x0008;
        public const int ETO_CLIPPED = 0x0004;
        public const int ETO_OPAQUE = 0x0002;
        public const int E_ABORT = unchecked((int) 0x80004004);
        public const int E_FAIL = unchecked((int) 0x80004005);

        public const int E_INVALIDARG = unchecked((int) 0x80070057),
                         E_NOINTERFACE = unchecked((int) 0x80004002);

        public const int E_NOTIMPL = unchecked((int) 0x80004001),
                         E_OUTOFMEMORY = unchecked((int) 0x8007000E);

        public const int E_UNEXPECTED = unchecked((int) 0x8000FFFF);

        public const int FADF_BSTR = (0x100);

        public const int FADF_DISPATCH = (0x400);

        public const int FADF_UNKNOWN = (0x200);

        public const int FADF_VARIANT = (unchecked(0x800));

        public const int FALT = 0x10;
        public const int FLASHW_ALL = FLASHW_CAPTION | FLASHW_TRAY;
        public const int FLASHW_CAPTION = 0x00000001;
        public const int FLASHW_STOP = 0;

        public const int FLASHW_TIMER = 0x00000004,
                         FLASHW_TIMERNOFG = 0x0000000C;

        public const int FLASHW_TRAY = 0x00000002;
        public const int FNERR_BUFFERTOOSMALL = 0x3003;
        public const int FNERR_INVALIDFILENAME = 0x3002;
        public const int FNERR_SUBCLASSFAILURE = 0x3001;

        public const int FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000,
                         FORMAT_MESSAGE_IGNORE_INSERTS = 0x00000200;

        public const int FRERR_BUFFERLENGTHZERO = 0x4001;

        public const int FSHIFT = 0x04;

        public const int FVIRTKEY = 0x01;
        public const int GDI_ERROR = (unchecked((int) 0xFFFFFFFF));
        public const int GDTR_MAX = 0x0002;
        public const int GDTR_MIN = 0x0001;
        public const int GDT_NONE = 1;
        public const int GDT_VALID = 0;
        public const int GMEM_DDESHARE = 0x2000;

        public const int GMEM_MOVEABLE = 0x0002,
                         GMEM_ZEROINIT = 0x0040;

        public const int GMR_DAYSTATE = 1;
        public const int GMR_VISIBLE = 0;

        public const int GWL_EXSTYLE = (-20);

        public const int GWL_HWNDPARENT = (-8);

        public const int GWL_ID = (-12);

        public const int GWL_STYLE = (-16);
        public const int GWL_WNDPROC = (-4);
        public const int GW_CHILD = 5;

        public const int GW_HWNDFIRST = 0,
                         GW_HWNDLAST = 1,
                         GW_HWNDNEXT = 2,
                         GW_HWNDPREV = 3;

        public const int HCF_HIGHCONTRASTON = 0x00000001;

        public const int HC_ACTION = 0,
                         HC_GETNEXT = 1,
                         HC_SKIP = 2;

        public const int HDI_ORDER = 0x0080;

        public const int HDM_GETITEMA = (0x1200 + 3);

        public const int HDM_GETITEMCOUNT = (0x1200 + 0);

        public const int HDM_GETITEMW = (0x1200 + 11);

        public const int HDM_INSERTITEMA = (0x1200 + 1),
                         HDM_INSERTITEMW = (0x1200 + 10);

        public const int HDM_SETITEMA = (0x1200 + 4),
                         HDM_SETITEMW = (0x1200 + 12);

        public const int HDN_BEGINTDRAG = ((0 - 300) - 10),
                         HDN_BEGINTRACKA = ((0 - 300) - 6),
                         HDN_BEGINTRACKW = ((0 - 300) - 26);

        public const int HDN_DIVIDERDBLCLICKA = ((0 - 300) - 5),
                         HDN_DIVIDERDBLCLICKW = ((0 - 300) - 25);

        public const int HDN_ENDDRAG = ((0 - 300) - 11),
                         HDN_ENDTRACKA = ((0 - 300) - 7),
                         HDN_ENDTRACKW = ((0 - 300) - 27);

        public const int HDN_GETDISPINFOA = ((0 - 300) - 9),
                         HDN_GETDISPINFOW = ((0 - 300) - 29);

        public const int HDN_ITEMCHANGEDA = ((0 - 300) - 1),
                         HDN_ITEMCHANGEDW = ((0 - 300) - 21);

        public const int HDN_ITEMCHANGINGA = ((0 - 300) - 0),
                         HDN_ITEMCHANGINGW = ((0 - 300) - 20);

        public const int HDN_ITEMCLICKA = ((0 - 300) - 2),
                         HDN_ITEMCLICKW = ((0 - 300) - 22),
                         HDN_ITEMDBLCLICKA = ((0 - 300) - 3),
                         HDN_ITEMDBLCLICKW = ((0 - 300) - 23);

        public const int HDN_TRACKA = ((0 - 300) - 8),
                         HDN_TRACKW = ((0 - 300) - 28);

        public const int HELPINFO_WINDOW = 0x0001;

        public const int HLP_FILE = 1,
                         HLP_KEYWORD = 2,
                         HLP_NAVIGATOR = 3,
                         HLP_OBJECT = 4;

        public const int HOLLOW_BRUSH = 5;

        public const int HTBOTTOM = 15,
                         HTBOTTOMRIGHT = 17;

        public const int HTCLIENT = 1;
        public const int HTNOWHERE = 0;
        public const int ICC_BAR_CLASSES = 0x00000004;
        public const int ICC_DATE_CLASSES = 0x00000100;
        public const int ICC_LISTVIEW_CLASSES = 0x00000001;
        public const int ICC_PROGRESS_CLASS = 0x00000020;
        public const int ICC_TAB_CLASSES = 0x00000008;
        public const int ICC_TREEVIEW_CLASSES = 0x00000002;

        public const int ICON_BIG = 1;

        public const int ICON_SMALL = 0;
        public const int IDC_APPSTARTING = 32650;

        public const int IDC_ARROW = 32512;

        public const int IDC_CROSS = 32515;

        public const int IDC_HELP = 32651;
        public const int IDC_IBEAM = 32513;
        public const int IDC_NO = 32648;

        public const int IDC_SIZEALL = 32646;

        public const int IDC_SIZENESW = 32643;

        public const int IDC_SIZENS = 32645;

        public const int IDC_SIZENWSE = 32642;
        public const int IDC_SIZEWE = 32644;

        public const int IDC_UPARROW = 32516;

        public const int IDC_WAIT = 32514;
        public const int IDM_PAGESETUP = 2004;
        public const int IDM_PRINT = 27;

        public const int IDM_PRINTPREVIEW = 2003,
                         IDM_PROPERTIES = 28,
                         IDM_SAVEAS = 71;

        public const int ILC_COLOR = 0x0000;

        public const int ILC_COLOR16 = 0x0010,
                         ILC_COLOR24 = 0x0018,
                         ILC_COLOR32 = 0x0020;

        public const int ILC_COLOR4 = 0x0004,
                         ILC_COLOR8 = 0x0008;

        public const int ILC_MASK = 0x0001;

        public const int ILD_MASK = 0x0010;

        public const int ILD_NORMAL = 0x0000;

        public const int ILD_ROP = 0x0040;

        public const int ILD_TRANSPARENT = 0x0001;
        public const int ILS_ALPHA = 0x8;

        public const int ILS_GLOW = 0x1;

        public const int ILS_NORMAL = 0x0;

        public const int ILS_SATURATE = 0x4;

        public const int ILS_SHADOW = 0x2;
        public const int IMAGE_CURSOR = 2;
        public const int IMAGE_ICON = 1;
        public const int IME_CMODE_FULLSHAPE = 0x0008;
        public const int IME_CMODE_KATAKANA = 0x0002;
        public const int IME_CMODE_NATIVE = 0x0001;
        public const int INET_E_DEFAULT_ACTION = unchecked((int) 0x800C0011);
        public const int INPLACE_E_NOTOOLSPACE = unchecked((int) 0x800401A1);

        public const int KEYEVENTF_KEYUP = 0x0002;
        public const int LANG_NEUTRAL = 0x00;

        public const int LBN_DBLCLK = 2;

        public const int LBN_SELCHANGE = 1;
        public const int LBS_DISABLENOSCROLL = 0x1000;
        public const int LBS_EXTENDEDSEL = 0x0800;
        public const int LBS_HASSTRINGS = 0x0040;
        public const int LBS_MULTICOLUMN = 0x0200;
        public const int LBS_MULTIPLESEL = 0x0008;
        public const int LBS_NOINTEGRALHEIGHT = 0x0100;
        public const int LBS_NOSEL = 0x4000;
        public const int LBS_NOTIFY = 0x0001;

        public const int LBS_OWNERDRAWFIXED = 0x0010,
                         LBS_OWNERDRAWVARIABLE = 0x0020;

        public const int LBS_USETABSTOPS = 0x0080;
        public const int LBS_WANTKEYBOARDINPUT = 0x0400;

        public const int LB_ADDSTRING = 0x0180;

        public const int LB_DELETESTRING = 0x0182;

        public const int LB_ERR = (-1),
                         LB_ERRSPACE = (-2);

        public const int LB_FINDSTRING = 0x018F;
        public const int LB_FINDSTRINGEXACT = 0x01A2;

        public const int LB_GETCARETINDEX = 0x019F,
                         LB_GETCURSEL = 0x0188;

        public const int LB_GETITEMHEIGHT = 0x01A1;
        public const int LB_GETITEMRECT = 0x0198;
        public const int LB_GETSEL = 0x0187;

        public const int LB_GETSELCOUNT = 0x0190,
                         LB_GETSELITEMS = 0x0191;

        public const int LB_GETTEXT = 0x0189,
                         LB_GETTEXTLEN = 0x018A,
                         LB_GETTOPINDEX = 0x018E;

        public const int LB_INSERTSTRING = 0x0181;
        public const int LB_ITEMFROMPOINT = 0x01A9;
        public const int LB_RESETCONTENT = 0x0184;

        public const int LB_SETCOLUMNWIDTH = 0x0195;

        public const int LB_SETCURSEL = 0x0186;
        public const int LB_SETHORIZONTALEXTENT = 0x0194;

        public const int LB_SETITEMHEIGHT = 0x01A0;

        public const int LB_SETLOCALE = 0x01A5;

        public const int LB_SETSEL = 0x0185;
        public const int LB_SETTABSTOPS = 0x0192;
        public const int LB_SETTOPINDEX = 0x0197;

        public const int LCID_INSTALLED = 0x01;
        public const int LCID_SUPPORTED = 0x02;

        public const int LOCALE_IFIRSTDAYOFWEEK = 0x0000100C;

        public const int LOCK_EXCLUSIVE = 0x2,
                         LOCK_ONLYONCE = 0x4;

        public const int LOCK_WRITE = 0x1;

        public const int LOGPIXELSX = 88,
                         LOGPIXELSY = 90;

        public const int LVA_ALIGNLEFT = 0x0001,
                         LVA_ALIGNTOP = 0x0002;

        public const int LVA_DEFAULT = 0x0000;
        public const int LVA_SNAPTOGRID = 0x0005;

        public const int LVBKIF_SOURCE_NONE = 0x0000,
                         LVBKIF_SOURCE_URL = 0x0002,
                         LVBKIF_STYLE_NORMAL = 0x0000,
                         LVBKIF_STYLE_TILE = 0x0010;

        public const int LVCDI_ITEM = 0x0000,
                         LVCF_FMT = 0x0001;

        public const int LVCF_IMAGE = 0x0010,
                         LVCF_ORDER = 0x0020;

        public const int LVCF_SUBITEM = 0x0008;
        public const int LVCF_TEXT = 0x0004;
        public const int LVCF_WIDTH = 0x0002;
        public const int LVFI_NEARESTXY = 0x0040;
        public const int LVFI_PARAM = 0x0001;

        public const int LVFI_PARTIAL = 0x0008,
                         LVFI_STRING = 0x0002;

        public const int LVGA_FOOTER_CENTER = 0x00000010;
        public const int LVGA_FOOTER_LEFT = 0x00000008;
        public const int LVGA_FOOTER_RIGHT = 0x00000020;
        public const int LVGA_HEADER_CENTER = 0x00000002;
        public const int LVGA_HEADER_LEFT = 0x00000001;
        public const int LVGA_HEADER_RIGHT = 0x00000004;
        public const int LVGF_ALIGN = 0x00000008;
        public const int LVGF_FOOTER = 0x00000002;
        public const int LVGF_GROUPID = 0x00000010;
        public const int LVGF_HEADER = 0x00000001;
        public const int LVGF_NONE = 0x00000000;
        public const int LVGF_STATE = 0x00000004;

        public const int LVGS_COLLAPSED = 0x00000001,
                         LVGS_HIDDEN = 0x00000002;

        public const int LVGS_NORMAL = 0x00000000;

        public const int LVHT_ABOVE = 0x0008,
                         LVHT_BELOW = 0x0010;

        public const int LVHT_LEFT = 0x0040;
        public const int LVHT_NOWHERE = 0x0001;
        public const int LVHT_ONITEM = (0x0002 | 0x0004 | 0x0008);

        public const int LVHT_ONITEMICON = 0x0002,
                         LVHT_ONITEMLABEL = 0x0004;

        public const int LVHT_ONITEMSTATEICON = 0x0008;
        public const int LVHT_RIGHT = 0x0020;
        public const int LVIF_COLUMNS = 0x0200;
        public const int LVIF_GROUPID = 0x0100;

        public const int LVIF_IMAGE = 0x0002,
                         LVIF_INDENT = 0x0010,
                         LVIF_PARAM = 0x0004,
                         LVIF_STATE = 0x0008;

        public const int LVIF_TEXT = 0x0001;
        public const int LVIM_AFTER = 0x00000001;

        public const int LVIR_BOUNDS = 0,
                         LVIR_ICON = 1,
                         LVIR_LABEL = 2,
                         LVIR_SELECTBOUNDS = 3;

        public const int LVIS_CUT = 0x0004,
                         LVIS_DROPHILITED = 0x0008;

        public const int LVIS_FOCUSED = 0x0001;

        public const int LVIS_OVERLAYMASK = 0x0F00;

        public const int LVIS_SELECTED = 0x0002;

        public const int LVIS_STATEIMAGEMASK = 0xF000;

        public const int LVM_ARRANGE = (0x1000 + 22);

        public const int LVM_DELETEALLITEMS = (0x1000 + 9);

        public const int LVM_DELETECOLUMN = (0x1000 + 28);
        public const int LVM_DELETEITEM = (0x1000 + 8);

        public const int LVM_EDITLABELA = (0x1000 + 23),
                         LVM_EDITLABELW = (0x1000 + 118);

        public const int LVM_ENABLEGROUPVIEW = (0x1000 + 157);
        public const int LVM_ENSUREVISIBLE = (0x1000 + 19);

        public const int LVM_FINDITEMA = (0x1000 + 13),
                         LVM_FINDITEMW = (0x1000 + 83);

        public const int LVM_GETCOLUMNA = (0x1000 + 25),
                         LVM_GETCOLUMNW = (0x1000 + 95);

        public const int LVM_GETCOLUMNWIDTH = (0x1000 + 29);
        public const int LVM_GETGROUPINFO = (0x1000 + 149);
        public const int LVM_GETHEADER = (0x1000 + 31);
        public const int LVM_GETHOTITEM = (0x1000 + 61);
        public const int LVM_GETINSERTMARK = (0x1000 + 167);
        public const int LVM_GETINSERTMARKCOLOR = (0x1000 + 171);
        public const int LVM_GETINSERTMARKRECT = (0x1000 + 169);

        public const int LVM_GETISEARCHSTRINGA = (0x1000 + 52),
                         LVM_GETISEARCHSTRINGW = (0x1000 + 117);

        public const int LVM_GETITEMA = (0x1000 + 5);

        public const int LVM_GETITEMRECT = (0x1000 + 14);

        public const int LVM_GETITEMSTATE = (0x1000 + 44),
                         LVM_GETITEMTEXTA = (0x1000 + 45),
                         LVM_GETITEMTEXTW = (0x1000 + 115);

        public const int LVM_GETITEMW = (0x1000 + 75);
        public const int LVM_GETNEXTITEM = (0x1000 + 12);
        public const int LVM_GETSELECTEDCOUNT = (0x1000 + 50);

        public const int LVM_GETSTRINGWIDTHA = (0x1000 + 17),
                         LVM_GETSTRINGWIDTHW = (0x1000 + 87);

        public const int LVM_GETSUBITEMRECT = (0x1000 + 56);
        public const int LVM_GETTILEVIEWINFO = (0x1000 + 163);
        public const int LVM_GETTOPINDEX = (0x1000 + 39);
        public const int LVM_HASGROUP = (0x1000 + 161);

        public const int LVM_HITTEST = (0x1000 + 18);

        public const int LVM_INSERTCOLUMNA = (0x1000 + 27),
                         LVM_INSERTCOLUMNW = (0x1000 + 97),
                         LVM_INSERTGROUP = (0x1000 + 145);

        public const int LVM_INSERTITEMA = (0x1000 + 7),
                         LVM_INSERTITEMW = (0x1000 + 77);

        public const int LVM_INSERTMARKHITTEST = (0x1000 + 168);

        public const int LVM_ISGROUPVIEWENABLED = (0x1000 + 175);
        public const int LVM_MOVEITEMTOGROUP = (0x1000 + 154);

        public const int LVM_REMOVEALLGROUPS = (0x1000 + 160);

        public const int LVM_REMOVEGROUP = (0x1000 + 150);

        public const int LVM_SCROLL = (0x1000 + 20),
                         LVM_SETBKCOLOR = (0x1000 + 1),
                         LVM_SETBKIMAGEA = (0x1000 + 68),
                         LVM_SETBKIMAGEW = (0x1000 + 138);

        public const int LVM_SETCOLUMNA = (0x1000 + 26),
                         LVM_SETCOLUMNW = (0x1000 + 96);

        public const int LVM_SETCOLUMNWIDTH = (0x1000 + 30);

        public const int LVM_SETEXTENDEDLISTVIEWSTYLE = (0x1000 + 54);
        public const int LVM_SETGROUPINFO = (0x1000 + 147);
        public const int LVM_SETIMAGELIST = (0x1000 + 3);
        public const int LVM_SETINFOTIP = (0x1000 + 173);

        public const int LVM_SETINSERTMARK = (0x1000 + 166);

        public const int LVM_SETINSERTMARKCOLOR = (0x1000 + 170);
        public const int LVM_SETITEMA = (0x1000 + 6);
        public const int LVM_SETITEMCOUNT = (0x1000 + 47);

        public const int LVM_SETITEMPOSITION = (0x1000 + 15);

        public const int LVM_SETITEMPOSITION32 = (0x01000 + 49);

        public const int LVM_SETITEMSTATE = (0x1000 + 43);

        public const int LVM_SETITEMTEXTA = (0x1000 + 46),
                         LVM_SETITEMTEXTW = (0x1000 + 116);

        public const int LVM_SETITEMW = (0x1000 + 76);
        public const int LVM_SETTEXTBKCOLOR = (0x1000 + 38);
        public const int LVM_SETTEXTCOLOR = (0x1000 + 36);

        public const int LVM_SETTILEVIEWINFO = (0x1000 + 162);

        public const int LVM_SETTOOLTIPS = (0x1000 + 74);
        public const int LVM_SETVIEW = (0x1000 + 142);
        public const int LVM_SORTITEMS = (0x1000 + 48);
        public const int LVM_SUBITEMHITTEST = (0x1000 + 57);

        public const int LVM_UPDATE = (0x1000 + 42),
                         LVNI_FOCUSED = 0x0001,
                         LVNI_SELECTED = 0x0002;

        public const int LVN_BEGINDRAG = ((0 - 100) - 9);

        public const int LVN_BEGINLABELEDITA = ((0 - 100) - 5),
                         LVN_BEGINLABELEDITW = ((0 - 100) - 75);

        public const int LVN_BEGINRDRAG = ((0 - 100) - 11);
        public const int LVN_COLUMNCLICK = ((0 - 100) - 8);

        public const int LVN_ENDLABELEDITA = ((0 - 100) - 6),
                         LVN_ENDLABELEDITW = ((0 - 100) - 76);

        public const int LVN_GETDISPINFOA = ((0 - 100) - 50),
                         LVN_GETDISPINFOW = ((0 - 100) - 77);

        public const int LVN_GETINFOTIPA = ((0 - 100) - 57),
                         LVN_GETINFOTIPW = ((0 - 100) - 58);

        public const int LVN_ITEMACTIVATE = ((0 - 100) - 14);
        public const int LVN_ITEMCHANGED = ((0 - 100) - 1);
        public const int LVN_ITEMCHANGING = ((0 - 100) - 0);
        public const int LVN_KEYDOWN = ((0 - 100) - 55);

        public const int LVN_ODCACHEHINT = ((0 - 100) - 13);

        public const int LVN_ODFINDITEMA = ((0 - 100) - 52),
                         LVN_ODFINDITEMW = ((0 - 100) - 79);

        public const int LVN_ODSTATECHANGED = ((0 - 100) - 15),
                         LVN_SETDISPINFOA = ((0 - 100) - 51),
                         LVN_SETDISPINFOW = ((0 - 100) - 78);

        public const int LVSCW_AUTOSIZE = -1,
                         LVSCW_AUTOSIZE_USEHEADER = -2;

        public const int LVSIL_NORMAL = 0,
                         LVSIL_SMALL = 1,
                         LVSIL_STATE = 2;

        public const int LVS_ALIGNLEFT = 0x0800;
        public const int LVS_ALIGNTOP = 0x0000;

        public const int LVS_AUTOARRANGE = 0x0100,
                         LVS_EDITLABELS = 0x0200;

        public const int LVS_EX_CHECKBOXES = 0x00000004;
        public const int LVS_EX_FULLROWSELECT = 0x00000020;
        public const int LVS_EX_GRIDLINES = 0x00000001;
        public const int LVS_EX_HEADERDRAGDROP = 0x00000010;
        public const int LVS_EX_INFOTIP = 0x00000400;
        public const int LVS_EX_ONECLICKACTIVATE = 0x00000040;
        public const int LVS_EX_TRACKSELECT = 0x00000008;
        public const int LVS_EX_TWOCLICKACTIVATE = 0x00000080;
        public const int LVS_EX_UNDERLINEHOT = 0x0800;
        public const int LVS_ICON = 0x0000;
        public const int LVS_LIST = 0x0003;
        public const int LVS_NOCOLUMNHEADER = 0x4000;
        public const int LVS_NOLABELWRAP = 0x0080;
        public const int LVS_NOSCROLL = 0x2000;

        public const int LVS_NOSORTHEADER = unchecked(0x8000),
                         LVS_OWNERDATA = 0x1000;

        public const int LVS_REPORT = 0x0001;
        public const int LVS_SHAREIMAGELISTS = 0x0040;
        public const int LVS_SHOWSELALWAYS = 0x0008;
        public const int LVS_SINGLESEL = 0x0004;
        public const int LVS_SMALLICON = 0x0002;

        public const int LVS_SORTASCENDING = 0x0010,
                         LVS_SORTDESCENDING = 0x0020;

        public const int LVTVIF_FIXEDSIZE = 0x00000003;
        public const int LVTVIM_COLUMNS = 0x00000002;
        public const int LVTVIM_TILESIZE = 0x00000001;
        public const int LV_VIEW_TILE = 0x0004;

        public const int LWA_ALPHA = 0x00000002;

        public const int LWA_COLORKEY = 0x00000001;

        public const int MAX_PATH = 260;

        public const uint MA_ACTIVATE = 1;
        public const uint MA_ACTIVATEANDEAT = 2;
        public const uint MA_NOACTIVATE = 3;
        public const uint MA_NOACTIVATEANDEAT = 4;

        public const int MB_ICONASTERISK = 0x000040;
        public const int MB_ICONEXCLAMATION = 0x000030;

        public const int MB_ICONHAND = 0x000010,
                         MB_ICONQUESTION = 0x000020;

        public const int MB_OK = 0x00000000;

        public const int MCHT_CALENDAR = 0x00020000;

        public const int MCHT_CALENDARBK = (0x00020000),
                         MCHT_CALENDARDATE = (0x00020000 | 0x0001),
                         MCHT_CALENDARDATENEXT = ((0x00020000 | 0x0001) | 0x01000000),
                         MCHT_CALENDARDATEPREV = ((0x00020000 | 0x0001) | 0x02000000),
                         MCHT_CALENDARDAY = (0x00020000 | 0x0002),
                         MCHT_CALENDARWEEKNUM = (0x00020000 | 0x0003);

        public const int MCHT_TITLE = 0x00010000;
        public const int MCHT_TITLEBK = (0x00010000);

        public const int MCHT_TITLEBTNNEXT = (0x00010000 | 0x01000000 | 0x0003),
                         MCHT_TITLEBTNPREV = (0x00010000 | 0x02000000 | 0x0003);

        public const int MCHT_TITLEMONTH = (0x00010000 | 0x0001),
                         MCHT_TITLEYEAR = (0x00010000 | 0x0002);

        public const int MCHT_TODAYLINK = 0x00030000;
        public const int MCM_GETMAXTODAYWIDTH = (0x1000 + 21);
        public const int MCM_GETMINREQRECT = (0x1000 + 9);
        public const int MCM_GETMONTHRANGE = (0x1000 + 7);

        public const int MCM_GETTODAY = (0x1000 + 13),
                         MCM_HITTEST = (0x1000 + 14);

        public const int MCM_SETCOLOR = (0x1000 + 10);
        public const int MCM_SETFIRSTDAYOFWEEK = (0x1000 + 15);
        public const int MCM_SETMAXSELCOUNT = (0x1000 + 4);
        public const int MCM_SETMONTHDELTA = (0x1000 + 20);
        public const int MCM_SETRANGE = (0x1000 + 18);
        public const int MCM_SETSELRANGE = (0x1000 + 6);
        public const int MCM_SETTODAY = (0x1000 + 12);
        public const int MCN_GETDAYSTATE = ((0 - 750) + 3);
        public const int MCN_SELCHANGE = ((0 - 750) + 1);
        public const int MCN_SELECT = ((0 - 750) + 4);
        public const int MCSC_MONTHBK = 4;

        public const int MCSC_TEXT = 1,
                         MCSC_TITLEBK = 2,
                         MCSC_TITLETEXT = 3;

        public const int MCSC_TRAILINGTEXT = 5;

        public const int MCS_DAYSTATE = 0x0001,
                         MCS_MULTISELECT = 0x0002;

        public const int MCS_NOTODAY = 0x0010;

        public const int MCS_NOTODAYCIRCLE = 0x0008;
        public const int MCS_WEEKNUMBERS = 0x0004;
        public const int MDITILE_HORIZONTAL = 0x0001;
        public const int MDITILE_VERTICAL = 0x0000;
        public const int MEMBERID_NIL = (-1);
        public const int MFT_MENUBREAK = 0x00000040;
        public const int MFT_RIGHTJUSTIFY = 0x00004000;
        public const int MFT_RIGHTORDER = 0x00002000;
        public const int MFT_SEPARATOR = 0x00000800;

        public const int MF_BYCOMMAND = 0x00000000,
                         MF_BYPOSITION = 0x00000400,
                         MF_ENABLED = 0x00000000,
                         MF_GRAYED = 0x00000001,
                         MF_POPUP = 0x00000010,
                         MF_SYSMENU = 0x00002000;

        public const int MIIM_DATA = 0x00000020;
        public const int MIIM_ID = 0x00000002;
        public const int MIIM_STATE = 0x00000001;

        public const int MIIM_SUBMENU = 0x00000004,
                         MIIM_TYPE = 0x00000010;

        public const int MK_CONTROL = 0x0008;
        public const int MK_LBUTTON = 0x0001;
        public const int MK_MBUTTON = 0x0010;

        public const int MK_RBUTTON = 0x0002,
                         MK_SHIFT = 0x0004;

        public const int MMIO_ALLOCBUF = 0x00010000,
                         MMIO_FINDRIFF = 0x00000020;

        public const int MMIO_READ = 0x00000000;
        public const int MM_ANISOTROPIC = 8;
        public const int MM_TEXT = 1;

        public const int MNC_EXECUTE = 2,
                         MNC_SELECT = 3;

        public const int MSAA_MENU_SIG = (unchecked((int) 0xAA0DF00D));

        public const int NFR_ANSI = 1,
                         NFR_UNICODE = 2;

        public const int NIF_ICON = 0x00000002;
        public const int NIF_MESSAGE = 0x00000001;
        public const int NIF_TIP = 0x00000004;
        public const int NIM_ADD = 0x00000000;
        public const int NIM_DELETE = 0x00000002;
        public const int NIM_MODIFY = 0x00000001;

        public const int NM_CLICK = ((0 - 0) - 2);

        public const int NM_CUSTOMDRAW = ((0 - 0) - 12);

        public const int NM_DBLCLK = ((0 - 0) - 3),
                         NM_RCLICK = ((0 - 0) - 5),
                         NM_RDBLCLK = ((0 - 0) - 6);

        public const int NM_RELEASEDCAPTURE = ((0 - 0) - 16);

        public const int OBJ_BITMAP = 7;

        public const int OBJ_BRUSH = 2,
                         OBJ_DC = 3;

        public const int OBJ_ENHMETADC = 12;
        public const int OBJ_EXTPEN = 11;
        public const int OBJ_FONT = 6;
        public const int OBJ_MEMDC = 10;

        public const int OBJ_METADC = 4;

        public const int OBJ_METAFILE = 9;

        public const int OBJ_PAL = 5;

        public const int OBJ_PEN = 1;

        public const int OBJ_REGION = 8;

        public const int ODS_CHECKED = 0x0008,
                         ODS_COMBOBOXEDIT = 0x1000,
                         ODS_DEFAULT = 0x0020,
                         ODS_DISABLED = 0x0004,
                         ODS_FOCUS = 0x0010,
                         ODS_GRAYED = 0x0002,
                         ODS_HOTLIGHT = 0x0040,
                         ODS_INACTIVE = 0x0080,
                         ODS_NOACCEL = 0x0100,
                         ODS_NOFOCUSRECT = 0x0200,
                         ODS_SELECTED = 0x0001;

        public const int OFN_ALLOWMULTISELECT = 0x00000200;
        public const int OFN_CREATEPROMPT = 0x00002000;
        public const int OFN_ENABLEHOOK = 0x00000020;
        public const int OFN_ENABLESIZING = 0x00800000;
        public const int OFN_EXPLORER = 0x00080000;
        public const int OFN_FILEMUSTEXIST = 0x00001000;

        public const int OFN_HIDEREADONLY = 0x00000004,
                         OFN_NOCHANGEDIR = 0x00000008;

        public const int OFN_NODEREFERENCELINKS = 0x00100000;
        public const int OFN_NOVALIDATE = 0x00000100;
        public const int OFN_OVERWRITEPROMPT = 0x00000002;
        public const int OFN_PATHMUSTEXIST = 0x00000800;
        public const int OFN_READONLY = 0x00000001;
        public const int OFN_SHOWHELP = 0x00000010;
        public const int OFN_USESHELLITEM = 0x01000000;

        public const int OLECLOSE_PROMPTSAVE = 2;

        public const int OLECLOSE_SAVEIFDIRTY = 0;
        public const int OLEIVERB_DISCARDUNDOSTATE = -6;
        public const int OLEIVERB_HIDE = -3;
        public const int OLEIVERB_INPLACEACTIVATE = -5;
        public const int OLEIVERB_PRIMARY = 0;
        public const int OLEIVERB_PROPERTIES = -7;
        public const int OLEIVERB_SHOW = -1;
        public const int OLEIVERB_UIACTIVATE = -4;

        public const int OLEMISC_ACTIVATEWHENVISIBLE = 0x0000100,
                         OLEMISC_ACTSLIKEBUTTON = 0x00001000;

        public const int OLEMISC_INSIDEOUT = 0x00000080;
        public const int OLEMISC_RECOMPOSEONRESIZE = 0x00000001;
        public const int OLEMISC_SETCLIENTSITEFIRST = 0x00020000;

        public const int OLE_E_NOCONNECTION = unchecked((int) 0x80040004),
                         OLE_E_PROMPTSAVECANCELLED = unchecked((int) 0x8004000C);

        public const int PATCOPY = 0x00F00021,
                         PATINVERT = 0x005A0049;

        public const int PBM_SETBARCOLOR = (0x0400 + 9),
                         PBM_SETBKCOLOR = (0x2000 + 1);

        public const int PBM_SETPOS = (0x0400 + 2);
        public const int PBM_SETRANGE = (0x0400 + 1);
        public const int PBM_SETRANGE32 = (0x0400 + 6);
        public const int PBM_SETSTEP = (0x0400 + 4);
        public const int PBS_SMOOTH = 0x01;
        public const int PDERR_CREATEICFAILURE = 0x100A;
        public const int PDERR_DEFAULTDIFFERENT = 0x100C;
        public const int PDERR_DNDMMISMATCH = 0x1009;

        public const int PDERR_GETDEVMODEFAIL = 0x1005,
                         PDERR_INITFAILURE = 0x1006;

        public const int PDERR_LOADDRVFAILURE = 0x1004;

        public const int PDERR_NODEFAULTPRN = 0x1008;

        public const int PDERR_NODEVICES = 0x1007;
        public const int PDERR_PARSEFAILURE = 0x1002;

        public const int PDERR_PRINTERNOTFOUND = 0x100B;

        public const int PDERR_RETDEFFAILURE = 0x1003;
        public const int PDERR_SETUPFAILURE = 0x1001;

        public const int PD_COLLATE = 0x00000010;

        public const int PD_DISABLEPRINTTOFILE = 0x00080000;

        public const int PD_ENABLEPRINTHOOK = 0x00001000;
        public const int PD_NOCURRENTPAGE = 0x00800000;

        public const int PD_NONETWORKBUTTON = 0x00200000;

        public const int PD_NOPAGENUMS = 0x00000008;
        public const int PD_NOSELECTION = 0x00000004;

        public const int PD_PRINTTOFILE = 0x00000020,
                         PD_SHOWHELP = 0x00000800;

        public const int PLANES = 14;
        public const int PM_NOREMOVE = 0x0000;
        public const int PM_NOYIELD = 0x0002;
        public const int PM_REMOVE = 0x0001;
        public const int PRF_CHECKVISIBLE = 0x00000001;
        public const int PRF_CHILDREN = 0x00000010;

        public const int PRF_CLIENT = 0x00000004,
                         PRF_ERASEBKGND = 0x00000008;

        public const int PRF_NONCLIENT = 0x00000002;

        public const int PSD_DISABLEMARGINS = 0x00000010;

        public const int PSD_DISABLEORIENTATION = 0x00000100,
                         PSD_DISABLEPAPER = 0x00000200;

        public const int PSD_DISABLEPRINTER = 0x00000020;

        public const int PSD_ENABLEPAGESETUPHOOK = 0x00002000;

        public const int PSD_INHUNDREDTHSOFMILLIMETERS = 0x00000008;
        public const int PSD_MARGINS = 0x00000002;
        public const int PSD_MINMARGINS = 0x00000001;

        public const int PSD_NONETWORKBUTTON = 0x00200000;

        public const int PSD_SHOWHELP = 0x00000800;

        public const int PSM_SETFINISHTEXTA = (0x0400 + 115),
                         PSM_SETFINISHTEXTW = (0x0400 + 121);

        public const int PSM_SETTITLEA = (0x0400 + 111),
                         PSM_SETTITLEW = (0x0400 + 120);

        public const int PS_DOT = 2;
        public const int PS_SOLID = 0;

        public const int QS_ALLEVENTS = QS_INPUT | QS_POSTMESSAGE | QS_TIMER | QS_PAINT | QS_HOTKEY,
                         QS_ALLINPUT = QS_INPUT | QS_POSTMESSAGE | QS_TIMER | QS_PAINT | QS_HOTKEY | QS_SENDMESSAGE;

        public const int QS_ALLPOSTMESSAGE = 0x0100;
        public const int QS_HOTKEY = 0x0080;
        public const int QS_INPUT = QS_MOUSE | QS_KEY;

        public const int QS_KEY = 0x0001;
        public const int QS_MOUSE = QS_MOUSEMOVE | QS_MOUSEBUTTON;

        public const int QS_MOUSEBUTTON = 0x0004;

        public const int QS_MOUSEMOVE = 0x0002;

        public const int QS_PAINT = 0x0020;

        public const int QS_POSTMESSAGE = 0x0008;

        public const int QS_SENDMESSAGE = 0x0040;

        public const int QS_TIMER = 0x0010;

        public const int RB_INSERTBANDA = (0x0400 + 1),
                         RB_INSERTBANDW = (0x0400 + 10);

        public const int RDW_ALLCHILDREN = 0x0080;
        public const int RDW_ERASE = 0x0004;
        public const int RDW_FRAME = 0x0400;
        public const int RDW_INVALIDATE = 0x0001;
        public const int RGN_AND = 1;
        public const int RGN_DIFF = 4;
        public const int RPC_E_CANTCALLOUT_ININPUTSYNCCALL = unchecked((int) 0x8001010D);
        public const int RPC_E_CHANGED_MODE = unchecked((int) 0x80010106);
        public const int SBARS_SIZEGRIP = 0x0100;

        public const int SBS_HORZ = 0x0000,
                         SBS_VERT = 0x0001;

        public const int SBT_NOBORDERS = 0x0100;
        public const int SBT_OWNERDRAW = 0x1000;

        public const int SBT_POPOUT = 0x0200,
                         SBT_RTLREADING = 0x0400;

        public const int SB_BOTTOM = 7;

        public const int SB_CTL = 2;

        public const int SB_ENDSCROLL = 8;
        public const int SB_GETRECT = (0x0400 + 10);
        public const int SB_GETTEXTA = (0x0400 + 2);

        public const int SB_GETTEXTLENGTHA = (0x0400 + 3),
                         SB_GETTEXTLENGTHW = (0x0400 + 12);

        public const int SB_GETTEXTW = (0x0400 + 13);

        public const int SB_GETTIPTEXTA = (0x0400 + 18),
                         SB_GETTIPTEXTW = (0x0400 + 19);

        public const int SB_HORZ = 0;
        public const int SB_LEFT = 6;

        public const int SB_LINEDOWN = 1;

        public const int SB_LINELEFT = 0;

        public const int SB_LINERIGHT = 1;

        public const int SB_LINEUP = 0;

        public const int SB_PAGEDOWN = 3;

        public const int SB_PAGELEFT = 2;

        public const int SB_PAGERIGHT = 3;

        public const int SB_PAGEUP = 2;
        public const int SB_RIGHT = 7;
        public const int SB_SETICON = (0x0400 + 15);
        public const int SB_SETPARTS = (0x0400 + 4);

        public const int SB_SETTEXTA = (0x0400 + 1),
                         SB_SETTEXTW = (0x0400 + 11);

        public const int SB_SETTIPTEXTA = (0x0400 + 16),
                         SB_SETTIPTEXTW = (0x0400 + 17);

        public const int SB_SIMPLE = (0x0400 + 9);

        public const int SB_THUMBPOSITION = 4,
                         SB_THUMBTRACK = 5;

        public const int SB_TOP = 6;

        public const int SB_VERT = 1;

        public const int SC_CLOSE = 0xF060,
                         SC_KEYMENU = 0xF100;

        public const int SC_MAXIMIZE = 0xF030;
        public const int SC_MINIMIZE = 0xF020;
        public const int SC_MOVE = 0xF010;
        public const int SC_RESTORE = 0xF120;
        public const int SC_SIZE = 0xF000;
        public const int SHGFI_ADDOVERLAYS = 0x000000020;
        public const int SHGFI_ATTRIBUTES = 0x000000800;
        public const int SHGFI_ATTR_SPECIFIED = 0x000020000;
        public const int SHGFI_DISPLAYNAME = 0x000000200;
        public const int SHGFI_EXETYPE = 0x000002000;
        public const int SHGFI_ICON = 0x000000100;
        public const int SHGFI_ICONLOCATION = 0x000001000;
        public const int SHGFI_LARGEICON = 0x000000000;
        public const int SHGFI_LINKOVERLAY = 0x000008000;
        public const int SHGFI_OPENICON = 0x000000002;
        public const int SHGFI_OVERLAYINDEX = 0x000000040;
        public const int SHGFI_PIDL = 0x000000008;
        public const int SHGFI_SELECTED = 0x000010000;
        public const int SHGFI_SHELLICONSIZE = 0x000000004;
        public const int SHGFI_SMALLICON = 0x000000001;
        public const int SHGFI_SYSICONINDEX = 0x000004000;
        public const int SHGFI_TYPENAME = 0x000000400;
        public const int SHGFI_USEFILEATTRIBUTES = 0x000000010;
        public const int SHGFP_TYPE_CURRENT = 0;
        public const int SIF_ALL = (0x0001 | 0x0002 | 0x0004 | 0x0010);

        public const int SIF_PAGE = 0x0002,
                         SIF_POS = 0x0004;

        public const int SIF_RANGE = 0x0001;
        public const int SIF_TRACKPOS = 0x0010;
        public const int SM_ARRANGE = 56;
        public const int SM_CLEANBOOT = 67;
        public const int SM_CMONITORS = 80;
        public const int SM_CMOUSEBUTTONS = 43;

        public const int SM_CXBORDER = 5;

        public const int SM_CXCURSOR = 13;

        public const int SM_CXDOUBLECLK = 36;
        public const int SM_CXDRAG = 68;
        public const int SM_CXEDGE = 45;
        public const int SM_CXFIXEDFRAME = 7;
        public const int SM_CXFOCUSBORDER = 83;
        public const int SM_CXFRAME = 32;

        public const int SM_CXHSCROLL = 21;

        public const int SM_CXHTHUMB = 10,
                         SM_CXICON = 11;

        public const int SM_CXICONSPACING = 38;
        public const int SM_CXMAXIMIZED = 61;
        public const int SM_CXMAXTRACK = 59;
        public const int SM_CXMENUCHECK = 71;
        public const int SM_CXMENUSIZE = 54;

        public const int SM_CXMIN = 28;

        public const int SM_CXMINIMIZED = 57;
        public const int SM_CXMINSPACING = 47;

        public const int SM_CXMINTRACK = 34;

        public const int SM_CXSCREEN = 0;
        public const int SM_CXSIZE = 30;
        public const int SM_CXSIZEFRAME = SM_CXFRAME;
        public const int SM_CXSMICON = 49;
        public const int SM_CXSMSIZE = 52;
        public const int SM_CXVIRTUALSCREEN = 78;
        public const int SM_CXVSCROLL = 2;
        public const int SM_CYBORDER = 6;
        public const int SM_CYCAPTION = 4;
        public const int SM_CYCURSOR = 14;

        public const int SM_CYDOUBLECLK = 37;

        public const int SM_CYDRAG = 69;

        public const int SM_CYEDGE = 46;

        public const int SM_CYFIXEDFRAME = 8;
        public const int SM_CYFOCUSBORDER = 84;
        public const int SM_CYFRAME = 33;
        public const int SM_CYHSCROLL = 3;
        public const int SM_CYICON = 12;
        public const int SM_CYICONSPACING = 39;
        public const int SM_CYKANJIWINDOW = 18;
        public const int SM_CYMAXIMIZED = 62;
        public const int SM_CYMAXTRACK = 60;
        public const int SM_CYMENU = 15;
        public const int SM_CYMENUCHECK = 72;
        public const int SM_CYMENUSIZE = 55;
        public const int SM_CYMIN = 29;
        public const int SM_CYMINIMIZED = 58;

        public const int SM_CYMINSPACING = 48;

        public const int SM_CYMINTRACK = 35;
        public const int SM_CYSCREEN = 1;
        public const int SM_CYSIZE = 31;
        public const int SM_CYSIZEFRAME = SM_CYFRAME;

        public const int SM_CYSMCAPTION = 51;

        public const int SM_CYSMICON = 50;

        public const int SM_CYSMSIZE = 53;

        public const int SM_CYVIRTUALSCREEN = 79;
        public const int SM_CYVSCROLL = 20;
        public const int SM_CYVTHUMB = 9;
        public const int SM_DBCSENABLED = 42;
        public const int SM_DEBUG = 22;
        public const int SM_MENUDROPALIGNMENT = 40;

        public const int SM_MIDEASTENABLED = 74;

        public const int SM_MOUSEPRESENT = 19;

        public const int SM_MOUSEWHEELPRESENT = 75;

        public const int SM_NETWORK = 63;
        public const int SM_PENWINDOWS = 41;
        public const int SM_REMOTESESSION = 0x1000;
        public const int SM_SAMEDISPLAYFORMAT = 81;
        public const int SM_SECURE = 44;
        public const int SM_SHOWSOUNDS = 70;
        public const int SM_SWAPBUTTON = 23;

        public const int SM_XVIRTUALSCREEN = 76,
                         SM_YVIRTUALSCREEN = 77;

        public const int SND_ASYNC = 0x0001;

        public const int SND_FILENAME = 0x00020000;

        public const int SND_LOOP = 0x0008;
        public const int SND_MEMORY = 0x0004;
        public const int SND_NODEFAULT = 0x0002;

        public const int SND_NOSTOP = 0x0010;

        public const int SND_PURGE = 0x0040;
        public const int SND_SYNC = 0000;
        public const int SORT_DEFAULT = 0x0;

        public const int SPI_GETACTIVEWINDOWTRACKING = 0x1000,
                         SPI_GETACTIVEWNDTRKTIMEOUT = 0x2002,
                         SPI_GETANIMATION = 0x0048,
                         SPI_GETBORDER = 0x0005,
                         SPI_GETCARETWIDTH = 0x2006;

        public const int SPI_GETCOMBOBOXANIMATION = 0x1004;
        public const int SPI_GETDEFAULTINPUTLANG = 89;
        public const int SPI_GETDRAGFULLWINDOWS = 38;

        public const int SPI_GETDROPSHADOW = 0x1024,
                         SPI_GETFLATMENU = 0x1022;

        public const int SPI_GETFONTSMOOTHING = 0x004A;

        public const int SPI_GETFONTSMOOTHINGCONTRAST = 0x200C;

        public const int SPI_GETFONTSMOOTHINGTYPE = 0x200A;
        public const int SPI_GETGRADIENTCAPTIONS = 0x1008;
        public const int SPI_GETHIGHCONTRAST = 66;
        public const int SPI_GETHOTTRACKING = 0x100E;

        public const int SPI_GETICONTITLELOGFONT = 0x001F;

        public const int SPI_GETICONTITLEWRAP = 0x0019;

        public const int SPI_GETKEYBOARDCUES = 0x100A,
                         SPI_GETKEYBOARDDELAY = 0x0016,
                         SPI_GETKEYBOARDPREF = 0x0044,
                         SPI_GETKEYBOARDSPEED = 0x000A;

        public const int SPI_GETLISTBOXSMOOTHSCROLLING = 0x1006,
                         SPI_GETMENUANIMATION = 0x1002;

        public const int SPI_GETMENUDROPALIGNMENT = 0x001B,
                         SPI_GETMENUFADE = 0x1012,
                         SPI_GETMENUSHOWDELAY = 0x006A;

        public const int SPI_GETMOUSEHOVERHEIGHT = 0x0064,
                         SPI_GETMOUSEHOVERTIME = 0x0066;

        public const int SPI_GETMOUSEHOVERWIDTH = 0x0062;
        public const int SPI_GETMOUSESPEED = 0x0070;
        public const int SPI_GETNONCLIENTMETRICS = 41;

        public const int SPI_GETSELECTIONFADE = 0x1014;

        public const int SPI_GETSNAPTODEFBUTTON = 95;

        public const int SPI_GETTOOLTIPANIMATION = 0x1016,
                         SPI_GETUIEFFECTS = 0x103E;

        public const int SPI_GETWHEELSCROLLLINES = 104;

        public const int SPI_GETWORKAREA = 48;

        public const int SPI_ICONHORIZONTALSPACING = 0x000D,
                         SPI_ICONVERTICALSPACING = 0x0018;

        public const int SRCCOPY = 0x00CC0020;

        public const int SS_CENTER = 0x00000001;
        public const int SS_LEFT = 0x00000000;
        public const int SS_NOPREFIX = 0x00000080;
        public const int SS_OWNERDRAW = 0x0000000D;
        public const int SS_RIGHT = 0x00000002;
        public const int SS_SUNKEN = 0x00001000;
        public const int STARTF_USESHOWWINDOW = 0x00000001;

        public const int STATFLAG_DEFAULT = 0x0,
                         STATFLAG_NONAME = 0x1,
                         STATFLAG_NOOPEN = 0x2;

        public const int STGC_DANGEROUSLYCOMMITMERELYTODISKCACHE = 0x4;

        public const int STGC_DEFAULT = 0x0;

        public const int STGC_ONLYIFCURRENT = 0x2;

        public const int STGC_OVERWRITE = 0x1;
        public const int STGM_CONVERT = 0x00020000;
        public const int STGM_CREATE = 0x00001000;
        public const int STGM_DELETEONRELEASE = 0x04000000;
        public const int STGM_READ = 0x00000000;

        public const int STGM_READWRITE = 0x00000002,
                         STGM_SHARE_EXCLUSIVE = 0x00000010;

        public const int STGM_TRANSACTED = 0x00010000;
        public const int STGM_WRITE = 0x00000001;
        public const int STG_E_ACCESSDENIED = unchecked((int) 0x80030005);
        public const int STG_E_DISKISWRITEPROTECTED = unchecked((int) 0x80030013);
        public const int STG_E_FILENOTFOUND = unchecked((int) 0x80030002);
        public const int STG_E_INSUFFICIENTMEMORY = unchecked((int) 0x80030008);
        public const int STG_E_INVALIDFUNCTION = unchecked((int) 0x80030001);
        public const int STG_E_INVALIDHANDLE = unchecked((int) 0x80030006);
        public const int STG_E_INVALIDPOINTER = unchecked((int) 0x80030009);
        public const int STG_E_LOCKVIOLATION = unchecked((int) 0x80030021);
        public const int STG_E_NOMOREFILES = unchecked((int) 0x80030012);
        public const int STG_E_PATHNOTFOUND = unchecked((int) 0x80030003);
        public const int STG_E_READFAULT = unchecked((int) 0x8003001E);
        public const int STG_E_SEEKERROR = unchecked((int) 0x80030019);
        public const int STG_E_SHAREVIOLATION = unchecked((int) 0x80030020);
        public const int STG_E_TOOMANYOPENFILES = unchecked((int) 0x80030004);
        public const int STG_E_WRITEFAULT = unchecked((int) 0x8003001D);

        public const int STREAM_SEEK_CUR = 0x1,
                         STREAM_SEEK_END = 0x2;

        public const int STREAM_SEEK_SET = 0x0;
        public const int SUBLANG_DEFAULT = 0x01;
        public const int SWP_DRAWFRAME = 0x0020;
        public const int SWP_HIDEWINDOW = 0x0080;
        public const int SWP_NOACTIVATE = 0x0010;
        public const int SWP_NOMOVE = 0x0002;
        public const int SWP_NOSIZE = 0x0001;
        public const int SWP_NOZORDER = 0x0004;
        public const int SWP_SHOWWINDOW = 0x0040;
        public const int SW_ERASE = 0x0004;
        public const int SW_HIDE = 0;
        public const int SW_INVALIDATE = 0x0002;
        public const int SW_MAX = 10;
        public const int SW_MAXIMIZE = 3;
        public const int SW_MINIMIZE = 6;
        public const int SW_NORMAL = 1;
        public const int SW_RESTORE = 9;
        public const int SW_SCROLLCHILDREN = 0x0001;
        public const int SW_SHOW = 5;
        public const int SW_SHOWMAXIMIZED = 3;
        public const int SW_SHOWMINIMIZED = 2;

        public const int SW_SHOWMINNOACTIVE = 7,
                         SW_SHOWNA = 8;

        public const int SW_SHOWNOACTIVATE = 4;
        public const int SW_SMOOTHSCROLL = 0x0010;

        public const int S_FALSE = 0x00000001;
        public const int S_OK = 0x00000000;
        public const int TBIF_COMMAND = 0x00000020;
        public const int TBIF_IMAGE = 0x00000001;
        public const int TBIF_SIZE = 0x00000040;

        public const int TBIF_STATE = 0x00000004,
                         TBIF_STYLE = 0x00000008;

        public const int TBIF_TEXT = 0x00000002;
        public const int TBM_GETPOS = (0x0400);
        public const int TBM_SETLINESIZE = (0x0400 + 23);
        public const int TBM_SETPAGESIZE = (0x0400 + 21);

        public const int TBM_SETPOS = (0x0400 + 5),
                         TBM_SETRANGE = (0x0400 + 6);

        public const int TBM_SETRANGEMAX = (0x0400 + 8);
        public const int TBM_SETRANGEMIN = (0x0400 + 7);
        public const int TBM_SETTIC = (0x0400 + 4);
        public const int TBM_SETTICFREQ = (0x0400 + 20);
        public const int TBN_DROPDOWN = ((0 - 700) - 10);

        public const int TBN_GETBUTTONINFOA = ((0 - 700) - 0),
                         TBN_GETBUTTONINFOW = ((0 - 700) - 20);

        public const int TBN_GETDISPINFOA = ((0 - 700) - 16),
                         TBN_GETDISPINFOW = ((0 - 700) - 17),
                         TBN_GETINFOTIPA = ((0 - 700) - 18),
                         TBN_GETINFOTIPW = ((0 - 700) - 19);

        public const int TBN_QUERYINSERT = ((0 - 700) - 6);

        public const int TBSTATE_CHECKED = 0x01,
                         TBSTATE_ENABLED = 0x04,
                         TBSTATE_HIDDEN = 0x08,
                         TBSTATE_INDETERMINATE = 0x10,
                         TBSTYLE_BUTTON = 0x00;

        public const int TBSTYLE_CHECK = 0x02,
                         TBSTYLE_DROPDOWN = 0x08;

        public const int TBSTYLE_EX_DRAWDDARROWS = 0x00000001;

        public const int TBSTYLE_FLAT = 0x0800,
                         TBSTYLE_LIST = 0x1000;

        public const int TBSTYLE_SEP = 0x01;
        public const int TBSTYLE_TOOLTIPS = 0x0100;
        public const int TBSTYLE_WRAPPABLE = 0x0200;
        public const int TBS_AUTOTICKS = 0x0001;
        public const int TBS_BOTH = 0x0008;
        public const int TBS_BOTTOM = 0x0000;
        public const int TBS_NOTICKS = 0x0010;
        public const int TBS_TOP = 0x0004;
        public const int TBS_VERT = 0x0002;

        public const int TB_ADDBUTTONSA = (0x0400 + 20),
                         TB_ADDBUTTONSW = (0x0400 + 68);

        public const int TB_ADDSTRINGA = (0x0400 + 28),
                         TB_ADDSTRINGW = (0x0400 + 77);

        public const int TB_AUTOSIZE = (0x0400 + 33);

        public const int TB_BOTTOM = 7;
        public const int TB_BUTTONSTRUCTSIZE = (0x0400 + 30);
        public const int TB_DELETEBUTTON = (0x0400 + 22);
        public const int TB_ENABLEBUTTON = (0x0400 + 1);
        public const int TB_ENDTRACK = 8;
        public const int TB_GETBUTTON = (0x0400 + 23);
        public const int TB_GETBUTTONINFOA = (0x0400 + 65);
        public const int TB_GETBUTTONINFOW = (0x0400 + 63);
        public const int TB_GETBUTTONSIZE = (0x0400 + 58);

        public const int TB_GETBUTTONTEXTA = (0x0400 + 45),
                         TB_GETBUTTONTEXTW = (0x0400 + 75);

        public const int TB_GETRECT = (0x0400 + 51);

        public const int TB_GETROWS = (0x0400 + 40);

        public const int TB_INSERTBUTTONA = (0x0400 + 21),
                         TB_INSERTBUTTONW = (0x0400 + 67);

        public const int TB_ISBUTTONCHECKED = (0x0400 + 10),
                         TB_ISBUTTONINDETERMINATE = (0x0400 + 13);

        public const int TB_LINEDOWN = 1;
        public const int TB_LINEUP = 0;

        public const int TB_MAPACCELERATORA = (0x0400 + 78);

        public const int TB_MAPACCELERATORW = (0x0400 + 90);

        public const int TB_PAGEDOWN = 3;
        public const int TB_PAGEUP = 2;

        public const int TB_SAVERESTOREA = (0x0400 + 26),
                         TB_SAVERESTOREW = (0x0400 + 76);

        public const int TB_SETBUTTONINFOA = (0x0400 + 66);
        public const int TB_SETBUTTONINFOW = (0x0400 + 64);
        public const int TB_SETBUTTONSIZE = (0x0400 + 31);
        public const int TB_SETEXTENDEDSTYLE = (0x0400 + 84);
        public const int TB_SETIMAGELIST = (0x0400 + 48);

        public const int TB_SETTOOLTIPS = (0x0400 + 36);

        public const int TB_THUMBPOSITION = 4,
                         TB_THUMBTRACK = 5,
                         TB_TOP = 6;

        public const int TCIF_IMAGE = 0x0002;
        public const int TCIF_TEXT = 0x0001;
        public const int TCM_ADJUSTRECT = (0x1300 + 40);
        public const int TCM_DELETEALLITEMS = (0x1300 + 9);
        public const int TCM_DELETEITEM = (0x1300 + 8);
        public const int TCM_GETCURSEL = (0x1300 + 11);
        public const int TCM_GETITEMA = (0x1300 + 5);
        public const int TCM_GETITEMRECT = (0x1300 + 10);
        public const int TCM_GETITEMW = (0x1300 + 60);

        public const int TCM_GETROWCOUNT = (0x1300 + 44),
                         TCM_GETTOOLTIPS = (0x1300 + 45);

        public const int TCM_INSERTITEMA = (0x1300 + 7),
                         TCM_INSERTITEMW = (0x1300 + 62);

        public const int TCM_SETCURSEL = (0x1300 + 12);
        public const int TCM_SETIMAGELIST = (0x1300 + 3);
        public const int TCM_SETITEMA = (0x1300 + 6);
        public const int TCM_SETITEMSIZE = (0x1300 + 41);
        public const int TCM_SETITEMW = (0x1300 + 61);
        public const int TCM_SETPADDING = (0x1300 + 43);

        public const int TCM_SETTOOLTIPS = (0x1300 + 46),
                         TCN_SELCHANGE = ((0 - 550) - 1),
                         TCN_SELCHANGING = ((0 - 550) - 2);

        public const int TCS_BOTTOM = 0x0002;
        public const int TCS_BUTTONS = 0x0100;
        public const int TCS_FIXEDWIDTH = 0x0400;

        public const int TCS_FLATBUTTONS = 0x0008,
                         TCS_HOTTRACK = 0x0040;

        public const int TCS_MULTILINE = 0x0200;
        public const int TCS_OWNERDRAWFIXED = 0x2000;
        public const int TCS_RAGGEDRIGHT = 0x0800;
        public const int TCS_RIGHT = 0x0002;
        public const int TCS_RIGHTJUSTIFY = 0x0000;
        public const int TCS_TABS = 0x0000;
        public const int TCS_TOOLTIPS = 0x4000;
        public const int TCS_VERTICAL = 0x0080;

        public const int TME_HOVER = 0x00000001,
                         TME_LEAVE = 0x00000002;

        public const int TO_ADVANCEDTYPOGRAPHY = 1;

        public const int TPM_LEFTALIGN = 0x0000;
        public const int TPM_LEFTBUTTON = 0x0000;
        public const int TPM_VERTICAL = 0x0040;
        public const int TRANSPARENT = 1;
        public const int TTDT_AUTOMATIC = 0;

        public const int TTDT_AUTOPOP = 2,
                         TTDT_INITIAL = 3;

        public const int TTDT_RESHOW = 1;
        public const int TTF_ABSOLUTE = 0x0080;
        public const int TTF_CENTERTIP = 0x0002;

        public const int TTF_IDISHWND = 0x0001,
                         TTF_RTLREADING = 0x0004;

        public const int TTF_SUBCLASS = 0x0010;

        public const int TTF_TRACK = 0x0020;

        public const int TTF_TRANSPARENT = 0x0100;

        public const int TTI_WARNING = 2;

        public const int TTM_ACTIVATE = (0x0400 + 1);

        public const int TTM_ADDTOOLA = (0x0400 + 4),
                         TTM_ADDTOOLW = (0x0400 + 50);

        public const int TTM_ADJUSTRECT = (0x400 + 31);

        public const int TTM_DELTOOLA = (0x0400 + 5),
                         TTM_DELTOOLW = (0x0400 + 51);

        public const int TTM_ENUMTOOLSA = (0x0400 + 14),
                         TTM_ENUMTOOLSW = (0x0400 + 58),
                         TTM_GETCURRENTTOOLA = (0x0400 + 15),
                         TTM_GETCURRENTTOOLW = (0x0400 + 59);

        public const int TTM_GETDELAYTIME = (0x0400 + 21);

        public const int TTM_GETTEXTA = (0x0400 + 11),
                         TTM_GETTEXTW = (0x0400 + 56);

        public const int TTM_GETTIPBKCOLOR = (0x0400 + 22);

        public const int TTM_GETTIPTEXTCOLOR = (0x0400 + 23),
                         TTM_GETTOOLINFOA = (0x0400 + 8),
                         TTM_GETTOOLINFOW = (0x0400 + 53);

        public const int TTM_HITTESTA = (0x0400 + 10),
                         TTM_HITTESTW = (0x0400 + 55);

        public const int TTM_NEWTOOLRECTA = (0x0400 + 6),
                         TTM_NEWTOOLRECTW = (0x0400 + 52);

        public const int TTM_POP = (0x0400 + 28);

        public const int TTM_RELAYEVENT = (0x0400 + 7);

        public const int TTM_SETDELAYTIME = (0x0400 + 3);
        public const int TTM_SETMAXTIPWIDTH = (0x0400 + 24);

        public const int TTM_SETTIPBKCOLOR = (0x0400 + 19),
                         TTM_SETTIPTEXTCOLOR = (0x0400 + 20);

        public const int TTM_SETTITLEA = (WM_USER + 32),
                         TTM_SETTITLEW = (WM_USER + 33);

        public const int TTM_SETTOOLINFOA = (0x0400 + 9),
                         TTM_SETTOOLINFOW = (0x0400 + 54);

        public const int TTM_TRACKACTIVATE = (0x0400 + 17),
                         TTM_TRACKPOSITION = (0x0400 + 18);

        public const int TTM_UPDATE = (0x0400 + 29),
                         TTM_UPDATETIPTEXTA = (0x0400 + 12),
                         TTM_UPDATETIPTEXTW = (0x0400 + 57);

        public const int TTM_WINDOWFROMPOINT = (0x0400 + 16);

        public const int TTN_GETDISPINFOA = ((0 - 520) - 0),
                         TTN_GETDISPINFOW = ((0 - 520) - 10);

        public const int TTN_NEEDTEXTA = ((0 - 520) - 0),
                         TTN_NEEDTEXTW = ((0 - 520) - 10);

        public const int TTN_POP = ((0 - 520) - 2);
        public const int TTN_SHOW = ((0 - 520) - 1);
        public const int TTS_ALWAYSTIP = 0x01;
        public const int TTS_BALLOON = 0x40;

        public const int TTS_NOANIMATE = 0x10,
                         TTS_NOFADE = 0x20;

        public const int TTS_NOPREFIX = 0x02;
        public const int TVC_BYKEYBOARD = 0x0002;
        public const int TVC_BYMOUSE = 0x0001;
        public const int TVC_UNKNOWN = 0x0000;

        public const int TVE_COLLAPSE = 0x0001,
                         TVE_EXPAND = 0x0002;

        public const int TVGN_CARET = 0x0009;
        public const int TVGN_FIRSTVISIBLE = 0x0005;
        public const int TVGN_NEXT = 0x0001;
        public const int TVGN_NEXTVISIBLE = 0x0006;
        public const int TVGN_PREVIOUS = 0x0002;
        public const int TVGN_PREVIOUSVISIBLE = 0x0007;

        public const int TVHT_ABOVE = 0x0100,
                         TVHT_BELOW = 0x0200;

        public const int TVHT_NOWHERE = 0x0001;
        public const int TVHT_ONITEM = (TVHT_ONITEMICON | TVHT_ONITEMLABEL | TVHT_ONITEMSTATEICON);
        public const int TVHT_ONITEMBUTTON = 0x0010;
        public const int TVHT_ONITEMICON = 0x0002;
        public const int TVHT_ONITEMINDENT = 0x0008;
        public const int TVHT_ONITEMLABEL = 0x0004;

        public const int TVHT_ONITEMRIGHT = 0x0020,
                         TVHT_ONITEMSTATEICON = 0x0040;

        public const int TVHT_TOLEFT = 0x0800;
        public const int TVHT_TORIGHT = 0x0400;
        public const int TVIF_HANDLE = 0x0010;

        public const int TVIF_IMAGE = 0x0002,
                         TVIF_PARAM = 0x0004;

        public const int TVIF_SELECTEDIMAGE = 0x0020;

        public const int TVIF_STATE = 0x0008;
        public const int TVIF_TEXT = 0x0001;

        public const int TVIS_EXPANDED = 0x0020,
                         TVIS_EXPANDEDONCE = 0x0040;

        public const int TVIS_SELECTED = 0x0002;

        public const int TVIS_STATEIMAGEMASK = 0xF000;

        public const int TVI_FIRST = (unchecked((int) 0xFFFF0001));

        public const int TVI_ROOT = (unchecked((int) 0xFFFF0000));

        public const int TVM_DELETEITEM = (0x1100 + 1);

        public const int TVM_EDITLABELA = (0x1100 + 14),
                         TVM_EDITLABELW = (0x1100 + 65);

        public const int TVM_ENDEDITLABELNOW = (0x1100 + 22);
        public const int TVM_ENSUREVISIBLE = (0x1100 + 20);

        public const int TVM_EXPAND = (0x1100 + 2);

        public const int TVM_GETEDITCONTROL = (0x1100 + 15);

        public const int TVM_GETINDENT = (0x1100 + 6);

        public const int TVM_GETISEARCHSTRINGA = (0x1100 + 23),
                         TVM_GETISEARCHSTRINGW = (0x1100 + 64);

        public const int TVM_GETITEMA = (0x1100 + 12);

        public const int TVM_GETITEMHEIGHT = (0x1100 + 28);
        public const int TVM_GETITEMRECT = (0x1100 + 4);

        public const int TVM_GETITEMW = (0x1100 + 62);

        public const int TVM_GETLINECOLOR = (TV_FIRST + 41);
        public const int TVM_GETNEXTITEM = (0x1100 + 10);

        public const int TVM_GETVISIBLECOUNT = (0x1100 + 16),
                         TVM_HITTEST = (0x1100 + 17);

        public const int TVM_INSERTITEMA = (0x1100 + 0),
                         TVM_INSERTITEMW = (0x1100 + 50);

        public const int TVM_SELECTITEM = (0x1100 + 11);
        public const int TVM_SETBKCOLOR = (TV_FIRST + 29);
        public const int TVM_SETIMAGELIST = (0x1100 + 9);
        public const int TVM_SETINDENT = (0x1100 + 7);
        public const int TVM_SETITEMA = (0x1100 + 13);

        public const int TVM_SETITEMHEIGHT = (0x1100 + 27);

        public const int TVM_SETITEMW = (0x1100 + 63);
        public const int TVM_SETLINECOLOR = (TV_FIRST + 40);
        public const int TVM_SETTEXTCOLOR = (TV_FIRST + 30);
        public const int TVM_SETTOOLTIPS = (TV_FIRST + 24);
        public const int TVM_SORTCHILDRENCB = (TV_FIRST + 21);

        public const int TVN_BEGINDRAGA = ((0 - 400) - 7),
                         TVN_BEGINDRAGW = ((0 - 400) - 56);

        public const int TVN_BEGINLABELEDITA = ((0 - 400) - 10),
                         TVN_BEGINLABELEDITW = ((0 - 400) - 59);

        public const int TVN_BEGINRDRAGA = ((0 - 400) - 8),
                         TVN_BEGINRDRAGW = ((0 - 400) - 57);

        public const int TVN_ENDLABELEDITA = ((0 - 400) - 11),
                         TVN_ENDLABELEDITW = ((0 - 400) - 60);

        public const int TVN_GETDISPINFOA = ((0 - 400) - 3),
                         TVN_GETDISPINFOW = ((0 - 400) - 52);

        public const int TVN_GETINFOTIPA = ((0 - 400) - 13),
                         TVN_GETINFOTIPW = ((0 - 400) - 14);

        public const int TVN_ITEMEXPANDEDA = ((0 - 400) - 6),
                         TVN_ITEMEXPANDEDW = ((0 - 400) - 55);

        public const int TVN_ITEMEXPANDINGA = ((0 - 400) - 5),
                         TVN_ITEMEXPANDINGW = ((0 - 400) - 54);

        public const int TVN_SELCHANGEDA = ((0 - 400) - 2),
                         TVN_SELCHANGEDW = ((0 - 400) - 51);

        public const int TVN_SELCHANGINGA = ((0 - 400) - 1),
                         TVN_SELCHANGINGW = ((0 - 400) - 50);

        public const int TVN_SETDISPINFOA = ((0 - 400) - 4),
                         TVN_SETDISPINFOW = ((0 - 400) - 53);

        public const int TVSIL_STATE = 2;

        public const int TVS_CHECKBOXES = 0x0100;
        public const int TVS_EDITLABELS = 0x0008;
        public const int TVS_FULLROWSELECT = 0x1000;

        public const int TVS_HASBUTTONS = 0x0001,
                         TVS_HASLINES = 0x0002;

        public const int TVS_INFOTIP = 0x0800;
        public const int TVS_LINESATROOT = 0x0004;
        public const int TVS_NOTOOLTIPS = 0x0080;
        public const int TVS_RTLREADING = 0x0040;
        public const int TVS_SHOWSELALWAYS = 0x0020;
        public const int TVS_TRACKSELECT = 0x0200;
        public const int TV_FIRST = 0x1100;
        public const int TYMED_NULL = 0;
        public const int UISF_HIDEACCEL = 0x2;
        public const int UISF_HIDEFOCUS = 0x1;

        public const int UIS_CLEAR = 2,
                         UIS_INITIALIZE = 3;

        public const int UIS_SET = 1;
        public const int UOI_FLAGS = 1;
        public const int USERCLASSTYPE_APPNAME = 3;

        public const int USERCLASSTYPE_FULL = 1,
                         USERCLASSTYPE_SHORT = 2;

        public const int VIEW_E_DRAW = unchecked((int) 0x80040140);
        public const int VK_CONTROL = 0x11;
        public const int VK_DOWN = 0x28;
        public const int VK_ESCAPE = 0x1B;

        public const int VK_LEFT = 0x25;

        public const int VK_MENU = 0x12;

        public const int VK_RIGHT = 0x27;

        public const int VK_SHIFT = 0x10;

        public const int VK_TAB = 0x09;
        public const int VK_UP = 0x26;

        public const int WAVE_FORMAT_ADPCM = 0x0002,
                         WAVE_FORMAT_IEEE_FLOAT = 0x0003;

        public const int WAVE_FORMAT_PCM = 0x0001;

        public const int WA_ACTIVE = 1,
                         WA_CLICKACTIVE = 2;

        public const int WA_INACTIVE = 0;
        public const int WHEEL_DELTA = 120;

        public const int WH_GETMESSAGE = 3;

        public const int WH_JOURNALPLAYBACK = 1;

        public const int WH_MOUSE = 7;

        public const int WM_ACTIVATE = 0x0006;
        public const int WM_ACTIVATEAPP = 0x001C;

        public const int WM_AFXFIRST = 0x0360,
                         WM_AFXLAST = 0x037F;

        public const int WM_APP = unchecked(0x8000);
        public const int WM_ASKCBFORMATNAME = 0x030C;
        public const int WM_CANCELJOURNAL = 0x004B;
        public const int WM_CANCELMODE = 0x001F;
        public const int WM_CAPTURECHANGED = 0x0215;
        public const int WM_CHANGECBCHAIN = 0x030D;
        public const int WM_CHANGEUISTATE = 0x0127;
        public const int WM_CHAR = 0x0102;
        public const int WM_CHARTOITEM = 0x002F;
        public const int WM_CHILDACTIVATE = 0x0022;
        public const int WM_CHOOSEFONT_GETLOGFONT = (0x0400 + 1);
        public const int WM_CLEAR = 0x0303;
        public const int WM_CLOSE = 0x0010;
        public const int WM_COMMAND = 0x0111;
        public const int WM_COMMNOTIFY = 0x0044;
        public const int WM_COMPACTING = 0x0041;
        public const int WM_COMPAREITEM = 0x0039;
        public const int WM_CONTEXTMENU = 0x007B;
        public const int WM_COPY = 0x0301;
        public const int WM_COPYDATA = 0x004A;

        public const int WM_CREATE = 0x0001;

        public const int WM_CTLCOLOR = 0x0019;

        public const int WM_CTLCOLORBTN = 0x0135,
                         WM_CTLCOLORDLG = 0x0136;

        public const int WM_CTLCOLOREDIT = 0x0133,
                         WM_CTLCOLORLISTBOX = 0x0134;

        public const int WM_CTLCOLORMSGBOX = 0x0132;

        public const int WM_CTLCOLORSCROLLBAR = 0x0137,
                         WM_CTLCOLORSTATIC = 0x0138;

        public const int WM_CUT = 0x0300;
        public const int WM_DEADCHAR = 0x0103;

        public const int WM_DELETEITEM = 0x002D,
                         WM_DESTROY = 0x0002;

        public const int WM_DESTROYCLIPBOARD = 0x0307;
        public const int WM_DEVICECHANGE = 0x0219;
        public const int WM_DEVMODECHANGE = 0x001B;
        public const int WM_DISPLAYCHANGE = 0x007E;
        public const int WM_DRAWCLIPBOARD = 0x0308;
        public const int WM_DRAWITEM = 0x002B;
        public const int WM_DROPFILES = 0x0233;

        public const int WM_ENABLE = 0x000A;

        public const int WM_ENDSESSION = 0x0016;

        public const int WM_ENTERIDLE = 0x0121;
        public const int WM_ENTERMENULOOP = 0x0211;
        public const int WM_ENTERSIZEMOVE = 0x0231;
        public const int WM_ERASEBKGND = 0x0014;
        public const int WM_EXITMENULOOP = 0x0212;
        public const int WM_EXITSIZEMOVE = 0x0232;

        public const int WM_FONTCHANGE = 0x001D;

        public const int WM_GETDLGCODE = 0x0087;

        public const int WM_GETFONT = 0x0031;

        public const int WM_GETHOTKEY = 0x0033;

        public const int WM_GETICON = 0x007F;
        public const int WM_GETMINMAXINFO = 0x0024;

        public const int WM_GETOBJECT = 0x003D;

        public const int WM_GETTEXT = 0x000D,
                         WM_GETTEXTLENGTH = 0x000E;

        public const int WM_HANDHELDFIRST = 0x0358,
                         WM_HANDHELDLAST = 0x035F;

        public const int WM_HELP = 0x0053;

        public const int WM_HOTKEY = 0x0312;
        public const int WM_HSCROLL = 0x0114;
        public const int WM_HSCROLLCLIPBOARD = 0x030E;
        public const int WM_ICONERASEBKGND = 0x0027;
        public const int WM_IME_CHAR = 0x0286;

        public const int WM_IME_COMPOSITION = 0x010F;

        public const int WM_IME_COMPOSITIONFULL = 0x0284;
        public const int WM_IME_CONTROL = 0x0283;
        public const int WM_IME_ENDCOMPOSITION = 0x010E;
        public const int WM_IME_KEYDOWN = 0x0290;

        public const int WM_IME_KEYLAST = 0x010F;

        public const int WM_IME_KEYUP = 0x0291;
        public const int WM_IME_NOTIFY = 0x0282;
        public const int WM_IME_SELECT = 0x0285;
        public const int WM_IME_SETCONTEXT = 0x0281;
        public const int WM_IME_STARTCOMPOSITION = 0x010D;

        public const int WM_INITDIALOG = 0x0110;

        public const int WM_INITMENU = 0x0116,
                         WM_INITMENUPOPUP = 0x0117;

        public const int WM_INPUTLANGCHANGE = 0x0051;
        public const int WM_INPUTLANGCHANGEREQUEST = 0x0050;
        public const int WM_KEYDOWN = 0x0100;
        public const int WM_KEYFIRST = 0x0100;
        public const int WM_KEYLAST = 0x0108;
        public const int WM_KEYUP = 0x0101;
        public const int WM_KILLFOCUS = 0x0008;
        public const int WM_LBUTTONDBLCLK = 0x0203;

        public const int WM_LBUTTONDOWN = 0x0201,
                         WM_LBUTTONUP = 0x0202;

        public const int WM_MBUTTONDBLCLK = 0x0209;

        public const int WM_MBUTTONDOWN = 0x0207,
                         WM_MBUTTONUP = 0x0208;

        public const int WM_MDIACTIVATE = 0x0222;

        public const int WM_MDICASCADE = 0x0227;

        public const int WM_MDICREATE = 0x0220,
                         WM_MDIDESTROY = 0x0221;

        public const int WM_MDIGETACTIVE = 0x0229;

        public const int WM_MDIICONARRANGE = 0x0228;
        public const int WM_MDIMAXIMIZE = 0x0225;
        public const int WM_MDINEXT = 0x0224;

        public const int WM_MDIREFRESHMENU = 0x0234;

        public const int WM_MDIRESTORE = 0x0223;
        public const int WM_MDISETMENU = 0x0230;
        public const int WM_MDITILE = 0x0226;
        public const int WM_MEASUREITEM = 0x002C;
        public const int WM_MENUCHAR = 0x0120;
        public const int WM_MENUSELECT = 0x011F;
        public const int WM_MOUSEACTIVATE = 0x0021;
        public const int WM_MOUSEFIRST = 0x0200;

        public const int WM_MOUSEHOVER = 0x02A1;

        public const int WM_MOUSELAST = 0x020A;

        public const int WM_MOUSELEAVE = 0x02A3;

        public const int WM_MOUSEMOVE = 0x0200;
        public const int WM_MOUSEWHEEL = 0x020A;
        public const int WM_MOVE = 0x0003;
        public const int WM_MOVING = 0x0216;
        public const int WM_NCACTIVATE = 0x0086;
        public const int WM_NCCALCSIZE = 0x0083;

        public const int WM_NCCREATE = 0x0081,
                         WM_NCDESTROY = 0x0082;

        public const int WM_NCHITTEST = 0x0084;
        public const int WM_NCLBUTTONDBLCLK = 0x00A3;

        public const int WM_NCLBUTTONDOWN = 0x00A1,
                         WM_NCLBUTTONUP = 0x00A2;

        public const int WM_NCMBUTTONDBLCLK = 0x00A9;

        public const int WM_NCMBUTTONDOWN = 0x00A7,
                         WM_NCMBUTTONUP = 0x00A8;

        public const int WM_NCMOUSEMOVE = 0x00A0;
        public const int WM_NCPAINT = 0x0085;
        public const int WM_NCRBUTTONDBLCLK = 0x00A6;

        public const int WM_NCRBUTTONDOWN = 0x00A4,
                         WM_NCRBUTTONUP = 0x00A5;

        public const int WM_NCXBUTTONDBLCLK = 0x00AD;

        public const int WM_NCXBUTTONDOWN = 0x00AB,
                         WM_NCXBUTTONUP = 0x00AC;

        public const int WM_NEXTDLGCTL = 0x0028;
        public const int WM_NEXTMENU = 0x0213;
        public const int WM_NOTIFY = 0x004E;
        public const int WM_NOTIFYFORMAT = 0x0055;
        public const int WM_NULL = 0x0000;
        public const int WM_PAINT = 0x000F;

        public const int WM_PAINTCLIPBOARD = 0x0309;

        public const int WM_PAINTICON = 0x0026;

        public const int WM_PALETTECHANGED = 0x0311;

        public const int WM_PALETTEISCHANGING = 0x0310;
        public const int WM_PARENTNOTIFY = 0x0210;
        public const int WM_PASTE = 0x0302;

        public const int WM_PENWINFIRST = 0x0380,
                         WM_PENWINLAST = 0x038F;

        public const int WM_POWER = 0x0048;
        public const int WM_POWERBROADCAST = 0x0218;

        public const int WM_PRINT = 0x0317,
                         WM_PRINTCLIENT = 0x0318;

        public const int WM_QUERYDRAGICON = 0x0037;
        public const int WM_QUERYENDSESSION = 0x0011;
        public const int WM_QUERYNEWPALETTE = 0x030F;
        public const int WM_QUERYOPEN = 0x0013;
        public const int WM_QUERYUISTATE = 0x0129;
        public const int WM_QUEUESYNC = 0x0023;
        public const int WM_QUIT = 0x0012;
        public const int WM_RBUTTONDBLCLK = 0x0206;

        public const int WM_RBUTTONDOWN = 0x0204,
                         WM_RBUTTONUP = 0x0205;

        public const int WM_REFLECT = WM_USER + 0x1C00;

        public const int WM_RENDERALLFORMATS = 0x0306;
        public const int WM_RENDERFORMAT = 0x0305;
        public const int WM_SETCURSOR = 0x0020;
        public const int WM_SETFOCUS = 0x0007;
        public const int WM_SETFONT = 0x0030;
        public const int WM_SETHOTKEY = 0x0032;
        public const int WM_SETICON = 0x0080;

        public const int WM_SETREDRAW = 0x000B,
                         WM_SETTEXT = 0x000C;

        public const int WM_SETTINGCHANGE = 0x001A;
        public const int WM_SHOWWINDOW = 0x0018;
        public const int WM_SIZE = 0x0005;
        public const int WM_SIZECLIPBOARD = 0x030B;
        public const int WM_SIZING = 0x0214;
        public const int WM_SPOOLERSTATUS = 0x002A;
        public const int WM_STYLECHANGED = 0x007D;
        public const int WM_STYLECHANGING = 0x007C;
        public const int WM_SYSCHAR = 0x0106;
        public const int WM_SYSCOLORCHANGE = 0x0015;
        public const int WM_SYSCOMMAND = 0x0112;
        public const int WM_SYSDEADCHAR = 0x0107;

        public const int WM_SYSKEYDOWN = 0x0104,
                         WM_SYSKEYUP = 0x0105;

        public const int WM_TCARD = 0x0052;
        public const int WM_TIMECHANGE = 0x001E;
        public const int WM_TIMER = 0x0113;
        public const int WM_UNDO = 0x0304;
        public const int WM_UNINITMENUPOPUP = 0x0125;
        public const int WM_UPDATEUISTATE = 0x0128;
        public const int WM_USER = 0x0400;
        public const int WM_USERCHANGED = 0x0054;
        public const int WM_VKEYTOITEM = 0x002E;
        public const int WM_VSCROLL = 0x0115;
        public const int WM_VSCROLLCLIPBOARD = 0x030A;
        public const int WM_WINDOWPOSCHANGED = 0x0047;
        public const int WM_WINDOWPOSCHANGING = 0x0046;
        public const int WM_WININICHANGE = 0x001A;
        public const int WM_XBUTTONDBLCLK = 0x020D;

        public const int WM_XBUTTONDOWN = 0x020B,
                         WM_XBUTTONUP = 0x020C;

        public const int WPF_SETMINPOSITION = 0x0001;
        public const int WSF_VISIBLE = 0x0001;
        public const int WS_BORDER = 0x00800000;
        public const int WS_CAPTION = 0x00C00000;

        public const int WS_CHILD = 0x40000000;

        public const int WS_CLIPCHILDREN = 0x02000000;

        public const int WS_CLIPSIBLINGS = 0x04000000;
        public const int WS_DISABLED = 0x08000000;

        public const int WS_DLGFRAME = 0x00400000;

        public const int WS_EX_APPWINDOW = 0x00040000;

        public const int WS_EX_CLIENTEDGE = 0x00000200,
                         WS_EX_CONTEXTHELP = 0x00000400;

        public const int WS_EX_CONTROLPARENT = 0x00010000;

        public const int WS_EX_DLGMODALFRAME = 0x00000001;

        public const int WS_EX_LAYERED = 0x00080000;

        public const int WS_EX_LAYOUTRTL = 0x00400000;

        public const int WS_EX_LEFT = 0x00000000;
        public const int WS_EX_LEFTSCROLLBAR = 0x00004000;
        public const int WS_EX_MDICHILD = 0x00000040;

        public const int WS_EX_NOINHERITLAYOUT = 0x00100000;

        public const int WS_EX_RIGHT = 0x00001000;
        public const int WS_EX_RTLREADING = 0x00002000;
        public const int WS_EX_STATICEDGE = 0x00020000;
        public const int WS_EX_TOOLWINDOW = 0x00000080;
        public const int WS_EX_TOPMOST = 0x00000008;
        public const int WS_HSCROLL = 0x00100000;
        public const int WS_MAXIMIZE = 0x01000000;
        public const int WS_MAXIMIZEBOX = 0x00010000;
        public const int WS_MINIMIZE = 0x20000000;
        public const int WS_MINIMIZEBOX = 0x00020000;

        public const int WS_OVERLAPPED = 0x00000000,
                         WS_POPUP = unchecked((int) 0x80000000);

        public const int WS_SYSMENU = 0x00080000;
        public const int WS_TABSTOP = 0x00010000;
        public const int WS_THICKFRAME = 0x00040000;
        public const int WS_VISIBLE = 0x10000000;
        public const int WS_VSCROLL = 0x00200000;
        public const int cmb4 = 0x0473; // C:\Program Files\Common
        public const int stc4 = 0x0443;
    }
}