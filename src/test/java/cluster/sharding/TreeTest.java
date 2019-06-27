package cluster.sharding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Assert;
import org.junit.Test;

public class TreeTest {
    @Test
    public void findExistingEntityInTree() {
        HttpServerActor.Tree tree = testTree();

        Assert.assertNotNull(tree.find("entity36", "entity"));
    }

    @Test
    public void findExistingShardInTree() {
        HttpServerActor.Tree tree = testTree();

        Assert.assertNotNull(tree.find("shard11", "shard"));
    }

    @Test
    public void findExistingNodeInTree() {
        HttpServerActor.Tree tree = testTree();

        Assert.assertNotNull(tree.find("member3", "member"));
    }

    @Test
    public void treeNonExistingNodeNotInTree() {
        HttpServerActor.Tree tree = testTree();

        Assert.assertNull(tree.find("x", "member"));
    }

    @Test
    public void treeIsValidJson() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(testTree());

        Assert.assertNotNull(json);
    }

    @Test
    public void addToEmptyTree() {
        HttpServerActor.Tree tree = new HttpServerActor.Tree("cluster", "cluster");

        Assert.assertNull(tree.find("member1", "member"));
        Assert.assertNull(tree.find("shard01", "shard"));
        Assert.assertNull(tree.find("entity01", "entity"));

        tree.add("member1", "shard01", "entity01");

        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("shard01", "shard"));
        Assert.assertNotNull(tree.find("entity01", "entity"));

        tree.add("member1", "shard01", "entity01");

        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("shard01", "shard"));
        Assert.assertNotNull(tree.find("entity01", "entity"));

        tree.add("member1", "shard01", "entity02");

        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("shard01", "shard"));
        Assert.assertNotNull(tree.find("entity02", "entity"));

        tree.add("member2", "shard04", "entity12");

        Assert.assertNotNull(tree.find("member2", "member"));
        Assert.assertNotNull(tree.find("shard04", "shard"));
        Assert.assertNotNull(tree.find("entity12", "entity"));
    }

    @Test
    public void removeByMemberShardEntityWorksForExistingEntity() {
        HttpServerActor.Tree tree = testTree();

        tree.remove("member1", "shard01", "entity01");

        Assert.assertNull(tree.find("entity01", "entity"));
        Assert.assertNotNull(tree.find("shard01", "shard"));
        Assert.assertNotNull(tree.find("member1", "member"));

        tree.remove("member1", "shard01", "entity02");
        tree.remove("member1", "shard01", "entity03");

        Assert.assertNull(tree.find("entity02", "entity"));
        Assert.assertNull(tree.find("entity03", "entity"));
        Assert.assertNull(tree.find("shard01", "shard"));
        Assert.assertNotNull(tree.find("member1", "member"));

        tree.remove("member1", "shard02", "entity04");
        tree.remove("member1", "shard02", "entity05");
        tree.remove("member1", "shard02", "entity06");

        Assert.assertNull(tree.find("entity04", "entity"));
        Assert.assertNull(tree.find("entity05", "entity"));
        Assert.assertNull(tree.find("entity06", "entity"));
        Assert.assertNull(tree.find("shard02", "shard"));
        Assert.assertNotNull(tree.find("member1", "member"));

        tree.remove("member1", "shard03", "entity07");
        tree.remove("member1", "shard03", "entity08");
        tree.remove("member1", "shard03", "entity09");

        Assert.assertNull(tree.find("entity04", "entity"));
        Assert.assertNull(tree.find("entity05", "entity"));
        Assert.assertNull(tree.find("entity06", "entity"));
        Assert.assertNull(tree.find("shard02", "shard"));
        Assert.assertNull(tree.find("member1", "member"));
    }

    @Test
    public void removeEntityRemovesMultipleEntities() {
        HttpServerActor.Tree cluster = HttpServerActor.Tree.create("cluster", "cluster");
        cluster.add("member1", "shard1", "entity1");
        cluster.add("member1", "shard2", "entity1");
        cluster.add("member2", "shard2", "entity1");

        Assert.assertNotNull(cluster.find("entity1", "entity"));

        cluster.removeEntity("entity1");

        Assert.assertNull(cluster.find("entity1", "entity"));
    }

    @Test
    public void removeEntityRemovesDuplicateEntities() {
        HttpServerActor.Tree cluster = HttpServerActor.Tree.create("cluster", "cluster");
        cluster.add("member1", "shard1", "entity1");
        cluster.add("member1", "shard1", "entity1");

        cluster.removeEntity("entity1");

        Assert.assertNull(cluster.find("entity1", "entity"));
    }

    @Test
    public void setUnsetTypeWorks() {
        HttpServerActor.Tree tree = testTree();
        tree.setMemberType("member2", "singleton");
        Assert.assertNotNull(tree.find("member2", "singleton"));
        Assert.assertNotNull(tree.find("member2", "member"));

        tree.unsetMemberType("member2", "singleton");
        Assert.assertNotNull(tree.find("member2", "member"));
        Assert.assertNull(tree.find("member2", "singleton"));

        tree = HttpServerActor.Tree.create("cluster", "cluster");
        tree.setMemberType("member1", "singleton");
        tree.add("member1", "1", "1");
        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNull(tree.find("member1", "singleton"));

        tree.setMemberType("member1", "singleton");
        tree.add("member1", "1", "2");
        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("member1", "singleton"));

        tree.add("member2", "2", "3");
        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("member1", "singleton"));

        Assert.assertNotNull(tree.find("member2", "member"));
        Assert.assertNull(tree.find("member2", "singleton"));

        tree.setMemberType("member1", "singleton");
        tree.setMemberType("member1", "httpServer");
        tree.add("member1", "1", "2");
        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("member1", "singleton"));
        Assert.assertNotNull(tree.find("member1", "httpServer"));

        tree = HttpServerActor.Tree.create("cluster", "cluster");
        tree.add("member1", "1", "1");
        tree.add("member2", "2", "2");
        tree.add("member3", "2", "3");
        tree.setMemberType("member1", "singleton");
        tree.setMemberType("member1", "httpServer");
        Assert.assertNotNull(tree.find("member1", "httpServer"));
        tree.setMemberType("member3", "httpServer");
        Assert.assertNotNull(tree.find("member1", "member"));
        Assert.assertNotNull(tree.find("member1", "singleton"));
        Assert.assertNull(tree.find("member1", "httpServer"));
        Assert.assertNotNull(tree.find("member3", "member"));
        Assert.assertNotNull(tree.find("member3", "httpServer"));
    }

    @Test
    public void leafCountWorks() {
        HttpServerActor.Tree tree = HttpServerActor.Tree.create("cluster", "cluster");
        tree.add("member1", "shard1", "entity1");
        Assert.assertEquals(1, tree.leafCount());

        tree.add("member2", "2", "2");
        tree.add("member3", "2", "3");
        Assert.assertEquals(3, tree.leafCount());

        Assert.assertEquals(36, testTree().leafCount());
    }

    @Test
    public void eventCountWorks() {
        HttpServerActor.Tree tree = testTree();
        tree.incrementEvents("member1", "shard01", "entity01");
        tree.incrementEvents("member1", "shard01", "entity01");

        Assert.assertEquals(2, tree.eventsCount());

        tree.incrementEvents("member2", "shard04", "entity12");
        tree.incrementEvents("member3", "shard09", "entity27");

        Assert.assertEquals(4, tree.eventsCount());

        tree.incrementEvents("member2", "shard04", "entity12");
        tree.incrementEvents("member3", "shard09", "entity27");
        tree.incrementEvents("member3", "shard09", "entity27");

        Assert.assertEquals(7, tree.eventsCount());
    }

    @Test
    public void toJson() {
        String json = testTree().toJson();
        Assert.assertNotNull(json);
        System.out.println(json);
    }

    private static HttpServerActor.Tree testTree() {
        return HttpServerActor.Tree.create("cluster", "cluster")
                .children(
                        HttpServerActor.Tree.create("member1", "member")
                                .children(
                                        HttpServerActor.Tree.create("shard01", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity01", "entity"),
                                                        HttpServerActor.Tree.create("entity02", "entity"),
                                                        HttpServerActor.Tree.create("entity03", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard02", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity04", "entity"),
                                                        HttpServerActor.Tree.create("entity05", "entity"),
                                                        HttpServerActor.Tree.create("entity06", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard03", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity07", "entity"),
                                                        HttpServerActor.Tree.create("entity08", "entity"),
                                                        HttpServerActor.Tree.create("entity09", "entity")
                                                )
                                ),
                        HttpServerActor.Tree.create("member2", "member")
                                .children(
                                        HttpServerActor.Tree.create("shard04", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity10", "entity"),
                                                        HttpServerActor.Tree.create("entity11", "entity"),
                                                        HttpServerActor.Tree.create("entity12", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard05", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity13", "entity"),
                                                        HttpServerActor.Tree.create("entity14", "entity"),
                                                        HttpServerActor.Tree.create("entity15", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard06", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity16", "entity"),
                                                        HttpServerActor.Tree.create("entity17", "entity"),
                                                        HttpServerActor.Tree.create("entity18", "entity")
                                                )
                                ),
                        HttpServerActor.Tree.create("member3", "member")
                                .children(
                                        HttpServerActor.Tree.create("shard07", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity19", "entity"),
                                                        HttpServerActor.Tree.create("entity20", "entity"),
                                                        HttpServerActor.Tree.create("entity21", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard08", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity22", "entity"),
                                                        HttpServerActor.Tree.create("entity23", "entity"),
                                                        HttpServerActor.Tree.create("entity24", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard09", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity25", "entity"),
                                                        HttpServerActor.Tree.create("entity26", "entity"),
                                                        HttpServerActor.Tree.create("entity27", "entity")
                                                )
                                ),
                        HttpServerActor.Tree.create("member4", "member")
                                .children(
                                        HttpServerActor.Tree.create("shard10", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity28", "entity"),
                                                        HttpServerActor.Tree.create("entity29", "entity"),
                                                        HttpServerActor.Tree.create("entity30", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard11", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity31", "entity"),
                                                        HttpServerActor.Tree.create("entity32", "entity"),
                                                        HttpServerActor.Tree.create("entity33", "entity")
                                                ),
                                        HttpServerActor.Tree.create("shard12", "shard")
                                                .children(
                                                        HttpServerActor.Tree.create("entity34", "entity"),
                                                        HttpServerActor.Tree.create("entity35", "entity"),
                                                        HttpServerActor.Tree.create("entity36", "entity")
                                                )
                                )
                );

    }
}
