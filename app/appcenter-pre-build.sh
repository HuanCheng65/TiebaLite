#!/usr/bin/env bash

echo "\norg.gradle.java.home=$JAVA_HOME_11_X64" >> ${APPCENTER_SOURCE_DIRECTORY}/gradle.properties

cat ${APPCENTER_SOURCE_DIRECTORY}/gradle.properties

openssl aes-256-cbc -d -in "${APPCENTER_SOURCE_DIRECTORY}/${releaseKeyStore}.encrypted" -k $RELEASE_ENCRYPT_SECRET_KEY -md md5 >> ${APPCENTER_SOURCE_DIRECTORY}/$releaseKeyStore

printf 'releaseKeyAlias=%s\nreleaseKeyPassword=%s\nreleaseKeyStore=%s\nreleaseStorePassword=%s' $releaseKeyAlias $releaseKeyPassword $releaseKeyStore $releaseStorePassword > ${APPCENTER_SOURCE_DIRECTORY}/keystore.properties

chmod +x "${APPCENTER_SOURCE_DIRECTORY}/gradlew"