# CS6650 Fall 2021 Assignment 1

## <a name="github">**GitHub**</a>:

**https://github.com/timegao/cs6650-fa21/tree/main/assign01**

&nbsp;

## **Submission Requirements**:

> [the URL for your git repo](#github). Make sure that the code for the client part 1 and part 2 are in separate folders in your repo

> a 1-2 page description of your [client design](#design). Include [major classes](#classes), [packages](#dependencies), [relationships](#uml), whatever you need to convey concisely how your client works. Include [Little’s Law](#littles-law) throughput predictions.

> Client (Part 1) - run your client with 32, 64, 128 and 256 threads, with numSkiers=20000, and numLifts=40. Include the outputs of each run in your submission (showing the wall time) and plot a simple chart showing the [wall time](#wall) by the number of threads. This should be a [screenshot](https://github.com/timegao/cs6650-fa21/tree/main/assign01/data/png/part1) of your output window.

> Client (Part 2) - run the client as per Part 1, showing the output window for each run. Also generate a plot of [throughput](#throughput) and [mean response time](#mean) against number of threads. Again, this should be a [screenshot](https://github.com/timegao/cs6650-fa21/tree/main/assign01/data/png/part2) of your output window.

## Bonus:

> It is usually interesting to plot average latencies over the whole duration of a test run. To do this you will have to capture timestamps of when the request occurs, and then generate a plot that shows [latencies](#latencies) against time (there’s a good example in the percentile article earlier).

&nbsp;

## **Introduction**:

Assignment description: https://gortonator.github.io/bsds-6650/assignments-2021/Assignment-1

> You work for Upic - a global acquirer of ski resorts that is homogenezing skiing around the world. Upic ski resorts all use RFID lift ticket readers so that every time a skiier gets on a ski lift, the time of the ride and the skier ID are recorded.

> In Assignment 1, we’ll build a client that generates and sends lift ride data to a server in the cloud. The server will simply accept and validate requests, and send an HTTP 200/201 response.

&nbsp;

## **Specs**:

### Laptop

All the tests were run on a Apple MacBook Air (Dec 2020) with the Apple M1 SoC.

### Internet

I used my home internet for the entirety of the tests on BlueWave 100 mbps internet.

![speedtest](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/other/speedtest.png?token=AMABNPV622TZUWNBEIXQ4MLBM5FCY)

I tried using the WiFi on campus because it's been recently upgraded to fiber speeds. However, as before, the limitation was the number of requests it could send out before connection timed out. Connections would time out after about 64 requests no matter what I did, and I spent a whole afternoon troubleshooting the issue to no avail. I did not try the instructor's recommendation to use the WiFi from a coffee shop being that my home internet did the job, being that many of the coffee shops nearby did not have good internet (.e.g, Fresh Flours internet can't even stream a video, and Capital One Cafe internet is very slow). If I had to pick an internet to try, I would try the Starbucks on 442 Terry Ave N, Seattle, WA 98109, but I don't know how good the internet is because I've never had to use it. The reason is that from the instructor's story about a previous student who couldn't get her tests to run, Starbucks' internet was the internet that worked.

### Testing

Testing were mostly done around noon hours in Pacific Standard time. The server location was limited to the us-west-1 Virginia on AWS because that's the only region available for AWSEducate, and the requests originated from Seattle, WA.

&nbsp;

## <a name="design">**Design**</a>:

### Part 1

> In this assignment you need to implement this API using a Java servlets. Each API should:

> - Accept the parameters for each operations as per the specification

> - Do basic parameter validation, and return a 4XX response code and error message if invalid values/formats supplied
> - If the request is valid, return a 200/201 response code and some dummy data as a response body

I used the same server built from [lab-2](https://github.com/timegao/cs6650-fa21/tree/main/lab02).

It is a Java Servlet with doPost and doGet methods that accepts parameters to /skiers/ url. The specifications for the servlet are loosely tied to [skiDataAPI Swagger specifications](https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.1#/skiers/getSkierDayVertical). I say loosely because not all of the GET requests are being handled and checked, although for POST requests, it should be working according to specifications.

> Your client should accept a set of parameters from the command line (or a parameter file) at startup. These are:

> - maximim number of threads to run (numThreads - max 256)
> - number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the skier’s ID (skierID)
> - number of ski lifts (numLifts - range 5-60, default 40)
> - mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
> - IP/port address of the server

I used Apache Options from the [Commons CLI library](https://commons.apache.org/proper/commons-cli/) to write according to the specifications. The `Options` are parsed using the `CommandLineOptions` class that inputs the required `Option` parameters and then additionally parsed and handled using the `CommandLineInput` class that stores the information.

```
usage: options

-i, --ip <arg> ip address of server

-l, --lifts <arg> number of ski lifts, range 5-60, default 40

-m, --mean <arg> mean number ski lifts per ski rider per day, default 10, max 20

-s, --skiers <arg> number of skier IDs, max 100,000

-t, --threads <arg> number of threads, max 2
```

> Phase 1, the startup phase, will launch numThreads/4 threads, and each thread will be passed:

> - a start and end range for skierIDs, so that each thread has an identical number of skierIDs, caluculated as numSkiers/(numThreads/4). Pass each thread a disjoint range of skierIDs so that the whole range of IDs is covered by the threads, ie, thread 0 has skierIDs from 1 to (numSkiers/(numThreads/4)), thread 1 has skierIDs from (1x(numSkiers/(numThreads/4)+1) to (numSkiers/(numThreads/4))x2
> - a start and end time, for this phase this is the first 90 minutes of the ski day (1-90)

The logic to generate the random LiftRide is in `PostRequestTask`.

> Once each thread has started it should send (numRunsx0.2)x(numSkiers/(numThreads/4)) POST requests to the server. Each POST should randomly select:

> - a skierID from the range of ids passed to the thread
> - a lift number (liftID)
> - a time value from the range of minutes passed to each thread (between start and end time)

> Once 10% (rounded up) of the threads in Phase 1 have completed, Phase 2, the peak phase should begin. Phase 2 behaves like Phase 1, except:

> - it creates numThreads threads
> - the start and end time interval is 91 to 360
> - each thread is passed a disjoint skierID range of size (numSkiers/numThreads)

> Finally, once 10% of the threads in Phase 2 complete, Phase 3 should begin. Phase 3, the cooldown phase, is identical to Phase 1, starting 25% of numThreads, with each thread sending (0.1xnumRuns) POST requests, and with a time interval range of 361 to 420.

`ExecutorServiceTask` uses `CountDownLatch` to track when to move from one phase to the next. It uses a method startPhase to create a phase based on the specifications. To ensure that all threads are created before the `ExecutorService` shutsdown, the final `CountDownLatch` has all its threads in the `CountDownLatch`. `ExecutorServiceTask` has a `ExecutorService` to track the progress of all three tasks.

> When all threads from all phases are complete, the programs should print out:

> - number of successful requests sent
> - number of unsuccessful requests (ideally should be 0)
> - the total run time (wall time) for all phases to complete. Calculate this by taking a timestamp before commencing Phase 1 and another after all Phase 3 threads are complete.
>   the total throughput in requests per second (total number of requests/wall time)

To counter the number of successful and failed requests, I used a `Counter` class that has synchronized methods to get and add a count. `ExecutorServiceTask` uses System.out.println to print the required fields, including the number of threads.

### Part 2

> With your load generating client working wonderfully, we want to now instrument the client so we have deeper insights into the performance of the system. To this end, for each POST request:

> - before sending the POST, take a timestamp
> - when the HTTP response is received, take another timestamp
> - calculate the latency (end - start) in milliseconds
> - Write out a record containing {start time, request type (ie POST), latency, response code}. CSV is a good file format.

> Once all phases have completed, we need to calculate:

> - mean response time (millisecs)
> - median response time (millisecs)
> - throughput = total number of requests/wall time
> - p99 (99th percentile) response time. Here’s a nice article about why percentiles are important and why calculating them is not always easy.
> - max response time

I added another class `Request` to track the required information in a model. I also added another class `RequestAnalyzer` to calculate the response values to System.out.println. Part 2 has additional values to print both to console, and it also writes the values recoreded in a csv.

### <a name="classes">**Classes**</a>

#### server

`SkierServlet` - starts the `Servlet` that runs on Tomcat on AWS EC2 instance.

#### commandline

`CommandLineInput` - exception handles the information generated from `CommandLineOptions` and stores it.

`CommandLineOptions` - specifies the `Options` and `Option` to be parsed from the commandline.

#### model

`Counter` (part1 only) - stores the counter information for part 1 of the assignment.

`Lift` - stores the calculated values to generate a random `LiftRide`.

`Request` (part2 only) - stores the relevant request information for part 2 of the assignment.

#### task

`ExecutorServiceTask` - takes in the input from `CommandLineInput` and generates the information required to create `PostRequestTask` and runs it. Prints the output to terminal and prints the output to csv.

`PostRequestTask` - takes in the required information for to generate a number of POST requests.

`RequestAnalyzer` (part2 only) - analyzes the `ConcurrentLinkedQueue` generated from `ExecutorServiceTask` and calculates the mean, median, max, and p99 values.

### <a name="dependencies">**Dependencies**</a>:

#### server

json - used to instantiate and edit JSON objects.

javax.servlet-api - used to create the Servlet.

##### client

lombok - used to simplify classes with annotations.

swagger-java-client - used for Swagger Client SDK.

&nbsp;

## **Charts**:

### <a name="littles-law">**Little's Law**</a>

![single thread](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/other/single-1000.png?token=AMABNPTJWOP2DFRYXY2CLVLBM6TEM)

| threads                 | 1       |
| ----------------------- | ------- |
| successes               | 10,000  |
| failures                | 0       |
| wall (ms)               | 764,544 |
| throughput (requests/s) | 13.0797 |

Based on the results from the single thread 10,000 requests, we have a minimum response time of 76.4544 ms per request.

Calculating for the values by multiplying the thread, assuming ZERO additional costs by adding threads, we would expect the following theoretical wall times and throughput for 180,000 requests by multiply by the number of threads and also 18 (since about 180,000 requests are created for multithread as opposed to 10,000 for single thread):

### Theoretical

| threads                 | 32       | 64       | 128        | 256        |
| ----------------------- | -------- | -------- | ---------- | ---------- |
| wall (ms)               | 430,056  | 215,028  | 107,514    | 53,757     |
| throughput (requests/s) | 418.5504 | 837.1008 | 1,674.2016 | 3,348.4032 |

### Part 1

| threads                 | 32       | 64       | 128        | 256       |
| ----------------------- | -------- | -------- | ---------- | --------- |
| requests                | 180,000  | 180,000  | 179,936    | 179,776   |
| wall (ms)               | 389,469  | 200,435  | 111,967    | 67,157    |
| throughput (requests/s) | 462.1677 | 898.0467 | 1,607.0449 | 2,676.951 |

### Part 2

| thread                  | 32       | 64       | 128        | 256       |
| ----------------------- | -------- | -------- | ---------- | --------- |
| requests                | 180,000  | 180,000  | 179,936    | 179,776   |
| wall (ms)               | 389,382  | 206,415  | 116,005    | 69,065    |
| throughput (requests/s) | 462.2709 | 872.0297 | 1,551.1055 | 2,602.997 |
| mean (ms)               | 78.1516  | 84.0199  | 94.7143    | 114.4744  |
| median (ms)             | 74       | 76       | 77         | 81        |
| max (ms)                | 1177     | 1431     | 3,510      | 6,076     |
| p99 (ms)                | 158      | 234      | 330        | 374       |

### Part 1 Part 2 Comparison

We are able to conclude that the difference in wall time between part 1 and part 2 is less than 5% across the board.

#### 32 64 Threads

We observe that the change in wall time is less than 3%.

| thread | p1-32   | p2-32   | change | p1-64   | p2-64   | change |
| ------ | ------- | ------- | ------ | ------- | ------- | ------ |
| wall   | 389,469 | 389,382 | 0.00%  | 200,435 | 206,415 | 2.99%  |

#### 128 256 Threads

We observe that the change in wall time is about 3%.

| p1-128  | p2-128  | change | p1-256 | p2256  | change |
| ------- | ------- | ------ | ------ | ------ | ------ |
| 111,967 | 116,005 | 3.61%  | 67,157 | 69,065 | 2.84%  |

### <a name="mean">**Mean Median**</a>

What accounts for the fact that as the number of thread increases, the increase in throughput and decrease in wall time does not keep up? We can see that the median and median increases in value as we double the number of threads.

![mean-median](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/mean-median.png?token=AMABNPSRDDJTOQNGZSEOM2TBM6UAO)

### Max P99

We can also see that while the p99 response time is increasing quickly, the max response time is close to doubling every time we double the number of threads.

![max-p99](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/max-p99.png?token=AMABNPVTWEVZJ6TLXL6L6Y3BM6V7O)

### <a name="wall">**Wall Time**</a>

We can compare the theoretical wall time with the actual wall time based on the number of threads (the lower the better). For whatever reason, the estimated wall time at 32 and 64 threads are actually less than the single thread thereotical limits. However, while the tests keep up with 32 and 64 threads, even exceeding the expected the result for 32 threads, once we hit 128 and 256 threads, the test values quickly lose out to the theoretical limits.

![wall](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/wall.png?token=AMABNPR4YK3T2X74JQKNOMDBM5WBS)

### <a name="throughput">**Throughput**</a>

We can compare the theoretical throughput with the actual throughput based on the number of threads (the higher the better). Since the throughput is based on the wall time, likewise, it fails to keep up once we hit 128 and 256 threads.

![throughput](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/throughput.png?token=AMABNPVQAQXTMFYSTBR7TZLBM6UEA)

### <a name="latencies">**Latencies**</a>

Interestingly, we can see that as the number of thread increases, the longer it takes for the first threads to finish (response time). The first threads are the slowest among all the threads, often taking up the max response time slot. This is most exacerbated once we hit 256 threads, where we can see that the max response time of 6,000 ms happens at the very beginning of the program.

#### 32 Threads

![32 threads](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/latency-32.png?token=AMABNPXEE2WDTHDU2DKGSXLBM6UFW)

#### 64 Threads

![64 threads](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/latency-64.png?token=AMABNPVJGWLLRP7UI3MUMJDBM6UGK)

#### 128 Threads

![128 threads](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/latency-128.png?token=AMABNPVGIDPC2LOMGQBRY7DBM6UHC)

##### 256 Threads

![256 threads](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/charts/latency-256.png?token=AMABNPXIEABTI5T5NUNRJOTBM6UHW)

&nbsp;

## <a name="uml">**UML**</a>

### Part 1

![part1-uml](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/other/part1-uml.png?token=AMABNPTBRVMV7MC2R4HB62LBM5WOY)

### Part 2

![part2-uml](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/other/part2-uml.png?token=AMABNPUPSDWE2BHHPDXAUJDBM5WPY)

&nbsp;

## **Screenshots**

### Part 1

[thread(s), successes, failures, wall, throughput](https://github.com/timegao/cs6650-fa21/tree/main/assign01/data/png/part1)

### Part 2

[thread(s), successes, failures, wall, throughput, mean, median, max, p99](https://github.com/timegao/cs6650-fa21/tree/main/assign01/data/png/part2)
