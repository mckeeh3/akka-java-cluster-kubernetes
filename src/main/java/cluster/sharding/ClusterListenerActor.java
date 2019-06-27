package cluster.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

class ClusterListenerActor extends AbstractLoggingActor {
    private final Cluster cluster = Cluster.get(context().system());
    private Cancellable showClusterStateCancelable;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ShowClusterState.class, this::showClusterState)
                .matchAny(this::logClusterEvent)
                .build();
    }

    private void showClusterState(ShowClusterState showClusterState) {
        log().info("{} sent to {}", showClusterState, cluster.selfMember());
        logClusterMembers(cluster.state());
        showClusterStateCancelable = null;
    }

    private void logClusterEvent(Object clusterEventMessage) {
        log().info("{} sent to {}", clusterEventMessage, cluster.selfMember());
        logClusterMembers();
    }

    @Override
    public void preStart() {
        log().debug("Start");
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent.class);
    }

    @Override
    public void postStop() {
        log().debug("Stop");
        cluster.unsubscribe(self());
    }

    static Props props() {
        return Props.create(ClusterListenerActor.class);
    }

    private void logClusterMembers() {
        logClusterMembers(cluster.state());

        if (showClusterStateCancelable == null) {
            showClusterStateCancelable = context().system().scheduler().scheduleOnce(
                    Duration.ofSeconds(15),
                    self(),
                    new ShowClusterState(),
                    context().system().dispatcher(),
                    null);
        }
    }

    private void logClusterMembers(CurrentClusterState currentClusterState) {
        Optional<Member> old = StreamSupport.stream(currentClusterState.getMembers().spliterator(), false)
                .reduce((older, member) -> older.isOlderThan(member) ? older : member);

        Member oldest = old.orElse(cluster.selfMember());

        StreamSupport.stream(currentClusterState.getMembers().spliterator(), false)
                .forEach(new Consumer<Member>() {
                    int m = 0;

                    @Override
                    public void accept(Member member) {
                        log().info("{} {}{}{}", ++m, leader(member), oldest(member), member);
                    }

                    private String leader(Member member) {
                        return member.address().equals(currentClusterState.getLeader()) ? "(LEADER) " : "";
                    }

                    private String oldest(Member member) {
                        return oldest.equals(member) ? "(OLDEST) " : "";
                    }
                });
    }

    private static class ShowClusterState {
        @Override
        public String toString() {
            return ShowClusterState.class.getSimpleName();
        }
    }
}
