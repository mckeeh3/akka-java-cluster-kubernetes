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

The above visualization is rendered using the [D3Js](https://d3js.org/) library. The web page used to render the visualization is located in `src/main/resources/monitor.html`.

The monitor webpage is accessed via the HTTP server that is running in the `HttpServerActor` class. The HTTP server is started in this actor so in addition to handling HTTP requests this actor also handles incoming actor messages.

The monitor web page uses a web socket to communicate with the HTTP server. The primary action is that every few seconds, the web page sends a request for the current status of the running cluster. The HTTP server actor responds with a JSON tree that contains the current cluster state.

This tree starts with a single root element that represents the cluster. The next tree elements up from the root represent Akka cluster nodes. These elements also represent actors that are started one per cluster node. An example of a single actor per cluster node is the `HttpServerActor`. One instance of this actor is started on each Akka cluster node.

In the visualization shown in Figure 1, the three large circles represent the Akka cluster nodes. These circles are shown using the colors brown, orange, and yellow. A yellow circle indicates which node is receiving HTTP requests from the browser. A brown circle is used to show where cluster singleton actors are currently located in the cluster. Orange circles show other cluster nodes.

Continuing up from the Akka node tree elements are shard actor elements. Shard actors are used in Akka Cluster Sharding to distribute entity actor instances across the cluster nodes. In the visualized tree the shard actors are shown as green circles.

The tree leaf elements represent individual entity actors. The entity actors are shown as blue circles. When an entity actor instance is first started, the color used is a darker blue. When an entity actor instance is stopped the color fades to pink and then the circle disappears.

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

Instructions and download are availabel on the [Welcome to MiniShift](https://docs.okd.io/latest/minishift/index.html) page.

#### Using OpenShift Online

TODO

#### Uising IBM Cloud Kubernetes Service

TODO

### Build and Deploy the Demo Application

The following command builds the project, loads all of the code and dependencies into a single JAR file, and then creates a Docker image that is set up to run the JAR file in a Docker container.

When running Kubernetes on your laptop, the instructions for each specific set up define how Docker is used when Docker images are created.

~~~bash
mvn clean package docker:build
~~~

The Docker image is deployed to a Kubernetes environment using the following two steps.

First, a Role and RoleBinging are set up. The Role and RoleBinding give the pods access to the API server. More details are provided in the [Kubernetes API](https://doc.akka.io/docs/akka-management/current/bootstrap/kubernetes-api.html#kubernetes-api) documentation, which is part of [Akka Cluster Bootstrap](https://doc.akka.io/docs/akka-management/current/bootstrap/index.html#akka-cluster-bootstrap). The Role and RoleBinding only need to be created once.

~~~bash
kubectl apply -f kubernetes/akka-cluster-rolebindging.yml
~~~



TODO
