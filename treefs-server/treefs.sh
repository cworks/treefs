#!/bin/bash
export TREEFS_HOME=/tmp/treefs-home
export TREEFS_MOUNT=treefs-mount
export TREEFS_DOWNLOADS=downloads
export TREEFS_UPLOADS=uploads

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

MOD_NAME=treefs-server\~treefs-server\~SNAPSHOT
TREEFS_MOD=build/mods/$MOD_NAME

VERTX_OPTS="-Xms2048M -Xmx4096M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 "
export VERTX_OPTS

export CLASSPATH=build/mods/treefs-server\~treefs-server\~SNAPSHOT:build/mods/treefs-server\~treefs-server\~SNAPSHOT/lib/*

# Display our environment
echo "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>"
echo ""
echo "  TreeFs Environment"
echo ""
echo "  TREEFS_HOME: $TREEFS_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  VERTX_OPTS: $VERTX_OPTS"
echo ""
echo "  CLASSPATH: $CLASSPATH"
echo "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>"
echo ""

while true; do
   if [ "x$LAUNCH_IN_BACKGROUND" = "x" ]; then
      eval $JAVA -cp $CLASSPATH net.cworks.treefs.server.VertxContainer
      # Execute the JVM in the foreground
      TREEFS_STATUS=$?
   fi
   if [ "$TREEFS_STATUS" -eq 10 ]; then
      echo "Restarting TreeFs..."
   else
      exit $TREEFS_STATUS
   fi
done
