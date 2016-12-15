package resilientbtree;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by sohaib on 10/12/16.
 */
public class BTreeNode {
    final boolean isLeaf;
    RedBlackTree rbTree;
    int parent = -1;
    final int phi; // contains link to b tree node with value less the min key in the rbtree
    final int selfPosition;
    int nextLeafPos;

    static final int metadataSize = 21;

    BTreeNode(RedBlackTree rbTree, boolean isLeaf, int parent, int phi, int selfPosition, int nextLeafPos) {
        this.rbTree = rbTree;
        this.isLeaf = isLeaf;
        this.phi = phi;
        this.parent = parent;
        this.selfPosition = selfPosition;
        this.nextLeafPos = nextLeafPos;
    }

    public int getLower(int key) {
        Integer value = rbTree.getLower(key);
        return value == null ? phi : value;
    }

    public byte[] serialize() {
        int rbTreeSize = rbTree.size();
        // 4 bytes for size of RBTree plus
        // 4 bytes for pointer to parent plus
        // 4 bytes for pointer to phi node plus
        // 4 bytes for pointer to self plus
        // 1 byte for isLeaf - it is negative if rbTree is not a leaf
        ByteBuffer buffer = ByteBuffer.allocate(rbTreeSize * RBTNode.SERIALIZED_SIZE + metadataSize);
        buffer.put((byte)(isLeaf ? +1 : -1));
        buffer.putInt(rbTreeSize);
        buffer.putInt(parent);
        buffer.putInt(phi);
        buffer.putInt(selfPosition);
        buffer.putInt(nextLeafPos);
        buffer.put(rbTree.serialize());
        return buffer.array();
    }

    // The batchsize is the same as when read through
    public static BTreeNode deSerialize(byte[] bytes, int batchSize) throws IOException {
        if (bytes.length % RBTNode.SERIALIZED_SIZE != 4 || bytes.length == batchSize) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            boolean isLeaf = buffer.get() == ((byte) 1);
            int rbtSize = buffer.getInt();
            int parent = buffer.getInt();
            int phi = buffer.getInt();
            int selfPosition = buffer.getInt();
            int nextLeafPos = buffer.getInt();
            RedBlackTree rbt =
                    RedBlackTree.
                            deserialize(ByteBuffer.wrap(bytes, metadataSize, rbtSize * RBTNode.SERIALIZED_SIZE));
            return new BTreeNode(rbt, isLeaf, parent, phi, selfPosition, nextLeafPos);
        } else {
            throw new IOException("Error while deserializing BTreeNode");
        }
    }
}
