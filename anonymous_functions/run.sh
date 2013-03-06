
CP="$HOME/Dropbox/code/lib/mallet-2.0.7/dist/mallet-deps.jar"
CP="$CP:$HOME/Dropbox/code/lib/mallet-2.0.7/dist/mallet.jar"

MEM="128M"
JAVA_OPTS="-Xmx$MEM -agentlib:hprof=cpu=samples,depth=20" scala -cp $CP App $@

