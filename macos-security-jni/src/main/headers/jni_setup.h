#ifndef jni_setup_h
#define jni_setup_h
#include <jni.h>
#include <stdio.h>

extern jclass JC_MacOsKeychainSettings;
extern jclass JC_KeychainRef;
extern jclass JC_DefaultKeychainRef;

extern jmethodID JMID_JC_MacOsKeychainSettings_init;
extern jmethodID JMID_JC_MacOsKeychainSettings_getLockWhenSystemSleeps;
extern jmethodID JMID_JC_MacOsKeychainSettings_getTimeout;
extern jmethodID JMID_JC_KeychainRef_getRef;
extern jmethodID JMID_JC_DefaultKeychainRef_init;

//Exception types

extern jclass JC_NoSuchKeychainException;
extern jclass JC_InvalidKeychainException;
extern jclass JC_InvalidKeychainCredentialsException;
extern jclass JC_DuplicateKeychainException;
extern jclass JC_NoDefaultKeychainException;
extern jclass JC_InvalidPreferenceDomainException;
extern jclass JC_MacOsSecurityException;

extern jclass JC_IllegalArgumentException;
extern jclass JC_NullPointerException;
extern jclass JC_NoClassDefFoundError;

#endif /* jni_setup_h */
