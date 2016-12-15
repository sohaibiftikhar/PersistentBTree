package resilientbtree;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by sohaib on 06/12/16.
 */


/**
 * This class implements a red black tree that can store mappings of numbers sorted by key
 * Guaranteed performance of log(n) for all put/get/delete
 * This uses a left leaning Red Black tree suggested by Robert Sedgewick in his book Algorithms
 */
public class RedBlackTree implements Serializable, Iterable<RBTNode> {

    private RBTNode root = null;

    enum Color {
        RED, BLACK
    }

    //Tries to find a key in the subtree rooted at node
    private RBTNode _get(RBTNode root, int key) {
        if(root == null) {
            return null;
        } else {
            while (root != null && root.key != key) {
                if (root.key > key) {
                    root = root.left;
                } else {
                    root = root.right;
                }
            }
            return root;
        }
    }

    /**
     * Gets the node with the minimum key rooted at root
     **/
    private RBTNode getMin(RBTNode root) {
        while(root.left != null) {
            root = root.left;
        }
        return root;
    }

    /**
     * Gets the node with the maximum key rooted at root
     **/
    private RBTNode getMax(RBTNode root) {
        while(root.right != null) {
            root = root.right;
        }
        return root;
    }

    //Tries to find a key in the subtree rooted at node
    private RBTNode _getLower(RBTNode root, int key) {
        if(root == null) {
            return null;
        } else {
            if (root.key == key) {
                return root;
            } else if (root.key > key){ // If key is lesser then it is in the left subtree
                return _getLower(root.left, key);
            } else { // if key is greater then it is either in the right subtree or the current node is the lowest
                RBTNode lower = _getLower(root.right, key);
                return lower == null ? root : lower;
            }
        }
    }

    private RBTNode _put(RBTNode root, int key, int value) {
        if(root == null) {
           return new RBTNode(key, value, Color.RED);
        } else {
            int cmp = key - root.key;
            if (cmp < 0) {
                root.left = _put(root.left, key, value);
            } else if (cmp > 0) {
                root.right = _put(root.right, key, value);
            } else {
                root.value = value;
            }
            //Fix imbalance
            if (isRed(root.right) && !isRed(root.left)) {
                root = rotateLeft(root);
            } else if (isRed(root.left) && isRed(root.left.left)) {
                root = rotateRight(root);
            } else if (isRed(root.left) && isRed(root.right)) {
                flipColors(root);
            }
            root.size = size(root.right) + size(root.left) + 1;
            return root;
        }
    }

    private RBTNode _delete(RBTNode root, int key) {
        if(key < root.key) {
            if (!isRed(root.left) && !isRed(root.left.left)) {
                root = moveRedLeft(root);
            }
            root = _delete(root.left, key);
        } else {
            if(isRed(root.left)) {
                root = rotateRight(root);
                if (key == root.key && root.right == null) {
                    return null;
                }
                if (!isRed(root.right) && !isRed(root.right.left)) {
                    root = moveRedRight(root);
                }
                if (key == root.key) {
                    RBTNode x = min(root.right);
                    root.key = x.key;
                    root.value = x.value;
                    root.right = _deleteMin(root.right);
                } else {
                    root.right = _delete(root.right, key);
                }
            }
        }
        return balance(root);
    }

    private RBTNode _deleteMin(RBTNode root) {
        if (root.left == null) {
            return null;
        }
        if (!isRed(root.left) && !isRed(root.left.left)) {
            root = moveRedLeft(root);
        }
        root.left = _deleteMin(root.left);
        return balance(root);
    }

    // Assuming that h is red and both h.left and h.left.left
    // are black, make h.left or one of its children red.
    private RBTNode moveRedLeft(RBTNode t) {
        flipColors(t);
        if(isRed(t.right.left)) {
            t.right = rotateRight(t.right);
            t = rotateLeft(t);
            flipColors(t);
        }
        return t;
    }

    // Assuming that h is red and both h.right and h.right.left
    // are black, make h.right or one of its children red.
    private RBTNode moveRedRight(RBTNode t) {
        flipColors(t);
        if(isRed(t.left.left)) {
            t = rotateRight(t);
            flipColors(t);
        }
        return t;
    }

