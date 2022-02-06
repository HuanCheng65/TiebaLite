#!/usr/bin/env bash

openssl aes-256-cbc -d -in "${releaseKeyStore}.encrypted" -k $RELEASE_ENCRYPT_SECRET_KEY -md md5 >> $releaseKeyStore

printf 'releaseKeyAlias=%s\nreleaseKeyPassword=%s\nreleaseKeyStore=%s\nreleaseStorePassword=%s' $releaseKeyAlias $releaseKeyPassword $releaseKeyStore $releaseStorePassword > keystore.properties

chmod +x ./gradlew