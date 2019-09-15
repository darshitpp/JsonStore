package com.darshit.operations;

import com.darshit.data.Data;
import com.darshit.exception.JsonStoreException;

public interface JsonOperations {
    boolean create(Data data, String path) throws JsonStoreException;
    Data read(String key, String path) throws JsonStoreException;
    boolean delete(String key, String path) throws JsonStoreException;
}
