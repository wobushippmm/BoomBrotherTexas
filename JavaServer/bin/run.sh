#!/bin/sh
cd `dirname $0`

sh ./stop.sh

rm -rf log_*

java -cp "../lib/*:" Server -p 5841 -t sos -n Bill &
java -cp "../lib/*:" Server -p 5842 -t login -n Larry -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379 -wp 5741&
java -cp "../lib/*:" Server -p 5843 -t gateway -n Gale -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379 -wp 5742&
java -cp "../lib/*:" Server -p 5844 -t scene -n Sam -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379&
java -cp "../lib/*:" Server -p 5845 -t battle -n Bard -sa 127.0.0.1 -sp 5841 -up 5941 -ra 127.0.0.1 -rp 6379&
java -cp "../lib/*:" Server -p 5847 -t database -n Diana -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379&
java -cp "../lib/*:" Server -t client -n Carl