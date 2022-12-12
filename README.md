This small project includes
1. a load balancer (passes requests to workers via RabbitMQ, gets back responses via RabbitMQ
and passes them on to clients),
2. (a) (more or less slow) worker(s) doing the actual work of processing requests,
3. a "bomber" producing many simultaneous requests to see it all in action.

The load balancer and the worker have been coded in Java 11, the bomber in Scala 2.13

For details see respective README.md's in subdirectories.

If yoy are happy enough to use a Linux machine and to have gnome-terminal installed and
RabbitMQ server installed and running, you can compile and run the whole zoo just by
typing

$ ./startall.sh
