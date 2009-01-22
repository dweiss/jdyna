#!/bin/bash

if [ $# -eq 0 ]; then
	echo "$0 [server|client|game|localgame]"
	exit 1
fi

COMMAND=$1
shift

case $COMMAND in
	server) java -cp `dirname $0`/dyna.jar com.dawidweiss.dyna.corba.GameServerLauncher "$@";;
	client) java -cp `dirname $0`/dyna.jar com.dawidweiss.dyna.corba.GameClientLauncher "$@";;
	game)   java -cp `dirname $0`/dyna.jar com.dawidweiss.dyna.corba.GameLauncher "$@";;
	localgame) java -cp `dirname $0`/dyna.jar com.dawidweiss.dyna.launchers.Main "$@";;
esac
