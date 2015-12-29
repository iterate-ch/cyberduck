package ch.cyberduck.core;

import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.library.Native;

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

public abstract class CrashReporter extends NSObject {

    static {
        Native.load("core");
    }

    private static final CrashReporter._Class CLASS = (CrashReporter._Class) Rococoa.createClass("UKCrashReporter", CrashReporter._Class.class);

    public static CrashReporter create() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        CrashReporter alloc();
    }

    public abstract CrashReporter init();

    /**
     * Send crash report
     */
    public abstract void checkForCrash();
}
