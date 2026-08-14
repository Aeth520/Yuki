/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginLogger
 */
package de.jpx3.intave;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.adapter.ProtocolLibraryAdapter;
import de.jpx3.intave.cleanup.StartupTasks;
import de.jpx3.intave.diagnostic.ConsoleOutput;
import de.jpx3.intave.executor.BackgroundExecutors;
import de.jpx3.intave.resource.FileArchiver;
import de.jpx3.intave.version.JavaVersion;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

public final class IntaveLogger
extends PluginLogger {
    public static FileLoggingState FILE_OUTPUT = FileLoggingState.UNRESOLVED;
    public static boolean DISABLE_COLOR_OUTPUT = JavaVersion.current() > 8;
    private static final String LOG_PATH = "plugins" + File.separator + "Intave" + File.separator + "logs";
    private final IntavePlugin plugin;
    private final List<PrintStream> outputStreams = new CopyOnWriteArrayList<PrintStream>();
    private static IntaveLogger singletonInstance;
    private long lastNameCheck;
    private PrintWriter printWriter;
    private String activeFileName;
    private static final DateTimeFormatter MESSAGE_DATE_FORMATTER;
    private static final List<String> PENDING_LOG_ENTRIES;
    private static final ThreadLocal<Format> dateFormat;

    public IntaveLogger(IntavePlugin plugin) {
        super((Plugin)plugin);
        singletonInstance = this;
        this.plugin = plugin;
        StartupTasks.add(() -> {
            boolean enabled = plugin.settings().getBoolean("logging.file-log", true);
            FILE_OUTPUT = FileLoggingState.fromBoolean(enabled);
            if (enabled) {
                this.setup();
            }
        });
    }

    public void checkColorAvailability() {
        if (!ProtocolLibraryAdapter.protocolLibAvailable()) {
            return;
        }
        if (JavaVersion.current() > 8 && MinecraftVersions.VER1_16_2.atOrAbove()) {
            DISABLE_COLOR_OUTPUT = false;
        }
    }

    public void log(LogRecord logRecord) {
        Level level = logRecord.getLevel();
        int levelInt = level.intValue();
        String message = logRecord.getMessage();
        if (levelInt == Integer.MAX_VALUE) {
            return;
        }
        if (levelInt >= Level.SEVERE.intValue()) {
            this.error(message);
        } else if (levelInt >= Level.WARNING.intValue()) {
            this.warn(message);
        } else {
            this.info(message);
        }
    }

    public void info(String infoMessage) {
        String message = IntavePlugin.prefix() + infoMessage;
        for (PrintStream outputStream : this.outputStreams) {
            outputStream.print(ChatColor.stripColor((String)message));
        }
        if (DISABLE_COLOR_OUTPUT) {
            Bukkit.getLogger().info(ChatColor.stripColor((String)message));
        } else {
            Bukkit.getConsoleSender().sendMessage(message);
        }
        this.logToFile("(INF) " + infoMessage);
    }

    public void error(String message) {
        String fullMessage = IntavePlugin.prefix() + ChatColor.DARK_RED + ChatColor.BOLD + "ERROR" + IntavePlugin.defaultColor() + ": " + ChatColor.RED + message;
        for (PrintStream outputStream : this.outputStreams) {
            outputStream.print(ChatColor.stripColor((String)fullMessage));
        }
        if (DISABLE_COLOR_OUTPUT) {
            Bukkit.getLogger().warning(ChatColor.stripColor((String)fullMessage));
        } else {
            Bukkit.getConsoleSender().sendMessage(fullMessage);
        }
        this.logToFile("(ERR) " + message);
    }

    public void warn(String message) {
        String fullMessage = IntavePlugin.prefix() + ChatColor.YELLOW + ChatColor.BOLD + "WARNING" + IntavePlugin.defaultColor() + ": " + ChatColor.RED + message;
        for (PrintStream outputStream : this.outputStreams) {
            outputStream.print(ChatColor.stripColor((String)fullMessage));
        }
        if (DISABLE_COLOR_OUTPUT) {
            Bukkit.getLogger().warning(ChatColor.stripColor((String)fullMessage));
        } else {
            Bukkit.getConsoleSender().sendMessage(fullMessage);
        }
        this.logToFile("(WARN) " + message);
    }

    public void violation(String violation) {
        this.logToFile("(DET) " + violation);
    }

    public void commandExecution(String command) {
        if (ConsoleOutput.COMMAND_EXECUTION_DEBUG) {
            command = ChatColor.stripColor((String)command);
            this.printLine("[Intave] Issued server command /" + command);
            this.logToFile("(EXE) " + command);
        }
    }

    @Deprecated
    public void exception(Throwable throwable) {
        this.printLine("[Intave] Caught an " + throwable.getClass().getSimpleName() + " exception");
        for (PrintStream outputStream : this.outputStreams) {
            throwable.printStackTrace(outputStream);
        }
    }

    public void printLine(Object object) {
        this.printLine(object.toString());
    }

    public void printLine(String message) {
        for (PrintStream outputStream : this.outputStreams) {
            outputStream.print(message);
        }
        Bukkit.getLogger().info(message);
    }

    public void addOutputStream(PrintStream outputStream) {
        this.outputStreams.add(outputStream);
    }

    public void removeOutputStream(PrintStream outputStream) {
        this.outputStreams.remove(outputStream);
    }

    private synchronized void logToFile(String message) {
        if (!this.plugin.dataFolder().exists()) {
            return;
        }
        switch (FILE_OUTPUT.ordinal()) {
            case 0: {
                PENDING_LOG_ENTRIES.add(message);
                return;
            }
            case 2: {
                PENDING_LOG_ENTRIES.clear();
                return;
            }
            case 1: {
                if (PENDING_LOG_ENTRIES.size() <= 0) break;
                String[] messages = PENDING_LOG_ENTRIES.toArray(new String[0]);
                PENDING_LOG_ENTRIES.clear();
                for (String pendingMessage : messages) {
                    this.logToFile(pendingMessage);
                }
                break;
            }
        }
        try {
            boolean compressLogsLater = false;
            if (this.activeFileName != null && System.currentTimeMillis() - this.lastNameCheck > 10000L) {
                if (!this.activeFileName.equalsIgnoreCase(this.activeFileName())) {
                    this.setup();
                    this.activeFileName = this.activeFileName();
                    compressLogsLater = true;
                }
                this.lastNameCheck = System.currentTimeMillis();
            }
            message = message.replace("\n", "\\n").replace("\r", "\\r");
            String timestamp = "[" + LocalDateTime.now().format(MESSAGE_DATE_FORMATTER) + "] ";
            String clearMessage = ChatColor.stripColor((String)message);
            boolean finalCompressLogsLater = compressLogsLater;
            BackgroundExecutors.execute(() -> {
                this.printWriter.println(timestamp + clearMessage);
                this.printWriter.flush();
                if (finalCompressLogsLater) {
                    BackgroundExecutors.executeWhenever(this::performCompression);
                }
            });
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void setup() {
        this.activeFileName = this.activeFileName();
        try {
            File activeFile = this.activeFile();
            if (!activeFile.exists()) {
                activeFile.getParentFile().mkdirs();
                activeFile.createNewFile();
            }
            if (this.printWriter != null) {
                this.printWriter.close();
            }
            this.printWriter = new PrintWriter(new BufferedWriter(new FileWriter(activeFile, true)));
        }
        catch (IOException exception) {
            throw new IllegalStateException("Unable to create log file " + this.activeFileName, exception);
        }
    }

    public void shutdown() {
        if (this.printWriter != null) {
            this.printWriter.close();
        }
    }

    public synchronized void performCompression() {
        File[] pendingFiles = this.pendingLogFiles();
        if (pendingFiles == null || pendingFiles.length == 0) {
            return;
        }
        HashMap<File, File> filesToArchive = new HashMap<File, File>();
        for (File pendingFile : pendingFiles) {
            File archiveFile = this.archiveFileOf(pendingFile);
            if (!pendingFile.exists() || archiveFile.exists()) continue;
            filesToArchive.put(pendingFile, archiveFile);
        }
        for (Map.Entry entry : filesToArchive.entrySet()) {
            File originalFile = (File)entry.getKey();
            File archiveFile = (File)entry.getValue();
            if (!originalFile.exists() || archiveFile.exists()) continue;
            FileArchiver.archiveAndDeleteFile(originalFile, archiveFile);
            this.info("Compressed \"" + originalFile + "\"");
        }
    }

    private boolean useFileLogs() {
        return true;
    }

    private File archiveFileOf(File fileToCompress) {
        String pendingFileName = fileToCompress.getName();
        String archiveName = pendingFileName.substring(0, pendingFileName.lastIndexOf(46)) + ".zip";
        return new File(fileToCompress.getParent(), archiveName);
    }

    private File[] pendingLogFiles() {
        File folder = new File(LOG_PATH);
        return folder.listFiles((dir, name) -> this.checkIfCompressionNeeded(name));
    }

    private boolean checkIfCompressionNeeded(String fileName) {
        if (fileName.equalsIgnoreCase(this.activeFileName())) {
            return false;
        }
        return fileName.startsWith("intave") && fileName.endsWith(".log");
    }

    private File activeFile() {
        return new File(LOG_PATH, this.activeFileName);
    }

    private String activeFileName() {
        String timestamp = dateFormat.get().format(System.currentTimeMillis());
        return "intave" + timestamp + ".log";
    }

    public static IntaveLogger logger() {
        return singletonInstance;
    }

    static {
        MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm.ss.SSS");
        PENDING_LOG_ENTRIES = new ArrayList<String>();
        dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy_MM_dd"));
    }

    static enum FileLoggingState {
        UNRESOLVED,
        ENABLED,
        DISABLED;


        private static FileLoggingState fromBoolean(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }
    }
}

