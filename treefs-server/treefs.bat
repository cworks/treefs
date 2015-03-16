@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem  treefs startup script for Windows
@rem ##########################################################################
set TREEFS_ARGS=%*

:checkJava
set JAVA=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%JAVA%" == "" set JAVA=%JAVA_HOME%\bin\java.exe
goto endJavaCheck

:noJavaHome
if "%JAVA%" == "" set JAVA=java.exe

:endJavaCheck

set MOD_NAME=cworks~treefs~1.0
set MOD_PATH=build\mods\%MOD_NAME%
set JAVA_OPTS=-Xms2048M -Xmx4096M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
set CLASSPATH=%MOD_PATH%;%MOD_PATH%\lib\*

echo **************************************************************************
echo * TreeFs Environment:
echo *
echo * TREEFS_HOME: %TREEFS_HOME%
echo * TREEFS_ARGS: %TREEFS_ARGS%
echo * JAVA_HOME: %JAVA_HOME%
echo * JAVA_OPTS: %JAVA_OPTS%
echo * CLASSPATH: %CLASSPATH%
echo **************************************************************************

"%JAVA%" %JAVA_OPTS% -classpath %CLASSPATH% cworks.treefs.server.TreeFsApp %TREEFS_ARGS%
goto end

:end
set JAVA=
set CLASSPATH=
set JAVA_OPTS=
set TREEFS_ARGS=
