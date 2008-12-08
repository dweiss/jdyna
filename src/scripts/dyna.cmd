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
GOTO HELP

:SERVER
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.corba.GameServer %ARGS%
	GOTO EXIT

:CLIENT
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.corba.GameClient %ARGS%
	GOTO EXIT

:GAME
	java -cp %~dp0dyna.jar com.dawidweiss.dyna.corba.GameLauncher %ARGS%
	GOTO EXIT

:HELP
	ECHO "%0 [server|client|game]"

:EXIT