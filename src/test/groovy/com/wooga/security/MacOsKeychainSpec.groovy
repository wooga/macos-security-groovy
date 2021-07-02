/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wooga.security

import com.wooga.security.command.FindIdentity
import spock.lang.Requires
import spock.lang.Specification

import static com.wooga.security.SecurityHelper.*

@Requires({ os.macOs })
class MacOsKeychainSpec extends Specification {
    def path = new File(File.createTempDir("test", "keychain"), "test.keychain")
    def keychainPassword = "12345"
    def initialSettings = new MacOsKeychainSettings(true, 60000)
    def keychain = MacOsKeychain.create(path, keychainPassword, initialSettings)

    def cleanup() {
        keychain.delete()
    }

    def "convenient method lock locks keychain"() {
        given: "an unlocked keychain"
        keychain.unlock()
        assert keychain.status.isUnlocked()

        when:
        keychain.lock()

        then:
        keychain.status.isLocked()
    }

    def "convenient method unlock unlocks keychain"() {
        given: "a locked keychain"
        keychain.lock()
        assert keychain.status.isLocked()

        when:
        keychain.unlock()

        then:
        keychain.status.isUnlocked()
    }

    def "convenient method getSettings sets keychain settings"() {
        expect:
        keychain.getSettings() == initialSettings
    }

    def "convenient method setSettings sets keychain settings"() {
        given:
        def oldSettings = keychain.getSettings()
        and: "a settings object"
        def settings = new MacOsKeychainSettings(true, 1000)
        assert oldSettings != settings

        when:
        keychain.setSettings(settings)

        then:
        def newSettings = keychain.getSettings()
        newSettings == settings
    }

    def ":getTimout returns value from settings"() {
        given: "a settings object"
        assert initialSettings != settings
        keychain.setSettings(settings)

        when:
        def value = keychain.timeout

        then:
        value == settings.timeout

        where:
        settings = new MacOsKeychainSettings(false, 1000)
    }

    def ":getLockAfterTimeout returns value from settings"() {
        given: "a settings object"
        assert initialSettings != settings
        keychain.setSettings(settings)

        expect:
        keychain.lockAfterTimeout == settings.getLockAfterTimeout()

        where:
        settings = new MacOsKeychainSettings(true, 1000)
    }

    def ":getLockWhenSystemSleeps returns value from settings"() {
        given: "a settings object"
        assert initialSettings != settings
        keychain.setSettings(settings)

        expect:
        keychain.lockWhenSystemSleeps == settings.getLockWhenSystemSleeps()

        where:
        settings = new MacOsKeychainSettings(false, 1000)
    }

    def "convenient method addInternetPassword adds new password item to keychain"() {
        given: "account and server"
        def account = "testAccount"
        def server = "testServer"

        when: "no password item for account and service in keychain"
        keychain.findInternetPassword(account, server)

        then:
        def e = thrown(IOException)
        e.message.contains("The specified item could not be found in the keychain")

        when:
        keychain.addInternetPassword(account, server, "somePassword")
        keychain.findInternetPassword(account, server)

        then:
        noExceptionThrown()
    }

    def "convenient method addGenericPassword adds new password item to keychain"() {
        given: "password item account and service"
        def account = "testAccount"
        def service = "testService"

        when: "no password item for account and service in keychain"
        keychain.findGenericPassword(account, service)

        then:
        def e = thrown(IOException)
        e.message.contains("The specified item could not be found in the keychain")

        when:
        keychain.addGenericPassword(account, service, "somePassword")
        keychain.findGenericPassword(account, service)

        then:
        noExceptionThrown()
    }

    def "convenient method importFile imports items into keychain"() {
        given: "a test certificate"
        def cert = createTestCertificatePkcs12([commonName: certName], passphrase)

        when:
        keychain.importFile(cert, [passphrase: passphrase])

        then:
        noExceptionThrown()
        keychainHasCertificateWithName(keychain.location, "customCert")

        where:
        certName = "customCert"
        passphrase = "testPassword"
    }

    def "convenient method findCertificate finds certificates"() {
        given: "a certificate in the keychain"
        def cert = createTestCertificatePkcs12([commonName: certName], passphrase)
        importPK12(cert, passphrase, keychain.location)
        assert keychainHasCertificateWithName(keychain.location, "customCert")

        when:
        def result = keychain.findCertificate([name: certName])

        then:
        noExceptionThrown()
        result != null
        result.contains(certName)

        where:
        certName = "customCert"
        passphrase = "testPassword"
    }

    def "convenient method findKey finds keys in keychain"() {
        given: "a certificate in the keychain"
        def cert = createTestCertificatePkcs12([privateKeyName: keyName], passphrase)
        importPK12(cert, passphrase, keychain.location)
        assert keychainHasPrivateKey(keychain.location, keyName)

        when:
        def result = keychain.findKey([label: keyName])

        then:
        noExceptionThrown()
        result != null

        where:
        keyName = "customKey"
        passphrase = "testPassword"
    }

    def "convenient method findIdentity finds signing identities"() {
        given: "a certificate in the keychain"
        def cert = createTestCertificatePkcs12([commonName: certName, privateKeyName: keyName], passphrase)
        importPK12(cert, passphrase, keychain.location)

        when:
        def result = keychain.findIdentity(policies: [FindIdentity.Policy.Basic], policySpecifier: certName)

        then:
        noExceptionThrown()
        result != null
        result.contains("\"${certName}\" (CSSMERR_TP_NOT_TRUSTED)")
        result.contains("1 identities found")

        where:
        certName = "customCert"
        keyName = "customKey"
        passphrase = "testPassword"
    }

    def "convenient method findGenericPassword returns password"() {
        given: "a password item in the keychain"
        addGenericPassword(keychain.location, account, service, password)
        assert keychainHasGenericPassword(keychain.location, account, service)

        when:
        def result = keychain.findGenericPassword(account, service)

        then:
        result == password

        where:
        account = "testAccount"
        service = "testService"
        password = "testPassword"
    }

    def "convenient method findInternetPassword returns password"() {
        given: "a password item in the keychain"
        addInternetPassword(keychain.location, account, server, password)
        assert keychainHasInternetPassword(keychain.location, account, server)

        when:
        def result = keychain.findInternetPassword(account, server)

        then:
        result == password

        where:
        account = "testAccount"
        server = "testServer"
        password = "testPassword"
    }
}
