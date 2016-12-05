package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class NSLevelIndicator extends NSControl {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSLevelIndicator", _Class.class);

    public static final int NSRelevancyLevelIndicatorStyle = 0;
    public static final int NSContinuousCapacityLevelIndicatorStyle = 1;
    public static final int NSDiscreteCapacityLevelIndicatorStyle = 2;
    public static final int NSRatingLevelIndicatorStyle = 3;

    public static NSLevelIndicator levelIndicatorWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public interface _Class extends ObjCClass {
        NSLevelIndicator alloc();
    }

    public abstract NSLevelIndicator initWithFrame(NSRect frameRect);

    public abstract int getMinValue();

    public abstract void setMinValue(final int minValue);

    public abstract int getMaxValue();

    public abstract void setMaxValue(int maxValue);

    public abstract int getWarningValue();

    public abstract void setWarningValue(int warningValue);

    public abstract int getCriticalValue();

    public abstract void setCriticalValue(int criticalValue);

    public abstract int getTickMarkPosition();

    public abstract void setTickMarkPosition(int tickMarkPosition);

    public abstract int getNumberOfTickMarks();

    public abstract void setNumberOfTickMarks(int numberOfTickMarks);

    public abstract int getLevelIndicatorStyle();

    public abstract void setLevelIndicatorStyle(final int levelIndicatorStyle);
}
