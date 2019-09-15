package com.darshit.client;

import com.darshit.data.Data;
import com.darshit.exception.JsonStoreException;
import com.darshit.operations.JsonClientOperations;

import java.util.UUID;

public class JsonStoreClient {

    private static JsonStoreClient jsonStoreClient = null;
    private String jsonPath;
    private static JsonClientOperations jsonClientOperations = null;

    private JsonStoreClient(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    private JsonStoreClient() {
        this("C:\\Users\\asus\\Desktop\\" + UUID.randomUUID() + ".json");
    }

    public static JsonStoreClient getJsonStoreClient() {
        synchronized (JsonStoreClient.class) {
            if (jsonStoreClient == null) {
                jsonStoreClient = new JsonStoreClient();
            }
        }
        synchronized (JsonStoreClient.class) {
            if (jsonClientOperations == null) {
                jsonClientOperations = new JsonClientOperations();
            }
        }
        return jsonStoreClient;
    }

    public static JsonStoreClient getJsonStoreClient(String jsonPath) {
        synchronized (JsonStoreClient.class) {
            if (jsonStoreClient == null) {
                jsonStoreClient = new JsonStoreClient(jsonPath);
            }
        }
        synchronized (JsonStoreClient.class) {
            if (jsonClientOperations == null) {
                jsonClientOperations = new JsonClientOperations();
            }
        }
        return jsonStoreClient;
    }

    public boolean create(Data data) throws JsonStoreException {
        return jsonClientOperations.create(data, jsonPath);
    }

    public Data read(String key) throws JsonStoreException {
        return jsonClientOperations.read(key, jsonPath);
    }

    public boolean delete(String key) throws JsonStoreException {
        return jsonClientOperations.delete(key, jsonPath);
    }
}
