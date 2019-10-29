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

public interface NSGraphics {
    /// <i>native declaration : :17</i>
    int NSCompositeClear = 0;
    /// <i>native declaration : :18</i>
    int NSCompositeCopy = 1;
    /// <i>native declaration : :19</i>
    int NSCompositeSourceOver = 2;
    /// <i>native declaration : :20</i>
    int NSCompositeSourceIn = 3;
    /// <i>native declaration : :21</i>
    int NSCompositeSourceOut = 4;
    /// <i>native declaration : :22</i>
    int NSCompositeSourceAtop = 5;
    /// <i>native declaration : :23</i>
    int NSCompositeDestinationOver = 6;
    /// <i>native declaration : :24</i>
    int NSCompositeDestinationIn = 7;
    /// <i>native declaration : :25</i>
    int NSCompositeDestinationOut = 8;
    /// <i>native declaration : :26</i>
    int NSCompositeDestinationAtop = 9;
    /// <i>native declaration : :27</i>
    int NSCompositeXOR = 10;
    /// <i>native declaration : :28</i>
    int NSCompositePlusDarker = 11;
    /// <i>native declaration : :29</i>
    int NSCompositeHighlight = 12;
    /// <i>native declaration : :30</i>
    int NSCompositePlusLighter = 13;
    /// <i>native declaration : :36</i>
    int NSBackingStoreRetained = 0;
    /// <i>native declaration : :37</i>
    int NSBackingStoreNonretained = 1;
    /// <i>native declaration : :38</i>
    int NSBackingStoreBuffered = 2;
    /// <i>native declaration : :44</i>
    int NSWindowAbove = 1;
    /// <i>native declaration : :45</i>
    int NSWindowBelow = -1;
    /// <i>native declaration : :46</i>
    int NSWindowOut = 0;
    /// <i>native declaration : :52</i>
    int NSFocusRingOnly = 0;
    /// <i>native declaration : :53</i>
    int NSFocusRingBelow = 1;
    /// <i>native declaration : :54</i>
    int NSFocusRingAbove = 2;
    /// <i>native declaration : :61</i>
    int NSFocusRingTypeDefault = 0;
    /// <i>native declaration : :62</i>
    int NSFocusRingTypeNone = 1;
    /// <i>native declaration : :63</i>
    int NSFocusRingTypeExterior = 2;
}
