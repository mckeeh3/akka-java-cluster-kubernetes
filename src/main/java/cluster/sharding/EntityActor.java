package cluster.sharding;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.sharding.ShardRegion;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

class EntityActor extends AbstractLoggingActor {
    private final ActorRef httpServer;
    private Entity entity;
    private String shardId;
    private String entityId;
    private final String memberId = Cluster.get(context().system()).selfMember().address().toString();
    private final FiniteDuration receiveTimeout = Duration.create(15, TimeUnit.SECONDS);

    EntityActor(ActorRef httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(EntityMessage.Command.class, this::command)
                .match(EntityMessage.Query.class, this::query)
                .matchEquals(ReceiveTimeout.getInstance(), t -> passivate())
                .build();
    }

    private void command(EntityMessage.Command command) {
        if (entity == null) {
            entity = command.entity;
            log().info("initialize {}", entity);

            sender().tell(new EntityMessage.CommandAck("initialize", command.entity), self());
            entityId = entity.id.id;
            shardId = EntityMessage.extractShardIdFromCommands(command);
            notifyStart();
        } else {
            log().info("update {} {} -> {}", entity.id, command.entity.value, entity.value);
            entity.value = command.entity.value;
            sender().tell(new EntityMessage.CommandAck("update", command.entity), self());
        }
    }

    private void query(EntityMessage.Query query) {
        log().info("query {} -> {}", query, entity == null ? "(not initialized)" : entity);
        if (entity == null) {
            sender().tell(new EntityMessage.QueryAckNotFound(query.id), self());
            entityId = query.id.id;
            shardId = EntityMessage.extractShardIdFromCommands(query);
            notifyStart();
        } else {
            sender().tell(new EntityMessage.QueryAck(entity), self());
        }
    }

    private void notifyStart() {
        EntityMessage.Action start = new EntityMessage.Action(memberId, shardId, entityId, "start", true);
        httpServer.tell(start, self());
    }

    private void notifyStop() {
        EntityMessage.Action stop = new EntityMessage.Action(memberId, shardId, entityId, "stop", true);
        httpServer.tell(stop, self());
    }

    private void passivate() {
        context().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), self());
    }

    @Override
    public void preStart() {
        log().info("Start");
        context().setReceiveTimeout(receiveTimeout);
    }

    @Override
    public void postStop() {
        notifyStop();
        log().info("Stop shard {}, entity {}", shardId, entityId);
    }

    static Props props(ActorRef httpServer) {
        return Props.create(EntityActor.class, httpServer);
    }
}
