JAVA_FILES := $(wildcard src/*.java)
N ?= 3
T ?= 300

package: $(JAR_FILES)
	mvn clean package

move: package
	cp target/ada-code-tester-1.0-SNAPSHOT-jar-with-dependencies.jar test-runner.jar

run.%: move
	cd algo/v$* && make
	java -jar test-runner.jar -exe ./algo/v$*/met -tr $N -te .p -tse .p.sol -tf ./algo/v$*/tests -rt $T -dt 0.5