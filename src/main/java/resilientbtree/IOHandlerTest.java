package resilientbtree;

import java.io.IOException;

/**
 * Created by sohaib on 10/12/16.
 */
public class IOHandlerTest {

    public static void main(String args[]) throws IOException {
        IOHandler handler = new IOHandler("myFile", 5);
        int stringPosition1 = handler.writeBatch("mafia".getBytes());
        int stringPosition2 = handler.writeBatch("doner".getBytes());
        String read1 = new String(handler.readBatch(5));
        System.out.println(read1);

    }
}
