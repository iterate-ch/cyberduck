package ch.cyberduck.ui.cocoa.application;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

/// <i>native declaration : :10</i>
public abstract class NSStepper extends NSControl {

    /**
     * Original signature : <code>double minValue()</code><br>
     * <i>native declaration : :18</i>
     */
    public abstract double minValue();

    /**
     * Original signature : <code>void setMinValue(double)</code><br>
     * <i>native declaration : :19</i>
     */
    public abstract void setMinValue(double minValue);

    /**
     * Original signature : <code>double maxValue()</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract double maxValue();

    /**
     * Original signature : <code>void setMaxValue(double)</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract void setMaxValue(double maxValue);

    /**
     * Original signature : <code>double increment()</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract double increment();

    /**
     * Original signature : <code>void setIncrement(double)</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract void setIncrement(double increment);

    /**
     * Original signature : <code>BOOL valueWraps()</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract boolean valueWraps();

    /**
     * Original signature : <code>void setValueWraps(BOOL)</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract void setValueWraps(boolean valueWraps);

    /**
     * Original signature : <code>BOOL autorepeat()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract boolean autorepeat();

    /**
     * Original signature : <code>void setAutorepeat(BOOL)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void setAutorepeat(boolean autorepeat);
}
