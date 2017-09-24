package pega.io;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

@RunWith(MockitoJUnitRunner.class)
public class FileOutputProviderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File file;

    @Before
    public void setUp() throws IOException {
        file = folder.newFile("test");
    }

    @Test
    public void testWriteToEmptyFileWithoutAppend() throws IOException {
        int[] input = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        new FileOutputProvider(file)
            .write(input);

        assertArrayEquals(input, readFile());
    }

    @Test
    public void testWriteToExistingFile() throws IOException {
        int[] input = {1, 2, 3};
        fillFile();

        new FileOutputProvider(file)
            .write(input);

        assertArrayEquals(input, readFile());
    }

    private void fillFile() throws IOException {
        try (RandomAccessFile outputFile = new RandomAccessFile(
            file,
            "rw"
        )) {
            for (int i = 1; i <= 5; i++) {
                outputFile.writeInt(i);
            }
        }
    }

    private int[] readFile() throws IOException {
        int[] result = new int[20];
        int index = 0;

        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            while (true) {
                result[index++] = input.readInt();
            }
        } catch (EOFException ignored) {
        }

        return Arrays.copyOf(result, index - 1);
    }
}