package com.sysgears.ui;

import com.beust.jcommander.ParameterException;
import com.sysgears.service.ServiceException;
import com.sysgears.ui.commands.CommandExit;
import com.sysgears.ui.commands.CommandHelp;

import java.io.*;

/**
 * Conducts dialogue with a user and performs their commands
 */
public class FileProcessor {
    /**
     * The saved {@code System.in} object
     */
    private InputStream systemIS;
    /**
     * The saved {@code System.out} object
     */
    private OutputStream systemOS;
    /**
     * The default prefix string of a part name
     */
    public final static String PART_PREFIX = ".part";
    /**
     * The default number of threads
     */
    public final static int THREADS_NUMBER = 4;
    /**
     * The default first part number
     */
    public final static int START_NUMBER = 1;
    /**
     * The default statistic output delay
     */
    public final static int DELAY = 1000;
    /**
     * The default IO buffer size
     */
    public final static int BUFFER_SIZE = 1024;
    /**
     * The default size of a chunk
     */
    public final static String CHUNK_SIZE = "10MB";

    /**
     * Constructs an object with default console Input and
     * Output streams
     */
    public FileProcessor() {}

    /**
     * Constructs an object with received Input and Output
     * streams
     *
     * @param is The Input stream
     * @param os The output stream
     */
    public FileProcessor(InputStream is, OutputStream os) {
        systemIS = System.in;
        systemOS = System.out;

        System.setIn(is);
        System.setOut(new PrintStream(os));
    }

    /**
     * Conducts dialogue with a user and performs their commands
     */
    public void run() {
        System.out.println("This program can split and join a file using multiple threads" + System.lineSeparator());
        CommandsHandler handler = new CommandsHandler();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("Type a command or 'help' to see how to use the program:");
                try {
                    IExecutable command = handler.encode(reader.readLine().split(" "));

                    if (command.getClass() == CommandExit.class) {
                        System.exit(0);

                    } else if (command.getClass() == CommandHelp.class) {
                        handler.showCommandsUsage();

                    } else {
                        command.execute();
                    }

                } catch (ParameterException e) {
                    System.out.println(e.getMessage());
                    System.out.println("You've entered the wrong command. Try again or type 'help':");

                } catch (UIException | ServiceException e) {
//                } catch (UIException | StatisticException | ServiceException | IOHandlerException e) {
                    System.out.println(e.getMessage() + System.lineSeparator());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (systemIS != null && systemOS != null) {
                System.setIn(systemIS);
                System.setOut(new PrintStream(systemOS));
            }
        }
    }
}