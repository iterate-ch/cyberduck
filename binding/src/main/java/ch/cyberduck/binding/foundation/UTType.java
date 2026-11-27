package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

/**
 * Uniform type identifier from UniformTypeIdentifiers framework. Available in macOS 11+
 */
public abstract class UTType extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("UTType", _Class.class);

    public static final String UTTypeData = "public.data";
    public static final String UTTypeFolder = "public.folder";

    public interface _Class extends ObjCClass {
        UTType typeWithFilenameExtension(String extension);
    }

    /**
     * @param extension Filename extension without leading dot
     * @return Type conforming to public.data or dynamic type if unknown. Null if extension is invalid
     */
    public static UTType typeWithFilenameExtension(final String extension) {
        return CLASS.typeWithFilenameExtension(extension);
    }

    public abstract String identifier();
}
