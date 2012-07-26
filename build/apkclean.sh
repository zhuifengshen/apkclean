#!/bin/bash

progdir=`dirname "$0"`
jarfile=apkclean.jar
#exec java -Xmx256M -jar "$progdir/$jarfile" "$@"
exec java -Xmx256M -cp "$progdir/apkclean.jar:$progdir/apktool.jar" com.rodrigo.apkclean.Main "$@"
