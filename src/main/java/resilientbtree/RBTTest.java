package resilientbtree;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by sohaib on 07/12/16.
 */
public class RBTTest {

    public static void printTree(RedBlackTree rbt) {
        Iterator<RBTNode> itr = rbt.iterator();
        while (itr.hasNext()) {
            RBTNode node = itr.next();
            System.out.println(node.key + " : " + node.value);
        }
    }

    public static void main(String args[]) throws RBTDeserializationException {
        RedBlackTree tree = new RedBlackTree();
        int[] input = {2,3,11,14,15,10,8,7,4,13};
        for (int i = 0; i< input.length; i++) {
            tree.put(input[i], i);
            //System.out.println(input[i] + " : " + i);
            System.out.println("Size is: " + tree.size());
        }
        System.out.println("===========================");
        System.out.println("Printing Tree:");
        printTree(tree);
        System.out.println("===========================");
        System.out.println("TreeSize:" + tree.size());
        System.out.println("Reconstructing...");
        RedBlackTree treeReconstructed = RedBlackTree.deserialize(tree.serialize());
        System.out.println("===========================");
        System.out.println("Printing Tree:");
        printTree(treeReconstructed);
        System.out.println("===========================");
        System.out.println("TreeSize:" + treeReconstructed.size());
    }
}
