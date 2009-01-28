@ECHO OFF

SET DYNA_JAR="%~dp0jdyna-sockets.jar;%~dp0jdyna-corba.jar;%~dp0player.jar;%CLASSPATH%"
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
GOTO HELP

:SERVER
	java -cp %DYNA_JAR% %SERVER_OPTS% org.jdyna.network.sockets.GameServer %ARGS%
	GOTO EXIT

:BOT
	java -cp %DYNA_JAR% org.jdyna.network.sockets.BotClient %ARGS%
	GOTO EXIT

:HUMAN
	java -cp %DYNA_JAR% org.jdyna.network.sockets.BotClient %ARGS% com.dawidweiss.dyna.players.HumanPlayerFactory
	GOTO EXIT

:ADMIN
	java -cp %DYNA_JAR% org.jdyna.network.sockets.Admin %ARGS%
	GOTO EXIT

:HELP
	ECHO "%0 [server | bot | human | admin] [options]"

:EXIT