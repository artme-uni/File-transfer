package ru.nsu.g.akononov.fileReceiver;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;

public class SpeedTester {

    private static final long SPEED_UPDATE_PERIOD = 1000L;
    private Date lastSpeedUpdate;
    private long previousFileSize;
    private final Date start;
    private final UploadingFile file;

    public SpeedTester(UploadingFile file) {
        this.file = file;
        start = new Date();
        lastSpeedUpdate = start;
    }

    public void check() {
        Date now = new Date();

        long currentPeriod = now.getTime() - lastSpeedUpdate.getTime();
        if (currentPeriod > SPEED_UPDATE_PERIOD) {
            
            long averageSpeed = file.getCurrentSize() * 1000 / (now.getTime() - start.getTime());
            long difference = file.getCurrentSize() - previousFileSize;
            long currentSpeed = difference * 1000 / currentPeriod;

            System.out.println("[" + file.getName() + "] " + "Average speed: " + readableByteCount(averageSpeed) + " /sec");
            System.out.println("[" + file.getName() + "] " + "Current speed: " + readableByteCount(currentSpeed) + " /sec");

            previousFileSize = file.getCurrentSize();
            lastSpeedUpdate = now;
        }
    }

    public static String readableByteCount(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}
