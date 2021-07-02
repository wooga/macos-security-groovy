#include "security.h"
#include "jni_helper.h"
#include "jni_setup.h"
#include <stdio.h>

jint throwException( JNIEnv *env, jclass exClass, const char *message) {
    return (*env)->ThrowNew( env, exClass, message );
}

jint throwNullPointerException(JNIEnv *env) {
   return throwException(env, JC_NullPointerException, "Object is null");
}

jint throwIllegalArgumentException( JNIEnv *env, const char *message) {
    return throwException(env, JC_IllegalArgumentException, message);
}

jint throwSecurityError(JNIEnv *env, OSStatus status, const char *message ) {
    jclass exceptionClass = exceptionClassNameFromOSStatus(status);
    const char* resultMessage = errorMessageFromOSStatus(status);

    char *msg = (char*)malloc(strlen(resultMessage) + strlen(message) + 10);
    sprintf(msg, "%s Error: '%s'", message, resultMessage);

    return (*env)->ThrowNew( env, exceptionClass, msg );
}

const char* errorMessageFromOSStatus(OSStatus status) {
    CFStringRef message = SecCopyErrorMessageString(status, NULL);
    CFIndex length = CFStringGetLength(message);
    unsigned long bufferLength = ((length / 2) * sizeof(char)) + 1;
    const char* cMessage = CFStringGetCStringPtr(message, kCFStringEncodingUTF8);
    if(cMessage == NULL) {
        cMessage = (char*)malloc(bufferLength);
        CFStringGetCString(message, (char *)cMessage, bufferLength, kCFStringEncodingUTF8);
    }
    return cMessage;
}

jclass exceptionClassNameFromOSStatus(OSStatus status) {
    jclass exceptionClass = NULL;
    switch (status) {
        case errSecNoSuchKeychain:
            exceptionClass = JC_NoSuchKeychainException;
            break;
        case errSecInvalidKeychain:
            exceptionClass = JC_InvalidKeychainException;
            break;
        case errSecAuthFailed:
            exceptionClass = JC_InvalidKeychainCredentialsException;
            break;
        case errSecDuplicateKeychain:
            exceptionClass = JC_DuplicateKeychainException;
            break;
        case errSecNoDefaultKeychain:
            exceptionClass = JC_NoDefaultKeychainException;
            break;
        case errSecInvalidPrefsDomain:
            exceptionClass = JC_InvalidPreferenceDomainException;
            break;
        default:
            exceptionClass = JC_MacOsSecurityException;
            break;
    }
    return exceptionClass;
}

SecKeychainSettings * keychainSettingsFromJNI(JNIEnv *env, jobject settings) {
    jboolean lockWhenSystemSleeps = (*env)->CallBooleanMethod(env, settings, JMID_JC_MacOsKeychainSettings_getLockWhenSystemSleeps);
    
    jint timeout = (*env)->CallIntMethod(env, settings, JMID_JC_MacOsKeychainSettings_getTimeout);
    
    SecKeychainSettings *newSettings = malloc(sizeof(SecKeychainSettings));
    newSettings->version = SEC_KEYCHAIN_SETTINGS_VERS1;
    newSettings->lockInterval = timeout;
    newSettings->lockOnSleep = lockWhenSystemSleeps;
    newSettings->useLockInterval = timeout;
    
    return newSettings;
}

jobject wrapKeychainRef( JNIEnv *env, SecKeychainRef keychain) {
    jobject keychainRef = (*env)->NewObject(env, JC_DefaultKeychainRef, JMID_JC_DefaultKeychainRef_init, (jlong)keychain);
    return keychainRef;
}

SecKeychainRef unwrapKeychainRef( JNIEnv *env, jobject keychainRef) {
    if((*env)->IsSameObject(env, keychainRef, NULL)) {
        throwNullPointerException(env);
        return NULL;
    }

    jlong refValue = (*env)->CallLongMethod(env, keychainRef, JMID_JC_KeychainRef_getRef);
    return (SecKeychainRef) refValue;
}


FourCharCode convertJbyteArrayTo4CharCode(JNIEnv *env, jbyteArray array) {
    if((*env)->GetArrayLength(env, array) != 4) {
        throwIllegalArgumentException(env, "Array must contain 4 bytes");
        return 0;
    }
    
    jbyte buffer[4];
    (*env)->GetByteArrayRegion(env, array, 0, sizeof(FourCharCode), buffer);
    FourCharCode ret = (buffer[0] << 24) | (buffer[1] << 16) | (buffer[2] << 8) | buffer[3];
    return ret;
}
