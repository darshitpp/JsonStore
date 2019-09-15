package com.darshit.operations;

import com.darshit.data.Data;
import com.darshit.exception.JsonStoreException;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonClientOperations implements JsonOperations {

    private Set<String> index = new HashSet<>();
    private Gson gson = new Gson();

    private Set<String> loadIndex(String path) throws JsonStoreException {
        String indexPath = path + ".idx";
        try (Stream<String> indexStream = Files.lines(Paths.get(indexPath))) {
            index = indexStream.collect(Collectors.toSet());
        } catch (IOException e) {
        }
        return index;
    }

    private void insertIntoIndex(String key, String path) throws JsonStoreException {
        try {
            Files.write(Paths.get(path + ".idx"), (key + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            index = loadIndex(path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new JsonStoreException(e.getMessage(), e.getCause());
        }
    }

    public boolean create(Data data, String jsonPath) throws JsonStoreException {
        String key = data.getKey();
        if (!jsonPath.endsWith(".json")) {
            jsonPath += ".json";
        }
        Path path = Paths.get(jsonPath);
        loadIndex(jsonPath);
        if (index.add(key)) {
            insertIntoIndex(key, jsonPath);
            try {
                Files.write(path, (gson.toJson(data) + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                throw new JsonStoreException(e.getMessage(), e.getCause());
            }
        }
        return false;
    }

    public Data read(String key, String path) throws JsonStoreException {
        Data data = null;
        Data removeData = null;
        path = path.endsWith(".json") ? path : path + ".json";
        boolean found = false;
        boolean remove = false;
        if (!index.isEmpty() && index.contains(key)) {
            try (FileInputStream in = new FileInputStream(path);
                 InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                 JsonReader jsonReader = new JsonReader(inputStreamReader);
            ) {
                jsonReader.setLenient(true);
                Data streamData;
                while (jsonReader.hasNext() && !found) {
                    streamData = gson.fromJson(jsonReader, Data.class);
                    if (key.equals(streamData.getKey())) {
                        if (LocalDateTime.now().isBefore(streamData.getExpireAt())) {
                            data = streamData;
                        } else {
                            index.remove(key);
                            removeData = streamData;
                            remove = true;
                        }
                        found = true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (remove) {
            deleteKey(removeData, path);
        }
        return data;
    }

    private boolean deleteKey(Data removeData, String path) {
        try {
            Path out = Paths.get(path + ".tmp");
            Path input = Paths.get(path);
            String dataToReplace = gson.toJson(removeData);
            replaceStringIntoNewFile(dataToReplace, out, input);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean deleteKeyFromIndex(String key, String path) {
        try {
            Path out = Paths.get(path + ".tmp");
            Path input = Paths.get(path);
            replaceStringIntoNewFile(key, out, input);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void replaceStringIntoNewFile(String key, Path out, Path input) throws IOException {
        Files.lines(input)
                .map(line -> line.replaceAll(Pattern.quote(key), ""))
                .forEach(line -> {
                    writeToTempFile(out, line);
                });
        Files.move(out, input, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeToTempFile(Path out, String line) {
        try {
            Files.write(out,
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete(String key, String path) throws JsonStoreException {
        if (!index.isEmpty() && index.contains(key)) {
            Data data = read(key, path);
            index.remove(key);
            deleteKeyFromIndex(key, path);
            return deleteKey(data, path);
        }
        return false;
    }
}
