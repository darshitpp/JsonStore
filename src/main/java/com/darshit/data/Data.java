package com.darshit.data;

import com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.util.Date;

public class Data {
    private String key;
    private JsonObject value;
    private int ttl;
    private LocalDateTime expireAt;

    private Data(String key, JsonObject value, int ttl) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
        this.expireAt = ttl == 0 ? LocalDateTime.MAX : LocalDateTime.now().plusSeconds((long) ttl);
    }

    public static class Builder {
        private String key;
        private JsonObject value;
        private int ttl = 0;
        private Date expireAt;

        public Builder store(String key, JsonObject value) {
            this.key = key;
            this.value = value;
            return this;
        }

        public Builder ttl(int ttl) {
            this.ttl = ttl;
            return this;
        }

        public Data build() {
            return new Data(this.getKey(), this.getValue(), this.getTtl());
        }

        private String getKey() {
            return key;
        }

        private JsonObject getValue() {
            return value;
        }

        private int getTtl() {
            return ttl;
        }
    }

    public String getKey() {
        return key;
    }

    public JsonObject getValue() {
        return value;
    }

    public int getTtl() {
        return ttl;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

}
