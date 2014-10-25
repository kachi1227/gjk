package com.gjk.logger;

import android.content.Context;

import com.gjk.utils.media2.ImageUtil;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author gpl
 */
public class Logger {

    private static final double MAX_LOG_SIZE = 1024 * 1024 * 10L; //10MB
    private static final double MAX_NUMBER_OF_LOGS = 10;

    private final LogProducer p;
    private final LogConsumer c;

    public Logger(Context ctx) throws IOException {
        final BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>();
        c = new LogConsumer(queue, ImageUtil.getFile(ctx, "Chassip.log").getPath());
        new Thread(c, "ChassipLogConsumer").start();

        p = new LogProducer(queue);
    }

    public void log(String msg) throws InterruptedException {
        p.log(msg);
    }

    public File getLogFile() {
        return c.f;
    }

    public void clear() {
        c.clear();
    }

    class LogEntry {
        private final long time;
        private final String message;

        LogEntry(long time, String message) {
            this.time = time;
            this.message = message;
        }
    }

    class LogProducer {
        private final BlockingQueue<LogEntry> queue;

        LogProducer(BlockingQueue<LogEntry> q) {
            queue = q;
        }

        public void log(String msg) throws InterruptedException {
            queue.put(new LogEntry(new Date().getTime(), msg));
        }
    }

    class LogConsumer implements Runnable {
        private final BlockingQueue<LogEntry> queue;
        private File f;

        LogConsumer(BlockingQueue<LogEntry> q, String filename) {
            queue = q;
            f = new File(filename);

            try {
                if (!f.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    f.createNewFile();
                    //noinspection ResultOfMethodCallIgnored
                    f.canWrite();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {

                    final LogEntry entry = queue.take();

                    if (f.length() > MAX_LOG_SIZE) {
                        final String oldName = f.getAbsolutePath();
                        rollLog(f, 0);
                        f = new File(oldName);
                    }

                    final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    out.println(String.format("%s: %s", df.format(new Date(entry.time)), entry.message));
                    out.close();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        public void clear() {
//            try {
//                final BufferedWriter bw = new BufferedWriter(new FileWriter(f));
//                bw.write("");
//                bw.flush();
//                bw.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        private void rollLog(File f, int count) {
            count++;
            if (count >= MAX_NUMBER_OF_LOGS) {
                return;
            }
            final String lastStuff = f.getName().substring(f.getName().lastIndexOf('.') + 1);
            final String oldName = f.getAbsolutePath();
            final File newFile = StringUtils.isNumeric(lastStuff) ? new File(oldName.substring(0,
                    oldName.lastIndexOf('.')) + "." + (Integer.valueOf(lastStuff) + 1)) : new File(oldName + "." + 1);
            if (newFile.exists()) {
                rollLog(newFile, count);
            }
            f.renameTo(newFile);
        }
    }
}
