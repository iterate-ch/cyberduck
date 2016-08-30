package ch.cyberduck.core.formatter;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

public class DecimalSizeFormatter extends AbstractSizeFormatter {

    public static final Unit KILO = new Unit(1000L) {
        @Override
        public String suffix() {
            return "KB";
        }
    }; //10^3
    public static final Unit MEGA = new Unit(1000000L) {
        @Override
        public String suffix() {
            return "MB";
        }
    }; //10^6
    public static final Unit GIGA = new Unit(1000000000L) {
        @Override
        public String suffix() {
            return "GB";
        }
    }; // 10^9

    public DecimalSizeFormatter() {
        super(KILO, MEGA, GIGA);
    }
}
