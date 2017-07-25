package com.sysgears.ui.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.sysgears.io.IOHandler;
import com.sysgears.io.SyncWriteIO;
import com.sysgears.service.FileWorkerFactory;
import com.sysgears.service.processor.IOProcessor;
import com.sysgears.service.processor.IProcessableProcessor;
import com.sysgears.service.processor.processable.FileJoinFactory;
import com.sysgears.service.processor.processable.IProcessable;
import com.sysgears.service.processor.processable.IProcessableFactory;
import com.sysgears.service.processor.processable.FileChunkIterator;
import com.sysgears.statistic.AbstractRecordsHolder;
import com.sysgears.statistic.ConcurrentRecordsHolder;
import com.sysgears.statistic.Watcher;
import com.sysgears.ui.FileProcessor;
import com.sysgears.ui.IExecutable;
import com.sysgears.ui.ServiceRunner;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Iterator;

/**
 * A command to glue a parts into one file
 */
@Parameters(commandNames = "join", commandDescription = "Join parts into file")
public class CommandJoin implements IExecutable {
    /**
     * The path to the first chunk
     */
    @Parameter(names = "-p", required = true, description = "The first part absolute file path, i.e. " +
            "'-p C:/DIR/file.example.part1'")
    private String path;
    /**
     * The number of threads
     */
    @Parameter(names = "-t", description = "Number of threads")
    private int threadsNumber = FileProcessor.THREADS_NUMBER;
    /**
     * The statistic output delay
     */
    @Parameter(names = "-d", description = "Statistic output delay(ms)")
    private int delay = FileProcessor.DELAY;
    /**
     * The part prefix name
     */
    @Parameter(names = "-n", description = "Part prefix name")
    private String partPrefix = FileProcessor.PART_PREFIX;
    /**
     * The IO buffer size
     */
    @Parameter(names = "-b", description = "IO buffer size(bytes)")
    private int bufferSize = FileProcessor.BUFFER_SIZE;
    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(CommandJoin.class);

    /**
     * Joins parts of a file into a big one
     */
    @Override
    public void execute() {
        log.info("Starting a join command execution");
        log.info("File: " + path);
        final long chunkSize = new File(path).length();
        log.info("Chunk size: " + chunkSize);
        final int firstPartNumber = getFirstPartNumber(path);
        log.info("The first part number: " + firstPartNumber);
        final String joinedFileName = getJoinedFileName(path);
        log.info("The joined file name: " + joinedFileName);
        final long fileSize = countFinalFileSize(path, joinedFileName, firstPartNumber);
        log.info("The joined file size: " + fileSize);

        log.info("Creating IO handler: " + SyncWriteIO.class.getSimpleName() + " object");
        final IOHandler syncWriteIO = new SyncWriteIO();

        log.debug("Trying to create a RandomAccessFile, file name: " + joinedFileName);
        RandomAccessFile source;
        try {
            source = new RandomAccessFile(joinedFileName, "rw");
        } catch (FileNotFoundException e) {
            throw new ParameterException(joinedFileName + " doesn't exist.");
        }

        log.info("Creating " + IProcessableFactory.class.getSimpleName() + " object");
        IProcessableFactory processableFactory = new FileJoinFactory();

        log.info("Creating " + FileChunkIterator.class.getSimpleName() + " object");
        final Iterator<IProcessable> fileJoiner = new FileChunkIterator(fileSize, joinedFileName, chunkSize, partPrefix,
                firstPartNumber, source, processableFactory);

        log.info("Creating statistic holder: " + ConcurrentRecordsHolder.class.getSimpleName() + " object");
        final AbstractRecordsHolder<Long, Pair<Long, Long>> holder = new ConcurrentRecordsHolder<>();

        log.info("Creating statistic watcher: " + Watcher.class.getSimpleName() + " object");
        final Watcher<Long, Pair<Long, Long>> watcher = new Watcher<>(holder, fileSize, delay);

        log.info("Creating processor: " + IOProcessor.class.getSimpleName() + " object");
        final IProcessableProcessor processor = new IOProcessor(syncWriteIO, holder, bufferSize);

        log.info("Creating workers factory" + FileWorkerFactory.class.getSimpleName() + " object");
        final FileWorkerFactory fileWorkerFactory = new FileWorkerFactory(fileJoiner, processor);

        log.info("Creating the execution service: " + ServiceRunner.class.getSimpleName() + " object");
        final ServiceRunner serviceRunner = new ServiceRunner(fileWorkerFactory, watcher, threadsNumber);

        log.info("Running a service");
        serviceRunner.run();
    }

    /**
     * Calculates the size of the joined file
     *
     * @param fileName The first part file name
     * @return The size of the joined file
     */
    long countFinalFileSize(final String fileName, final String joinedFileName, final int firstPartNumber) {
        long size = 0;
        int number = firstPartNumber;

        File file;
        while ((file = new File(joinedFileName + partPrefix + number++)).exists() && file.isFile()) {
            size += file.length();
        }


        return size;
    }

    /**
     * Gets the name of the joined file
     *
     * @param fileName The first part name
     * @return The joined file name
     */
    String getJoinedFileName(final String fileName) {
        final String name;

        try {
            name = fileName.substring(0, fileName.lastIndexOf(partPrefix));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParameterException("You've entered the wrong file name");
        }

        return name;
    }

    /**
     * Gets the start number of the first part
     *
     * @param fileName he first part name
     * @return The start number
     */
    int getFirstPartNumber(final String fileName) {
        int number;

        try {
            number = Integer.parseInt(fileName.substring(fileName.lastIndexOf(partPrefix) + partPrefix.length()));
        } catch (NumberFormatException e) {
            throw new ParameterException("You've entered the wrong file name");
        }

        return number;
    }

    /**
     * Builds a string representation of this
     *
     * @return The string representation of this
     */
    @Override
    public String toString() {
        return "CommandJoin{" +
                "path='" + path + '\'' +
                ", threadsNumber=" + threadsNumber +
                ", delay=" + delay +
                ", partPrefix='" + partPrefix + '\'' +
                ", bufferSize=" + bufferSize +
                '}';
    }
}