#include "com_wooga_security_MacOsSecurity.h"
#include "security.h"

#include <stdio.h>
#include <sys/mman.h>

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCreate
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobject JNICALL Java_com_wooga_security_MacOsSecurity_keychainCreate__Ljava_lang_String_2Ljava_lang_String_2
(JNIEnv *env, jclass class, jstring keychain, jstring password) {
    CHECK_NULL_PARAMETER(keychain)
    CHECK_NULL_PARAMETER(password)
    const char* keychainPassword = (*env)->GetStringUTFChars(env, password, NULL);
    unsigned long passwordLength = strlen(keychainPassword);
    const char* keychainPath = (*env)->GetStringUTFChars(env, keychain, NULL);

    SecKeychainRef k;
    
    OSStatus result = SecKeychainCreate(keychainPath, (UInt32)passwordLength, keychainPassword, false, NULL, &k);
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to create keychain.");
    }
    
    return wrapKeychainRef(env, k);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCreate
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/wooga/security/MacOsKeychainSettings;)Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobject JNICALL Java_com_wooga_security_MacOsSecurity_keychainCreate__Ljava_lang_String_2Ljava_lang_String_2Lcom_wooga_security_MacOsKeychainSettings_2
(JNIEnv *env, jclass class, jstring keychain, jstring password, jobject initialSettings) {
    jobject k = Java_com_wooga_security_MacOsSecurity_keychainCreate__Ljava_lang_String_2Ljava_lang_String_2(env, class, keychain, password);
    if((*env)->ExceptionOccurred(env)) {
        return 0;
    }

    if(!(*env)->IsSameObject(env, initialSettings, NULL)) {
        Java_com_wooga_security_MacOsSecurity_keychainSetSettings(env, class, k, initialSettings);
        if((*env)->ExceptionOccurred(env)) {
            return NULL;
        }
    }
    return k;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainDelete
 * Signature: (Lcom/wooga/security/KeychainRef;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainDelete
(JNIEnv *env, jclass class, jobject keychain) {
    CHECK_NULL_PARAMETER(keychain)
    SecKeychainRef k = unwrapKeychainRef(env, keychain);
    OSStatus result = SecKeychainDelete(k);
    return result != errSecSuccess;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainLock
 * Signature: (Lcom/wooga/security/KeychainRef;)I
 */
JNIEXPORT jint JNICALL Java_com_wooga_security_MacOsSecurity_keychainLock
(JNIEnv *env, jclass class, jobject keychain) {
    SecKeychainRef k = NULL;
    if(!(*env)->IsSameObject(env, keychain, NULL)) {
        k = unwrapKeychainRef(env, keychain);
        if((*env)->ExceptionOccurred(env)) {
            return NULL;
        }
    }

    OSStatus result = SecKeychainLock(k);
    
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Unable to lock keychain.");
        return false;
    }
    
    return Java_com_wooga_security_MacOsSecurity_keychainGetStatus(env, class, keychain);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainLockAll
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainLockAll
  (JNIEnv *env, jclass class) {
    OSStatus result = SecKeychainLockAll();
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Unable to lock all keychains.");
        return false;
    }
    return true;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainUnlock
 * Signature: (Lcom/wooga/security/KeychainRef;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_wooga_security_MacOsSecurity_keychainUnlock
(JNIEnv *env, jclass class, jobject keychain, jobject password) {
    SecKeychainRef k = NULL;
    if(!(*env)->IsSameObject(env, keychain, NULL)) {
        k = unwrapKeychainRef(env, keychain);
        if((*env)->ExceptionOccurred(env)) {
            return NULL;
        }
    }

    const char* keychainPassword = (*env)->GetStringUTFChars(env, password, NULL);
    unsigned long passwordLength = strlen(keychainPassword);
    
    OSStatus result = SecKeychainUnlock(k, (UInt32)passwordLength, keychainPassword, true);

    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Unable to unlock keychain.");
        return 0;
    }
    return Java_com_wooga_security_MacOsSecurity_keychainGetStatus(env, class, keychain);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCopySettings
 * Signature: (Lcom/wooga/security/KeychainRef;)Lcom/wooga/security/MacOsKeychainSettings;
 */
JNIEXPORT jobject JNICALL Java_com_wooga_security_MacOsSecurity_keychainCopySettings
(JNIEnv *env, jclass class, jobject keychain) {
    CHECK_NULL_PARAMETER(keychain)
    SecKeychainRef k = unwrapKeychainRef(env, keychain);
    
    SecKeychainSettings *settings = malloc(sizeof(SecKeychainSettings));
    settings->version = SEC_KEYCHAIN_SETTINGS_VERS1;
    settings->lockInterval = INT32_MAX;
    settings->lockOnSleep = false;
    settings->useLockInterval = false;
    
    OSStatus result = SecKeychainCopySettings(k, settings);
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to copy keychain settings.");
    }
    
    jobject jniSettings = (*env)->NewObject(env, JC_MacOsKeychainSettings, JMID_JC_MacOsKeychainSettings_init, settings->lockOnSleep, settings->lockInterval);
    free(settings);
    return jniSettings;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainSetSettings
 * Signature: (Lcom/wooga/security/KeychainRef;Lcom/wooga/security/MacOsKeychainSettings;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainSetSettings
 (JNIEnv *env, jclass class, jobject keychain, jobject settings) {
     SecKeychainRef k = NULL;
     if(!(*env)->IsSameObject(env, keychain, NULL)) {
         k = unwrapKeychainRef(env, keychain);
         if((*env)->ExceptionOccurred(env)) {
             return NULL;
         }
     }

     SecKeychainSettings *newSettings = keychainSettingsFromJNI(env, settings);
     OSStatus result = SecKeychainSetSettings(k, newSettings);
     free(newSettings);
     if(result != errSecSuccess) {
         throwSecurityError(env, result, "Failed to set keychain settings.");
         return false;
     }
     
     return true;
 }

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainOpen
 * Signature: (Ljava/lang/String;)Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobject JNICALL Java_com_wooga_security_MacOsSecurity_keychainOpen
 (JNIEnv *env, jclass class, jstring keychainPath) {
     CHECK_NULL_PARAMETER(keychainPath)
     const char* keychainPathString = (*env)->GetStringUTFChars(env, keychainPath, NULL);
     SecKeychainRef k = NULL;

     OSStatus result = SecKeychainOpen(keychainPathString, &k);
     if(result != errSecSuccess) {
         throwSecurityError(env, result, "Failed to open keychain.");
     }
     
     return wrapKeychainRef(env, k);
 }

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainSetDefault
 * Signature: (Lcom/wooga/security/KeychainRef;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainSetDefault__Lcom_wooga_security_KeychainRef_2
(JNIEnv *env, jclass class, jobject keychain) {
    return Java_com_wooga_security_MacOsSecurity_keychainSetDefault__ILcom_wooga_security_KeychainRef_2(env, class, -1, keychain);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainSetDefault
 * Signature: (ILcom/wooga/security/KeychainRef;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainSetDefault__ILcom_wooga_security_KeychainRef_2
(JNIEnv *env, jclass class, jint domain, jobject keychain) {
    CHECK_NULL_PARAMETER(keychain)
    SecKeychainRef k = unwrapKeychainRef(env, keychain);
    OSStatus result;
    
    if(domain == -1) {
        result = SecKeychainSetDefault(k);
    } else {
        SecPreferencesDomain d = (SecPreferencesDomain) domain;
        result = SecKeychainSetDomainDefault(d, k);
    }
    
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to set default keychain.");
        return false;
    }
    return true;
    
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCopyDefault
 * Signature: ()Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobject JNICALL Java_com_wooga_security_MacOsSecurity_keychainCopyDefault__
(JNIEnv *env, jclass class) {
    return Java_com_wooga_security_MacOsSecurity_keychainCopyDefault__I(env, class, -1);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCopyDefault
 * Signature: (I)Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobject JNICALL Java_com_wooga_security_MacOsSecurity_keychainCopyDefault__I
(JNIEnv *env, jclass class, jint domain) {
    SecKeychainRef k = NULL;
    OSStatus result;

    if(domain == -1) {
        result = SecKeychainCopyDefault(&k);
    } else {
        SecPreferencesDomain d = (SecPreferencesDomain) domain;
        result = SecKeychainCopyDomainDefault(d, &k);
    }

    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to copy default keychain.");
        return false;
    }
    return wrapKeychainRef(env, k);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainGetPath
 * Signature: (Lcom/wooga/security/KeychainRef;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_wooga_security_MacOsSecurity_keychainGetPath
(JNIEnv *env, jclass class, jobject keychain) {
    CHECK_NULL_PARAMETER(keychain)
    SecKeychainRef k = unwrapKeychainRef(env, keychain);
    OSStatus status;
    char *buffer;
    UInt32 l = 0;
    UInt32 bufferLength = 0;
    
    do {
        bufferLength += 100;
        l = bufferLength * sizeof(char);
        buffer = (char*)malloc(l);
        status = SecKeychainGetPath(k, &l, buffer);
        
        if(status != errSecSuccess && status != errSecBufferTooSmall) {
            throwSecurityError(env, status, "Unable to fetch keychain path.");
            return NULL;
        }
        
        if(status == errSecSuccess) {
            break;
        }
    } while (status == errSecBufferTooSmall);
    
    jstring path = (*env) -> NewStringUTF(env, buffer);
    return path;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainGetStatus
 * Signature: (Lcom/wooga/security/KeychainRef;)I
 */
JNIEXPORT jint JNICALL Java_com_wooga_security_MacOsSecurity_keychainGetStatus
(JNIEnv *env, jclass class, jobject keychain) {
    SecKeychainRef k = NULL;
    if(!(*env)->IsSameObject(env, keychain, NULL)) {
        k = unwrapKeychainRef(env, keychain);
        if((*env)->ExceptionOccurred(env)) {
            return NULL;
        }
    }

    SecKeychainStatus status = 0;
    OSStatus result = SecKeychainGetStatus(k, &status);
    
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Unable to fetch keychain status.");
    }
    
    return status;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainSetSearchList
 * Signature: ([Lcom/wooga/security/KeychainRef;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainSetSearchList___3Lcom_wooga_security_KeychainRef_2
(JNIEnv *env, jclass class, jobjectArray keychains) {
    return Java_com_wooga_security_MacOsSecurity_keychainSetSearchList__I_3Lcom_wooga_security_KeychainRef_2(env, class, -1, keychains);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainSetSearchList
 * Signature: (I[Lcom/wooga/security/KeychainRef;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainSetSearchList__I_3Lcom_wooga_security_KeychainRef_2
(JNIEnv *env, jclass class, jint domain, jobjectArray keychains) {
    jsize length = (*env)->GetArrayLength(env, keychains);

    CFMutableArrayRef keychainSearchList = CFArrayCreateMutable(kCFAllocatorDefault, length, &kCFTypeArrayCallBacks);
    for (int i=0; i<length; i++) {
        SecKeychainRef k = unwrapKeychainRef(env, (*env)->GetObjectArrayElement(env, keychains, i));
        if((*env)->ExceptionOccurred(env)) {
            return false;
        }
        CFArrayInsertValueAtIndex(keychainSearchList, i, k);
    }
    
    OSStatus result;

    if(domain == -1) {
        result = SecKeychainSetSearchList(keychainSearchList);
    } else {
        SecPreferencesDomain d = (SecPreferencesDomain) domain;
        result = SecKeychainSetDomainSearchList(d, keychainSearchList);
    }
    
    CFRelease(keychainSearchList);
  
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to set keychain searchlist.");
        return false;
    }
    return true;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCopySearchList
 * Signature: ()[Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobjectArray JNICALL Java_com_wooga_security_MacOsSecurity_keychainCopySearchList__
(JNIEnv *env, jclass class) {
    return Java_com_wooga_security_MacOsSecurity_keychainCopySearchList__I(env, class, -1);
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainCopySearchList
 * Signature: (I)[Lcom/wooga/security/KeychainRef;
 */
JNIEXPORT jobjectArray JNICALL Java_com_wooga_security_MacOsSecurity_keychainCopySearchList__I
  (JNIEnv *env, jclass class, jint domain) {
    CFArrayRef keychainSearchList;
    OSStatus result;
    
    if(domain == -1) {
        result = SecKeychainCopySearchList(&keychainSearchList);
    } else {
        SecPreferencesDomain d = (SecPreferencesDomain) domain;
        result = SecKeychainCopyDomainSearchList(d, &keychainSearchList);
    }
     
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to copy keychain searchlist.");
        CFRelease(keychainSearchList);
        return NULL;
    }
    
    CFIndex length = CFArrayGetCount(keychainSearchList);

    jobjectArray resultArray;
    resultArray = (*env)->NewObjectArray(env, (jsize)length, JC_KeychainRef, NULL);
    
    int i;
    for (i = 0; i < length; i++) {
        SecKeychainRef k = (SecKeychainRef)CFArrayGetValueAtIndex(keychainSearchList, i);
        CFRetain(k);
        jobject keychain = wrapKeychainRef(env, k);
        (*env)->SetObjectArrayElement(env, resultArray, i, keychain);
    }
    
    CFRelease(keychainSearchList);
    return resultArray;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainGetPreferenceDomain
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_wooga_security_MacOsSecurity_keychainGetPreferenceDomain
(JNIEnv *env, jclass class) {
    SecPreferencesDomain domain;
    OSStatus result = SecKeychainGetPreferenceDomain(&domain);
    
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to get keychain preference domain.");
        return -1;
    }
    
    return (jint)domain;
}

/*
 * Class:     com_wooga_security_MacOsSecurity
 * Method:    keychainSetPreferenceDomain
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_wooga_security_MacOsSecurity_keychainSetPreferenceDomain
(JNIEnv *env, jclass class, jint domain) {
    SecPreferencesDomain d = (SecPreferencesDomain) domain;
    OSStatus result = SecKeychainSetPreferenceDomain(d);
    if(result != errSecSuccess) {
        throwSecurityError(env, result, "Failed to set keychain preference domain.");
        return false;
    }
    return true;
}

JNIEXPORT void JNICALL Java_com_wooga_security_MacOsSecurity_release
(JNIEnv *env, jclass class, jlong keychain) {
    CFRelease((SecKeychainRef)keychain);
    return;
}
