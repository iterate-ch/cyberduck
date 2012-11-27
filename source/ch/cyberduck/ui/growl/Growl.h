#include <jni.h>
/* Header for class ch_cyberduck_ui_growl_GrowlNative */

#ifndef _Included_ch_cyberduck_ui_growl_GrowlNative
#define _Included_ch_cyberduck_ui_growl_GrowlNative
#ifdef __cplusplus
extern "C" {
#endif
	/* Inaccessible static: log */
	/* Inaccessible static: instance */
	/* Inaccessible static: class_00024ch_00024cyberduck_00024ui_00024cocoa_00024growl_00024Growl */

	/*
	 * Class:     ch_cyberduck_ui_growl_GrowlNative
	 * Method:    register
	 * Signature: ()V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_growl_GrowlNative_register
		(JNIEnv *, jobject);
	
	/*
	 * Class:     ch_cyberduck_ui_growl_GrowlNative
	 * Method:    notify
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_growl_GrowlNative_notify
		(JNIEnv *, jobject, jstring, jstring);

	/*
	 * Class:     ch_cyberduck_ui_growl_GrowlNative
	 * Method:    notifyWithImage
	 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_ch_cyberduck_ui_growl_GrowlNative_notifyWithImage
		(JNIEnv *, jobject, jstring, jstring, jstring);
	
#ifdef __cplusplus
}
#endif
#endif
