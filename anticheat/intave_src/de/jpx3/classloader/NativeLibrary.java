/*
 * Decompiled with CFR 0.152.
 */
package de.jpx3.classloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

public final class NativeLibrary {
    private final String name;
    private final int version;
    private final File tempDirectory;
    private final String baseDownloadURL;
    private final List<String> allowedHashes;

    public NativeLibrary(String name, int version, File tempDirectory, String baseDownloadURL, List<String> allowedHashes) {
        this.name = name;
        this.version = version;
        this.tempDirectory = tempDirectory;
        this.baseDownloadURL = baseDownloadURL;
        this.allowedHashes = allowedHashes;
    }

    public void load() {
        try {
            this.prepareCache();
            File tempFile = this.copyCacheToTempFile();
            System.load(tempFile.getAbsolutePath());
        }
        catch (Exception exception) {
            throw new IllegalStateException("Failed to load library " + this.name, exception);
        }
    }

    private File copyCacheToTempFile() throws IOException {
        int read;
        File tempFile;
        int i = 0;
        while ((tempFile = new File(this.tempDirectory, this.name + i + this.suffix())).exists() && i++ < 100) {
        }
        tempFile.createNewFile();
        InputStream resourceAsStream = Files.newInputStream(this.cacheFile().toPath(), new OpenOption[0]);
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        byte[] array = new byte[512];
        while ((read = resourceAsStream.read(array)) != -1) {
            fileOutputStream.write(array, 0, read);
        }
        fileOutputStream.close();
        resourceAsStream.close();
        return tempFile;
    }

    private void prepareCache() throws IOException, IllegalAccessException {
        if (!this.cacheFile().exists()) {
            this.tryDownload();
        }
        this.hashCheck();
    }

    private void hashCheck() throws IllegalAccessException {
        String hash = this.hashOf(this.cacheFile());
        boolean fileValid = this.allowedHashes.stream().anyMatch(hash::equalsIgnoreCase);
        if (!fileValid) {
            this.cacheFile().delete();
            throw new IllegalAccessException("Unknown " + this.name + " library (" + hash + ")");
        }
    }

    private String hashOf(File file) {
        StringBuilder jarChecksum = new StringBuilder();
        try {
            byte[] mdbytes;
            int nread;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            fis.close();
            for (byte mdbyte : mdbytes = md.digest()) {
                jarChecksum.append(Integer.toString((mdbyte & 0xFF) + 256, 16).substring(1));
            }
        }
        catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        return jarChecksum.toString();
    }

    private void tryDownload() throws IOException {
        URL url = new URL(this.downloadUrl());
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", "Intave/$VERSION$");
        connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        connection.addRequestProperty("Pragma", "no-cache");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        this.cacheFile().getParentFile().mkdirs();
        this.cacheFile().createNewFile();
        FileChannel fileChannel = new FileOutputStream(this.cacheFile()).getChannel();
        fileChannel.transferFrom(readableByteChannel, 0L, Long.MAX_VALUE);
    }

    private String targetURL() {
        String string;
        URLConnection con;
        try {
            URL url = new URL("https://raw.githubusercontent.com/intave/domains/main/service2");
            con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setUseCaches(false);
            con.setDefaultUseCaches(false);
        }
        catch (Exception exception) {
            return "service.intave.de";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        try {
            string = reader.readLine();
        }
        catch (Throwable throwable) {
            try {
                try {
                    reader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                return "service.intave.de";
            }
        }
        reader.close();
        return string;
    }

    public String downloadUrl() {
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (operatingSystem.contains("win")) {
            return this.baseDownloadURL + "classloader-" + this.suffix();
        }
        return this.baseDownloadURL + "libclassloader-" + this.suffix();
    }

    public String suffix() {
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT).replace("amd64", "x86_64");
        if (operatingSystem.contains("win")) {
            return "windows-" + arch + ".dll";
        }
        if (operatingSystem.contains("mac")) {
            return "macos-" + arch + ".dylib";
        }
        return "linux-" + arch + ".so";
    }

    private File cacheFile() {
        return new File(this.intaveFolder(), "classloader." + this.version + this.suffix());
    }

    private File intaveFolder() {
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String filePath = operatingSystem.contains("win") ? System.getenv("APPDATA") + "/Intave/" : System.getProperty("user.home") + "/.intave/";
        return new File(filePath);
    }
}

