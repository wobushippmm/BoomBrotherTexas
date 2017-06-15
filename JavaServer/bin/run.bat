taskkill /F /IM java.exe &
del log_* /f /s /q &
ping -n 1 127.0>nul
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5841 -t sos -n Bill &
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5842 -t login -n Larry -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379 -wp 5741&
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5843 -t gateway -n Gale -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379 -wp 5742&
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5844 -t scene -n Sam -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379&
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5845 -t battle -n Bard -sa 127.0.0.1 -sp 5841 -up 5941 -ra 127.0.0.1 -rp 6379&
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5847 -t database -n Diana -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379 -msa 127.0.0.1 -msp 3306 -msu root -msd texasdb&
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -p 5848 -t world -n Wendy -sa 127.0.0.1 -sp 5841 -ra 127.0.0.1 -rp 6379&
start java -cp "../lib/*;../lib/jetty/*;../lib/jsp/*;" Server -t client -n Carl