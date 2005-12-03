package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class ValidatorFactory {
    private static Logger log = Logger.getLogger(ValidatorFactory.class);

    private static Map factories = new HashMap();

    protected abstract Validator create();

    public static void addFactory(Class clazz, ValidatorFactory factory) {
        factories.put(clazz, factory);
    }

    public static Validator createValidator(Class clazz) {
        log.debug("createValidator:" + clazz.getName());
        if (!factories.containsKey(clazz)) {
            try {
                // Load dynamically
                String clazzname = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
                Class.forName("ch.cyberduck.ui.cocoa.CD" + clazzname + "ValidatorController");
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("No validator for queue of type: " + clazz.getName());
            }
            // See if it was put in:
            if (!factories.containsKey(clazz)) {
                throw new RuntimeException("No validator for queue of type: " + clazz.getName());
            }
        }
        return ((ValidatorFactory) factories.get(clazz)).create();
    }
}