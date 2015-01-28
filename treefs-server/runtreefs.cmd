@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  treefs-server startup script for Windows
@rem
@rem ##########################################################################

set TREEFS_HOME=%TEMP%\treefs-home
set TREEFS_MOUNT=treefs-mount
set TREEFS_DOWNLOADS=downloads
set TREEFS_UPLOADS=uploads

set MOD_NAME=treefs-server~treefs-server~SNAPSHOT
set TREEFS_MOD=build\mods\%MOD_NAME%

set JAVA_OPTS=-Xms2048M -Xmx4096M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
@rem VERTX_OPTS="%VERTX_OPTS% -Dorg.vertx.logger-delegate-factory-class-name=org.vertx.java.core.logging.impl.Log4jLogDelegateFactory "
@rem VERTX_OPTS="%VERTX_OPTS% -Dlog4j.configuration=%TREEFS_MOD%\log4j.properties"

echo %VERTX_OPTS%

set CLASSPATH=build\mods\treefs-server~treefs-server~SNAPSHOT;build\mods\treefs-server~treefs-server~SNAPSHOT\lib\*
vertx runmod %MOD_NAME% -conf %TREEFS_MOD%\treefsconfig.json -cp build\mods\treefs-server~treefs-server~SNAPSHOT