/*
 * Copyright (c) 2002-2012 David Kocher. All rights reserved.
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

#import "launcher.h"

#define JAVA_LAUNCH_ERROR "Runtime Launcher Error"

#define JVM_PROPERTIES_KEY "Runtime"
#define JVM_LIB_KEY "Library"
#define JVM_MAIN_CLASS_NAME_KEY "MainClass"
#define JVM_WORKING_DIRECTORY_KEY "WorkingDirectory"
#define JVM_STARTONMAINTHREAD_KEY "StartOnMainThread"
#define JVM_OPTIONS_KEY "VMOptions"

#define APP_ROOT_PREFIX "$APP_PACKAGE"

typedef int (JNICALL *JLI_Launch_t)(int argc, char ** argv, /* main argc, argc */
                                    int jargc, const char** jargv, /* java args */
                                    int appclassc, const char** appclassv,/* app classpath */
                                    const char* fullversion, /* full version defined */
                                    const char* dotversion, /* dot version defined */
                                    const char* pname, /* program name */
                                    const char* lname, /* launcher name */
                                    jboolean javaargs, /* JAVA_ARGS */
                                    jboolean cpwildcard, /* classpath wildcard*/
                                    jboolean javaw, /* windows-only javaw */
                                    jint ergo); /* ergonomics class policy */

static int launches;
static char **progargv = NULL;;
static int progargc = 0;

int launch(int argc, char *argv[]) {
    if(0 == launches) {
        // main() is invoked twice including the java options the second time
        progargc = argc;
        progargv = argv;
    }
    // Increment the launch count
    ++launches;
    // Get the main bundle
    NSBundle *mainBundle = [NSBundle mainBundle];
    if (mainBundle == nil) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
            reason:NSLocalizedString(@"Main bundle not found", nil)
            userInfo:nil] raise];
    }
    // Canonicalized absolute pathname
    NSURL *resolved = [[mainBundle executableURL] URLByResolvingSymlinksInPath];
    if(![resolved isEqual:[mainBundle executableURL]]) {
        // Run from a symbolic link
        do {
            // Get parent directory of executable path
            resolved = [resolved URLByDeletingLastPathComponent];
            mainBundle = [NSBundle bundleWithURL:resolved];
        }
        // Repeat until bundle is found that has Contents/MacOS/duck
        while(nil == [mainBundle executablePath]);
    }
    NSDictionary *infoDictionary = [mainBundle infoDictionary];
    NSDictionary *javaDict = [infoDictionary objectForKey:@JVM_PROPERTIES_KEY];
    // Get the main class name
    NSString *mainClassName = [javaDict objectForKey:@JVM_MAIN_CLASS_NAME_KEY];
    if (mainClassName == nil) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
            reason:NSLocalizedString(@"Main class name required in Info.plist", nil)
            userInfo:nil] raise];
    }

    NSString *javaPath = [[javaDict objectForKey:@JVM_WORKING_DIRECTORY_KEY] stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]];
    if (javaPath == nil) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
            reason:NSLocalizedString(@"Working directory path required in Info.plist", nil)
            userInfo:nil] raise];
    }
    NSMutableString *classPath = [NSMutableString stringWithFormat:@"-Djava.class.path=%@", javaPath];
    // Set the class path
    NSFileManager *defaultFileManager = [NSFileManager defaultManager];
    for (NSString *file in [defaultFileManager contentsOfDirectoryAtPath:javaPath error:nil]) {
        if ([file hasSuffix:@".jar"]) {
            [classPath appendFormat:@":%@/%@", javaPath, file];
        }
    }
    NSArray *options;
    // Get the VM options
    if ([javaDict objectForKey:@JVM_OPTIONS_KEY] == nil) {
        options = [NSArray array];
    }
    else {
        if([[javaDict objectForKey:@JVM_OPTIONS_KEY] isKindOfClass:[NSString class]]) {
            options = [[javaDict objectForKey:@JVM_OPTIONS_KEY] componentsSeparatedByString:@" "];
        }
        else {
            options = [javaDict objectForKey:@JVM_OPTIONS_KEY];
        }
    }
    if ([javaDict objectForKey:@JVM_STARTONMAINTHREAD_KEY] != nil) {
        if ([[javaDict objectForKey:@JVM_STARTONMAINTHREAD_KEY] boolValue]) {
            options = [options arrayByAddingObject:@"-XstartOnFirstThread"];
        }
    }
    if (0 == progargc) {
        return 1;
    }
    // Get the application arguments
    NSMutableArray *arguments = [NSMutableArray arrayWithCapacity:progargc];
    // Program name
    [arguments addObject:[NSString stringWithUTF8String:(progargv[0])]];
    [arguments addObject:classPath];
    // VM Options
    for (NSString *option in options) {
        [arguments addObject: [option stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]]];
    }
    // Main class name
    [arguments addObject:mainClassName];
    // Main method arguments
    for (int i = 1; i < progargc; i++) {
        [arguments addObject:[NSString stringWithUTF8String:(progargv[i])]];
    }

    // Find the runtime bundle
    NSString *runtimePath = [[javaDict objectForKey:@JVM_LIB_KEY] stringByReplacingOccurrencesOfString:@APP_ROOT_PREFIX withString:[mainBundle bundlePath]];
    CFBundleRef runtimeBundle = CFBundleCreate(NULL, (CFURLRef)[NSURL fileURLWithPath:runtimePath]);
    if (!runtimeBundle) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
                                 reason:NSLocalizedString(@"Error loading runtime bundle.", nil)
                               userInfo:nil] raise];
    }
    JLI_Launch_t jli_LaunchFxnPtr = NULL;
    // Locate the JLI_Launch() function
    NSString *libjliPath = [runtimePath stringByAppendingString:@"/Contents/Home/lib/jli/libjli.dylib"];
    void *libJLI = dlopen([libjliPath fileSystemRepresentation], RTLD_LAZY);
    if(libJLI != NULL) {
        jli_LaunchFxnPtr = dlsym(libJLI, "JLI_Launch");
    }
    if(jli_LaunchFxnPtr == NULL) {
        [[NSException exceptionWithName:@JAVA_LAUNCH_ERROR
                                 reason:NSLocalizedString(@"Error loading runtime executable.", nil)
                               userInfo:nil] raise];
    }

    int jliArgumentsCount = (int)[arguments count];
    // Initialize the arguments to JLI_Launch()
    char *jliArguments[jliArgumentsCount];
    for (int i = 0; i < jliArgumentsCount; i++) {
        jliArguments[i] = strdup([[arguments objectAtIndex:i] UTF8String]);
    }
    // Invoke JLI_Launch()
    return jli_LaunchFxnPtr(jliArgumentsCount, jliArguments,
                            0, NULL, 0, NULL, "", "",
                            [[infoDictionary objectForKey:@"CFBundleName"] UTF8String], "Launcher",
                            FALSE, FALSE, FALSE, 0);
}