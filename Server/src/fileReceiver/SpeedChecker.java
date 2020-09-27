package fileReceiver;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;

public class SpeedChecker {

    private static final long SPEED_UPDATE_PERIOD = 1000L;
    private Date lastSpeedUpdate;
    private long lastUploadedByteCount;
    private final Date start;

    private final String fileName;

    public SpeedChecker(String fileName) {
        this.fileName = fileName;
        start = new Date();
        lastSpeedUpdate = start;
    }

    public void checkSpeed(long uploadedBytesCount) {
        Date now = new Date();

        long currentPeriod = now.getTime() - lastSpeedUpdate.getTime();
        if (currentPeriod > SPEED_UPDATE_PERIOD) {
            long averageSpeed = uploadedBytesCount * 1000 / (now.getTime() - start.getTime());
            long difference = uploadedBytesCount - lastUploadedByteCount;
            long currentSpeed = difference * 1000 / currentPeriod;

            System.out.println("[" + fileName + "] " + "Average speed: " + readableByteCount(averageSpeed) + " /sec");
            System.out.println("[" + fileName + "] " + "Current speed: " + readableByteCount(currentSpeed) + " /sec");

            lastUploadedByteCount = uploadedBytesCount;
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
