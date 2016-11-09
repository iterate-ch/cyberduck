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
using System.Globalization;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core
{
    //-----------------------------------------------------------------------------
    // Enums

    public enum OSPlatformId
    {
        Win32s = 0,
        Win32Windows = 1,
        Win32NT = 2,
        WinCE = 3,
    }

    public enum OSBuildNumber
    {
        None = 0,

        Win2000SP4 = 2195,

        WinXPSP2 = 2600,

        Win2003SP1 = 3790,
    }

    [Flags]
    public enum OSSuites
    {
        None = 0,
        SmallBusiness = 0x00000001,
        Enterprise = 0x00000002,
        BackOffice = 0x00000004,
        Communications = 0x00000008,
        Terminal = 0x00000010,
        SmallBusinessRestricted = 0x00000020,
        EmbeddedNT = 0x00000040,
        Datacenter = 0x00000080,
        SingleUserTS = 0x00000100,
        Personal = 0x00000200,
        Blade = 0x00000400,
        EmbeddedRestricted = 0x00000800,
    }

    public enum OSProductType
    {
        Invalid = 0,
        Workstation = 1,
        DomainController = 2,
        Server = 3,
    }

    public enum OSVersion
    {
        Win32s,
        Win95,
        Win98,
        WinME,
        WinNT351,
        WinNT4,
        Win2000,
        WinXP,
        Win2003,
        WinXPx64,
        WinCE,
        Vista,
        Win2008,
        Win2008R2,
        Win7
    }

    public enum OSArchitecture
    {
        x86,
        x64,
    }

    //-----------------------------------------------------------------------------
    // OSVersionInfo, Thanks to Nicholas Butler
    // http://www.codeproject.com/KB/system/osversion.aspx

    public class OSVersionInfo : IComparable, ICloneable
    {
        //-----------------------------------------------------------------------------
        // Constants

        //-----------------------------------------------------------------------------
        // Static Fields

        private static readonly OSVersionInfo _Vista = new OSVersionInfo(OSPlatformId.Win32NT, MajorVersionConst.Vista,
                                                                         MinorVersionConst.Vista,
                                                                         OSProductType.Workstation, true);

        private static readonly OSVersionInfo _Win2000 = new OSVersionInfo(OSPlatformId.Win32NT,
                                                                           MajorVersionConst.Win2000,
                                                                           MinorVersionConst.Win2000, true);

        private static readonly OSVersionInfo _Win2003 = new OSVersionInfo(OSPlatformId.Win32NT,
                                                                           MajorVersionConst.Win2003,
                                                                           MinorVersionConst.Win2003,
                                                                           OSProductType.Server, true);

        private static readonly OSVersionInfo _Win2008 = new OSVersionInfo(OSPlatformId.Win32NT,
                                                                           MajorVersionConst.Win2008,
                                                                           MinorVersionConst.Win2008,
                                                                           OSProductType.Server, true);

        private static readonly OSVersionInfo _Win2008R2 = new OSVersionInfo(OSPlatformId.Win32NT,
                                                                             MajorVersionConst.Win2008R2,
                                                                             MinorVersionConst.Win2008R2,
                                                                             OSProductType.Server, true);

        private static readonly OSVersionInfo _Win32s = new OSVersionInfo(OSPlatformId.Win32s, MajorVersionConst.Win32s,
                                                                          MinorVersionConst.Win32s, true);

        private static readonly OSVersionInfo _Win7 = new OSVersionInfo(OSPlatformId.Win32NT, MajorVersionConst.Win7,
                                                                        MinorVersionConst.Win7,
                                                                        OSProductType.Workstation, true);

        private static readonly OSVersionInfo _Win95 = new OSVersionInfo(OSPlatformId.Win32Windows,
                                                                         MajorVersionConst.Win95,
                                                                         MinorVersionConst.Win95, true);

        private static readonly OSVersionInfo _Win98 = new OSVersionInfo(OSPlatformId.Win32Windows,
                                                                         MajorVersionConst.Win98,
                                                                         MinorVersionConst.Win98, true);

        private static readonly OSVersionInfo _WinCE = new OSVersionInfo(OSPlatformId.WinCE, true);

        private static readonly OSVersionInfo _WinME = new OSVersionInfo(OSPlatformId.Win32Windows,
                                                                         MajorVersionConst.WinME,
                                                                         MinorVersionConst.WinME, true);

        private static readonly OSVersionInfo _WinNT351 = new OSVersionInfo(OSPlatformId.Win32NT,
                                                                            MajorVersionConst.WinNT351,
                                                                            MinorVersionConst.WinNT351, true);

        private static readonly OSVersionInfo _WinNT4 = new OSVersionInfo(OSPlatformId.Win32NT, MajorVersionConst.WinNT4,
                                                                          MinorVersionConst.WinNT4, true);

        private static readonly OSVersionInfo _WinXP = new OSVersionInfo(OSPlatformId.Win32NT, MajorVersionConst.WinXP,
                                                                         MinorVersionConst.WinXP, true);

        private static readonly OSVersionInfo _WinXPx64 = new OSVersionInfo(OSPlatformId.Win32NT,
                                                                            MajorVersionConst.WinXPx64,
                                                                            MinorVersionConst.WinXPx64,
                                                                            OSProductType.Workstation, true);

        private int _BuildNumber = -1;
        //        private int _PlatformId;
        private string _CSDVersion = String.Empty;
        private bool _ExtendedPropertiesAreSet;
        private bool _Locked;
        private int _MajorVersion = -1;
        private int _MinorVersion = -1;
        private OSArchitecture _OSArchitecture = 0;
        private OSPlatformId _OSPlatformId;
        private OSProductType _OSProductType;
        private OSSuites _OSSuiteFlags;
        private byte _Reserved;
        private Int16 _ServicePackMajor = -1;
        private Int16 _ServicePackMinor = -1;

        public OSVersionInfo()
        {
        }

        public OSVersionInfo(
            OSPlatformId osPlatformId)
        {
            _OSPlatformId = osPlatformId;
        }

        public OSVersionInfo(
            OSPlatformId osPlatformId,
            bool locked)
        {
            _OSPlatformId = osPlatformId;

            _Locked = locked;
        }

        public OSVersionInfo(
            OSPlatformId osPlatformId,
            int majorVersion,
            int minorVersion)
        {
            _OSPlatformId = osPlatformId;
            _MajorVersion = majorVersion;
            _MinorVersion = minorVersion;
        }

        public OSVersionInfo(
            OSPlatformId osPlatformId,
            int majorVersion,
            int minorVersion,
            bool locked)
        {
            _OSPlatformId = osPlatformId;
            _MajorVersion = majorVersion;
            _MinorVersion = minorVersion;

            _Locked = locked;
        }

        public OSVersionInfo(
            OSPlatformId osPlatformId,
            int majorVersion,
            int minorVersion,
            OSProductType osProductType,
            bool locked)
        {
            _OSPlatformId = osPlatformId;
            _MajorVersion = majorVersion;
            _MinorVersion = minorVersion;
            _OSProductType = osProductType;

            _Locked = locked;
        }

        //-----------------------------------------------------------------------------
        // Copying

        public OSVersionInfo(OSVersionInfo o)
        {
            CopyThis(o);
        }

        //-----------------------------------------------------------------------------
        // Static Properties

        public static OSVersionInfo Win32s
        {
            get { return _Win32s; }
        }

        public static OSVersionInfo Win95
        {
            get { return _Win95; }
        }

        public static OSVersionInfo Win98
        {
            get { return _Win98; }
        }

        public static OSVersionInfo WinME
        {
            get { return _WinME; }
        }

        public static OSVersionInfo WinNT351
        {
            get { return _WinNT351; }
        }

        public static OSVersionInfo WinNT4
        {
            get { return _WinNT4; }
        }

        public static OSVersionInfo Win2000
        {
            get { return _Win2000; }
        }

        public static OSVersionInfo WinXP
        {
            get { return _WinXP; }
        }

        public static OSVersionInfo Win2003
        {
            get { return _Win2003; }
        }

        public static OSVersionInfo WinXPx64
        {
            get { return _WinXPx64; }
        }

        public static OSVersionInfo WinCE
        {
            get { return _WinCE; }
        }

        public static OSVersionInfo Vista
        {
            get { return _Vista; }
        }

        public static OSVersionInfo Win2008
        {
            get { return _Win2008; }
        }

        public static OSVersionInfo Win2008R2
        {
            get { return _Win2008R2; }
        }

        public static OSVersionInfo Win7
        {
            get { return _Win7; }
        }

        //-----------------------------------------------------------------------------
        // Static methods

        //-----------------------------------------------------------------------------
        // Normal Properties

        public OSPlatformId OSPlatformId
        {
            get { return _OSPlatformId; }

            set
            {
                CheckLock("OSPlatformId");

                _OSPlatformId = value;
            }
        }

        public int OSMajorVersion
        {
            get { return _MajorVersion; }

            set
            {
                CheckLock("MajorVersion");

                _MajorVersion = value;
            }
        }

        public int OSMinorVersion
        {
            get { return _MinorVersion; }

            set
            {
                CheckLock("MinorVersion");

                _MinorVersion = value;
            }
        }

        public int BuildNumber
        {
            get { return _BuildNumber; }

            set
            {
                CheckLock("BuildNumber");

                _BuildNumber = value;
            }
        }

        //        public int PlatformId
        //        {
        //            get { return _PlatformId; }
        //
        //            set
        //            {
        //                CheckLock( "PlatformId" );
        //
        //                _PlatformId = value;
        //            }
        //        }

        public string OSCSDVersion
        {
            get { return _CSDVersion; }

            set
            {
                CheckLock("CSDVersion");

                _CSDVersion = value;
            }
        }

        public OSArchitecture OSArchitecture
        {
            get { return _OSArchitecture; }
            protected set { _OSArchitecture = value; }
        }

        //-----------------------------------------------------------------------------
        // Extended Properties

        public OSSuites OSSuiteFlags
        {
            get
            {
                CheckExtendedProperty("OSSuiteFlags");

                return _OSSuiteFlags;
            }

            set
            {
                CheckLock("OSSuiteFlags");

                _OSSuiteFlags = value;
            }
        }

        public OSProductType OSProductType
        {
            get
            {
                CheckExtendedProperty("OSProductType");

                return _OSProductType;
            }

            set
            {
                CheckLock("OSProductType");

                _OSProductType = value;
            }
        }

        public Int16 OSServicePackMajor
        {
            get
            {
                CheckExtendedProperty("ServicePackMajor");

                return _ServicePackMajor;
            }

            set
            {
                CheckLock("ServicePackMajor");

                _ServicePackMajor = value;
            }
        }

        public Int16 OSServicePackMinor
        {
            get
            {
                CheckExtendedProperty("ServicePackMinor");

                return _ServicePackMinor;
            }

            set
            {
                CheckLock("ServicePackMinor");

                _ServicePackMinor = value;
            }
        }

        //        public UInt16 SuiteMask
        //        {
        //            get
        //            {
        //                CheckExtendedProperty( "SuiteMask" );
        //
        //                return _SuiteMask;
        //            }
        //
        //            set
        //            {
        //                CheckLock( "SuiteMask" );
        //
        //                _SuiteMask = value;
        //            }
        //        }

        //        public byte ProductType
        //        {
        //            get
        //            {
        //                CheckExtendedProperty( "ProductType" );
        //
        //                return _ProductType;
        //            }
        //
        //            set
        //            {
        //                CheckLock( "ProductType" );
        //
        //                _ProductType = value;
        //            }
        //        }

        public byte OSReserved
        {
            get
            {
                CheckExtendedProperty("Reserved");

                return _Reserved;
            }

            set
            {
                CheckLock("Reserved");

                _Reserved = value;
            }
        }

        //-----------------------------------------------------------------------------
        // Get Properties

        public int Platform
        {
            get { return (int) _OSPlatformId; }
        }

        public int SuiteMask
        {
            get
            {
                CheckExtendedProperty("SuiteMask");

                return (int) _OSSuiteFlags;
            }
        }

        public byte ProductType
        {
            get
            {
                CheckExtendedProperty("ProductType");

                return (byte) _OSProductType;
            }
        }

        //-----------------------------------------------------------------------------
        // Calculated Properties

        public Version Version
        {
            get
            {
                if (OSMajorVersion < 0 || OSMinorVersion < 0)
                    return new Version();

                if (BuildNumber < 0)
                    return new Version(OSMajorVersion, OSMinorVersion);

                return new Version(OSMajorVersion, OSMinorVersion, BuildNumber);
            }
        }

        public string VersionString
        {
            get { return Version.ToString(); }
        }

        public string OSPlatformIdString
        {
            get
            {
                switch (OSPlatformId)
                {
                    case OSPlatformId.Win32s:
                        return "Windows 32s";
                    case OSPlatformId.Win32Windows:
                        return "Windows 32";
                    case OSPlatformId.Win32NT:
                        return "Windows NT";
                    case OSPlatformId.WinCE:
                        return "Windows CE";

                    default:
                        throw new InvalidOperationException("Invalid OSPlatformId: " + OSPlatformId);
                }
            }
        }

        public string OSSuiteString
        {
            get
            {
                string s = String.Empty;

                OSSuites flags = OSSuiteFlags;

                if (OSSuiteFlag(flags, OSSuites.SmallBusiness))
                    OSSuiteStringAdd(ref s, "Small Business");

                if (OSSuiteFlag(flags, OSSuites.Enterprise))
                    switch (OSVersion)
                    {
                        case OSVersion.WinNT4:
                            OSSuiteStringAdd(ref s, "Enterprise");
                            break;
                        case OSVersion.Win2000:
                            OSSuiteStringAdd(ref s, "Advanced");
                            break;
                        case OSVersion.Win2003:
                            OSSuiteStringAdd(ref s, "Enterprise");
                            break;
                    }

                if (OSSuiteFlag(flags, OSSuites.BackOffice))
                    OSSuiteStringAdd(ref s, "BackOffice");

                if (OSSuiteFlag(flags, OSSuites.Communications))
                    OSSuiteStringAdd(ref s, "Communications");

                if (OSSuiteFlag(flags, OSSuites.Terminal))
                    OSSuiteStringAdd(ref s, "Terminal Services");

                if (OSSuiteFlag(flags, OSSuites.SmallBusinessRestricted))
                    OSSuiteStringAdd(ref s, "Small Business Restricted");

                if (OSSuiteFlag(flags, OSSuites.EmbeddedNT))
                    OSSuiteStringAdd(ref s, "Embedded");

                if (OSSuiteFlag(flags, OSSuites.Datacenter))
                    OSSuiteStringAdd(ref s, "Datacenter");

                //                if ( OSSuiteFlag( flags, OSSuites.SingleUserTS ) )
                //                    OSSuiteStringAdd( ref s, "Single User Terminal Services" );

                if (OSSuiteFlag(flags, OSSuites.Personal))
                    OSSuiteStringAdd(ref s, "Home Edition");

                if (OSSuiteFlag(flags, OSSuites.Blade))
                    OSSuiteStringAdd(ref s, "Web Edition");

                if (OSSuiteFlag(flags, OSSuites.EmbeddedRestricted))
                    OSSuiteStringAdd(ref s, "Embedded Restricted");

                return s;
            }
        }

        public string OSProductTypeString
        {
            get
            {
                switch (OSProductType)
                {
                    case OSProductType.Workstation:

                        switch (OSVersion)
                        {
                            case OSVersion.Win32s:
                                return String.Empty;
                            case OSVersion.Win95:
                                return String.Empty;
                            case OSVersion.Win98:
                                return String.Empty;
                            case OSVersion.WinME:
                                return String.Empty;
                            case OSVersion.WinNT351:
                                return String.Empty;
                            case OSVersion.WinNT4:
                                return "Workstation";
                            case OSVersion.Win2000:
                                return "Professional";

                            case OSVersion.WinXP:

                                if (OSSuiteFlag(OSSuiteFlags, OSSuites.Personal))
                                    return "Home Edition";
                                else
                                    return "Professional";

                            case OSVersion.Win2003:
                                return String.Empty;
                            case OSVersion.WinXPx64:
                                return String.Empty;
                            case OSVersion.WinCE:
                                return String.Empty;

                            case OSVersion.Vista:
                            case OSVersion.Win2008:
                            case OSVersion.Win2008R2:
                            case OSVersion.Win7:

                                switch (OSArchitecture)
                                {
                                    case OSArchitecture.x86:
                                        return "x86";
                                    case OSArchitecture.x64:
                                        return "x64";

                                    default:
                                        throw new InvalidOperationException("Invalid OSArchitecture: " + OSArchitecture);
                                }

                            default:
                                throw new InvalidOperationException("Invalid OSVersion: " + OSVersion);
                        }

                    case OSProductType.DomainController:
                        {
                            string s = OSSuiteString;

                            if (s.Length > 0) s += " ";

                            return s + "Domain Controller";
                        }

                    case OSProductType.Server:
                        {
                            string s = OSSuiteString;

                            if (s.Length > 0) s += " ";

                            return s + "Server";
                        }

                    default:
                        throw new InvalidOperationException("Invalid OSProductType: " + OSProductType);
                }
            }
        }

        public OSVersion OSVersion
        {
            get
            {
                switch (OSPlatformId)
                {
                    case OSPlatformId.Win32s:
                        return OSVersion.Win32s;

                    case OSPlatformId.Win32Windows:

                        switch (OSMinorVersion)
                        {
                            case MinorVersionConst.Win95:
                                return OSVersion.Win95;
                            case MinorVersionConst.Win98:
                                return OSVersion.Win98;
                            case MinorVersionConst.WinME:
                                return OSVersion.WinME;

                            default:
                                throw new InvalidOperationException("Invalid Win32Windows MinorVersion: " +
                                                                    OSMinorVersion);
                        }

                    case OSPlatformId.Win32NT:

                        switch (OSMajorVersion)
                        {
                            case MajorVersionConst.WinNT351:
                                return OSVersion.WinNT351;
                            case MajorVersionConst.WinNT4:
                                return OSVersion.WinNT4;

                            case MajorVersionConst.WinNT5:

                                switch (OSMinorVersion)
                                {
                                    case MinorVersionConst.Win2000:
                                        return OSVersion.Win2000;
                                    case MinorVersionConst.WinXP:
                                        return OSVersion.WinXP;

                                    case MinorVersionConst.Win2003:
                                        //case MinorVersionConst.WinXPx64: // same ( 5.2 )

                                        switch (_OSProductType)
                                        {
                                            case OSProductType.Workstation:
                                                return OSVersion.WinXPx64;

                                            case OSProductType.DomainController:
                                            case OSProductType.Server:
                                                return OSVersion.Win2003;

                                            default:
                                                throw new InvalidOperationException(
                                                    "Invalid Win32NT WinNT5.2 OSProductType: " + OSProductType);
                                        }

                                    default:
                                        throw new InvalidOperationException("Invalid Win32NT WinNT5 MinorVersion: " +
                                                                            OSMinorVersion);
                                }

                            case MajorVersionConst.Vista:
                                //case MajorVersionConst.Win2008: same ( 6 )
                                //case MajorVersionConst.Win2008R2: same ( 6 )
                                //case MajorVersionConst.Win7: same ( 6 )

                                switch (_OSProductType)
                                {
                                    case OSProductType.Workstation:
                                        switch (OSMinorVersion)
                                        {
                                            case MinorVersionConst.Vista:
                                                return OSVersion.Vista;
                                            case MinorVersionConst.Win7:
                                                return OSVersion.Win7;
                                            default:
                                                throw new InvalidOperationException(
                                                    "Invalid Win32NT WinNT6 Workstation MinorVersion: " + OSMinorVersion);
                                        }


                                    case OSProductType.DomainController:
                                    case OSProductType.Server:
                                        switch (OSMinorVersion)
                                        {
                                            case MinorVersionConst.Win2008:
                                                return OSVersion.Win2008;
                                            case MinorVersionConst.Win2008R2:
                                                return OSVersion.Win2008R2;
                                            default:
                                                throw new InvalidOperationException(
                                                    "Invalid Win32NT WinNT6 Server MinorVersion: " + OSMinorVersion);
                                        }

                                    default:
                                        throw new InvalidOperationException("Invalid Win32NT WinNT6 OSProductType: " +
                                                                            OSProductType);
                                }

                            default:
                                throw new InvalidOperationException("Invalid Win32NT MajorVersion: " + OSMajorVersion);
                        }

                    case OSPlatformId.WinCE:
                        return OSVersion.WinCE;

                    default:
                        throw new InvalidOperationException("Invalid OSPlatformId: " + OSPlatformId);
                }
            }
        }

        public string OSVersionString
        {
            get
            {
                switch (OSVersion)
                {
                    case OSVersion.Win32s:
                        return "Windows 32s";
                    case OSVersion.Win95:
                        return "Windows 95";
                    case OSVersion.Win98:
                        return "Windows 98";
                    case OSVersion.WinME:
                        return "Windows ME";
                    case OSVersion.WinNT351:
                        return "Windows NT 3.51";
                    case OSVersion.WinNT4:
                        return "Windows NT 4";
                    case OSVersion.Win2000:
                        return "Windows 2000";
                    case OSVersion.WinXP:
                        return "Windows XP";
                    case OSVersion.Win2003:
                        return "Windows 2003";
                    case OSVersion.WinXPx64:
                        return "Windows XP x64";
                    case OSVersion.WinCE:
                        return "Windows CE";
                    case OSVersion.Vista:
                        return "Windows Vista";
                    case OSVersion.Win2008:
                        return "Windows 2008";
                    case OSVersion.Win2008R2:
                        return "Windows 2008 R2";
                    case OSVersion.Win7:
                        return "Windows 7";

                    default:
                        throw new InvalidOperationException("Invalid OSVersion: " + OSVersion);
                }
            }
        }

        //-----------------------------------------------------------------------------
        // State Properties

        public bool ExtendedPropertiesAreSet
        {
            get { return _ExtendedPropertiesAreSet; }
            set { _ExtendedPropertiesAreSet = value; }
        }

        public bool IsLocked
        {
            get { return _Locked; }
        }

        public virtual object Clone()
        {
            return CreateCopy();
        }

        public virtual int CompareTo(object o)
        {
            if (o == null) throw new InvalidOperationException("CompareTo( object o ): 'o' is null");

            OSVersionInfo p = o as OSVersionInfo;
            if (p == null) throw new InvalidOperationException("CompareTo( object o ): 'o' is not an OSVersionInfo");

            if (this == p) return 0;
            if (this > p) return 1;
            return -1;
        }

        public static OSVersionInfo GetOSVersionInfo(OSVersion v)
        {
            switch (v)
            {
                case OSVersion.Win32s:
                    return Win32s;
                case OSVersion.Win95:
                    return Win95;
                case OSVersion.Win98:
                    return Win98;
                case OSVersion.WinME:
                    return WinME;
                case OSVersion.WinNT351:
                    return WinNT351;
                case OSVersion.WinNT4:
                    return WinNT4;
                case OSVersion.Win2000:
                    return Win2000;
                case OSVersion.WinXP:
                    return WinXP;
                case OSVersion.Win2003:
                    return Win2003;
                case OSVersion.WinXPx64:
                    return WinXPx64;
                case OSVersion.WinCE:
                    return WinCE;
                case OSVersion.Vista:
                    return Vista;
                case OSVersion.Win2008:
                    return Win2008;
                case OSVersion.Win2008R2:
                    return Win2008R2;
                case OSVersion.Win7:
                    return Win7;

                default:
                    throw new InvalidOperationException();
            }
        }

        public static bool OSSuiteFlag(OSSuites flags, OSSuites test)
        {
            return ((flags & test) > 0);
        }

        private static void OSSuiteStringAdd(ref string s, string suite)
        {
            if (s.Length > 0) s += ", ";

            s += suite;
        }

        public void Lock()
        {
            _Locked = true;
        }

        //-----------------------------------------------------------------------------
        // Property helpers

        private void CheckExtendedProperty(string property)
        {
            if (_ExtendedPropertiesAreSet) return;

            throw new InvalidOperationException("'" + property + "' is not set");
        }

        private void CheckLock(string property)
        {
            if (!_Locked) return;

            throw new InvalidOperationException("Cannot set '" + property + "' on locked instance");
        }

        //-----------------------------------------------------------------------------
        // Constructors

        public virtual void Copy(OSVersionInfo o)
        {
            CopyThis(o);
        }

        public virtual OSVersionInfo CreateCopy()
        {
            return new OSVersionInfo(this);
        }

        private void CopyThis(OSVersionInfo o)
        {
            // normal fields
            _OSPlatformId = o._OSPlatformId;

            _MajorVersion = o._MajorVersion;
            _MinorVersion = o._MinorVersion;
            _BuildNumber = o._BuildNumber;
            _CSDVersion = o._CSDVersion;

            // extended fields
            _OSSuiteFlags = o._OSSuiteFlags;
            _OSProductType = o._OSProductType;

            _ServicePackMajor = o._ServicePackMajor;
            _ServicePackMinor = o._ServicePackMinor;
            _Reserved = o._Reserved;

            // state fields
            //            _Locked                   = o._Locked                   ;
            _Locked = false;
            _ExtendedPropertiesAreSet = o._ExtendedPropertiesAreSet;
        }

        //-----------------------------------------------------------------------------
        // overrides

        public override bool Equals(object o)
        {
            OSVersionInfo p = o as OSVersionInfo;

            if (p != null) return (this == p);

            return base.Equals(o);
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

        public override string ToString()
        {
            string s = OSVersionString;

            if (ExtendedPropertiesAreSet) s += " " + OSProductTypeString;

            if (OSCSDVersion.Length > 0) s += " " + OSCSDVersion;

            s += " v" + VersionString;

            return s;
        }

        //-----------------------------------------------------------------------------
        // Operators

        public static bool operator ==(OSVersionInfo o, OSVersionInfo p)
        {
            if (o.OSPlatformId != p.OSPlatformId) return false;

            if (o.OSMajorVersion < 0 || p.OSMajorVersion < 0) goto hell;
            if (o.OSMajorVersion != p.OSMajorVersion) return false;

            if (o.OSMinorVersion < 0 || p.OSMinorVersion < 0) goto hell;
            if (o.OSMinorVersion != p.OSMinorVersion) return false;

            if (o.BuildNumber < 0 || p.BuildNumber < 0) goto hell;
            if (o.BuildNumber != p.BuildNumber) return false;

            if ((!o.ExtendedPropertiesAreSet) || (!p.ExtendedPropertiesAreSet)) goto hell;

            if (o.OSServicePackMajor < 0 || p.OSServicePackMajor < 0) goto hell;
            if (o.OSServicePackMajor != p.OSServicePackMajor) return false;

            if (o.OSServicePackMinor < 0 || p.OSServicePackMinor < 0) goto hell;
            if (o.OSServicePackMinor != p.OSServicePackMinor) return false;

            hell:

            return true;
        }

        public static bool operator !=(OSVersionInfo o, OSVersionInfo p)
        {
            return !(o == p);
        }

        public static bool operator <(OSVersionInfo o, OSVersionInfo p)
        {
            if (o.OSPlatformId < p.OSPlatformId) return true;
            if (o.OSPlatformId > p.OSPlatformId) return false;

            if (o.OSMajorVersion < 0 || p.OSMajorVersion < 0) goto hell;
            if (o.OSMajorVersion < p.OSMajorVersion) return true;
            if (o.OSMajorVersion > p.OSMajorVersion) return false;

            if (o.OSMinorVersion < 0 || p.OSMinorVersion < 0) goto hell;
            if (o.OSMinorVersion < p.OSMinorVersion) return true;
            if (o.OSMinorVersion > p.OSMinorVersion) return false;

            if (o.BuildNumber < 0 || p.BuildNumber < 0) goto hell;
            if (o.BuildNumber < p.BuildNumber) return true;
            if (o.BuildNumber > p.BuildNumber) return false;

            if ((!o.ExtendedPropertiesAreSet) || (!p.ExtendedPropertiesAreSet)) goto hell;

            if (o.OSServicePackMajor < 0 || p.OSServicePackMajor < 0) goto hell;
            if (o.OSServicePackMajor < p.OSServicePackMajor) return true;
            if (o.OSServicePackMajor > p.OSServicePackMajor) return false;

            if (o.OSServicePackMinor < 0 || p.OSServicePackMinor < 0) goto hell;
            if (o.OSServicePackMinor < p.OSServicePackMinor) return true;
            if (o.OSServicePackMinor > p.OSServicePackMinor) return false;

            hell:

            return false;
        }

        public static bool operator >(OSVersionInfo o, OSVersionInfo p)
        {
            if (o.OSPlatformId < p.OSPlatformId) return false;
            if (o.OSPlatformId > p.OSPlatformId) return true;

            if (o.OSMajorVersion < 0 || p.OSMajorVersion < 0) goto hell;
            if (o.OSMajorVersion < p.OSMajorVersion) return false;
            if (o.OSMajorVersion > p.OSMajorVersion) return true;

            if (o.OSMinorVersion < 0 || p.OSMinorVersion < 0) goto hell;
            if (o.OSMinorVersion < p.OSMinorVersion) return false;
            if (o.OSMinorVersion > p.OSMinorVersion) return true;

            if (o.BuildNumber < 0 || p.BuildNumber < 0) goto hell;
            if (o.BuildNumber < p.BuildNumber) return false;
            if (o.BuildNumber > p.BuildNumber) return true;

            if ((!o.ExtendedPropertiesAreSet) || (!p.ExtendedPropertiesAreSet)) goto hell;

            if (o.OSServicePackMajor < 0 || p.OSServicePackMajor < 0) goto hell;
            if (o.OSServicePackMajor < p.OSServicePackMajor) return false;
            if (o.OSServicePackMajor > p.OSServicePackMajor) return true;

            if (o.OSServicePackMinor < 0 || p.OSServicePackMinor < 0) goto hell;
            if (o.OSServicePackMinor < p.OSServicePackMinor) return false;
            if (o.OSServicePackMinor > p.OSServicePackMinor) return true;

            hell:

            return false;
        }

        public static bool operator <=(OSVersionInfo o, OSVersionInfo p)
        {
            return (o < p || o == p);
        }

        public static bool operator >=(OSVersionInfo o, OSVersionInfo p)
        {
            return (o > p || o == p);
        }

        private class MajorVersionConst
        {
            public const int Vista = 6;
            public const int Win2000 = WinNT5;
            public const int Win2003 = WinNT5;
            public const int Win2008 = 6;
            public const int Win2008R2 = 6;
            public const int Win32s = 0;
            public const int Win10 = 10;
            public const int Win8 = 6;
            public const int Win7 = 6;
            public const int Win95 = 4;
            public const int Win98 = 4;
            public const int WinME = 4;
            public const int WinNT351 = 3;
            public const int WinNT4 = 4;
            public const int WinNT5 = 5;
            public const int WinXP = WinNT5;
            public const int WinXPx64 = WinNT5;

            private MajorVersionConst()
            {
            }
        }

        private class MinorVersionConst
        {
            public const int Vista = 0;
            public const int Win2000 = 0;
            public const int Win2003 = 2;
            public const int Win2008 = 0;
            public const int Win2008R2 = 1;
            public const int Win32s = 0;
            public const int Win10 = 0;
            public const int Win81 = 3;
            public const int Win8 = 2;
            public const int Win7 = 1;
            public const int Win95 = 0;
            public const int Win98 = 10;
            public const int WinME = 90;
            public const int WinNT351 = 51;
            public const int WinNT4 = 0;
            public const int WinXP = 1;
            public const int WinXPx64 = 2;

            private MinorVersionConst()
            {
            }
        }

        //-----------------------------------------------------------------------------
    }

    // OSVersionInfo

    //-----------------------------------------------------------------------------
    // OperatingSystemVersion

    public class OperatingSystemVersion : OSVersionInfo
    {
        //-----------------------------------------------------------------------------
        // Current

        private static OperatingSystemVersion sCurrent;

        //-----------------------------------------------------------------------------
        // Constructors

        public OperatingSystemVersion()
        {
            OSVERSIONINFO osVersionInfo = new OSVERSIONINFO();

            if (!UseOSVersionInfoEx(osVersionInfo))
                InitOsVersionInfo(osVersionInfo);
            else
                InitOsVersionInfoEx();

            //            Lock();

            InitArchitecture();
        }

        public static OperatingSystemVersion Current
        {
            get
            {
                if (ReferenceEquals(sCurrent, null)) sCurrent = new OperatingSystemVersion();

                return sCurrent;
            }
        }

        // check for NT4 SP6 or later
        private static bool UseOSVersionInfoEx(OSVERSIONINFO info)
        {
            bool b = NativeMethods.GetVersionEx(info);

            if (!b)
            {
                int error = Marshal.GetLastWin32Error();

                throw new InvalidOperationException(
                    "Failed to get OSVersionInfo. Error = 0x" +
                    error.ToString("8X", CultureInfo.CurrentCulture));
            }

            if (info.MajorVersion < 4) return false;
            if (info.MajorVersion > 4) return true;

            if (info.MinorVersion < 0) return false;
            if (info.MinorVersion > 0) return true;

            if (info.CSDVersion == "Service Pack 6") return true;

            return false;
        }

        private void InitOsVersionInfo(OSVERSIONINFO info)
        {
            OSPlatformId = GetOSPlatformId(info.PlatformId);

            OSMajorVersion = info.MajorVersion;
            OSMinorVersion = info.MinorVersion;
            BuildNumber = info.BuildNumber;
            //            PlatformId     = info.PlatformId   ;
            OSCSDVersion = info.CSDVersion;
        }

        private void InitOsVersionInfoEx()
        {
            OSVERSIONINFOEX info = new OSVERSIONINFOEX();

            bool b = NativeMethods.GetVersionEx(info);

            if (!b)
            {
                int error = Marshal.GetLastWin32Error();

                throw new InvalidOperationException(
                    "Failed to get OSVersionInfoEx. Error = 0x" +
                    error.ToString("8X", CultureInfo.CurrentCulture));
            }

            OSPlatformId = GetOSPlatformId(info.PlatformId);

            OSMajorVersion = info.MajorVersion;
            OSMinorVersion = info.MinorVersion;
            BuildNumber = info.BuildNumber;
            //            PlatformId         = info.PlatformId       ;
            OSCSDVersion = info.CSDVersion;

            OSSuiteFlags = GetOSSuiteFlags(info.SuiteMask);
            OSProductType = GetOSProductType(info.ProductType);

            OSServicePackMajor = info.ServicePackMajor;
            OSServicePackMinor = info.ServicePackMinor;
            //            SuiteMask          = info.SuiteMask        ;
            //            ProductType        = info.ProductType      ;
            OSReserved = info.Reserved;

            ExtendedPropertiesAreSet = true;
        }

        private static OSPlatformId GetOSPlatformId(int platformId)
        {
            switch (platformId)
            {
                case VerPlatformId.Win32s:
                    return OSPlatformId.Win32s;
                case VerPlatformId.Win32Windows:
                    return OSPlatformId.Win32Windows;
                case VerPlatformId.Win32NT:
                    return OSPlatformId.Win32NT;
                case VerPlatformId.WinCE:
                    return OSPlatformId.WinCE;

                default:
                    throw new InvalidOperationException("Invalid PlatformId: " + platformId);
            }
        }

        private static OSSuites GetOSSuiteFlags(UInt16 suiteMask)
        {
            return (OSSuites) suiteMask;
        }

        private static OSProductType GetOSProductType(byte productType)
        {
            switch (productType)
            {
                case VerProductType.VER_NT_WORKSTATION:
                    return OSProductType.Workstation;
                case VerProductType.VER_NT_DOMAIN_CONTROLLER:
                    return OSProductType.DomainController;
                case VerProductType.VER_NT_SERVER:
                    return OSProductType.Server;

                default:
                    throw new InvalidOperationException("Invalid ProductType: " + productType);
            }
        }

        private void InitArchitecture()
        {
            SYSTEM_INFO lpSystemInfo = new SYSTEM_INFO();
            NativeMethods.GetSystemInfo(ref lpSystemInfo);

            switch (lpSystemInfo.uProcessorInfo.wProcessorArchitecture)
            {
                case VerArchitecture.INTEL:
                    OSArchitecture = OSArchitecture.x86;
                    break;

                case VerArchitecture.AMD64:
                case VerArchitecture.IA64:
                    OSArchitecture = OSArchitecture.x64;
                    break;

                default:
                    throw new ApplicationException("Unknown architecture: " +
                                                   lpSystemInfo.uProcessorInfo.wProcessorArchitecture);
            }
        }

        private class NativeMethods
        {
            private NativeMethods()
            {
            }

            [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Auto)]
            public static extern bool GetVersionEx
                (
                [In, Out] OSVERSIONINFO osVersionInfo
                );

            [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Auto)]
            public static extern bool GetVersionEx
                (
                [In, Out] OSVERSIONINFOEX osVersionInfoEx
                );

            //            [ DllImport( "kernel32.dll", SetLastError = true ) ]
            //            public static extern bool VerifyVersionInfo
            //                (
            //                    [ In ] OSVERSIONINFOEX VersionInfo,
            //                    UInt32 TypeMask,
            //                    UInt64 ConditionMask
            //                );

            [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
            public static extern void GetSystemInfo([MarshalAs(UnmanagedType.Struct)] ref SYSTEM_INFO lpSystemInfo);
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
        private class OSVERSIONINFO
        {
            public int OSVersionInfoSize;
            public int MajorVersion;
            public int MinorVersion;
            public int BuildNumber;
            public int PlatformId;
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 0x80)] public string CSDVersion;

            public OSVERSIONINFO()
            {
                OSVersionInfoSize = Marshal.SizeOf(this);
            }

            private void StopTheCompilerComplaining()
            {
                MajorVersion = 0;
                MinorVersion = 0;
                BuildNumber = 0;
                PlatformId = 0;
                CSDVersion = String.Empty;
            }
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
        private class OSVERSIONINFOEX
        {
            public int OSVersionInfoSize;
            public int MajorVersion;
            public int MinorVersion;
            public int BuildNumber;
            public int PlatformId;
            [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 0x80)] public string CSDVersion;
            public Int16 ServicePackMajor;
            public Int16 ServicePackMinor;
            public UInt16 SuiteMask;
            public byte ProductType;
            public byte Reserved;

            public OSVERSIONINFOEX()
            {
                OSVersionInfoSize = Marshal.SizeOf(this);
            }

            private void StopTheCompilerComplaining()
            {
                MajorVersion = 0;
                MinorVersion = 0;
                BuildNumber = 0;
                PlatformId = 0;
                CSDVersion = String.Empty;
                ServicePackMajor = 0;
                ServicePackMinor = 0;
                SuiteMask = 0;
                ProductType = 0;
                Reserved = 0;
            }
        }

        [StructLayout(LayoutKind.Sequential)]
        private struct SYSTEM_INFO
        {
            internal _PROCESSOR_INFO_UNION uProcessorInfo;
            public readonly uint dwPageSize;
            public readonly IntPtr lpMinimumApplicationAddress;
            public readonly IntPtr lpMaximumApplicationAddress;
            public readonly IntPtr dwActiveProcessorMask;
            public readonly uint dwNumberOfProcessors;
            public readonly uint dwProcessorType;
            public readonly uint dwAllocationGranularity;
            public readonly ushort dwProcessorLevel;
            public readonly ushort dwProcessorRevision;
        }

        private class VerArchitecture
        {
            //public const ushort ALPHA64 = 7;
            //public const ushort MSIL = 8;
            public const ushort AMD64 = 9;
            public const ushort IA64 = 6;
            public const ushort INTEL = 0;
            //public const ushort IA32_ON_WIN64 = 10;

            public const ushort UNKNOWN = 0xFFFF;

            private VerArchitecture()
            {
            }
        }

        private class VerPlatformId
        {
            public const Int32 Win32NT = 2;
            public const Int32 Win32Windows = 1;
            public const Int32 Win32s = 0;
            public const Int32 WinCE = 3;

            private VerPlatformId()
            {
            }
        }

        private class VerProductType
        {
            public const byte VER_NT_DOMAIN_CONTROLLER = 0x00000002;
            public const byte VER_NT_SERVER = 0x00000003;
            public const byte VER_NT_WORKSTATION = 0x00000001;

            private VerProductType()
            {
            }
        }

        private class VerSuiteMask
        {
            public const UInt32 VER_SERVER_NT = 0x80000000;
            public const UInt16 VER_SUITE_BACKOFFICE = 0x00000004;
            public const UInt16 VER_SUITE_BLADE = 0x00000400;
            public const UInt16 VER_SUITE_COMMUNICATIONS = 0x00000008;
            public const UInt16 VER_SUITE_DATACENTER = 0x00000080;
            public const UInt16 VER_SUITE_EMBEDDEDNT = 0x00000040;
            public const UInt16 VER_SUITE_EMBEDDED_RESTRICTED = 0x00000800;
            public const UInt16 VER_SUITE_ENTERPRISE = 0x00000002;
            public const UInt16 VER_SUITE_PERSONAL = 0x00000200;
            public const UInt16 VER_SUITE_SINGLEUSERTS = 0x00000100;
            public const UInt16 VER_SUITE_SMALLBUSINESS = 0x00000001;
            public const UInt16 VER_SUITE_SMALLBUSINESS_RESTRICTED = 0x00000020;
            public const UInt16 VER_SUITE_TERMINAL = 0x00000010;
            public const UInt32 VER_WORKSTATION_NT = 0x40000000;

            private VerSuiteMask()
            {
            }
        }

        [StructLayout(LayoutKind.Explicit)]
        private struct _PROCESSOR_INFO_UNION
        {
            [FieldOffset(0)] internal readonly uint dwOemId;
            [FieldOffset(0)] internal readonly ushort wProcessorArchitecture;
            [FieldOffset(2)] internal readonly ushort wReserved;
        }

        //-----------------------------------------------------------------------------
    }

    // OperatingSystemVersion

    //-----------------------------------------------------------------------------
}