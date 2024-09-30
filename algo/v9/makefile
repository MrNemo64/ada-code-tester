OPTS = -Wall -O3 -std=c++0x

all: met

met: met_bb.cc
	g++ ${OPTS} -o met met_bb.cc

run.%: met
	./met -f tests/$*.p

clean: 
	rm -f *.out *~ met met.sol met.sal

clear: clean