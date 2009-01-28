#!/bin/bash

if [ $# -eq 0 ]; then
	echo "$0 [server | bot | human | admin] [options]"
	exit 1
fi

DYNA_JAR="`dirname $0`/jdyna-sockets.jar:`dirname $0`/jdyna-corba.jar:`dirname $0`/player.jar:$CLASSPATH"
SERVER_OPTS="-Dlog4j.configuration=log4j-server.xml $SERVER_OPTS"

COMMAND=$1
shift

case $COMMAND in
	server) java -cp $DYNA_JAR $SERVER_OPTS org.jdyna.network.sockets.GameServer "$@";;
	bot)    java -cp $DYNA_JAR org.jdyna.network.sockets.BotClient "$@";;
	human)  java -cp $DYNA_JAR org.jdyna.network.sockets.BotClient "$@" com.dawidweiss.dyna.players.HumanPlayerFactory;;
	admin)  java -cp $DYNA_JAR org.jdyna.network.sockets.Admin "$@";;
esac
