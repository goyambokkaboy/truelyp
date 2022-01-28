package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.storage.Recording;
import com.ulyp.storage.RecordingListener;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmptyStorageReader implements StorageReader {

    @Override
    public CompletableFuture<ProcessMetadata> getProcessMetadata() {
        return new CompletableFuture<>();
    }

    @Override
    public void subscribe(RecordingListener listener) {

    }

    @Override
    public List<Recording> availableRecordings() {
        return Collections.emptyList();
    }

    @Override
    public void close() throws StorageException {

    }
}
