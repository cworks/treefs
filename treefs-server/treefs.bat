@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  treefs startup script for Windows
@rem
@rem ##########################################################################
set TREEFS_HOME=c:\\tmp\treefs-home
set TREEFS_MOUNT=treefs-mount
set TREEFS_DOWNLOADS=downloads
set TREEFS_UPLOADS=uploads
set TREEFS_ARGS=hello

:checkJava
set JAVA=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%JAVA%" == "" set JAVA=%JAVA_HOME%\bin\java.exe
goto endJavaCheck

:noJavaHome
if "%JAVA%" == "" set JAVA=java.exe

:endJavaCheck

set MOD_NAME=treefs-server~treefs-server~SNAPSHOT
set TREEFS_MOD=build\mods\%MOD_NAME%
set JAVA_OPTS=-Xms2048M -Xmx4096M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
set CLASSPATH=%TREEFS_MOD%;%TREEFS_MOD%\lib\*

echo **************************************************************************
echo * TreeFs Environment:
echo *
echo * TREEFS_HOME: %TREEFS_HOME%
echo * TREEFS_ARGS: %TREEFS_ARGS%
echo * JAVA_HOME: %JAVA_HOME%
echo * JAVA_OPTS: %JAVA_OPTS%
echo * CLASSPATH: %CLASSPATH%
echo **************************************************************************

"%JAVA%" %JAVA_OPTS% -classpath %CLASSPATH% cworks.treefs.server.VertxContainer %TREEFS_ARGS%
goto end

:end
set JAVA=
set CLASSPATH=
set JAVA_OPTS=
set TREEFS_ARGS=
