package resilientbtree;

import java.nio.ByteBuffer;

/**
 * Created by sohaib on 07/12/16.
 */

public class RBTNode {
    int key;
    int value;
    int size; //Length of subtree below this node
    RedBlackTree.Color color;
    resilientbtree.RBTNode left;
    resilientbtree.RBTNode right;
    public static final int SERIALIZED_SIZE = 13;

    RBTNode(int key, int value, RedBlackTree.Color color) {
        this.key = key;
        this.value = value;
        this.color = color;
        this.size = 1;
        this.left = null;
        this.right = null;
    }

    /**
     *
     * Converts a tree rooted at this this node to its byte form using pre-order traversal
     * The size of the byte array for one node is 4(key) + 4(value) + 4(size) + 1(color) = 13 bytes
     * This function is not recursive.
     **/
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(size * SERIALIZED_SIZE);
        byte colorEncoded = color == RedBlackTree.Color.BLACK ? (byte)0x00 : (byte)0x01;
        // Encode the current node
        buffer.putInt(key).putInt(value).putInt(size).put(colorEncoded);
        if (this.left != null)
            buffer.put(this.left.serialize());
        if (this.right != null)
            buffer.put(this.right.serialize());
        return buffer.array();
    }

    public static RBTNode deserialize(byte[] stream) throws RBTDeserializationException {
        if (stream.length == 0) {
            return null;
        } else if (stream.length % SERIALIZED_SIZE != 0){
            throw new RBTDeserializationException(
                    "Byte stream must be a perfect multiple of " + SERIALIZED_SIZE
            );
        }
        //Read the root
        ByteBuffer buffer = ByteBuffer.wrap(stream);
        return deserialize(buffer, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static RBTNode deserialize(ByteBuffer stream) throws RBTDeserializationException {
        if (stream.capacity() == 0) {
            return null;
        } else if ((stream.limit() - stream.position()) % SERIALIZED_SIZE != 0) {
            throw new RBTDeserializationException(
                    "Byte stream must be a perfect multiple of " + SERIALIZED_SIZE
            );
        }
        //Read the root
        return deserialize(stream, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static RBTNode deserialize(ByteBuffer buffer, int minVal, int maxVal) {
        if (buffer.hasRemaining()) {
            int key = buffer.getInt();
            int value = buffer.getInt();
            int size = buffer.getInt();
            byte colorEncoded = buffer.get();
            RedBlackTree.Color color =
                    colorEncoded == (byte)0x00 ? RedBlackTree.Color.BLACK : RedBlackTree.Color.RED;
            if (key >= minVal && key < maxVal) {
                resilientbtree.RBTNode newRBTNode = new resilientbtree.RBTNode(key, value, color);
                newRBTNode.size = size;
                newRBTNode.left = deserialize(buffer, minVal, key);
                newRBTNode.right = deserialize(buffer, key, maxVal);
                return newRBTNode;
            } else {
                buffer.position(buffer.position() - SERIALIZED_SIZE);
                return null;
            }
        } else {
            return null;
        }
    }
}
