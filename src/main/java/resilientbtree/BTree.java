package resilientbtree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by sohaib on 08/12/16.
 */
public class BTree {

    // IO Handler for the index file
    IOHandler handler;
    // IO handler for the value file
    IOHandler valueHandler;
    BTreeNode root;
    int batchsize;
    int nodeSize;

    BTree(String fileName, int nodeSize) throws  IOException {
        // the 17 bytes are for BTreeNode information
        // See BTreeNode.java
        batchsize = nodeSize * RBTNode.SERIALIZED_SIZE + BTreeNode.metadataSize;
        this.nodeSize = nodeSize;
        handler = new IOHandler(fileName, batchsize);
        valueHandler = new IOHandler(fileName + "_values", batchsize);
        root = getRoot();
    }

    private RBTNode _putInternal(BTreeNode root, int key, int value) throws IOException {
        root.rbTree.put(key, value);
        RBTNode midNode = null;
        if (root.rbTree.size() > nodeSize) {
            // Need to split this node into two nodes;
            //creates a copy of the nodes
            int rbtSize = root.rbTree.size();
            Iterator<RBTNode> rbtIterator = root.rbTree.iterator();
            root.rbTree = new RedBlackTree();
            RedBlackTree rightRBT = new RedBlackTree();
            BTreeNode rightBTNode = new BTreeNode(rightRBT, root.isLeaf, root.parent, -1, handler.fileLength());
            for (int i=1; i <= rbtSize/2; i++) {
                RBTNode rbtNode = rbtIterator.next();
                root.rbTree.put(rbtNode.key, rbtNode.value);
            }
            for (int i = (rbtSize/2) + 1; i <= rbtSize; i++) {
                RBTNode rbtNode = rbtIterator.next();
                if (midNode == null) {
                    midNode = rbtNode;
                }
                rightBTNode.rbTree.put(rbtNode.key, rbtNode.value);
            }
            if (root.parent < 1) {
                // The next node to be written after the right node would be the parent of the node.
                root.parent = handler.fileLength() + batchsize;
                //System.out.println(root.parent);
                rightBTNode.parent = root.parent;
            }
            midNode.value = handler.writeBatch(rightBTNode.serialize());
        }
        handler.writeBatch(root.serialize(), root.selfPosition); // Persist change to disk
        return midNode; // Pass the middle node up if any
    }

    private BTreeNode getBTreeNodeAtIndex(int index) throws IOException {
        byte[] batch = handler.readBatch(index);
        return  BTreeNode.deSerialize(batch, batchsize);
    }

    // Put currently takes log(n) space on the stack. We can convert this to a loop but would need to
    // read each node twice in the worst case.
    private RBTNode put(BTreeNode root, int key, int value) throws IOException {
        //int valuePos = valueHandler.append(value);
        if (root.isLeaf) {
            return _putInternal(root, key, value); // Just put it like that for leaf
        } else { // For parents first let it go to child then pick up floated middle terms if any
            int valueLower = root.getLower(key);
            BTreeNode node = getBTreeNodeAtIndex(valueLower);
            RBTNode toPut = put(node, key, value);
            if (toPut != null) {
                return _putInternal(root, toPut.key, toPut.value);
            } else {
                return null;
            }
        }
    }

    private byte[] get(BTreeNode root, int key) throws IOException {
        if (root.isLeaf) {
            Integer valueInd = root.rbTree.get(key);
            if (valueInd == null) {
                return null;
            } else {
                int len = ByteBuffer.wrap(valueHandler.read(4, valueInd)).getInt();
                return valueHandler.read(len, valueInd+4);
            }
        } else {
            int valueLower = root.getLower(key);
            BTreeNode node = getBTreeNodeAtIndex(valueLower);
            return get(node, key);
        }
    }

    public void put(int key, byte[] value) throws IOException {
        int valueInd = valueHandler.append(ByteBuffer.allocate(4 + value.length).putInt(value.length).put(value).array());
        RBTNode toPut = put(root, key, valueInd);
        if (toPut != null) {
            BTreeNode newRoot = new BTreeNode(
                    new RedBlackTree(),
                    false,
                    -1,
                    root.selfPosition,
                    handler.fileLength()
            );
            newRoot.rbTree.put(toPut.key, toPut.value);
            int position = handler.writeBatch(newRoot.serialize());
            this.root = newRoot;
            // Write the root position at the beginning of the file
            handler.write(ByteBuffer.allocate(4).putInt(position).array(), 4, 0);
        }
    }

    public byte[] get(int key) throws IOException {
        return get(root, key);
    }

    public static BTree makeBTree() throws  IOException {
        String fileName = "btree.data";
        int nodeSize = 5;
        return new BTree(fileName, nodeSize);
    }

    public static BTree makeBTree(String fileName, int nodeSize) throws  IOException {
        return new BTree(fileName, nodeSize);
    }

    private BTreeNode getRoot() throws IOException {
        int rootAddr = ByteBuffer.wrap(handler.read(4, 0)).getInt();
        if (rootAddr > 0) {
            byte[] buffer = handler.readBatch(rootAddr);
            return BTreeNode.deSerialize(buffer, this.batchsize);
        } else {
            // The tree was empty so start fresh
            // The self position is four because the first 4 bytes [0,3] are taken up by the root address
            return new BTreeNode(new RedBlackTree(), true, -1, -1, 4);
        }
    }

}
