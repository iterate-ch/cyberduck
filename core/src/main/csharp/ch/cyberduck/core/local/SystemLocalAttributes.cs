// Copyright (c) 2010-2026 Yves Langisch. All rights reserved.
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

using System;
using System.IO;
using System.Security.AccessControl;
using System.Security.Principal;
using ch.cyberduck.core;
using ch.cyberduck.core.serializer;
using org.apache.logging.log4j;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local
{
    public class SystemLocalAttributes : LocalAttributes
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemLocalAttributes).FullName);

        private readonly SystemLocal local;

        public SystemLocalAttributes(SystemLocal local) : base(local.getAbsolute())
        {
            this.local = local;
        }

        public override long getSize()
        {
            var resolved = local.Resolve();
            try
            {
                var fileInfo = new FileInfo(resolved.PlatformPath());
                if (fileInfo.Attributes.HasFlag(FileAttributes.Directory))
                {
                    return -1L;
                }

                return fileInfo.Length;
            }
            catch (Exception e)
            {
                Log.warn($"Failure getting size of {resolved}. {e.Message}");
                return -1L;
            }
        }

        public override long getModificationDate()
        {
            var resolved = local.Resolve();
            try
            {
                return ((DateTimeOffset)File.GetLastWriteTimeUtc(resolved.PlatformPath())).ToUnixTimeMilliseconds();
            }
            catch (Exception e)
            {
                Log.warn($"Failure getting timestamp of {resolved}. {e.Message}");
                return -1L;
            }
        }

        public override long getCreationDate()
        {
            var resolved = local.Resolve();
            try
            {
                return ((DateTimeOffset)File.GetCreationTimeUtc(resolved.PlatformPath())).ToUnixTimeMilliseconds();
            }
            catch (Exception e)
            {
                Log.warn($"Failure getting timestamp of {resolved}. {e.Message}");
                return -1L;
            }
        }

        public override long getAccessedDate()
        {
            var resolved = local.Resolve();
            try
            {
                return ((DateTimeOffset)File.GetLastAccessTimeUtc(resolved.PlatformPath())).ToUnixTimeMilliseconds();
            }
            catch (Exception e)
            {
                Log.warn($"Failure getting timestamp of {resolved}. {e.Message}");
                return -1L;
            }
        }

        public override Permission getPermission()
        {
            return new SystemLocalPermission(local.Resolve());
        }

        private class SystemLocalPermission(CoreLocal local) : Permission
        {
            public bool isWritable()
            {
                return Permission.__DefaultMethods.isWritable(this);
            }

            public string getMode()
            {
                return Permission.__DefaultMethods.getMode(this);
            }

            public Permission.Action getUser()

            {
                return ToAction(local);
            }

            public Permission.Action getGroup()
            {
                return Permission.Action.none;
            }

            public Permission.Action getOther()
            {
                return Permission.Action.none;
            }

            public string getSymbol()
            {
                return Permission.__DefaultMethods.getSymbol(this);
            }

            public bool isReadable()
            {
                return Permission.__DefaultMethods.isReadable(this);
            }

            public bool isExecutable()
            {
                return Permission.__DefaultMethods.isExecutable(this);
            }

            public bool isSetuid()
            {
                return Permission.__DefaultMethods.isSetuid(this);
            }

            public bool isSetgid()
            {
                return Permission.__DefaultMethods.isSetgid(this);
            }

            public bool isSticky()
            {
                return Permission.__DefaultMethods.isSticky(this);
            }

            object Permission.serialize(Serializer dict)
            {
                return Permission.__DefaultMethods.serialize(this, dict);
            }

            public string getDescription()
            {
                return Permission.__DefaultMethods.getDescription(this);
            }

            object Serializable.serialize(Serializer s)
            {
                return Permission.__DefaultMethods.serialize(this, s);
            }

            private static Permission.Action ToAction(CoreLocal local)
            {
                Permission.Action actions = Permission.Action.none;
                var currentUser = WindowsIdentity.GetCurrent();
                var fileInfo = new FileInfo(local.PlatformPath());
                var security = fileInfo.GetAccessControl();
                var rules = security.GetAccessRules(true, true, typeof(SecurityIdentifier));
                foreach (FileSystemAccessRule rule in rules)
                {
                    if (rule.AccessControlType == AccessControlType.Allow)
                    {
                        if (currentUser.User?.Equals(rule.IdentityReference) == true)
                        {
                            if (rule.FileSystemRights.HasFlag(FileSystemRights.ReadData))
                            {
                                actions = actions.or(Permission.Action.read);
                            }

                            if (rule.FileSystemRights.HasFlag(FileSystemRights.WriteData))
                            {
                                actions = actions.or(Permission.Action.write);
                            }

                            if (rule.FileSystemRights.HasFlag(FileSystemRights.ExecuteFile))
                            {
                                actions = actions.or(Permission.Action.execute);
                            }
                        }
                    }
                }

                if (fileInfo.IsReadOnly)
                {
                    actions = actions.and(Permission.Action.write.not());
                }

                return actions;
            }
        }
    }
}
