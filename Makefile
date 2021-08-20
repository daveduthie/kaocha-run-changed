##
# Project Title
#
# @file
# @version 0.1

jar:
	clojure -X:build make :to jar

install:
	clojure -X:build make :to install

deploy: jar
	mvn deploy

# end
