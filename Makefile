CC=javac

all: *.java
	$(CC) $^ && make doc

doc: *.java 
	cd doc/ && javadoc -author ../*.java

clean: 
	rm -f *.class 

clobber:
	make clean && rm -f *~ && rm -rf doc/*
