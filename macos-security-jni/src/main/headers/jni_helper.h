#ifndef jni_helper_h
#define jni_helper_h
#include <jni.h>
#include <stdio.h>

jint throwNoClassDefError( JNIEnv *env, const char *message );
jint throwIllegalArgumentException( JNIEnv *env, const char *message);
jint throwSecurityError(JNIEnv *env, OSStatus status, const char *message );

const char* getPathFromJObjectFile(JNIEnv *env, jobject file);
const char* errorMessageFromOSStatus(OSStatus status);
jclass exceptionClassNameFromOSStatus(OSStatus status);

SecKeychainSettings * keychainSettingsFromJNI(JNIEnv *env, jobject settings);

jobject wrapKeychainRef( JNIEnv *env, SecKeychainRef keychain);
SecKeychainRef unwrapKeychainRef( JNIEnv *env, jobject keychainRef);

FourCharCode convertJbyteArrayTo4CharCode(JNIEnv *env, jbyteArray array);

#define CHECK_NULL_PARAMETER(param) if((*env)->IsSameObject(env, param, NULL)) { \
throwIllegalArgumentException(env, "Parameter " #param " should not be null"); \
    return NULL; \
}


#endif /* jni_helper_h */
