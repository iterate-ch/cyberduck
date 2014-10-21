package ch.cyberduck.core.io.watchservice.jna;

import com.sun.jna.ptr.PointerByReference;

public class CFStringRef extends PointerByReference {

    public static CFStringRef toCFString(String s) {
        final char[] chars = s.toCharArray();
        int length = chars.length;
        return FSEvents.library.CFStringCreateWithCharacters(null, chars, CFIndex.valueOf(length));
    }

}
