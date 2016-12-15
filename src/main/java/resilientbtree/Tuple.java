package resilientbtree;

/**
 * Created by sohaib on 17/11/16.
 */

/**
 * This class is a simple implementation of a 2 Tuple in a java.
 *
 * @param <X> The type of the first param in the tuple
 * @param <Y> The type in the second param of the tuple
 */
public class Tuple<X ,Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        if (x != null && y != null) {
            this.x = x;
            this.y = y;
        } else {
            throw new RuntimeException("Tuple values cannot be null");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Tuple.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Tuple other = (Tuple) obj;
        return other.x.equals(this.x) && other.y.equals(this.y);
    }

    @Override
    public int hashCode() {
        int hash = 13; // Some random prime number
        // We ensure that x and y of this class are non null
        hash = 37 * hash + (this.x.hashCode());
        hash = 37 * hash + (this.y.hashCode());
        return hash;
    }
}
