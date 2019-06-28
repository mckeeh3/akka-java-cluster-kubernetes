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

### Installation

There are several options for how we set up a running Kubernetes environment. You can install everything on your local development system, or you can use one fo the cloud-based offerings. In addition to setting up a Kubernetes environment, you will also need to install Docker on your local development system. Of course, you will also need to install Java 8 and Maven 3.6.x.

#### Install Java and Maven
"There are various ways to get free updates (including security), and (new and existing) paid support models available from various vendors to consider." - [Source Java is Still Free](https://medium.com/@javachampions/java-is-still-free-c02aef8c9e04).

You can find Java downloads at the followinf locations:

* [AdoptOpenJDK](https://adoptopenjdk.net/)
* [Amazon](https://aws.amazon.com/corretto/)
* [Azul](https://www.azul.com/products/zulu-enterprise/)
* [BellSoft](https://bell-sw.com/java.html)
* [IBM](https://www.ibm.com/marketplace/support-for-runtimes)
* [jClarity](https://www.jclarity.com/)
* [Red Hat](https://access.redhat.com/articles/1299013)
* [SAP](https://github.com/SAP/SapMachine)

The [ApaptOpenJDK](https://adoptopenjdk.net/) is recommended as this is the JDK that Lightbend engineerin teams build against.

Maven 3.6.x is available for download on the Apache Maven [download page](https://maven.apache.org/download.cgi).

#### Install Docker

This project includes two Maven plugins. One of the plugins creates a self-contained JAR file that contains all of the compiled project class files and all of the dependencies. The other plugin creates a Docker image that contains the JAR file and the necessary settings that are used to run the Java code.

Follow the link for the [Docker installation](https://hub.docker.com/search?q=&type=edition&offering=community) for your device.

#### Install Kubernetes MiniKube Locally

Instructions and download are available on the [Install MiniKube](https://kubernetes.io/docs/tasks/tools/install-minikube/) page.

#### Install OpenShift MiniShift

TODO
