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

JNIEXPORT jobjectArray JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getProxyExceptionsNative(JNIEnv *env, jobject this)
{
    jint i = 0;
    NSArray* exceptions;
    NSEnumerator* list = [Proxy getProxiesExceptionList];
    if(nil == list) {
        exceptions = [NSArray array];
    }
    else {
        exceptions = [list allObjects];
    }
    jobjectArray result = (jobjectArray)(*env)->NewObjectArray(env, [exceptions count], (*env)->FindClass(env, "java/lang/String"), (*env)->NewStringUTF(env, ""));
    for(i = 0; i < [exceptions count]; i++) {
        (*env)->SetObjectArrayElement(env, result, i, JNFNSToJavaString(env, [exceptions objectAtIndex:i]));
    }
    return result;
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isSimpleHostnameExcludedNative(JNIEnv *env, jobject this)
{
	return [Proxy isSimpleHostnameExcluded];
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isSOCKSProxyEnabledNative(JNIEnv *env, jobject this)
{
	return [Proxy isSOCKSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getSOCKSProxyPortNative(JNIEnv *env,  jobject this)
{
	return [[Proxy getSOCKSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getSOCKSProxyHostNative(JNIEnv *env, jobject this)
{
	return JNFNSToJavaString(env, [Proxy getSOCKSProxyHost]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isHTTPProxyEnabledNative(JNIEnv *env, jobject this)
{
	return [Proxy isHTTPProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPProxyPortNative(JNIEnv *env, jobject this)
{
	return [[Proxy getHTTPProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPProxyHostNative(JNIEnv *env, jobject this)
{
	return JNFNSToJavaString(env, [Proxy getHTTPProxyHost]);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_isHTTPSProxyEnabledNative(JNIEnv *env, jobject this)
{
	return [Proxy isHTTPSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPSProxyPortNative(JNIEnv *env, jobject this)
{
	return [[Proxy getHTTPSProxyPort] intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_SystemConfigurationProxy_getHTTPSProxyHostNative(JNIEnv *env, jobject this)
{
	return JNFNSToJavaString(env, [Proxy getHTTPSProxyHost]);
}

@implementation Proxy

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

+ (NSEnumerator*)getProxiesExceptionList {
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
    NSEnumerator *exceptions = [[proxies objectForKey:(NSString *)kSCPropNetProxiesExceptionsList] objectEnumerator];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
    return exceptions;
}

+ (BOOL)isSimpleHostnameExcluded {
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesExcludeSimpleHostnames] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (BOOL)isSOCKSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getSOCKSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSString *hostname = [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSProxy] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [hostname autorelease];
}

+ (NSNumber *)getSOCKSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSNumber *port = [[proxies objectForKey:(NSNumber *)kSCPropNetProxiesSOCKSPort] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [port autorelease];
}

+ (BOOL)isHTTPProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getHTTPProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSString *hostname = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPProxy] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [hostname autorelease];
}

+ (NSNumber *)getHTTPProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSNumber *port = [[proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPPort] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [port autorelease];
}

+ (BOOL)isHTTPSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return NO;
	BOOL enabled = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSEnable] boolValue];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return enabled;
}

+ (NSString *)getHTTPSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSString *hostname = [[proxies objectForKey:(NSString *)kSCPropNetProxiesHTTPSProxy] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [hostname autorelease];
}

+ (NSNumber *)getHTTPSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
    if(!proxies) return nil;
	NSNumber *port = [[proxies objectForKey:(NSNumber *)kSCPropNetProxiesHTTPSPort] retain];
	if (proxies != NULL) {
        CFRelease(proxies);
    }
	return [port autorelease];
}

@end