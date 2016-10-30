#!/bin/bash

CLASSPATH="."
for jar in `ls ./lib`
do
	CLASSPATH="$CLASSPATH:./lib/$jar"
done

CLASSPATH="./classes:$CLASSPATH"

echo $CLASSPATH

export CLASSPATH

JAVA_OPTS='-Xmx1000m -Xms500m -XX:+PrintGC -XX:+PrintGCTimeStamps -XX:+PrintGCDetails'

nohup java $JAVA_OPTS -Dapp.name=SpiderCenter -Dbase=$PWD com.jiou.Bootstrap >> /dev/null 2>&1 &
