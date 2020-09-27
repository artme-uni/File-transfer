package fileReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadingFile {
    private File file;
    private FileOutputStream fileOutput;
    private long expectedSize;
    private long remainingByteCount;

    private static final String ROOT_PATH = "Server" + File.separator + "uploads" + File.separator;

    public void create(String fileName){
        try {
            createFile(fileName);
            fileOutput = new FileOutputStream(file.getPath());

        } catch (IOException e) {
            System.err.println("Cannot create file " + fileName);
            e.printStackTrace();
        }
    }

    public void closeOutputStream() throws IOException {
        fileOutput.flush();
        fileOutput.close();
    }

    public void write(byte[] buffer, int offset, int length) throws IOException {
        fileOutput.write(buffer, offset, length);
        remainingByteCount -= buffer.length;
    }

    public boolean isReady(){
        return remainingByteCount == 0;
    }

    public long getCurrentSize(){
        return expectedSize - remainingByteCount;
    }

    public String getName(){
        return file.getName();
    }

    public void setExpectedSize(long expectedSize) {
        this.expectedSize = expectedSize;
        remainingByteCount = expectedSize;
    }

    public long getRemainingByteCount() {
        return remainingByteCount;
    }

    private void createFile(String fileName) throws IOException {
        file = new File(ROOT_PATH + fileName);
        file.getParentFile().mkdirs();

        if (!file.createNewFile()) {
            file = addExtension(fileName, 1);
        }
    }

    private File addExtension(String fileName, int number) throws IOException {
        String[] splitName = fileName.split("\\.");
        splitName[splitName.length - 2] = splitName[splitName.length - 2] + number;
        StringBuilder newName = new StringBuilder();
        for (int i = 0; i < splitName.length; i++) {
            newName.append(splitName[i]);
            if (i != splitName.length - 1) {
                newName.append(".");
            }
        }
        File file = new File(ROOT_PATH + newName);
        if (file.createNewFile()) {
            return file;
        } else {
            number++;
            return addExtension(fileName, number);
        }
    }
}