package resilientbtree;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

/**
 * Created by sohaib on 12/12/16.
 */
public class BTreeTest {

    public static void main(String args[]) throws IOException {
        Random random = new Random();
        BTree bTree = new BTree("btree.dat", 5);
        String[] values =
                {
                        "apple",
                        "ball",
                        "cat",
                        "dog",
                        "elephant",
                        "frog",
                        "grass",
                        "Hund",
                        "ich",
                        "Jana",
                        "Kleid",
                        "lustig",
                        "mango",
                        "neunzehn",
                        "orange",
                        "proscuito",
                        "quid",
                        "rabbit",
                        "steffen",
                        "turtle",
                        "unterschied",
                        "versicherung",
                        "wartotle",
                        "xenophobia",
                        "yamcha",
                        "zeitung"
                };
        int offset = 0;
        /*for (int i = 0; i < 20; i++) {
            //int key = i + offset;
            int key = Math.abs(random.nextInt() % 20);
            //String value = new BigInteger(130, random).toString(random.nextInt(32));
            String value = values[key];
            System.out.println("Putting key: " + key + " value: " + value);
            bTree.put(key, value.getBytes());
        }*/
        for (int i = 0; i < 20; i++) {
            byte[] value = bTree.get(i);
            if (value != null && value.length > 0) {
                System.out.println("key: " + i + " value: " + new String(bTree.get(i)));
            } else {
                System.out.println("No value found for key: " + i);
            }
        }
        for (Tuple<Integer, byte[]> keyValue : bTree.getInRange(0, 20)) {
            System.out.println("Key: " + keyValue.x + " value:" + new String(keyValue.y));
        }

        //System.out.println("key: " + 7 + " value: " + new String(bTree.get(7)));
    }
}
