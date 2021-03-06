#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



THIS_SCRIPT="$0"
SCRIPT_DIR="${THIS_SCRIPT%/*}"
SCRIPT_DIR=`cd $SCRIPT_DIR ; pwd`
echo $SCRIPT_DIR

ACCUMULO_HOME=${ACCUMULO_HOME}
ZOOKEEPER_HOME=${ZOOKEEPER_HOME}

#
# Check ZOOKEEPER_HOME
#
if [[ -z $ZOOKEEPER_HOME ]]; then
	echo "You must set ZOOKEEPER_HOME environment variable"
	exit -1;
else
	for f in $ZOOKEEPER_HOME/zookeeper-*.jar; do
		CLASSPATH=$f
		break
	done	
fi

#
# Check ACCUMULO_HOME
#
if [[ -z $ACCUMULO_HOME ]]; then
	echo "You must set ACCUMULO_HOME environment variable"
	exit -1;
else
	for f in $ACCUMULO_HOME/lib/*.jar; do
		CLASSPATH=${CLASSPATH}:$f
	done	
fi

#
# Add our jars
#
for f in $SCRIPT_DIR/../lib/*.jar; do
	CLASSPATH=${CLASSPATH}:$f  
done

#
# Transform the classpath into a comma-separated list also
#
LIBJARS=`echo $CLASSPATH | sed 's/:/,/g'`


#
# Map/Reduce job
#
JAR=$SCRIPT_DIR/../lib/accumulo-sample-ingest-1.5.0-incubating-SNAPSHOT.jar
CONF=$SCRIPT_DIR/../conf/wikipedia.xml
HDFS_DATA_DIR=$1
export HADOOP_CLASSPATH=$CLASSPATH
echo "hadoop jar $JAR ingest.WikipediaIngester -libjars $LIBJARS -conf $CONF -Dwikipedia.input=${HDFS_DATA_DIR}"
hadoop jar $JAR ingest.WikipediaIngester -libjars $LIBJARS -conf $CONF -Dwikipedia.input=${HDFS_DATA_DIR}
