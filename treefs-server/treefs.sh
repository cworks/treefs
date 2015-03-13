#!/bin/bash
TREEFS_ARGS=$@

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

MOD_NAME=cworks~treefs~1.0
MOD_PATH=build/mods/$MOD_NAME

JAVA_OPTS="-Xms2048M -Xmx4096M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 "

export CLASSPATH="$MOD_PATH:$MOD_PATH/lib/*"

# Display our environment
echo "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>"
echo ""
echo "  TreeFs Environment"
echo ""
echo "  TREEFS_HOME: $TREEFS_HOME"
echo ""
echo "  TREEFS_ARGS: $TREEFS_ARGS"
echo ""
echo "  MOD_PATH: $MOD_PATH"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "  CLASSPATH: $CLASSPATH"
echo "<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>"
echo ""

while true; do
   if [ "x$LAUNCH_IN_BACKGROUND" = "x" ]; then
      eval $JAVA $JAVA_OPTS -cp $CLASSPATH cworks.treefs.server.TreeFsApp $TREEFS_ARGS
      # Execute the JVM in the foreground
      TREEFS_STATUS=$?
   fi
   if [ "$TREEFS_STATUS" -eq 10 ]; then
      echo "Restarting cworks.treefs.server.TreeFsApp..."
   else
      exit $TREEFS_STATUS
   fi
done
