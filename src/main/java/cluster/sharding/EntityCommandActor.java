package cluster.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Cancellable;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class EntityCommandActor extends AbstractLoggingActor {
    private final ActorRef shardRegion;
    private Cancellable ticker;
    private int messageNumber;
    private final Receive sending;
    private final Receive receiving;
    private final Cluster cluster = Cluster.get(context().system());

    {
        sending = receiveBuilder()
                .matchEquals("tick", t -> tickSending())
                .match(EntityMessage.CommandAck.class, this::commandAckSending)
                .matchAny(this::adjustMessageRate)
                .build();

        receiving = receiveBuilder()
                .matchEquals("tick", t -> tickReceiving())
                .match(EntityMessage.CommandAck.class, this::commandAckReceiving)
                .matchAny(this::adjustMessageRate)
                .build();
    }

    private EntityCommandActor(ActorRef shardRegion) {
        this.shardRegion = shardRegion;
    }

    @Override
    public Receive createReceive() {
        return sending;
    }

    private void commandAckSending(EntityMessage.CommandAck commandAck) {
        log().warning("Received (late) {} {}", commandAck, sender());
    }

    private void tickSending() {
        shardRegion.tell(command(), self());
        getContext().become(receiving);
    }

    private void commandAckReceiving(EntityMessage.CommandAck commandAck) {
        log().info("Received {} {}", commandAck, sender());
        getContext().become(sending);
    }

    private void tickReceiving() {
        log().warning("No response to last command {}", messageNumber);
        getContext().become(sending);
    }

    private EntityMessage.Command command() {
        return new EntityMessage.Command(randomEntity());
    }

    private Entity randomEntity() {
        return new Entity(Random.entityId(1, 100), new Entity.Value(String.format("%s-%d", self().path().name(), ++messageNumber)));
    }

    private void adjustMessageRate(Object clusterEventMessage) {
        scheduleMessageRateTicker(clusterEventMessage);
    }

    private void scheduleMessageRateTicker(Object event) {
        int memberCount = cluster.state().members().size();
        int messageRatePerSec = 10;
        int millsPerMessage = 1000 / messageRatePerSec * (memberCount == 0 ? 3 : memberCount);
        log().info("Message interval {}ms, cluster member count {}, {}", millsPerMessage, memberCount, event);

        FiniteDuration tickInterval = Duration.create(millsPerMessage, TimeUnit.MILLISECONDS);

        if (ticker != null) {
            ticker.cancel();
        }

        ticker = context().system().scheduler().schedule(
                Duration.Zero(),
                tickInterval,
                self(),
                "tick",
                context().system().dispatcher(),
                null
        );
    }

    @Override
    public void preStart() {
        log().info("Start");
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent.class);
        scheduleMessageRateTicker("Start");
    }

    @Override
    public void postStop() {
        log().info("Stop");
        cluster.unsubscribe(self());
        ticker.cancel();
    }

    static Props props(ActorRef shardRegion) {
        return Props.create(EntityCommandActor.class, shardRegion);
    }
}
