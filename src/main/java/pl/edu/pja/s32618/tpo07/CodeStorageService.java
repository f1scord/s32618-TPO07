package pl.edu.pja.s32618.tpo07;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class CodeStorageService {

    private final Map<String, SavedCode> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Path storageDir = Paths.get("./saved-codes");

    public CodeStorageService() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    public void save(SavedCode code) {
        storage.put(code.getId(), code);

        Path file = storageDir.resolve(code.getId() + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize code with id: " + code.getId(), e);
        }

        long delaySeconds = Duration.between(LocalDateTime.now(), code.getExpiresAt()).toSeconds();
        if (delaySeconds > 0) {
            scheduler.schedule(() -> delete(code.getId()), delaySeconds, TimeUnit.SECONDS);
        } else {
            delete(code.getId());
        }
    }

    public Optional<SavedCode> findById(String id) {
        SavedCode code = storage.get(id);
        if (code != null) {
            if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
                delete(id);
                return Optional.empty();
            }
            return Optional.of(code);
        }

        Path file = storageDir.resolve(id + ".dat");
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            code = (SavedCode) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return Optional.empty();
        }

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            delete(id);
            return Optional.empty();
        }

        storage.put(id, code);
        return Optional.of(code);
    }

    public void delete(String id) {
        storage.remove(id);
        Path file = storageDir.resolve(id + ".dat");
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }
}
