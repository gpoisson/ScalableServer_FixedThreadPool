JC = javac
CLASSPATH = ./bin/
JFLAGS = -g -d $(CLASSPATH) -classpath $(CLASSPATH)

.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		./src/cs455/scaling/Node.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) -r $(CLASSPATH)*