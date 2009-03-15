@ECHO OFF

SET DYNA_JAR="%~dp0jdyna-sockets.jar;%~dp0player.jar;%CLASSPATH%"
SET SERVER_OPTS="-Dlog4j.configuration=log4j-server.xml"

SET COMMAND=%1
SET ARGS=
:COLLECT
	SHIFT
	IF "%1" == "" GOTO COLLECTED
	SET ARGS=%ARGS% "%1"
	GOTO COLLECT
:COLLECTED

IF "%COMMAND%" == "server" GOTO SERVER
IF "%COMMAND%" == "bot" GOTO BOT
IF "%COMMAND%" == "human" GOTO HUMAN
IF "%COMMAND%" == "admin" GOTO ADMIN
IF "%COMMAND%" == "replay" GOTO REPLAY
GOTO HELP

:SERVER
	java -cp %DYNA_JAR% %SERVER_OPTS% org.jdyna.network.sockets.GameServer %ARGS%
	GOTO EXIT

:BOT
	java -cp %DYNA_JAR% org.jdyna.network.sockets.BotClient %ARGS%
	GOTO EXIT

:HUMAN
	java -cp %DYNA_JAR% org.jdyna.network.sockets.BotClient %ARGS% org.jdyna.players.HumanPlayerFactory
	GOTO EXIT

:ADMIN
	java -cp %DYNA_JAR% org.jdyna.network.sockets.Admin %ARGS%
	GOTO EXIT

:REPLAY
	java -cp %DYNA_JAR% org.jdyna.launchers.ReplaySavedGame %ARGS%
	GOTO EXIT

:HELP
	ECHO "%0 [server | bot | human | admin | replay] [options]"

:EXIT