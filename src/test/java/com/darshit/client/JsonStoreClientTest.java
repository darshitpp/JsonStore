package com.darshit.client;

import com.darshit.data.Data;
import com.darshit.exception.JsonStoreException;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

class JsonStoreClientTest {

    @BeforeEach
    @AfterEach
    void setUp() {
        try {
            Files.delete(Paths.get("Path\\to\\file\\test.json"));
            Files.delete(Paths.get("Path\\to\\file\\test.json.idx"));
        } catch (IOException ignored) {
        }
    }

    @Test
    void create() throws JsonStoreException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("testCreateJson", "testJsonValue");
        Data data = new Data.Builder()
                .store("testCreate", jsonObject)
                .build();
        boolean b = jsonStoreClient.create(data);
        Assertions.assertTrue(b);
    }

    @Test
    void create_with_duplicate_key() throws JsonStoreException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("testCreateWithDuplicateJson", "testJsonValue");
        Data data = new Data.Builder()
                .store("testCreateWithDuplicate", jsonObject)
                .build();
        boolean b = jsonStoreClient.create(data);
        Assertions.assertTrue(b);

        jsonObject = new JsonObject();
        jsonObject.addProperty("testCreateWithDuplicateJson", "testJsonValue");
        data = new Data.Builder()
                .store("testCreateWithDuplicate", jsonObject)
                .build();
        b = jsonStoreClient.create(data);
        Assertions.assertFalse(b);
    }

    @Test
    void read_valid_key() throws JsonStoreException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("readValidJsonKey", "testJsonValue");
        Data data = new Data.Builder()
                .store("readValidKey", jsonObject)
                .build();
        boolean b = jsonStoreClient.create(data);
        Assertions.assertTrue(b);

        Data testKey = jsonStoreClient.read("readValidKey");
        Assertions.assertEquals(testKey.getKey(), data.getKey());
        Assertions.assertEquals(testKey.getValue(), data.getValue());
    }

    @Test
    void read_invalid_key() throws JsonStoreException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("readInvalidJsonKey", "testJsonValue");
        Data data = new Data.Builder()
                .store("readInvalidKey", jsonObject)
                .build();
        boolean b = jsonStoreClient.create(data);
        Assertions.assertTrue(b);

        Data testKey = jsonStoreClient.read("invalid");
        Assertions.assertNull(testKey);
    }

    @Test
    void read_expired_key() throws JsonStoreException, InterruptedException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("readExpiredJsonKey", "testJsonValue");
        Data data = new Data.Builder()
                .store("readExpiredKey", jsonObject)
                .ttl(5)
                .build();
        boolean b = jsonStoreClient.create(data);
        Assertions.assertTrue(b);

        TimeUnit.SECONDS.sleep(7);

        Data testKey = jsonStoreClient.read("readExpiredKey");
        Assertions.assertNull(testKey);
    }

    @Test
    void read_expired_and_non_expired_key() throws JsonStoreException, InterruptedException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("readExpiredAndNonExpiredJsonKey1", "testJsonValue");
        Data data = new Data.Builder()
                .store("readExpiredAndNonExpiredKey1", jsonObject)
                .ttl(5)
                .build();
        boolean b = jsonStoreClient.create(data);

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("readExpiredAndNonExpiredJsonKey2", "testJsonValue");
        Data data2 = new Data.Builder()
                .store("readExpiredAndNonExpiredKey2", jsonObject)
                .build();
        boolean c = jsonStoreClient.create(data2);
        Assertions.assertTrue(b);
        Assertions.assertTrue(c);

        TimeUnit.SECONDS.sleep(7);

        Data testKey = jsonStoreClient.read("readExpiredAndNonExpiredKey1");
        Assertions.assertNull(testKey);

        Data testKey2 = jsonStoreClient.read("readExpiredAndNonExpiredKey2");
        Assertions.assertEquals(data2.getKey(), testKey2.getKey());
        Assertions.assertEquals(data2.getValue(), testKey2.getValue());
    }

    @Test
    void delete() throws JsonStoreException {
        JsonStoreClient jsonStoreClient = JsonStoreClient.getJsonStoreClient("Path\\to\\file\\test.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("deleteJsonKey", "testJsonValue");
        Data data = new Data.Builder()
                .store("deleteKey", jsonObject)
                .build();
        boolean b = jsonStoreClient.create(data);
        Assertions.assertTrue(b);

        boolean deleted = jsonStoreClient.delete("deleteKey");
        Assertions.assertTrue(deleted);
    }
}