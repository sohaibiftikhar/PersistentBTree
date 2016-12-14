package resilientbtree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Arrays;

/**
 * Created by sohaib on 08/12/16.
 */
public class IOHandler {
    InputStream iStream;
    OutputStream oStream;
    RandomAccessFile randomAccessFile;
    MappedByteBuffer mappedBuffer;
    int batchSize;

    public IOHandler(String fileName, int batchSize) throws IOException {
        Path filePath = FileSystems.getDefault().getPath(fileName);
        if (Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS)) {
            Files.createFile(filePath);
        }
        randomAccessFile = new RandomAccessFile(filePath.toFile(), "rw");
        //fileChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
        // TODO: Eventually we may need an array of these buffers cause this buffer runs out at 2GB of data
        mappedBuffer =
                randomAccessFile
                        .getChannel()
                        .map(
                            FileChannel.MapMode.READ_WRITE,
                            0,
                            randomAccessFile.length()
                        );
        this.batchSize = batchSize;
    }

    public byte[] readBatch(int offset) throws IOException {
        //System.out.println("Reading from" + offset);
        return read(batchSize, offset);
    }

    /**
     * Puts a batch in the buffer. Returns the index in the file where it wrote the batch.
     * @param bytes
     * @param offset
     * @return index on which this batch was written in the file
     * @throws IOException
     */
    public int writeBatch(byte[] bytes, int offset) throws IOException {
        if (bytes.length <= batchSize) {
            return write(bytes, batchSize, offset);
        } else {
            throw new IOException("Cannot write more than a batch");
        }
    }

    public int writeBatch(byte[] bytes) throws IOException {
        //System.out.println("Writing at index: " + randomAccessFile.length());
        return writeBatch(bytes, (int)randomAccessFile.length());
    }

    public byte[] read(int length, int offset) {
        byte[] bytes = new byte[length];
        // Initialize with zeroes
        Arrays.fill(bytes, (byte) 0x00);
        mappedBuffer.position(offset);
        // Check if there is anything?
        if (mappedBuffer.remaining() >= length) {
            mappedBuffer.get(bytes, 0, length);
        } else {
            mappedBuffer.get(bytes, 0, mappedBuffer.remaining());
        }
        return bytes;
    }

    public int write(byte[] bytes, int length, int offset) throws IOException {
        //System.out.println("Writing on offset");
        if ((offset + length) > randomAccessFile.length()) {
            // Increase the file length if it is small
            randomAccessFile.setLength(offset + length);
            mappedBuffer =
                    randomAccessFile
                            .getChannel()
                            .map(
                                FileChannel.MapMode.READ_WRITE,
                                0,
                                randomAccessFile.length()
                            );
        }
        mappedBuffer.position(offset);
        mappedBuffer.put(bytes, 0, bytes.length);

        for (int i =0; i < length - bytes.length; i++) {
            // Put zeroes where batch size is not complete
            mappedBuffer.put((byte)(0x00));
        }
        return mappedBuffer.position() - length; // same as offset
    }

    public int append(byte[] bytes) throws IOException {
        int limit = (int)randomAccessFile.length();
        randomAccessFile.setLength(randomAccessFile.length() + bytes.length);
        mappedBuffer =
                randomAccessFile
                        .getChannel()
                        .map(
                                FileChannel.MapMode.READ_WRITE,
                                0,
                                randomAccessFile.length()
                        );
        mappedBuffer.position(limit);
        mappedBuffer.put(bytes);
        return limit;
    }

    public void close() throws IOException {
        mappedBuffer.force();
        randomAccessFile.close();
    }

    public int fileLength() {
        try {
            return (int)randomAccessFile.length();
        } catch(IOException io) {
            return 0;
        }
    }
}
