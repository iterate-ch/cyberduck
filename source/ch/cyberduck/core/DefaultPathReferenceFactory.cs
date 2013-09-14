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

namespace Ch.Cyberduck.Core
{
    public class DefaultPathReferenceFactory
    {
        public static void Register()
        {
            PathReferenceFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM,
                                            new Factory());
        }

        private class Factory : PathReferenceFactory
        {
            protected override object create()
            {
                throw new NotImplementedException("Please provide a parameter");
            }

            protected override PathReference create(Path p)
            {
                return new DefaultPathReference(p);
            }
        }
    }
}