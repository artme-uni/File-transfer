package ru.nsu.g.akononov.fileReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadingFile implements AutoCloseable{
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

    private void createFile(String fileName) throws IOException {
        file = new File(ROOT_PATH + fileName);
        if(file.getParentFile().mkdirs()){
            System.out.println("Create new directory \"" + file.getParentFile() + "\" for uploading files");
        }

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

    public void setExpectedSize(long expectedSize) {
        this.expectedSize = expectedSize;
        remainingByteCount = expectedSize;
    }

    public boolean isReady(){
        return remainingByteCount == 0;
    }

    public boolean isCorrect(){
        return expectedSize == file.length();
    }

    public long getCurrentSize(){
        return expectedSize - remainingByteCount;
    }

    public String getName(){
        return file.getName();
    }


    @Override
    public void close() {
        try {
            fileOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] buffer, int i, int readByteCount) throws IOException {
        fileOutput.write(buffer, i, readByteCount);
        remainingByteCount -= readByteCount;
    }
}