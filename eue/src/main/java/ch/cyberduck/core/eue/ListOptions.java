package ch.cyberduck.core.eue;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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


import java.util.ArrayList;
import java.util.List;

public class ListOptions {

    private ListOptions() {
    }

    public static List<String> getForWin32AndShares() {
        List<String> options = new ArrayList<>();
        options.add("win32props");
        options.add("shares");
        return options;
    }

    public static List<String> getDownload() {
        List<String> options = new ArrayList<>();
        options.add("download");
        return options;
    }

}
