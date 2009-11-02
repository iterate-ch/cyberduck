package com.barbarysoftware.jna;

import com.sun.jna.ptr.PointerByReference;

public class CFStringRef extends PointerByReference {

    public static CFStringRef toCFString(String s) {
        final char[] chars = s.toCharArray();
        int length = chars.length;
        return CarbonAPI.INSTANCE.CFStringCreateWithCharacters(null, chars, CFIndex.valueOf(length));
    }

}
