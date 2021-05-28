#include "jni_setup.h"

jclass JC_MacOsKeychainSettings;
jclass JC_KeychainRef;
jclass JC_DefaultKeychainRef;

jmethodID JMID_JC_MacOsKeychainSettings_init;
jmethodID JMID_JC_MacOsKeychainSettings_getLockWhenSystemSleeps;
jmethodID JMID_JC_MacOsKeychainSettings_getTimeout;
jmethodID JMID_JC_KeychainRef_getRef;
jmethodID JMID_JC_DefaultKeychainRef_init;

//Exception types

jclass JC_NoSuchKeychainException;
jclass JC_InvalidKeychainException;
jclass JC_InvalidKeychainCredentialsException;
jclass JC_DuplicateKeychainException;
jclass JC_NoDefaultKeychainException;
jclass JC_InvalidPreferenceDomainException;
jclass JC_MacOsSecurityException;

jclass JC_IllegalArgumentException;
jclass JC_NullPointerException;
jclass JC_NoClassDefFoundError;

static jint JNI_VERSION = JNI_VERSION_1_8;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {

    // Obtain the JNIEnv from the VM and confirm JNI_VERSION
    JNIEnv* env;
    
    jint r = (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION);

    if (r != JNI_OK) {
        printf("unable to load env");
        return JNI_ERR;
    }

    jclass tempLocalClassRef;

    //com.wooga.security.MacOsKeychainSettings
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/MacOsKeychainSettings");
    JC_MacOsKeychainSettings = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);

    JMID_JC_MacOsKeychainSettings_init = (*env)->GetMethodID(env, JC_MacOsKeychainSettings, "<init>", "(ZI)V");
    JMID_JC_MacOsKeychainSettings_getLockWhenSystemSleeps = (*env)->GetMethodID(env, JC_MacOsKeychainSettings, "getLockWhenSystemSleeps", "()Z");
    JMID_JC_MacOsKeychainSettings_getTimeout = (*env)->GetMethodID(env, JC_MacOsKeychainSettings, "getTimeout", "()I");
    
    //com.wooga.security.KeychainRef
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/KeychainRef");
    JC_KeychainRef = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    JMID_JC_KeychainRef_getRef = (*env)->GetMethodID(env, JC_KeychainRef, "getRef", "()J");
    
    //com.wooga.security.DefaultKeychainRef
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/DefaultKeychainRef");
    JC_DefaultKeychainRef = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    JMID_JC_DefaultKeychainRef_init = (*env)->GetMethodID(env, JC_DefaultKeychainRef, "<init>", "(J)V");

    //Exception types
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/NoSuchKeychainException");
    JC_NoSuchKeychainException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
   
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/InvalidKeychainException");
    JC_InvalidKeychainException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/InvalidKeychainCredentialsException");
    JC_InvalidKeychainCredentialsException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/DuplicateKeychainException");
    JC_DuplicateKeychainException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/NoDefaultKeychainException");
    JC_NoDefaultKeychainException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/InvalidPreferenceDomainException");
    JC_InvalidPreferenceDomainException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "com/wooga/security/error/MacOsSecurityException");
    JC_MacOsSecurityException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "java/lang/NoClassDefFoundError");
    JC_NoClassDefFoundError = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "java/lang/NullPointerException");
    JC_NullPointerException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    tempLocalClassRef = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    JC_IllegalArgumentException = (jclass) (*env)->NewGlobalRef(env, tempLocalClassRef);
    (*env)->DeleteLocalRef(env, tempLocalClassRef);
    
    return JNI_VERSION;
}

/**************************************************************
 * Destroy the global static Class Id variables
 **************************************************************/
void JNI_OnUnload(JavaVM *vm, void *reserved) {

    // Obtain the JNIEnv from the VM
    // NOTE: some re-do the JNI Version check here, but I find that redundant
    JNIEnv* env;
    (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION);

    (*env)->DeleteGlobalRef(env, JC_MacOsKeychainSettings);
    (*env)->DeleteGlobalRef(env, JC_KeychainRef);
    (*env)->DeleteGlobalRef(env, JC_DefaultKeychainRef);
    
    (*env)->DeleteGlobalRef(env, JC_NoSuchKeychainException);
    (*env)->DeleteGlobalRef(env, JC_InvalidKeychainException);
    (*env)->DeleteGlobalRef(env, JC_InvalidKeychainCredentialsException);
    (*env)->DeleteGlobalRef(env, JC_DuplicateKeychainException);
    (*env)->DeleteGlobalRef(env, JC_NoDefaultKeychainException);
    (*env)->DeleteGlobalRef(env, JC_InvalidPreferenceDomainException);
    (*env)->DeleteGlobalRef(env, JC_MacOsSecurityException);
    
    (*env)->DeleteGlobalRef(env, JC_IllegalArgumentException);
    (*env)->DeleteGlobalRef(env, JC_NullPointerException);
    (*env)->DeleteGlobalRef(env, JC_NoClassDefFoundError);
    
}
