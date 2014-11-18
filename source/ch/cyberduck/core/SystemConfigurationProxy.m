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

#import "SystemConfigurationProxy.h"
#import <JavaNativeFoundation/JNFString.h>

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_usePassiveFTPNative(JNIEnv *env, jobject this)
{
	return [Proxy usePassiveFTP];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_findNative(JNIEnv *env, jobject this, jstring target)
{

	return (*env)->NewStringUTF(env, [[Proxy find:JNFJavaToNSString(env, target)] UTF8String]);
}

@implementation Proxy

+ (NSString*)find:(NSString*)targetURL
{
    NSDictionary *defaultConfiguration = (NSDictionary *)CFNetworkCopySystemProxySettings();
    if(!defaultConfiguration) {
        // No proxy settings have been defined
        return nil;
    }
	NSArray *proxyConfigurations = (NSArray *)CFNetworkCopyProxiesForURL((CFURLRef)[NSURL URLWithString:targetURL], (CFDictionaryRef) defaultConfiguration);
    CFRelease(defaultConfiguration);
    if(!proxyConfigurations) {
        // No proxy settings have been defined
        return nil;
    }
    NSEnumerator *enumerator = [proxyConfigurations objectEnumerator];
    NSDictionary *proxyConfiguration;
    while((proxyConfiguration = [enumerator nextObject]) != nil) {
        // Every proxy dictionary has an entry for kCFProxyTypeKey
        if([[proxyConfiguration objectForKey:(NSString *)kCFProxyTypeKey] isEqualToString:(NSString *)kCFProxyTypeNone]) {
            return nil;
        }
        // Look for PAC configuration
        if([[proxyConfiguration objectForKey:(NSString *)kCFProxyTypeAutoConfigurationURL] boolValue]) {
            // If the type is kCFProxyTypeAutoConfigurationURL, it has an entry for kCFProxyAutoConfigurationURLKey
            NSString *pacLocation = [proxyConfiguration objectForKey:(NSString *)kCFProxyAutoConfigurationURLKey];
            if(!pacLocation) {
                CFRelease(proxyConfiguration);
                continue;
            }
            // Obtain from URL for automatic proxy configuration
            NSString *pacScript = [NSString stringWithContentsOfURL:[NSURL URLWithString:pacLocation] encoding:NSUTF8StringEncoding error:NULL];
            if(!pacScript) {
                CFRelease(proxyConfiguration);
                continue;
            }
            CFErrorRef error = NULL;
            // Executes a proxy auto configuration script to determine the best proxy to use to retrieve a specified URL
            NSArray *pacProxies = (NSArray*)CFNetworkCopyProxiesForAutoConfigurationScript((CFStringRef)pacScript, (CFURLRef)[NSURL URLWithString:targetURL], &error);
            if(error) {
                CFRelease(error);
                CFRelease(proxyConfiguration);
                continue;
            }
            NSEnumerator *enumerator = [pacProxies objectEnumerator];
            NSDictionary *dict;
            NSString *proxyUrl = nil;
            while (nil != (dict = [enumerator nextObject])) {
                proxyUrl = [Proxy evaluate:dict];
                if(nil != proxyUrl) {
                    // Break on first match
                    break;
                }
            }
            CFRelease(proxyConfiguration);
            CFRelease(pacProxies);
            return proxyUrl;
        }
        else {
            NSString *proxyUrl = [Proxy evaluate:proxyConfiguration];
            CFRelease(proxyConfiguration);
            return proxyUrl;
        }
    }
    // Empty list
    return nil;
}

+ (NSString*)evaluate:(NSDictionary *) dict
{
   return [NSString stringWithFormat:@"%@://%@:%@",
           [[[dict objectForKey:(NSString *)kCFProxyTypeKey] componentsSeparatedByString:@"kCFProxyType"] objectAtIndex:0],
           [dict objectForKey:(NSString *)kCFProxyHostNameKey],
           [dict objectForKey:(NSString *)kCFProxyPortNumberKey]];
}

+ (BOOL)usePassiveFTP
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesFTPPassive] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
    return enabled;
}

@end