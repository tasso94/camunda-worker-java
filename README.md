# camunda-worker-java

Implement workers for external tasks in [Camunda BPM](http://camunda.org) in Java.

> Alternative Versions: [NodeJS](https://github.com/nikku/camunda-worker-node),  [Akka / Scala](https://github.com/saig0/camunda-worker-akka)

## Summary

This tool provides a Java interface to external tasks exposed by the process engine.

## Getting started

> Note: you need a [special fork of the Camunda BPM Platform](https://github.com/saig0/camunda-bpm-platform/blob/worker/README.md) to be able to use this.

Example of a Java consumer

```java
public class MyApp {

  public static void main(String[] args) {

    CamundaClient client = CamundaClient.create()
      .endpointUrl("http://localhost:8080/engine-rest/")
      .build();

    WorkerRegistration registration = client.registerWorker()
      .topicName("reserveOrderItems")
      .lockTime(5000)
      .worker(new Worker() {

        public void doWork(TaskContext taskContext) {

          // do the work
          
          // if the work was successful, complete the task
          taskContext.complete();
          
          // else if the work was un-successful, fail the task
          taskContext.failed("some error message");

        }
      })
      .build();

  }

}
```

## Maven dependency

```xml
<dependency>
  <groupId>org.camunda.bpm.ext</groupId>
  <artifactId>camunda-bpm-sdk-java</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Features

* *Efficient Threading & Multipoll*: single thread polling multiple topics at once delegating to a pool of worker threads
* *Adaptive Polling with Exponential Backoff*: fetcher dynamically adjusts to avaiable work and backs off in case no work is available.

