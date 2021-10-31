# CS6650 Fall 2021 Assignment 2

## <a name="github">**GitHub**</a>:

**https://github.com/timegao/cs6650-fa21/tree/main/assign02**

&nbsp;

## **Submission Requirements**:

> [The URL for your git repo](#github).

> Create a [new folder for your Assignment 2 server code](https://github.com/timegao/cs6650-fa21/tree/main/assign02/src/main/java/producer).

> A 1-2 page description of your [server design](#design). Include [major classes](#classes), [packages](#dependencies), [relationships](#uml), how messages get sent/received, etc.

> [Test runs](#results) ([command lines](#commands), [RMQ management windows showing queue size, send/receive rates for 64, 128 and 256 client threads](#results))

> [Bonus Points](#bonus) - Results for a test with 512 client threads

&nbsp;

## **Introduction**:

Assignment description: https://gortonator.github.io/bsds-6650/assignments-2021/Assignment-2

This assignment builds on assignment 1. Your client won’t change (unless it has a bug in it!). Your will implement processing logic in your server and post the result to a queue for subsequent processing.

&nbsp;

## <a name="design">**Design**</a>:

### Step 1 - Implement the Server

In this assignment, you will implement the doPost() method in your servlet to:

> fully validate the URL and JSON payload

I tweaked the server built from [lab-2](https://github.com/timegao/cs6650-fa21/tree/main/lab02). The changes were to use gson instead of org.json for processing JSON objects and switching them back and forth from JSON objects to Java objects. The advantage of using gson over org.json was that I could parse nested JSON objects without anotations.

> if valid, format the incoming data and send it as a payload to queue
> Choose your own queue technology. RabbitMQ is an obvious one, AWS SQS another. Make sure you deploy RabbitMQ on its own EC2 instance. You can find various installation instructions here.

I added the `GenericObjectPool` class to hold the `ChannelPooledFactory`, a wrapper for storing `Channel` objects, which can then the connection to rabbitmq.

> Your aim is to keep response times as low as possible. One free tier server will probably get pretty busy, so you will want to introduce load balancing.

> You can set up AWS Elastic Load Balancing using either Application or Network load balancers. Enable load balancing with e.g. 4 free tier EC2 instances and see what effect this has on your performance.

Unfortunately, due to the new limitations with AWS Free Tier, it seems that only 5 EC2 instances can be open at any time. Whenever I start a 6th EC2 instance, it automatically shuts down when completing its checks and then terminates. Because of this limitation, I was only able to setup 3 servers instead of 4. My 5 EC2 instances are: 3 EC2 servers/providers, 1 rabbitmq server, and 1 plain old Java program consumer, with client running locally so that I can have an additional server.

### Step 2 - Implement a Consumer

> Implement a plain old Java program to pull messages off the queue. This program simply receives messages from the queue and keeps a record of the individual lift rides for each skier in a hash map.

> Your aim is to consume messages, ideally, as quickly as they are produced. This means your consumer will need to be multithreaded and your hash map thread safe.

I have the `SkierConsumer` initialize number of threads equal to the argument passed in. The `ConcurrentHashMap` used to store the messages are prepopulated based on the number of SkierID's so that the `ConcurrentHashMap` does not require an additional check for existence, which may not be threadsafe and would waste additional compute resources. The task to initialize the connection is in a separate `RunnableChannelTask` class.

### Step 3 - Load Testing

Your aim here is to find the ‘best’ application configuration in terms of responsiveness to the client and managing queue size.

The questions you need to explore are:

> Do I need to scale out with load balancing for my server? Or can my system work with 1 free-tier server

See [results](#results) [with](#with) and [without](#without) load balancing.

In my limited testing, load balancing was not helpful except for the highest client thread count at 512.

> How many queue consumers threads do I need to keep the queue size as close to zero as possible.

In my limited testing, any number of threads between 64 and 256 was about the same. I chose to go with 32 which sounded about right given how limited a free tier t2-micro instace is.

### <a name="classes">**Classes**</a>

#### client

I needed to make a change in the `CommandLineInput` class since there the previous assignment specified that there was a maximum of 256 threads, whereas 512 threads was required for the bonus section.

I also needed to make a change in the `PostRequestTask` class since there was a bug in my client/server where I had set a default DAY_ID of 789, but the server only accepted DAY_ID that was a maximum of 420.

#### server/producer

`SkierServlet` - initializes `ChannelPooledFactory` to make connection to rabbitmq server.

`ChannelPooledFactory` - makes connection to rabbitmq server and creates channel.

`AbstractMessage` - parent class of other messages, which store a message with response code for easy JSON output.

`InvalidGetMessage` - invalid GET message for 400 code.

`MissingGetMessage` - missing GET message for 404 code.

`SuccessGetMessage` - successful GET message for 200 code.

`InvalidPostMessage` -invalid POST message for 400 code.

`MissingPostMessage` - missing POST message for 404 code.

#### consumer

`SkierConsumer` - Creates the connection, prepopulates the `ConcurrentHashMap` with empty `ConcurrentLinkedQueue`, and calls for a number of threads equal to the sepcified argument.

`RunnableChannelTask` - Runs a task to create a channel to receive messages and send acknowledgements. Adds `Skier` to the `ConcurrentHashMap`.

`LiftRide` - stores the LiftRide information.

`Skier` - stores the id and `LiftRide` information.

`ConsumerCommandLineInput` - exception handles the information generated from `ConsumerCommandLineOptions` and stores it.

`ConsumerCommandLineOptions` - specifies the `Options` and `Option` to be parsed from commandline.

### <a name="dependencies">**Dependencies**</a>

gson - used to serialize and deserialize JSON and Java objects.

commons-pool2 - used for pool classes.

commons.cli - used to for Apache options.

amqp-client - rabbitmq server.

slf4j-log4j12 - used to log outputs.

log4j - a dependency for org.slf4j.

slf4j-api - dependency for org.sl4j.

maven-assembly-plugin - used to generate the jar files.

javax.servlet-api - used to create the Servlet.

lombok - used to simplify classes with annotations.

swagger-java-client - used for Swagger Client SDK.

### <a name="commands">**Commands**</a>

I used Apache Options from the [Commons CLI library](https://commons.apache.org/proper/commons-cli/) to write the client according to the specifications.

```
usage: options

-i, --ip <arg> ip address of server

-l, --lifts <arg> number of ski lifts, range 5-60, default 40

-m, --mean <arg> mean number ski lifts per ski rider per day, default 10, max 20

-s, --skiers <arg> number of skier IDs, max 100,000

-t, --threads <arg> number of threads, max 2
```

I also used Apache Options from the [Commons CLI library](https://commons.apache.org/proper/commons-cli/) to write the consumer according to the specifications.

```
usage: options

 -h,--host <arg>       host name of connection, default localhost

 -p,--password <arg>   password of the connection, default 'guest'

 -q,--queue <arg>      queue name of channel, default 'test'

 -s,--skiers <arg>     number of skier IDs, default 20,000, max 100,000

 -t,--threads <arg>    number of threads

 -u,--username <arg>   username of the connection, default 'guest'
```

&nbsp;

## <a name="results">**Results**</a>

### <a name="bonus">**Bonus**</a>

I used the 512 client threads to test the number of consumer threads I should use. The logic was that since 512 client threads would have the highest throughput, then it would be the best way stress test. All tests were performed with an application load balancer. The throughput for 512 client threads maxes out at 6,000 requests per second which is quite a lot.

From my limited testing, it seems like the performance was about equal between 32, 64, 128, and 256 threads. The negligible difference in performance could be chalked to the differences in throughput, network connection, startup time, etc.

### 16 Threads

16 threads was not enough. The queue ran up over 20,000 messages.

![16 threads test 1](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/16/512-16(1).png?token=AMABNPR46XSOZX55H5KZO63BQ5WWK>)

### 32 Threads

32 threads performed the best. The queue only ran up to about 25 messages at most.

![32 threads test 1](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/32/512-32(1).png?token=AMABNPQZC2L2V22F5DIGBSLBQ5W5W>)

![32 threads test 2](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/32/512-32(2).png?token=AMABNPWHI2LWL2U3VYS7YKDBQ5W6E>)

![32 threads test 3](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/32/512-32(3).png?token=AMABNPXLNCRWVJPTRSGA3E3BQ5W7G>)

### 64 Threads

64 threads performed about the same. The queue only ran up to about 25 messages at most.

![64 threads test 1](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/64/512-64(1).png?token=AMABNPTZ2MKWOL34ODCXP4LBQ5XEQ>)

![64 threads test 2](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/64/512-64(2).png?token=AMABNPWOTZJESQM6WOW4OADBQ5XHS>)

![64 threads test 3](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/64/512-64(3).png?token=AMABNPTF634YSVOJJ7T4UCLBQ5XIA>)

### 128 Threads

128 threads performed about the same. It did cross the 30 messages line one time.

![128 threads test 1](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/128/512-128(1).png?token=AMABNPVS7CZ4P2EDSBJW3VDBQ5XKC>)

![128 threads test 2](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/128/512-128(2).png?token=AMABNPSSZUYTAY5PMTCF6JDBQ5XKQ>)

![128 threads test 3](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/128/512-128(3).png?token=AMABNPXBISRFS5ZHFSZO3RLBQ5XK4>)

### 256 Threads

256 threads performed about the same. The queue only ran up to about 26 messages at most.

![256 threads test 1](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/256/512-256(1).png?token=AMABNPURSL6RPVSIXZP5KXLBQ5XNM>)

![256 threads test 2](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/256/512-256(2).png?token=AMABNPWYOHIRXS3VUWPZLMLBQ5XOC>)

![256 threads test 3](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/256/512-256(3).png?token=AMABNPTRI67OOX5JVGPCWB3BQ5XOW>)

### 512 Threads

512 threads started performing worse, the results were more sporadic, and the queue would have over 50 messages.

![512 threads test 1](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/512/512-512(1).png?token=AMABNPRTDHX4NOKWXW3B47LBQ5X5I>)

![512 threads test 2](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/512/512-512(2).png?token=AMABNPUUF74BYJHE7JOOXPLBQ5X56>)

![512 threads test 3](<https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/bonus/512/512-512(3).png?token=AMABNPS7Z44BSVLRRITHPQLBQ5X6K>)

### <a name="without">**_Without Load Balancing_**</a>

The results for lower client threads was actually better without a load balancing, perhaps due to the added latency.

#### 64 Client Threads/32 Consumer Threads

The most queue messages was about 6, which is quite good.

![64 threads no elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/no/64-32-no.png?token=AMABNPTR5PBUBFNIBCC57OTBQ5YYO)

#### 128 Client Threads/32 Consumer Threads

The most queue messages was about 7, which is quite good.

![128 threads no elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/no/128-32-no.png?token=AMABNPXCZSJZF2UIVVVSESTBQ5Y34)

#### 256 Client Threads/32 Consumer Threads

![256 threads no elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/no/256-32-no.png?token=AMABNPWVG7RPZ5NSVZRGXBDBQ5Y4S)

#### 512 Client Threads/32 Consumer Threads

![512 threads no elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/no/512-32-no.png?token=AMABNPT7Q6BI57LDK2AK5GDBQ5Y54)

####

### <a name="with">**_With Load Balancing_**</a>

The results for just the highest client thread of 512 was better with the load balancer. Load balancers may only be effective when there's a greater load than the server by itself can tolerate.

#### 64 Client Threads/32 Consumer Threads

About 19 queue messages is much worse than the 6 without a load balancer.

![64 threads elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/elb/64-32-elb.png?token=AMABNPX7XVIJI45F3FOT5SDBQ5ZBM)

#### 128 Client Threads/32 Consumer Threads

Over 20 queue messages is much worse than the 7 without a load balancer.

![128 threads elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/elb/128-32-elb.png?token=AMABNPWNTWVNCAAY346WGM3BQ5Y7O)

#### 256 Client Threads/32 Consumer Threads

256 was about the same with and without a load balancer.

![256 threads elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/elb/256-32-elb.png?token=AMABNPWIR3V2OVONK32KBNTBQ5ZAW)

#### 512 Client Threads/32 Consumer Threads

512 is about where with the load balancer starts to outperform without the load balancer, though one sample size is not enough to tell the story.

![512 threads elb](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/regular/elb/512-32-elb.png?token=AMABNPRKB5MIG4WZK7EHMX3BQ5ZCO)

### Observation

While not observable from the charts, the increase in lag time from first starting the client program and the queue messages was much more noticeable with higher client thread counts.

&nbsp;

## <a name="uml">**UML**</a>

### Producer

![producer uml](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/uml/producer%20uml.png?token=AMABNPW3TG3HWHCEHLAXXQDBQ52US)

### Consumer

![consumer uml](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign02/images/uml/consumer%20uml.png?token=AMABNPXW6B5WGWTO242J6W3BQ52WA)
