package cluster.sharding;

import akka.Done;
import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;

import java.util.concurrent.CompletableFuture;

public class Runner {
    public static void main(String[] args) {
        startupClusterNode();
    }

    private static void startupClusterNode() {
        ActorSystem actorSystem = ActorSystem.create("akka-cluster-kubernetes");

        startClusterBootstrap(actorSystem);

        actorSystem.log().info("Started actor system '{}', member {}", actorSystem, actorSystem.provider().getDefaultAddress());

        actorSystem.actorOf(ClusterListenerActor.props(), "clusterListener");
        ActorRef httpServer = actorSystem.actorOf(HttpServerActor.props(), "httpServer");
        ActorRef shardingRegion = setupClusterSharding(actorSystem, httpServer);
        createClusterSingletonManagerActor(actorSystem, httpServer);

        actorSystem.actorOf(EntityCommandActor.props(shardingRegion), "entityCommand");
        actorSystem.actorOf(EntityQueryActor.props(shardingRegion), "entityQuery");

        addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());

        registerMemberEvents(actorSystem);
    }

    private static void startClusterBootstrap(ActorSystem actorSystem) {
        AkkaManagement.get(actorSystem).start();
        ClusterBootstrap.get(actorSystem).start();
    }

    private static ActorRef setupClusterSharding(ActorSystem actorSystem, ActorRef httpServer) {
        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem);
        return ClusterSharding.get(actorSystem).start(
                "entity",
                EntityActor.props(httpServer),
                settings,
                EntityMessage.messageExtractor()
        );
    }

    private static void createClusterSingletonManagerActor(ActorSystem actorSystem, ActorRef httpServer) {
        Props clusterSingletonManagerProps = ClusterSingletonManager.props(
                ClusterSingletonActor.props(httpServer),
                PoisonPill.getInstance(),
                ClusterSingletonManagerSettings.create(actorSystem)
        );

        actorSystem.actorOf(clusterSingletonManagerProps, "clusterSingletonManager");
    }

    private static void addCoordinatedShutdownTask(ActorSystem actorSystem, String coordinatedShutdownPhase) {
        CoordinatedShutdown.get(actorSystem).addTask(
                coordinatedShutdownPhase,
                coordinatedShutdownPhase,
                () -> {
                    actorSystem.log().warning("Coordinated shutdown phase {}", coordinatedShutdownPhase);
                    return CompletableFuture.completedFuture(Done.getInstance());
                });
    }

    private static void registerMemberEvents(ActorSystem actorSystem) {
        Cluster cluster = Cluster.get(actorSystem);
        cluster.registerOnMemberUp(() -> memberUp(actorSystem, cluster.selfMember()));
        cluster.registerOnMemberRemoved(() -> memberRemoved(actorSystem, cluster.selfMember()));
    }

    private static void memberUp(ActorSystem actorSystem, Member member) {
        actorSystem.log().info("Member up {}", member);
    }

    private static void memberRemoved(ActorSystem actorSystem, Member member) {
        actorSystem.log().info("Member removed {}", member);
    }
}
