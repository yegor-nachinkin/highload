$ sbt assembly

creates a fat jar Bomber.jar in target/scala-2.13
Run it with

$ java -jar Bomber.jar 5 100 10 http://localhost:9000/reverse/abc

The first argument is the number of threads to use, the second determines the number
of requests each thread will make, the third argument specifies the number of milliseconds to pause
between requests (zero meaning no pause at all). The fourth argument... well, you have guessed it :-)

It is, btw, the default address at which the balancer is listening for requests. The worker will reply
with the reversed string "cba" (it will reverse anything you put after reverse/ ).

As of now the bomber has no defence against any failures (connectivity, no RabbitMQ server running etc)
and just crashes disgracefully. Perhaps I shall improve that in the future.
