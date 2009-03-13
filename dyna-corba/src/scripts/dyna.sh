#!/bin/bash

if [ $# -eq 0 ]; then
	echo "$0 [server|client|game|localgame]"
	exit 1
fi

DYNA_JAR="`dirname $0`/jdyna-corba.jar"

COMMAND=$1
shift

case $COMMAND in
	server)    java -cp $DYNA_JAR com.dawidweiss.dyna.corba.GameServerLauncher "$@";;
	client)    java -cp $DYNA_JAR com.dawidweiss.dyna.corba.GameClientLauncher "$@";;
	game)      java -cp $DYNA_JAR com.dawidweiss.dyna.corba.GameLauncher "$@";;
	localgame) java -cp $DYNA_JAR com.dawidweiss.dyna.launchers.Main "$@";;
esac
