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

import java.util.*;
import java.util.Collection;

/**
 * @version $Id$
 */
public interface Validator {

    public static final String OVERWRITE = "overwrite";
    public static final String RESUME = "resume";
    public static final String SIMILAR = "similar";
    public static final String ASK = "ask";

    /**
     *
     * @param p
     */
    public abstract  void prompt(Path p);

    /**
     *
     * @return
     */
    public abstract  Collection result();

    /**
     * 
     * @return
     */
    public abstract  boolean isCanceled();
}
