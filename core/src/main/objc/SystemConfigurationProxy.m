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

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_proxy_SystemConfigurationProxy_usePassiveFTPNative(JNIEnv *env, jobject this)
{
	return [Proxy usePassiveFTP];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_proxy_SystemConfigurationProxy_findNative(JNIEnv *env, jobject this, jstring target)
{
    NSString *uri = [Proxy find:JNFJavaToNSString(env, target)];
    if(nil == uri) {
        return NULL;
    }
	return (*env)->NewStringUTF(env, [uri UTF8String]);
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
        if(![proxyConfiguration respondsToSelector:@selector(objectForKey:)]) {
            continue;
        }
        // Every proxy dictionary has an entry for kCFProxyTypeKey
        if([[proxyConfiguration objectForKey:(NSString *)kCFProxyTypeKey] isEqualToString:(NSString *)kCFProxyTypeNone]) {
            return nil;
        }
        // Look for PAC configuration
        if([[proxyConfiguration objectForKey:(NSString *)kCFProxyTypeAutoConfigurationURL] boolValue]) {
            // If the type is kCFProxyTypeAutoConfigurationURL, it has an entry for kCFProxyAutoConfigurationURLKey
            NSString *pacLocation = [proxyConfiguration objectForKey:(NSString *)kCFProxyAutoConfigurationURLKey];
            if(!pacLocation) {
                CFRelease(proxyConfigurations);
                continue;
            }
            NSError* error;
            // Obtain from URL for automatic proxy configuration
            NSString *pacScript = [NSString stringWithContentsOfURL:[NSURL URLWithString:pacLocation] encoding:NSUTF8StringEncoding error:&error];
            if(error) {
                CFRelease(error);
                CFRelease(proxyConfigurations);
                continue;
            }
            CFErrorRef err = NULL;
            // Executes a proxy auto configuration script to determine the best proxy to use to retrieve a specified URL
            NSArray *pacProxies = (NSArray*)CFNetworkCopyProxiesForAutoConfigurationScript((CFStringRef)pacScript, (CFURLRef)[NSURL URLWithString:targetURL], &err);
            if(error) {
                CFRelease(error);
                CFRelease(proxyConfigurations);
                continue;
            }
            NSEnumerator *enumerator = [pacProxies objectEnumerator];
            NSDictionary *dict;
            NSString *proxyUrl = nil;
            while (nil != (dict = [enumerator nextObject])) {
                proxyUrl = [Proxy evaluate:dict];
                if(nil != proxyUrl) {
                    // Break on first match. The array is ordered optimally for requesting the URL specified.
                    break;
                }
            }
            CFRelease(proxyConfigurations);
            CFRelease(pacProxies);
            return proxyUrl;
        }
        else {
            NSString *proxyUrl = [Proxy evaluate:proxyConfiguration];
            CFRelease(proxyConfigurations);
            return proxyUrl;
        }
    }
    // Empty list
    return nil;
}

+ (NSString*)evaluate:(NSDictionary *) dict
{
    if(nil == dict) {
        return nil;
    }
    if(![dict respondsToSelector:@selector(objectForKey:)]) {
        return nil;
    }
    if(nil == [dict objectForKey:(NSString *)kCFProxyTypeKey]) {
        return nil;
    }
    if(nil == [dict objectForKey:(NSString *)kCFProxyHostNameKey]) {
        return nil;
    }
    if(nil == [dict objectForKey:(NSString *)kCFProxyPortNumberKey]) {
        return nil;
    }
    NSString *type = [dict objectForKey:(NSString *)kCFProxyTypeKey];
    if([[type componentsSeparatedByString:@"kCFProxyType"] count] == 2) {
        return [NSString stringWithFormat:@"%@://%@:%@",
               [[type componentsSeparatedByString:@"kCFProxyType"] objectAtIndex:1],
               [dict objectForKey:(NSString *)kCFProxyHostNameKey],
               [dict objectForKey:(NSString *)kCFProxyPortNumberKey]];
    }
    return nil;
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