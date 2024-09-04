package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

public interface NSAccessibility {

    /**
     * The title of the accessibility element—for example, a button’s visible text.
     *
     * @param title
     * @since macOS 10.10+
     */
    void setAccessibilityTitle(String title);

    /**
     * Use this property only when the results of activating this element are not obvious from the element’s label.
     * This string functions as a tooltip. For example, VoiceOver reads this string when you pause over a control.
     * To help ensure that accessibility clients like VoiceOver read the help text with the proper inflection, begin
     * this string with a verb, capitalize the first letter, and end the string with a period. Always localize this
     * string. The default value is nil.
     *
     * @param help The help text for the accessibility element.
     */
    void setAccessibilityHelp(String help);

    /**
     * Do not include the accessibility element’s type in the label (for example, write Play, not Play button.).
     * If possible, use a single word. To help ensure that accessibility clients such as VoiceOver read the label
     * with the correct intonation, start this label with a capital letter. Do not put a period at the end.
     * Always localize the label.
     *
     * @param label A short description of the accessibility element.
     * @since macOS 10.10+
     */
    void setAccessibilityLabel(String label);
}
