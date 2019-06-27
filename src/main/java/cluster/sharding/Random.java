package cluster.sharding;

class Random {
    private static final java.util.Random random = new java.util.Random();

    static Entity.Id entityId(int from, int to) {
        return new Entity.Id(String.valueOf(inRange(from, to)));
    }

    private static int inRange(int from, int to) {
        return from + random.nextInt(to - from + 1);
    }
}
