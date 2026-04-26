package pl.edu.pja.s32618.tpo07;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SavedCode implements Serializable {

    private final String id;
    private final String originalCode;
    private final String formattedCode;
    private final LocalDateTime expiresAt;

    public SavedCode(String id, String originalCode, String formattedCode, LocalDateTime expiresAt) {
        this.id = id;
        this.originalCode = originalCode;
        this.formattedCode = formattedCode;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getFormattedCode() {
        return formattedCode;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
