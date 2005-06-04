#include <jni.h>
/* Header for class ch_cyberduck_ui_cocoa_growl_Growl */

#ifndef _Included_ch_cyberduck_ui_cocoa_growl_Growl
#define _Included_ch_cyberduck_ui_cocoa_growl_Growl
#ifdef __cplusplus
extern "C" {
#endif
	/* Inaccessible static: log */
	/* Inaccessible static: instance */
	/* Inaccessible static: class_00024ch_00024cyberduck_00024ui_00024cocoa_00024growl_00024Growl */

	/*
	 * Class:     ch_cyberduck_ui_cocoa_growl_Growl
	 * Method:    register
	 * Signature: ()V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_register
		(JNIEnv *, jobject);
	
	/*
	 * Class:     ch_cyberduck_ui_cocoa_growl_Growl
	 * Method:    notify
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_notify
		(JNIEnv *, jobject, jstring, jstring);

	/*
	 * Class:     ch_cyberduck_ui_cocoa_growl_Growl
	 * Method:    notifyWithImage
	 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_growl_Growl_notifyWithImage
		(JNIEnv *, jobject, jstring, jstring, jstring);
	
#ifdef __cplusplus
}
#endif
#endif

#import <Cocoa/Cocoa.h>
#import <Growl/Growl.h>

static id instance;

@interface Growl : NSObject<GrowlApplicationBridgeDelegate> {

}

+ (id)defaultInstance;

- (void)registerGrowl;
- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImageName:(NSString *) image;
- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description withImage:(NSImage *) image;
- (void)notifyGrowl:(NSString *)title withDescription:(NSString *)description;

@end
