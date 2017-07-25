package com.sysgears.service;

import com.sysgears.service.processor.IProcessableProcessor;
import com.sysgears.service.processor.processable.IProcessable;

import java.util.Iterator;

public class FileWorkerFactory {
    private final Iterator<IProcessable> splittable;
    private final IProcessableProcessor processor;

    public FileWorkerFactory(final Iterator<IProcessable> splittable, final IProcessableProcessor processor) {
        this.splittable = splittable;
        this.processor = processor;
    }

    public FileWorker create() {
        return splittable.hasNext() ? new FileWorker(splittable.next(), processor) : null;
    }
}