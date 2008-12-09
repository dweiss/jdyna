@ECHO OFF

SET COMMAND=%1
SET ARGS=
:COLLECT
	SHIFT
	IF "%1" == "" GOTO COLLECTED
	SET ARGS=%ARGS% "%1"
	GOTO COLLECT
:COLLECTED

IF "%COMMAND%" == "server" GOTO SERVER
IF "%COMMAND%" == "client" GOTO CLIENT
IF "%COMMAND%" == "game" GOTO GAME
IF "%COMMAND%" == "localgame" GOTO LOCALGAME
GOTO HELP

:SERVER
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.corba.server.GameServer %ARGS%
	GOTO EXIT

:CLIENT
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.corba.client.GameClient %ARGS%
	GOTO EXIT

:GAME
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.corba.client.GameLauncher %ARGS%
	GOTO EXIT

:LOCALGAME
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.Main %ARGS%
	GOTO EXIT

:HELP
	ECHO "%0 [server|client|game|localgame]"

:EXIT