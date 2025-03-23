package com.azlan.mindcare;

public class JournalEntry {
    private String id;
    private String content;
    private long timestamp; // Ensure timestamp is included

    // Default constructor for Firebase
    public JournalEntry() {}

    public JournalEntry(String id, String content, long timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}
