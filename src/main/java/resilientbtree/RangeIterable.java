package resilientbtree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by sohaib on 15/12/16.
 */
public class RangeIterable implements Iterable<Tuple<Integer, byte[]>> {

    /**
     * The node passed along to this is the key which contains the key just higher or equal to the keyLower
     * @param node
     * @param keyLow
     * @param keyHigh
     * @param handler - The IO Handler user to read the next node
     */

    private BTreeNode startNode;
    private int rangeLow;
    private int rangeHigh;
    private IOHandler handler;
    private IOHandler valueHandler;

    public RangeIterable(BTreeNode node, int keyLow, int keyHigh, IOHandler handler, IOHandler valueHandler) {
        this.startNode = node;
        this.rangeLow = keyLow;
        this.rangeHigh = keyHigh;
        this.handler = handler;
        this.valueHandler = valueHandler;
    }

    @Override
    public Iterator<Tuple<Integer, byte[]>> iterator() {
        return new RangeIterator(startNode, rangeLow, rangeHigh, handler, valueHandler);
    }
}

class RangeIterator implements  Iterator<Tuple<Integer, byte[]>> {

    private BTreeNode current;
    private int rangeLow;
    private int rangeHigh;
    private Tuple<Integer, byte[]> currKV;
    private Iterator<RBTNode> itr = null;
    private IOHandler handler;
    private IOHandler valueHandler;

    RangeIterator(BTreeNode current, int rangeLow, int rangeHigh, IOHandler handler, IOHandler valueHandler) {
        this.current = current;
        this.handler = handler;
        this.valueHandler = valueHandler;
        this.rangeLow = rangeLow;
        this.rangeHigh = rangeHigh;
        if (this.current != null) {
            itr = this.current.rbTree.iterator();
            RBTNode node = null;
            while (itr.hasNext() && (node == null || node.key < rangeLow)) {
                node = itr.next();
            }
            if (node != null) {
                int len = ByteBuffer.wrap(valueHandler.read(4, node.value)).getInt();
                currKV = new Tuple<>(node.key, valueHandler.read(len, node.value+4));
            }
        }
    }
    @Override
    public void remove() {
        // This is unsupported. The iterator is immutable
    }

    @Override
    public boolean hasNext() {
        return currKV != null;
    }

    @Override
    public Tuple<Integer, byte[]> next() {
        if (currKV == null) {
            throw new NoSuchElementException("Range Iterator is empty");
        }
        Tuple<Integer, byte[]> toRet = currKV;
        RBTNode next = null;
        if (itr.hasNext()) {
            next = itr.next();
        } else { // Move to the next btreenode
            try {
                if (current.nextLeafPos > 0) {
                    current = BTreeNode.deSerialize(handler.readBatch(current.nextLeafPos), handler.batchSize);
                    itr = current.rbTree.iterator();
                    if (itr.hasNext()) {
                        next = itr.next();
                    }
                } else {
                    // Nothing more left
                    next = null;
                    current = null;
                }
            } catch (IOException e) {
                current = null;
                next = null; // Terminate iterator
            }
        }
        if (next != null && next.key <= rangeHigh) {
            int len = ByteBuffer.wrap(valueHandler.read(4, next.value)).getInt();
            currKV = new Tuple<>(next.key, valueHandler.read(len, next.value+4));
        } else {
            currKV = null;
        }
        return toRet;
    }

}
