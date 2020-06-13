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
There are only few tests, just to make the point they are needed.
```console
> mvn test
```
## Run 
```console
> mvn spring-boot:run
```


## Architecture Overview
* Sprint-Boot project (minimal dependencies) 
* Components are communicating using a "Service-Bus" 
* ServiceBus current implemenation is BlockingQueue but its easier to migrate to real service bus such as kafka
* An 'Orchestrator' class is used as Coordinator for building and running the pipeline
* The Orchestractor is also responsible from controlling when a flow is completed (unlike polling/asking)


![Alt text](images/arch.png?raw=true "architecture")

## SW Engineering concepts used 

- **"Solid"** - e.g : Separation of Concerns, Dependecy Inversion, etc.
- **"Event Driven System"** - the flow advances by placing events, not calling objects.
- **"Performance"** - The algorithm I was using (map/reduce) makes it very efficient, its O(n). 
- **"Functional Programming"** :
    * Java does not yet allow a full FP paradigm, but I did try to move away from imperative programming
    * Scala is a better approach here, especially with the Actor framework (Akka)
- **"Generic"** - The messages are translated to Json Strings, many of the components are <T> , etc.

## Improvement Suggestions
If we want to use this component in production for large scale we need to modify the following :

* Separate each worker to its own microservice and scale accordingly. it is quite easy effort since the workers
already does not have dependencies on other components.
* Move from Blocking Queue to Kafka
* Change the Aggregator Concurrent Hash Map to use REDIS/NoSql Db and scale accordingly.
* User metrics , more logs and more tests.

  

## Thank you for your time on reviewing this submission
