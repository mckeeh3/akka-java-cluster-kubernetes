package cluster.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.cluster.Cluster;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class ClusterSingletonActor extends AbstractLoggingActor {
    private final ActorRef httpServer;
    private final String memberId = Cluster.get(context().system()).selfMember().address().toString();
    private final FiniteDuration tickInterval = Duration.create(5, TimeUnit.SECONDS);
    private Cancellable ticker;

    public ClusterSingletonActor(ActorRef httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("tick", t -> tick())
                .build();
    }

    private void tick() {
        httpServer.tell(new Action(memberId, "start", true), self());
    }

    @Override
    public void preStart() {
        log().info("Start");
        ticker = context().system().scheduler()
                .schedule(Duration.Zero(),
                        tickInterval,
                        self(),
                        "tick",
                        context().system().dispatcher(),
                        null);
    }

    @Override
    public void postStop() {
        log().info("Stop");
        httpServer.tell(new Action(memberId, "stop", true), self());
        ticker.cancel();
    }

    static Props props(ActorRef httpServer) {
        return Props.create(ClusterSingletonActor.class, httpServer);
    }

    static class Action implements Serializable {
        final String member;
        final String action;
        final boolean forward;

        Action(String member, String action, boolean forward) {
            this.member = member;
            this.action = action;
            this.forward = forward;
        }

        Action asNoForward() {
            return new Action(member, action, false);
        }

        @Override
        public String toString() {
            return String.format("%s[%s, %s, %b]", getClass().getSimpleName(), member, action, forward);
        }
    }
}
