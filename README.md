## Akka Java Cluster Kubernetes Example

### Introduction

This is a Java, Maven, Akka project that demonstrates how to setup an
[Akka Cluster](https://doc.akka.io/docs/akka/current/index-cluster.html)
with an example implementation of
[Cluster Sharding](https://doc.akka.io/docs/akka/current/cluster-sharding.html) running in a Kubernetes cluster.

This project is one in a series of projects that starts with a simple Akka Cluster project and progressively builds up to examples of event sourcing and command query responsibility segregation.

The project series is composed of the following projects:
* [akka-java-cluster](https://github.com/mckeeh3/akka-java-cluster)
* [akka-java-cluster-aware](https://github.com/mckeeh3/akka-java-cluster-aware)
* [akka-java-cluster-singleton](https://github.com/mckeeh3/akka-java-cluster-singleton)
* [akka-java-cluster-sharding](https://github.com/mckeeh3/akka-java-cluster-sharding)
* [akka-java-cluster-persistence](https://github.com/mckeeh3/akka-java-cluster-persistence)
* [akka-java-cluster-persistence-query](https://github.com/mckeeh3/akka-java-cluster-persistence-query)
* [akka-java-cluster-kubernetes](https://github.com/mckeeh3/akka-java-cluster-kubernetes) (this project)

Each project can be cloned, built, and runs independently of the other projects.

This project contains an example implementation of cluster sharding running in a Kubernetes cluster. The project also includes a live web client visualization of the cluster and the Akka actors running in the cluster JVMs.

![Visualization of cluster sharding](docs/images/akka-cluster-k8-3-pods.png)
<center>Figure 1, Visualization of cluster sharding</center><br/>

Once this application is successfully deployed to a properly configured Kubernetes cluster you will be able to pull up a live view of the running system, as shown above in Figure 1.

TODO
