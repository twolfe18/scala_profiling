
CP="$HOME/Dropbox/code/lib/mallet-2.0.7/dist/mallet-deps.jar"
CP="$CP:$HOME/Dropbox/code/lib/mallet-2.0.7/dist/mallet.jar"

#scala -cp $CP
scalac -cp $CP proto.scala

