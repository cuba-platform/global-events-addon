#!/bin/sh

keytool -genkey -alias signJars     -keystore sign-jars-keystore.jks     -dname "CN=Haulmont"     -keypass HaulmontSignJars     -storepass HaulmontSignJars
