package ch.cyberduck.binding.application;

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

/// <i>native declaration : :83</i>
public abstract class NSPanel extends NSWindow {

    /**
     * Original signature : <code>BOOL isFloatingPanel()</code><br>
     * <i>native declaration : :88</i>
     */
    public abstract boolean isFloatingPanel();

    /**
     * Original signature : <code>void setFloatingPanel(BOOL)</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract void setFloatingPanel(boolean flag);

    /**
     * Original signature : <code>BOOL becomesKeyOnlyIfNeeded()</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract boolean becomesKeyOnlyIfNeeded();

    /**
     * Original signature : <code>void setBecomesKeyOnlyIfNeeded(BOOL)</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract void setBecomesKeyOnlyIfNeeded(boolean flag);

    /**
     * Original signature : <code>void setWorksWhenModal(BOOL)</code><br>
     * <i>native declaration : :93</i>
     */
    public abstract void setWorksWhenModal(boolean flag);

    /// <i>native declaration : :61</i>
    public static final int NSOKButton = 1;
    /// <i>native declaration : :62</i>
    public static final int NSCancelButton = 0;
    /// <i>native declaration : :67</i>
    public static final int NSUtilityWindowMask = 1 << 4;
    /// <i>native declaration : :68</i>
    public static final int NSDocModalWindowMask = 1 << 6;
    /**
     * specify a panel that does not activate owning application<br>
     * <i>native declaration : :73</i>
     */
    public static final int NSNonactivatingPanelMask = 1 << 7;
    /**
     * specify a heads up display panel<br>
     * <i>native declaration : :79</i>
     */
    public static final int NSHUDWindowMask = 1 << 13;
}
