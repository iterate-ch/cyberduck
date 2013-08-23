// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Controller
{
    public class TreePathReference : PathReference
    {
        private readonly int _hashCode;
        private readonly Path _path;
        private readonly PathReference _reference;

        public TreePathReference(Path path)
        {
            _path = path;
            _reference = new DefaultPathReference(_path);
            _hashCode = _reference.unique().GetHashCode();
        }

        public object unique()
        {
            return _reference.unique();
        }

        public override int GetHashCode()
        {
            return _hashCode;
        }

        public override bool Equals(object other)
        {
            if (null == other)
            {
                return false;
            }
            if (other is PathReference)
            {
                return _reference.unique().GetHashCode() == ((PathReference) other).unique().GetHashCode();
            }
            return false;
        }

        public static void Register()
        {
            PathReferenceFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : PathReferenceFactory
        {
            protected override object create()
            {
                throw new NotImplementedException("Please provide a parameter");
            }

            protected override PathReference create(Path p)
            {
                return new TreePathReference(p);
            }
        }
    }
}