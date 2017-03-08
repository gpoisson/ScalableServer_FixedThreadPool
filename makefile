JC = javac
CLASSPATH = ./bin/
JFLAGS = -g -d $(CLASSPATH) -classpath $(CLASSPATH)

.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		src/cs455/util/HashComputer.java \
		src/cs455/message/HashMessage.java \
		src/cs455/scaling/Node.java \
		src/cs455/scaling/server/tasks/Task.java \
		src/cs455/scaling/server/tasks/AcceptIncomingTrafficTask.java \
		src/cs455/scaling/server/tasks/ComputeHashTask.java \
		src/cs455/scaling/server/tasks/ReplyToClientTask.java \
		src/cs455/util/StatTracker.java \
		src/cs455/scaling/server/WorkerThread.java \
		src/cs455/scaling/server/ThreadPoolManager.java \
		src/cs455/scaling/server/Server.java \
		src/cs455/scaling/client/ClientComms.java \
		src/cs455/scaling/client/Client.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) -r $(CLASSPATH)*