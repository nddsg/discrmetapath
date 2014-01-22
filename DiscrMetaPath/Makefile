wikibuild:
	mvn clean compile assembly:single

wikitest:
	mvn clean test

wikirun:
	rm -f log.log
	java -Xmx25G -XX:+UseConcMarkSweepGC -jar target/DiscrMetaPath-0.1-jar-with-dependencies.jar -NoSQL >> log.log 2>&1
