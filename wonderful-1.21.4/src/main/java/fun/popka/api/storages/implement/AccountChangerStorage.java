package fun.popka.api.storages.implement;

import com.google.gson.*;
import fun.popka.Popka;
import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AccountChangerStorage {

    private static final String FILE_NAME = "account_changer.json";

    @Getter private final List<String> nicknames = new ArrayList<>();
    @Getter private String activeNickname = "";

    public AccountChangerStorage() {
        load();
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    public void add(String nickname) {
        if (nickname == null) return;
        String trimmed = nickname.trim();
        if (trimmed.isEmpty()) return;
        if (containsIgnoreCase(trimmed)) return;
        nicknames.add(trimmed);
        save();
    }

    public void remove(String nickname) {
        if (nickname == null) return;
        boolean removed = nicknames.removeIf(n -> n.equalsIgnoreCase(nickname));
        if (removed) {
            if (nickname.equalsIgnoreCase(activeNickname)) {
                activeNickname = "";
            }
            save();
        }
    }

    public void setActive(String nickname) {
        if (nickname == null) return;
        if (!containsIgnoreCase(nickname)) return;
        activeNickname = nicknames.stream()
                .filter(n -> n.equalsIgnoreCase(nickname))
                .findFirst()
                .orElse(nickname);
        save();
    }

    public String getActive() {
        return activeNickname == null ? "" : activeNickname;
    }

    public boolean isActive(String nickname) {
        if (nickname == null || activeNickname == null) return false;
        return nickname.equalsIgnoreCase(activeNickname);
    }

    public void clear() {
        nicknames.clear();
        activeNickname = "";
        save();
    }

    private boolean containsIgnoreCase(String nickname) {
        if (nickname == null) return false;
        for (String n : nicknames) {
            if (n.equalsIgnoreCase(nickname)) return true;
        }
        return false;
    }

    private File getFile() {
        return new File(Popka.INSTANCE.globalsDir, FILE_NAME);
    }

    public void save() {
        try {
            File file = getFile();
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            JsonObject object = new JsonObject();
            JsonArray array = new JsonArray();
            for (String n : nicknames) {
                array.add(n);
            }
            object.add("nicknames", array);
            object.addProperty("active", activeNickname == null ? "" : activeNickname);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void load() {
        try {
            File file = getFile();
            if (!file.exists()) return;
            JsonObject object;
            try (InputStream stream = Files.newInputStream(Paths.get(file.getAbsolutePath()));
                 Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                object = JsonParser.parseReader(reader).getAsJsonObject();
            }

            nicknames.clear();
            if (object.has("nicknames")) {
                for (JsonElement element : object.get("nicknames").getAsJsonArray()) {
                    String n = element.getAsString();
                    if (n != null && !n.isEmpty() && !containsIgnoreCase(n)) {
                        nicknames.add(n);
                    }
                }
            }
            if (object.has("active")) {
                activeNickname = object.get("active").getAsString();
                if (activeNickname == null) activeNickname = "";
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
