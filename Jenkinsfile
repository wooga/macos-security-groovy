#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'snyk-wooga-frontend-integration-token', variable: 'SNYK_TOKEN')]) {
    buildJavaLibraryOSSRH platforms: ['macos'], testEnv: ["ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC=YES"]
}
