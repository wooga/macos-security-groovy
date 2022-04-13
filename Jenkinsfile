#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'snyk-wdk-token', variable: 'SNYK_TOKEN')]) {
    withEnv(['SNYK_ORG_NAME=wooga-pipeline', 'SNYK_AUTO_DOWNLOAD=YES']) {
        buildJavaLibraryOSSRH platforms: ['macos'], testEnv: ["ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC=YES"]
    }
}
