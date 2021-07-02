#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'macos_security_coveralls_token', variable: 'coveralls_token')]) {
    buildJavaLibraryOSSRH platforms: ['macos'], coverallsToken: coveralls_token, testEnv: ["ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC=YES"]
}
