package com.wooga.security


import com.wooga.security.error.DuplicateKeychainException
import com.wooga.security.error.InvalidKeychainCredentialsException
import com.wooga.security.error.InvalidKeychainException
import com.wooga.security.error.NoSuchKeychainException
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Stepwise
@Requires({ os.macOs })
class MacOsSecuritySpec extends Specification {

    @AutoCleanup("deleteDir")
    def keychainDir = File.createTempDir("keychain", "test")

    @AutoCleanup("delete")
    def keychainFile = new File(keychainDir, "test.keychain-db")

    def keychainPassword = "somePassword"

    KeychainRef keychain

    def ":keychainCreate creates new keychain"() {
        given: "new keychain location"
        assert !keychainFile.exists()

        when:
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        then:
        keychainFile.exists()
    }

    def ":keychainCreate creates new keychain with initial settings"() {
        given: "new keychain location"
        assert !keychainFile.exists()

        and: "a settings object"
        def settings = new MacOsKeychainSettings(false, 3000)

        when:
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword, settings)

        then:
        keychainFile.exists()
        SecurityHelper.getKeychainSettings(keychainFile) == settings
    }

    def ":keychainCreate throws DuplicateKeychainException when keychain already exists"() {
        given: "an existing keychain"
        SecurityHelper.createKeychain(keychainFile, keychainPassword)
        assert keychainFile.exists()

        when:
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        then:
        def e = thrown(DuplicateKeychainException)
        e.message == "Failed to create keychain. Error: 'A keychain with the same name already exists.'"
    }

    def ":keychainCreate throws IllegalArgumentException when keychainFile is null"() {
        given: "new keychain location"
        assert !keychainFile.exists()

        when:
        keychain = MacOsSecurity.keychainCreate(null, keychainPassword)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter keychain should not be null"
    }

    def ":keychainCreate throws IllegalArgumentException when password is null"() {
        given: "new keychain location"
        assert !keychainFile.exists()

        when:
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter password should not be null"
    }

    def ":keychainCreate accepts optional initial settings parameter"() {
        given: "new keychain location"
        assert !keychainFile.exists()

        and: "a new settings object"
        def settings = new MacOsKeychainSettings(false, 7000)

        when:
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword, settings)

        then:
        noExceptionThrown()
        SecurityHelper.getKeychainSettings(keychainFile.absoluteFile) == settings
    }

    def ":keychainCreate accepts optional initial settings parameter which can be null"() {
        given: "new keychain location"
        assert !keychainFile.exists()

        when:
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword, null)

        then:
        noExceptionThrown()
    }

    def ":keychainOpen returns keychain reference"() {
        given: "new keychain"
        MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        when:
        MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        then:
        noExceptionThrown()
    }

    def ":keychainOpen returns keychain reference for invalid keychain data"() {
        given: "invalid keychain"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes

        when:
        MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        then:
        noExceptionThrown()
    }

    def ":keychainOpen returns keychain reference for non existing keychains"() {
        given: "invalid keychain"
        assert !keychainFile.exists()

        when:
        MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        then:
        noExceptionThrown()
    }

    def ":keychainOpen throws IllegalArgumentException when keychainPath is null"() {
        when:
        MacOsSecurity.keychainOpen(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter keychainPath should not be null"
    }

    def ":keychainGetPath returns canonical path of keychain"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        when:
        def result = MacOsSecurity.keychainGetPath(keychain)

        then:
        noExceptionThrown()
        result == keychainFile.canonicalPath
    }

    def ":keychainGetPath returns canonical path of non existing keychains"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        def result = MacOsSecurity.keychainGetPath(keychain)

        then:
        noExceptionThrown()
        result == keychainFile.canonicalPath
    }

    def ":keychainGetPath returns canonical path for invalid keychain"() {
        given: "new keychain"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        def result = MacOsSecurity.keychainGetPath(keychain)

        then:
        noExceptionThrown()
        result == keychainFile.canonicalPath
    }

    def ":keychainGetPath throws IllegalArgumentException when keychain is null"() {
        when:
        MacOsSecurity.keychainGetPath(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter keychain should not be null"
    }

    def ":keychainGetStatus returns status"() {
        given: "new keychain location"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        when:
        def status = KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain))

        then:
        status.unlocked
        status.readable
        status.writeable

        when:
        SecurityHelper.lockKeychain(keychainFile)
        status = KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain))

        then:
        status.locked
        status.readable
        !status.writeable
    }

    def ":keychainGetStatus throws NoSuchKeychainException when keychain does not exist"() {
        given: "keychain reference"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainGetStatus(keychain)

        then:
        def e = thrown(NoSuchKeychainException)
        e.message == "Unable to fetch keychain status. Error: 'The specified keychain could not be found.'"
    }

    def ":keychainGetStatus throws InvalidKeychainException when keychain is invalid"() {
        given: "an invalid keychain reference"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainGetStatus(keychain)

        then:
        def e = thrown(InvalidKeychainException)
        e.message == "Unable to fetch keychain status. Error: 'The specified keychain is not a valid keychain file.'"
    }

    def ":keychainDelete deletes keychain"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        when:
        MacOsSecurity.keychainDelete(keychain)

        then:
        !keychainFile.exists()
    }

    def ":keychainDelete throws IllegalArgumentException when keychain is null"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        when:
        MacOsSecurity.keychainDelete(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter keychain should not be null"
    }

    def ":keychainLock locks keychain"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)
        assert KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).unlocked

        when:
        def result = KeychainStatus.from(MacOsSecurity.keychainLock(keychain))

        then:
        noExceptionThrown()
        result.locked
    }

    def ":keychainLock throws NoSuchKeychainException when keychain does not exist"() {
        given: "keychain reference"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainLock(keychain)

        then:
        def e = thrown(NoSuchKeychainException)
        e.message == "Unable to lock keychain. Error: 'The specified keychain could not be found.'"
    }

    def ":keychainLock throws InvalidKeychainException when keychain is invalid"() {
        given: "an invalid keychain reference"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainLock(keychain)

        then:
        def e = thrown(InvalidKeychainException)
        e.message == "Unable to lock keychain. Error: 'The specified keychain is not a valid keychain file.'"
    }

    def ":keychainUnlock unlocks keychain"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)
        MacOsSecurity.keychainLock(keychain)
        assert KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).locked

        when:
        def result = KeychainStatus.from(MacOsSecurity.keychainUnlock(keychain, keychainPassword))

        then:
        noExceptionThrown()
        result.unlocked
    }

    def ":keychainUnLock throws InvalidKeychainCredentialsException when credentials are incorrect"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)
        MacOsSecurity.keychainLock(keychain)
        assert KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).locked

        when:
        def result = KeychainStatus.from(MacOsSecurity.keychainUnlock(keychain, "some password"))

        then:
        def e = thrown(InvalidKeychainCredentialsException)
        e.message == "Unable to unlock keychain. Error: 'The user name or passphrase you entered is not correct.'"
    }

    def ":keychainUnlock throws NoSuchKeychainException when keychain does not exist"() {
        given: "keychain reference"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainUnlock(keychain, keychainPassword)

        then:
        def e = thrown(NoSuchKeychainException)
        e.message == "Unable to unlock keychain. Error: 'The specified keychain could not be found.'"
    }

    def ":keychainUnlock throws InvalidKeychainException when keychain is invalid"() {
        given: "an invalid keychain reference"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainUnlock(keychain, keychainPassword)

        then:
        def e = thrown(InvalidKeychainException)
        e.message == "Unable to unlock keychain. Error: 'The specified keychain is not a valid keychain file.'"
    }

    def ":keychainCopySettings returns settings object"() {
        given: "a keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)
        MacOsSecurity.keychainUnlock(keychain, keychainPassword)
        SecurityHelper.setKeychainSettings(keychainFile, true, 4000)

        when:
        def settings = MacOsSecurity.keychainCopySettings(keychain)

        then:
        noExceptionThrown()
        settings.lockAfterTimeout
        settings.lockWhenSystemSleeps
        settings.timeout == 4000
    }

    def ":keychainCopySettings throws NoSuchKeychainException when keychain does not exist"() {
        given: "keychain reference"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainCopySettings(keychain)

        then:
        def e = thrown(NoSuchKeychainException)
        e.message == "Failed to copy keychain settings. Error: 'The specified keychain could not be found.'"
    }

    def ":keychainCopySettings throws InvalidKeychainException when keychain is invalid"() {
        given: "an invalid keychain reference"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        when:
        MacOsSecurity.keychainCopySettings(keychain)

        then:
        def e = thrown(InvalidKeychainException)
        e.message == "Failed to copy keychain settings. Error: 'The specified keychain is not a valid keychain file.'"
    }

    def ":keychainCopySettings throws IllegalArgumentException when keychainFile is null"() {
        when:
        MacOsSecurity.keychainCopySettings(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter keychain should not be null"
    }

    def ":keychainSetSettings writes keychain settings object"() {
        given: "a keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "a settings object"
        def settings = new MacOsKeychainSettings(true, 400)

        when:
        def result = MacOsSecurity.keychainSetSettings(keychain, settings)

        then:
        noExceptionThrown()
        result
        def newSettings = MacOsSecurity.keychainCopySettings(keychain)
        newSettings == settings
    }

    def ":keychainSetSettings throws NoSuchKeychainException when keychain does not exist"() {
        given: "keychain reference"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        and: "a settings object"
        def settings = new MacOsKeychainSettings(true, 400)

        when:
        MacOsSecurity.keychainSetSettings(keychain, settings)

        then:
        def e = thrown(NoSuchKeychainException)
        e.message == "Failed to set keychain settings. Error: 'The specified keychain could not be found.'"
    }

    def ":keychainSetSettings throws InvalidKeychainException when keychain is invalid"() {
        given: "an invalid keychain reference"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        and: "a settings object"
        def settings = new MacOsKeychainSettings(true, 400)

        when:
        MacOsSecurity.keychainSetSettings(keychain, settings)

        then:
        def e = thrown(InvalidKeychainException)
        e.message == "Failed to set keychain settings. Error: 'The specified keychain is not a valid keychain file.'"
    }

    def ":keychainCopyDefault returns reference to default keychain"() {
        given: "path to default keychain"
        def defaultKeychain = SecurityHelper.defaultKeychain

        expect:
        def keychain = MacOsSecurity.keychainCopyDefault()
        MacOsSecurity.keychainGetPath(keychain) == defaultKeychain.canonicalPath
    }

    def ":keychainCopyDefault with preference domain returns reference to domain default keychain"() {
        given: "path to default keychain"
        def defaultKeychain = SecurityHelper.defaultKeychain

        expect:
        def keychain = MacOsSecurity.keychainCopyDefault(Domain.user.ordinal())
        MacOsSecurity.keychainGetPath(keychain) == defaultKeychain.canonicalPath
    }

    def ":keychainSetDefault sets default keychain"() {
        given: "a keychain"
        def keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "the current default keychain"
        def defaultKeychain = SecurityHelper.defaultKeychain

        when:
        def result = MacOsSecurity.keychainSetDefault(keychain)

        then:
        SecurityHelper.defaultKeychain.canonicalPath == keychainFile.canonicalPath

        cleanup:
        SecurityHelper.setDefaultKeychain(defaultKeychain)
    }

    def ":keychainSetDefault sets default keychain with preference domain"() {
        given: "a keychain"
        def keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "the current default keychain"
        def defaultKeychain = SecurityHelper.defaultKeychain

        when:
        def result = MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), keychain)

        then:
        SecurityHelper.defaultKeychain.canonicalPath == keychainFile.canonicalPath

        cleanup:
        SecurityHelper.setDefaultKeychain(defaultKeychain)
    }

    @Ignore
    def ":keychainSetDefault throws NoSuchKeychainException when keychain does not exist"() {
        given: "keychain reference"
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        and: "the current default keychain"
        def defaultKeychain = SecurityHelper.defaultKeychain

        when:
        MacOsSecurity.keychainSetDefault(keychain)

        then:
        def e = thrown(NoSuchKeychainException)
        e.message == "Failed to set keychain settings. Error: 'The specified keychain could not be found.'"

        cleanup:
        SecurityHelper.setDefaultKeychain(defaultKeychain)
    }

    @Ignore
    def ":keychainSetDefault throws InvalidKeychainException when keychain is invalid"() {
        given: "an invalid keychain reference"
        keychainFile.bytes = "hgfsfsfjasdfjdslfs".bytes
        keychain = MacOsSecurity.keychainOpen(keychainFile.absolutePath)

        and: "the current default keychain"
        def defaultKeychain = SecurityHelper.defaultKeychain

        when:
        MacOsSecurity.keychainSetDefault(keychain)

        then:
        def e = thrown(NoSuchKeychainException)

        e.message == "Failed to set keychain settings. Error: 'The specified keychain could not be found.'"

        cleanup:
        SecurityHelper.setDefaultKeychain(defaultKeychain)
    }

    def ":keychainSetDefault throws IllegalArgumentException when keychainPath is null"() {
        when:
        MacOsSecurity.keychainSetDefault(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Parameter keychain should not be null"
    }

    @Ignore
    def ":keychainLock locks locks default keychain when keychain param is null"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)
        assert KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).unlocked

        and: "a reference to the user default keychain"
        def defaultKeychain = MacOsSecurity.keychainCopyDefault(Domain.user.ordinal())
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), keychain)

        when:
        def result = KeychainStatus.from(MacOsSecurity.keychainLock(null))

        then:
        noExceptionThrown()
        result.locked
        KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).locked

        cleanup:
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), defaultKeychain)
    }

    @Ignore("test involving default keychain can be flacky and dangerous")
    def ":keychainUnlock unlocks default keychain when keychain param is null"() {
        given: "new keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)
        MacOsSecurity.keychainLock(keychain)
        assert KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).locked

        and: "a reference to the user default keychain"
        def defaultKeychain = MacOsSecurity.keychainCopyDefault(Domain.user.ordinal())
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), keychain)

        when:
        def result = KeychainStatus.from(MacOsSecurity.keychainUnlock(null, keychainPassword))

        then:
        noExceptionThrown()
        result.unlocked
        KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain)).unlocked

        cleanup:
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), defaultKeychain)
    }

    @Ignore("test involving default keychain can be flacky and dangerous")
    def ":keychainSetSettings writes keychain settings object to default keychain when keychain param is null"() {
        given: "a keychain"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "a settings object"
        def settings = new MacOsKeychainSettings(true, 400)

        and: "a reference to the user default keychain"
        def defaultKeychain = MacOsSecurity.keychainCopyDefault(Domain.user.ordinal())
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), keychain)

        when:
        def result = MacOsSecurity.keychainSetSettings(null, settings)

        then:
        noExceptionThrown()
        result
        def newSettings = MacOsSecurity.keychainCopySettings(keychain)
        newSettings == settings

        cleanup:
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), defaultKeychain)
    }

    @Ignore("test involving default keychain can be flacky and dangerous")
    def ":keychainGetStatus returns status of default keychain when keychain param is null"() {
        given: "new keychain location"
        keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "a reference to the user default keychain"
        def defaultKeychain = MacOsSecurity.keychainCopyDefault(Domain.user.ordinal())
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), keychain)

        when:
        def status = KeychainStatus.from(MacOsSecurity.keychainGetStatus(null))

        then:
        status.unlocked
        status.readable
        status.writeable

        when:
        SecurityHelper.lockKeychain(keychainFile)
        status = KeychainStatus.from(MacOsSecurity.keychainGetStatus(null))

        then:
        status.locked
        status.readable
        !status.writeable

        cleanup:
        MacOsSecurity.keychainSetDefault(Domain.user.ordinal(), defaultKeychain)
    }

    def ":keychainCopySearchList returns search list"() {
        expect:
        def listA = SecurityHelper.getKeychainSearchList().collect { it.canonicalPath }
        def rawListB = MacOsSecurity.keychainCopySearchList().toList()
        def listB = rawListB.collect { MacOsSecurity.keychainGetPath(it) }
        listA == listB
    }

    @Unroll
    def ":keychainCopySearchList returns security domain specific search list with #domain"() {
        expect:
        def listA = SecurityHelper.getKeychainSearchList(domain).collect { it.canonicalPath }
        def rawListB = MacOsSecurity.keychainCopySearchList(domain.ordinal()).toList()
        def listB = rawListB.collect { MacOsSecurity.keychainGetPath(it) }
        listA == listB

        where:
        domain << [Domain.user, Domain.system, Domain.common]
    }

    def ":keychainSetSearchList sets new search list"() {
        given: "a new keychain"
        def keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "the current list of keychains"
        def currentList = MacOsSecurity.keychainCopySearchList()

        and: "a new list with new keychain appended"
        KeychainRef[] newList = (currentList.toList() << keychain).toArray(new KeychainRef[0])

        when:
        MacOsSecurity.keychainSetSearchList(newList)

        then:
        MacOsSecurity.keychainCopySearchList().toList().containsAll(newList)

        cleanup:
        MacOsSecurity.keychainSetSearchList(currentList)
    }

    @Unroll
    def ":keychainSetSearchList sets new domain specific search list with #domain"() {
        given: "a new keychain"
        def keychain = MacOsSecurity.keychainCreate(keychainFile.absolutePath, keychainPassword)

        and: "the current list of keychains"
        def currentList = MacOsSecurity.keychainCopySearchList(domain.ordinal())

        and: "a new list with new keychain appended"
        KeychainRef[] newList = (currentList.toList() << keychain).toArray(new KeychainRef[0])

        when:
        MacOsSecurity.keychainSetSearchList(domain.ordinal(), newList)

        then:
        MacOsSecurity.keychainCopySearchList(domain.ordinal()).toList().containsAll(newList)

        cleanup:
        MacOsSecurity.keychainSetSearchList(domain.ordinal(), currentList)

        where:
        //other domains need admin privileges
        domain << [Domain.user]
    }


    def ":keychainGetPreferenceDomain returns current Preference domain"() {
        when:
        def domain = MacOsSecurity.keychainGetPreferenceDomain();

        then:
        noExceptionThrown()
        Domain.values()[domain] == Domain.user
    }

    @Unroll
    def ":keychainSetPreferenceDomain sets current Preference domain with #domain"() {
        given: "current domain"
        def currentDomain = MacOsSecurity.keychainGetPreferenceDomain()

        when:
        MacOsSecurity.keychainSetPreferenceDomain(domain.ordinal())
        def newDomain = MacOsSecurity.keychainGetPreferenceDomain()

        then:
        noExceptionThrown()
        domain == Domain.values()[newDomain]

        cleanup:
        MacOsSecurity.keychainSetPreferenceDomain(currentDomain)

        where:
        domain << [Domain.user, Domain.system, Domain.common]
    }
}
