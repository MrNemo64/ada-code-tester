OPTS = -Wall -O3 -std=c++0x

all: met

met: met_bt.cc
	g++ ${OPTS} -o met met_bt.cc

run.%: met
	./met -f tests/$*.p

clean: 
	rm -f *.out *~ met met.sol met.sal

clear: clean