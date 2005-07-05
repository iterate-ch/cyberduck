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

#import "Proxy.h"

jstring convertToJString(JNIEnv *env, NSString *nsString) 
{
	if(nsString == nil) {
		return NULL;
	}
	const char *unichars = [nsString UTF8String];
	
	return (*env)->NewStringUTF(env, unichars);
}

JNIEXPORT jboolean JNICALL Java_ch_cyberduck_core_Proxy_isSOCKSProxyEnabled(JNIEnv *env, 
																			jobject this)
{
	return [Proxy isSOCKSProxyEnabled];
}

JNIEXPORT jint JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyPort(JNIEnv *env, 
																	  jobject this)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSNumber *port = [Proxy getSOCKSProxyPort];
	[pool release];
	return [port intValue];
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyHost(JNIEnv *env, 
																		 jobject this)
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSString * host = [Proxy getSOCKSProxyHost];
	[pool release];
	return convertToJString(env, host);
}

JNIEXPORT jstring JNICALL Java_ch_cyberduck_core_Proxy_getSOCKSProxyUser(JNIEnv *env, 
																		 jobject this) {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSString *user = [Proxy getSOCKSProxyUser];
	[pool release];
	return convertToJString(env, user);
}

@implementation Proxy

//+ (BOOL)usePassiveFTP
//{
//	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
//	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesFTPPassive] boolValue];
//}

+ (BOOL)isSOCKSProxyEnabled
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	return [[proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSEnable] boolValue];
}

+ (NSString *)getSOCKSProxyHost
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	return [proxies objectForKey:(NSString *)kSCPropNetProxiesSOCKSProxy];
}

+ (NSNumber *)getSOCKSProxyPort
{
	NSDictionary *proxies = (NSDictionary *)SCDynamicStoreCopyProxies(NULL);
	return [proxies objectForKey:(NSNumber *)kSCPropNetProxiesSOCKSPort];
}

+ (NSString *)getSOCKSProxyUser
{
	return nil;
}

@end
