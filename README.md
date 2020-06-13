# File Analysis Service

A submission task of BigID candidate exercise.
submitted by :Tomer Shaiman

The service is reads from a given blackbox utility that emitts json data to the Stdout, parses the data, filter bad input
and perform several Statics on the event stream :
* a "word-count" on the data field of the stream
* a count on the "event-type" field of the stream (a.k.a : how many events per each type received)

## Installation Instructions
The following software must be installed on the host :
* Java 8 and above
* maven


## Test 
There are only few tests due to lack of time.
The tests presetns how to use injection and profiles with SpringBoot.
```console
> mvn test
```
## Run 
you can run the project from mvn command line :
```console
> mvn spring-boot:run
```

or using docker with mount-bind to the input file that contains the big data text 
(note the file name must reside in /input/big.txt)
```console
> docker run --name file-service -v $(pwd)/input:/app/input  tshaiman/file-analysis-service:1.0
```



## Architecture Overview
* Sprint-Boot project (minimal dependencies) 
* Components are communicating using a "Service-Bus" 
* ServiceBus current implemenation is BlockingQueue but its easier to migrate to real service bus such as kafka
* An 'Orchestrator' class is used as Coordinator for building and running the pipeline
* The Orchestractor is also responsible from controlling when a flow is completed (unlike polling/asking)
* Configuration is in YAML format , as this is becoming the standard in kubernetes.


![Alt text](images/arch.png?raw=true "architecture")

## SW Engineering concepts used 

- **"Solid"** - e.g : Separation of Concerns, Dependecy Inversion, etc.
- **"Event Driven System"** - the flow advances by placing events, not calling objects.
- **"Performance"** - The algorithm I was using (map/reduce) makes it very efficient, its O(n). 
- **"Functional Programming vrs OOP"** :
    * Java does not yet allow a full FP paradigm, but I did try to move away from imperative programming
    * Scala is a better approach here, especially with the Actor framework (Akka)
    * Vertx could have been used as well since it offers the Reactive framework paradigm.
    * I did however used more OOP then FP in order to make easier reading. 
- **"Generic"** - The messages are translated to Json Strings, many of the components are <T> , etc.

## Improvement Suggestions
If we want to use this component in production for large scale we need to modify the following :

* Separate each worker to its own microservice and scale accordingly. it is quite easy effort since the workers
already does not have dependencies on other components.
* Move from Blocking Queue to Kafka
* Change the Aggregator Concurrent Hash Map to use REDIS/NoSql Db and scale accordingly.
* User metrics , more logs and more tests.

  

## Thank you for your time on reviewing this submission
