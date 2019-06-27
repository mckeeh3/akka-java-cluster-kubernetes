package cluster.sharding;

import org.junit.Assert;
import org.junit.Test;

public class StatisticsTest {
    @Test
    public void t() {
        HttpServerActor.Statistics statistics = new HttpServerActor.Statistics(121, 1000);

        System.out.println(statistics.toJson());

        statistics.statistics.size();
        Assert.assertEquals(statistics.statisticCount, statistics.statistics.size());
        Assert.assertEquals(statistics.intervalTimeMillis * (statistics.statisticCount - 1),
                statistics.statistics.get(0).time - statistics.statistics.get(statistics.statisticCount - 1).time);
    }
}
