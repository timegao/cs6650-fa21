# CS6650 Fall 2021 Assignment 1

## Introduction:

Assignment description: https://gortonator.github.io/bsds-6650/assignments-2021/Assignment-1

> You work for Upic - a global acquirer of ski resorts that is homogenezing skiing around the world. Upic ski resorts all use RFID lift ticket readers so that every time a skiier gets on a ski lift, the time of the ride and the skier ID are recorded.

> In Assignment 1, we’ll build a client that generates and sends lift ride data to a server in the cloud. The server will simply accept and validate requests, and send an HTTP 200/201 response.

## Specs:

### Laptop

All the tests were run on a Apple MacBook Air (Dec 2020) with the latest Apple M1 SoC.

### Internet

I used my home internet for the entirety of the tests on BlueWave 100 mbps internet.

![speedtest](https://raw.githubusercontent.com/timegao/cs6650-fa21/main/assign01/data/png/other/speedtest.png?token=AMABNPV622TZUWNBEIXQ4MLBM5FCY)

I tried using the WiFi on campus because it's been recently upgraded to fiber speeds. However, as before, the limitation was the number of requests it could send out before connection timed out. Connections would time out after about 64 requests no matter what I did, and I spent a whole afternoon troubleshooting the issue to no avail. I did not try the instructor's recommendation to use the WiFi from a coffee shop being that my home internet did the job, being that many of the coffee shops nearby did not have good internet (.e.g, Fresh Flours internet can't even stream a video, and Capital One Cafe internet is very slow). If I had to pick an internet to try, I would try the Starbucks on 442 Terry Ave N, Seattle, WA 98109, but I don't know how good the internet is because I've never had to use it. The reason is that from the instructor's story about a previous student who couldn't get her tests to run, Starbucks' internet was the internet that worked.

### Testing

Testing were mostly done at evening and morning hours in Pacific Standard time. The server location was limited to the us-west-1 Virginia on AWS because that's the only region available for AWSEducate.

## Design:

### Part 1

> In this assignment you need to implement this API using a Java servlets. Each API should:

> - Accept the parameters for each operations as per the specification

> - Do basic parameter validation, and return a 4XX response code and error message if invalid values/formats supplied
> - If the request is valid, return a 200/201 response code and some dummy data as a response body

I used the same server built from lab-2, please see [MISSING LINK].

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

> Once each thread has started it should send (numRunsx0.2)x(numSkiers/(numThreads/4)) POST requests to the server. Each POST should randomly select:

> - a skierID from the range of ids passed to the thread
> - a lift number (liftID)
> - a time value from the range of minutes passed to each thread (between start and end time)

The logic to generate the random LiftRide is in `PostRequestTask`.

> Once 10% (rounded up) of the threads in Phase 1 have completed, Phase 2, the peak phase should begin. Phase 2 behaves like Phase 1, except:

> - it creates numThreads threads
> - the start and end time interval is 91 to 360
> - each thread is passed a disjoint skierID range of size (numSkiers/numThreads)

> Finally, once 10% of the threads in Phase 2 complete, Phase 3 should begin. Phase 3, the cooldown phase, is identical to Phase 1, starting 25% of numThreads, with each thread sending (0.1xnumRuns) POST requests, and with a time interval range of 361 to 420.

`ExecutorServiceTask` uses `CountDownLatch` to track when to move from one phase to another phase. It uses a method startPhase to create a phase based on the specifications. To ensure that all threads are created before the `ExecutorService` shutsdown, the final `CountDownLatch` has all its threads in the `CountDownLatch`. `ExecutorServiceTask` has a `ExecutorService` to track the progress of all three tasks. Finally, shutdown the `ExecutorServiceTask`.

> When all threads from all phases are complete, the programs should print out:

> - number of successful requests sent
> - number of unsuccessful requests (ideally should be 0)
> - the total run time (wall time) for all phases to complete. Calculate this by taking a timestamp before commencing Phase 1 and another after all Phase 3 threads are complete.
>   the total throughput in requests per second (total number of requests/wall time)

`ExecutorServiceTask` uses System.out.println to print the required fields, including the number of threads.

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

### Classes

#### commandline

`CommandLineInput` - Exception handles the information generated from `CommandLineOptions` and stores it.

`CommandLineOptions` - Specifies the `Options` and `Option` to be parsed from the commandline.

#### model

`Counter` (part1 only) - stores the counter information for part 1 of the assignment.

`Lift` - stores the calculated values to generate a random `LiftRide`.

`Request` (part2 only) - stores the relevant request information for part 2 of the assignment.

#### task

`ExecutorServiceTask` - takes in the input from `CommandLineInput` and generates the information required to create `PostRequestTask` and runs it. Prints the output to terminal and prints the output to csv.

`PostRequestTask` - takes in the required information for to generate a number of POST requests.

`RequestAnalyzer` (part2 only) - analyzes the `ConcurrentLinkedQueue` generated from `ExecutorServiceTask` and calculates the mean, median, max, and p99 values.

### dependencies:

#### server

json - used to instantiate and edit JSON objects.

javax.servlet-api - used to create the Servlet.

##### client

lombok - used to simplify classes with annotations.

swagger-java-client - used for Swagger Client SDK.

## Submission Requirements:

> - the URL for your git repo. Make sure that the code for the client part 1 and part 2 are in seperate folders in your repo

> - a 1-2 page description of your client design. Include major classes, packages, relationships, whatever you need to convey concisely how your client works. Include Little’s Law throughput predictions.

> - Client (Part 1) - run your client with 32, 64, 128 and 256 threads, with numSkiers=20000, and numLifts=40. Include the outputs of each run in your submission (showing the wall time) and plot a simple chart showing the wall time by the number of threads. This should be a screen shot of your output window.

> - Client (Part 2) - run the client as per Part 1, showing the output window for each run. Also generate a plot of throughput and mean response time against number of threads. Again, this should be a screen shot of your output window.

## Bonus:

> It is usually interesting to plot average latencies over the whole duration of a test run. To do this you will have to capture timestamps of when the request occurs, and then generate a plot that shows latencies against time (there’s a good example in the percentile article earlier).

## Charts:

### Part 1

| threads                 | 32      | 64       | 128       | 256       |
| ----------------------- | ------- | -------- | --------- | --------- |
| requests                | 180000  | 180000   | 179936    | 179776    |
| wall (ms)               | 390486  | 213692   | 124623    | 79733     |
| throughput (requests/s) | 460.964 | 842.3339 | 1443.8426 | 2254.7252 |

### Part 2

| threads                 | 32       | 64       | 128       | 256       |
| ----------------------- | -------- | -------- | --------- | --------- |
| requests                | 180000   | 180000   | 179936    | 179776    |
| wall (ms)               | 399636   | 206530   | 114642    | 72433     |
| throughput (requests/s) | 450.4099 | 871.5441 | 1569.5469 | 2481.9627 |
| mean (ms)               | 79.9212  | 83.0636  | 91.9422   | 118.6241  |
| median (ms)             | 73       | 74       | 75        | 77        |
| max (ms)                | 1380     | 2082     | 3202      | 7128      |
| p99 (ms)                | 333      | 340      | 359       | 437       |