    private RBTNode balance(RBTNode t) {
        if (isRed(t.right)) {
            t = rotateLeft(t);
        }
        if (isRed(t.left) && isRed(t.left.left)) {
            t = rotateRight(t);
        }
        if (isRed(t.left) && isRed(t.right)) {
            flipColors(t);
        }
        t.size = size(t.right) + size(t.left) + 1;
        return t;
    }

    private RBTNode min(RBTNode t) {
        return (t.left == null) ? t : min(t.left);
    }

    private RBTNode max(RBTNode t) {
        return (t.right == null) ? t : max(t.right);
    }

    private RBTNode rotateRight(RBTNode t) {
        RBTNode l = t.left;
        t.left = l.right;
        l.right = t;
        l.color = l.right.color;
        l.right.color = Color.RED;
        l.size = t.size;
        t.size = size(t.left) + size(t.right) + 1;
        return l;
    }

    private RBTNode rotateLeft(RBTNode t) {
        RBTNode r = t.right;
        t.right = r.left;
        r.left = t;
        r.color = r.left.color;
        r.left.color = Color.RED;
        r.size = t.size;
        t.size = size(t.left) + size(t.right) + 1;
        return r;
    }

    private void flipColors(RBTNode t) {
        t.color = t.color == Color.RED ? Color.BLACK : Color.RED;
        t.right.color = t.right.color == Color.RED ? Color.BLACK : Color.RED;
        t.left.color = t.right.color == Color.RED ? Color.BLACK : Color.RED;
    }

    private int size(RBTNode t) {
        return t == null ? 0 : t.size;
    }

    private boolean isRed(RBTNode t) {
        return t != null && t.color == Color.RED;
    }

    public void put(int key, int value) {
        // Error Checking?
        root = _put(root, key, value);
    }

    // Returns the value associated with this key in this map else returns null
    public Integer get(int key) {
        RBTNode result = _get(root, key);
        return result == null ? null : result.value;
    }

    /**
     * Gets the minimum key and value in this RBTree
     * @return
     */
    public Tuple<Integer, Integer> min() {
        if (root != null) {
            RBTNode min = getMin(root);
            if (min != null) {
                return new Tuple<>(min.key, min.value);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the maximum key and value in this RBTree
     */
    public Tuple<Integer, Integer> max() {
        if (root != null) {
            RBTNode max = getMax(root);
            if (max != null) {
                return new Tuple<>(max.key, max.value);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets a key less than or equal to the current key or null otherwise
     **/

    public Integer getLower(int key) {
        RBTNode result = _getLower(root, key);
        return result == null ? null : result.value;
    }

    public boolean delete(int key) {
        RBTNode toDelete = _get(root, key);
        if (toDelete != null) {
            // If both children are black turn root as red
            if(!isRed(root.left) && !isRed(root.right)) {
                root.color = Color.RED;
            }
            root = _delete(root, key);
            if(root != null) {
                root.color = Color.BLACK;
            }
            return true;
        } else {
            // RBTNode was not found return false
            return false;
        }
    }

    public int size() {
        return (root == null) ? 0 : root.size;
    }

    public Iterator<RBTNode> iterator() {
        Queue<RBTNode> queue = new LinkedList<RBTNode>();
        getInRange(root, queue, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return queue.iterator();
    }

    public void getInRange(RBTNode root, Queue<RBTNode> queue, int rangeStart, int rangeEnd) {
        if(root == null) return;
        if (root.key > rangeStart) {
            getInRange(root.left, queue, rangeStart, root.key);
        }
        if (root.key >= rangeStart && root.key <=rangeEnd) {
            queue.add(root);
        }
        if (root.key < rangeEnd) {
            getInRange(root.right, queue, root.key, rangeEnd);
        }
    }

    public byte[] serialize() {
        if (root == null) {
            return new byte[0];
        } else {
            return root.serialize();
        }
    }

    public static RedBlackTree deserialize(byte[] serialized) throws RBTDeserializationException {
        RedBlackTree rbTree = new RedBlackTree();
        rbTree.root = RBTNode.deserialize(serialized);
        return rbTree;
    }

    public static RedBlackTree deserialize(ByteBuffer serialized) throws RBTDeserializationException {
        RedBlackTree rbTree = new RedBlackTree();
        rbTree.root = RBTNode.deserialize(serialized);
        return rbTree;
    }

}