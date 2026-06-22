package com.syncstream.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileUtil {

    private static final String REPORTS_DIR = "reports/";

    public static String saveChatLog(int roomId, String logContent) {
        try {
            Files.createDirectories(Paths.get(REPORTS_DIR));  // make folder if needed

            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String filename = REPORTS_DIR + "chat_room" + roomId + "_" + timestamp + ".txt";

            Files.writeString(Paths.get(filename), logContent);
            System.out.println("[FileUtil] Chat log saved: " + filename);
            return filename;
        } catch (IOException e) {
            System.err.println("[FileUtil] saveChatLog error: " + e.getMessage());
            return null;
        }
    }

    public static String saveSessionReport(int roomId, String roomName,
                                           List<String> participantSummaries) {
        try {
            Files.createDirectories(Paths.get(REPORTS_DIR));

            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String filename = REPORTS_DIR + "session_room" + roomId + "_" + timestamp + ".txt";

            StringBuilder sb = new StringBuilder();
            sb.append("=== Session Report ===\n");
            sb.append("Room: ").append(roomName).append("\n");
            sb.append("Room ID: ").append(roomId).append("\n");
            sb.append("Generated: ").append(LocalDateTime.now()).append("\n");
            sb.append("\n--- Participants ---\n");
            for (String summary : participantSummaries) {
                sb.append(summary).append("\n");
            }

            Files.writeString(Paths.get(filename), sb.toString());
            System.out.println("[FileUtil] Session report saved: " + filename);
            return filename;
        } catch (IOException e) {
            System.err.println("[FileUtil] saveSessionReport error: " + e.getMessage());
            return null;
        }
    }
}
