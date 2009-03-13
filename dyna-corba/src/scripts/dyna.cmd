@ECHO OFF

SET DYNA_JAR="%~dp0jdyna-corba.jar"

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
	java -cp %DYNA_JAR% com.dawidweiss.dyna.corba.GameServerLauncher %ARGS%
	GOTO EXIT

:CLIENT
	java -cp %DYNA_JAR% com.dawidweiss.dyna.corba.GameClientLauncher %ARGS%
	GOTO EXIT

:GAME
	java -cp %DYNA_JAR% com.dawidweiss.dyna.corba.GameLauncher %ARGS%
	GOTO EXIT

:LOCALGAME
	java -cp %DYNA_JAR% com.dawidweiss.dyna.launchers.Main %ARGS%
	GOTO EXIT

:HELP
	ECHO "%0 [server|client|game|localgame]"

:EXIT