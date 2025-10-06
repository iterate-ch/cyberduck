// Copyright (c) 2010-2025 iterate GmbH. All rights reserved.
// https://cyberduck.io/
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

using System;
using System.IO;
using ch.cyberduck.core;
using java.nio.file;
using java.nio.file.attribute;
using org.apache.logging.log4j;

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
                return new FileInfo(resolved.getAbsolute()).Length;
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
                return Files.getLastModifiedTime(Paths.get(resolved.getAbsolute())).toMillis();
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
                return Files
                    .readAttributes(Paths.get(resolved.getAbsolute()), typeof(BasicFileAttributes)).creationTime()
                    .toMillis();
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
                return Files
                    .readAttributes(Paths.get(resolved.getAbsolute()), typeof(BasicFileAttributes)).lastAccessTime()
                    .toMillis();
            }
            catch (Exception e)
            {
                Log.warn($"Failure getting timestamp of {resolved}. {e.Message}");
                return -1L;
            }
        }
    }
}
