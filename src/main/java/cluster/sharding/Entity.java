package cluster.sharding;

import java.io.Serializable;
import java.util.Objects;

class Entity implements Serializable {
    final Id id;
    Value value;

    Entity(Id id, Value value) {
        this.id = id;
        this.value = value;
    }

    Entity(String id, Object value) {
        this(new Id(id), new Value(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id) &&
                Objects.equals(value, entity.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return String.format("%s[%s -> %s]", getClass().getSimpleName(), id, value);
    }

    static class Id implements Serializable {
        final String id;

        Id(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), id);
        }
    }

    static class Value implements Serializable {
        final Object value;

        Value(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", getClass().getSimpleName(), value);
        }
    }
}
