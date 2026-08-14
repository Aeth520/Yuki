/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package de.jpx3.intave;

import de.jpx3.classloader.ClassLoader;
import de.jpx3.intave.IntaveLogger;
import de.jpx3.intave.access.IntaveAccess;
import de.jpx3.intave.access.IntaveInternalException;
import de.jpx3.intave.accessbackend.IntaveAccessService;
import de.jpx3.intave.adapter.ComponentLoader;
import de.jpx3.intave.adapter.ProtocolLibraryAdapter;
import de.jpx3.intave.adapter.ViaVersionAdapter;
import de.jpx3.intave.agent.AgentAccessor;
import de.jpx3.intave.analytics.Analytics;
import de.jpx3.intave.block.access.BlockAccess;
import de.jpx3.intave.block.access.BlockInteractionAccess;
import de.jpx3.intave.block.access.BlockWrapper;
import de.jpx3.intave.block.access.VolatileBlockAccess;
import de.jpx3.intave.block.collision.modifier.CollisionModifiers;
import de.jpx3.intave.block.fluid.Fluids;
import de.jpx3.intave.block.physics.BlockPhysics;
import de.jpx3.intave.block.shape.resolve.DrillResolver;
import de.jpx3.intave.block.shape.resolve.patch.BlockShapePatcher;
import de.jpx3.intave.block.type.BlockTypeAccess;
import de.jpx3.intave.block.variant.BlockVariantNativeAccess;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.check.CheckService;
import de.jpx3.intave.cleanup.GarbageCollector;
import de.jpx3.intave.cleanup.ShutdownTasks;
import de.jpx3.intave.cleanup.StartupTasks;
import de.jpx3.intave.command.CommandForwarder;
import de.jpx3.intave.config.ConfigurationService;
import de.jpx3.intave.connect.IntaveDomains;
import de.jpx3.intave.connect.cloud.Cloud;
import de.jpx3.intave.connect.cloud.LogTransmittor;
import de.jpx3.intave.connect.customclient.CustomClientSupportService;
import de.jpx3.intave.connect.proxy.ProxyMessenger;
import de.jpx3.intave.connect.sibyl.SibylBroadcast;
import de.jpx3.intave.connect.sibyl.SibylIntegrationService;
import de.jpx3.intave.connect.upload.ScheduledUploadService;
import de.jpx3.intave.diagnostic.ConsoleOutput;
import de.jpx3.intave.entity.EntityLookup;
import de.jpx3.intave.entity.size.HitboxSizeAccess;
import de.jpx3.intave.entity.type.EntityTypeDataAccessor;
import de.jpx3.intave.executor.BackgroundExecutors;
import de.jpx3.intave.executor.Synchronizer;
import de.jpx3.intave.executor.TaskTracker;
import de.jpx3.intave.klass.locate.Locate;
import de.jpx3.intave.library.Libraries;
import de.jpx3.intave.library.pledge.TickEnd;
import de.jpx3.intave.math.SinusCache;
import de.jpx3.intave.metric.Metrics;
import de.jpx3.intave.metric.ServerHealth;
import de.jpx3.intave.module.BootSegment;
import de.jpx3.intave.module.Modules;
import de.jpx3.intave.module.linker.bukkit.BukkitEventSubscriptionLinker;
import de.jpx3.intave.module.nayoro.Inventory;
import de.jpx3.intave.module.tracker.entity.Entity;
import de.jpx3.intave.packet.reader.PacketReaders;
import de.jpx3.intave.player.FaultKicks;
import de.jpx3.intave.player.ItemProperties;
import de.jpx3.intave.player.fake.IdentifierReserve;
import de.jpx3.intave.player.fake.event.FakePlayerEventService;
import de.jpx3.intave.reflect.access.ReflectiveAccess;
import de.jpx3.intave.resource.Resources;
import de.jpx3.intave.resource.legacy.EncryptedLegacyResource;
import de.jpx3.intave.security.PlayerListService;
import de.jpx3.intave.share.FriendlyByteBuf;
import de.jpx3.intave.share.link.WrapperConverter;
import de.jpx3.intave.test.IntegrationTestService;
import de.jpx3.intave.trustfactor.TrustFactorService;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.meta.ProtocolMetadata;
import de.jpx3.intave.user.storage.LongTermViolationStorage;
import de.jpx3.intave.version.DurationTranslator;
import de.jpx3.intave.version.IntaveVersion;
import de.jpx3.intave.version.IntaveVersionList;
import de.jpx3.intave.version.JavaVersion;
import de.jpx3.intave.world.chunk.ChunkProviderServerAccess;
import de.jpx3.intave.world.permission.WorldPermission;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class IntavePlugin
extends JavaPlugin {
    private static IntavePlugin singletonInstance;
    private static String version;
    private static String prefix;
    private static String defaultColor;
    private static final UUID gameId;
    private static boolean offlineMode;
    private static boolean successfullyBooted;
    private IntaveLogger logger;
    private LogTransmittor transmittor;
    private Cloud cloud;
    private ProxyMessenger proxyMessenger;
    private SibylIntegrationService sibylIntegrationService;
    private FakePlayerEventService fakePlayerEventService;
    private ConfigurationService configService;
    private CheckService checkService;
    private TrustFactorService trustFactorService;
    private IntaveVersionList versions;
    private CustomClientSupportService customClientSupportService;
    private IntaveAccessService accessService;
    private IntaveAccess access;
    private YamlConfiguration configuration;
    private PlayerListService blackListService;
    private ScheduledUploadService uploadService;
    private Analytics analytics;
    private Metrics metrics;
    private IntegrationTestService integrationTestService;
    public static final long INTEGRITY_ERASE_BUFFER;
    private static final long FILE_EXPIRE;
    private List<String> randomExitMessages = new ArrayList<String>();

    public IntavePlugin() {
        this.stage2();
    }

    public void stage2() {
        singletonInstance = this;
        version = this.getDescription().getVersion();
        this.createDataFolder();
        this.logger = new IntaveLogger(this);
        this.logger.checkColorAvailability();
        Modules.prepareModules();
        Modules.proceedBoot(BootSegment.STAGE_2);
        this.redirectPluginLogger();
        this.checkClassLoaderAvailability();
        System.setProperty("org.bytedeco.javacpp.cachedir", this.integrityFolder().getAbsolutePath());
        Libraries.setupLibraries();
        TickEnd.start();
        this.configService = new ConfigurationService();
        this.configService.init();
        prefix = this.configService.configuration().getString("layout.prefix", prefix);
        prefix = ChatColor.translateAlternateColorCodes((char)'&', (String)prefix);
    }

    public void onLoad() {
        Modules.proceedBoot(BootSegment.STAGE_3);
    }

    public void onEnable() {
        this.logger.info("Please stand by..");
        Modules.proceedBoot(BootSegment.STAGE_4);
        if (AgentAccessor.agentAvailable()) {
            this.logger.info("Using agent :{~-~}:");
        }
        DrillResolver.serverInit();
        IntaveDomains.setup();
        prefix = ChatColor.translateAlternateColorCodes((char)'&', (String)prefix);
        try {
            boolean writeSuccessLog;
            boolean offlineMode;
            EncryptedLegacyResource contextStatusResource;
            block17: {
                ComponentLoader componentLoader = new ComponentLoader(this);
                componentLoader.prepareComponents();
                componentLoader.loadComponents();
                ProtocolLibraryAdapter.checkIfOutdated();
                this.logger.checkColorAvailability();
                Modules.proceedBoot(BootSegment.STAGE_5);
                TaskTracker.setup();
                Locate.setup();
                ReflectiveAccess.setup();
                SinusCache.setup();
                ServerHealth.setup();
                Synchronizer.setup();
                PacketReaders.setup();
                SibylBroadcast.setup();
                IdentifierReserve.setup();
                Inventory.populateCache();
                EntityTypeDataAccessor.setup();
                ChunkProviderServerAccess.setup();
                this.trustFactorService = new TrustFactorService(this);
                this.blackListService = new PlayerListService(this);
                this.cloud = new Cloud();
                this.cloud.init();
                this.transmittor = new LogTransmittor();
                this.transmittor.init();
                Modules.proceedBoot(BootSegment.STAGE_6);
                BackgroundExecutors.start();
                FriendlyByteBuf.setup();
                contextStatusResource = new EncryptedLegacyResource("context-status", false);
                offlineMode = false;
                ProtocolMetadata.VERSION_DETAILS |= 0x100;
                ProtocolMetadata.VERSION_DETAILS |= 0x200;
                writeSuccessLog = true;
                try {
                    String textString;
                    if (!contextStatusResource.exists() || !(textString = contextStatusResource.readAsString()).startsWith("success")) break block17;
                    try {
                        long lastSuccessfulStart = Long.parseLong(textString.split("/")[1]);
                        if (System.currentTimeMillis() - lastSuccessfulStart < TimeUnit.DAYS.toMillis(2L)) {
                            writeSuccessLog = false;
                        }
                    }
                    catch (Exception lastSuccessfulStart) {}
                }
                catch (Exception textString) {
                    // empty catch block
                }
            }
            if (writeSuccessLog) {
                contextStatusResource.write(new ByteArrayInputStream(("success/" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)));
            }
            BlockVariantRegister.index();
            BlockWrapper.setup();
            Modules.proceedBoot(BootSegment.STAGE_7);
            Entity.setup();
            HitboxSizeAccess.setup();
            UserRepository.setup();
            WrapperConverter.setup();
            Fluids.setup();
            VolatileBlockAccess.setup();
            BlockAccess.setup();
            BlockInteractionAccess.setup();
            BlockVariantNativeAccess.setup();
            BlockTypeAccess.setup();
            CollisionModifiers.setup();
            ViaVersionAdapter.setup();
            WorldPermission.setup();
            BlockPhysics.setup();
            ItemProperties.setup();
            BlockShapePatcher.setup();
            EntityLookup.setup();
            this.versions = new IntaveVersionList();
            try {
                this.versions.setup();
            }
            catch (Error | Exception exception) {
                this.logger.error("Something went wrong checking version");
                exception.printStackTrace();
            }
            IntavePlugin.offlineMode = offlineMode;
            YamlConfiguration configuration = this.configService.configuration();
            prefix = configuration.getString("layout.prefix", prefix);
            prefix = ChatColor.translateAlternateColorCodes((char)'&', (String)prefix);
            defaultColor = ChatColor.getLastColors((String)prefix);
            FaultKicks.applyFrom(configuration.getConfigurationSection("fault-kicks"));
            ConsoleOutput.applyFrom(configuration.getConfigurationSection("logging"));
            this.cloud.configInit(configuration.getConfigurationSection("cloud"));
            Modules.proceedBoot(BootSegment.STAGE_8);
            this.accessService = new IntaveAccessService(this);
            this.accessService.setup();
            this.analytics = new Analytics(this);
            this.customClientSupportService = new CustomClientSupportService(this);
            this.customClientSupportService.setup();
            this.checkService = new CheckService(this);
            this.fakePlayerEventService = new FakePlayerEventService(this);
            this.proxyMessenger = new ProxyMessenger(this);
            this.sibylIntegrationService = new SibylIntegrationService(this);
            this.integrationTestService = new IntegrationTestService();
            this.integrationTestService.setup();
            this.uploadService = new ScheduledUploadService();
            this.uploadService.enable();
            this.getCommand("intave").setExecutor((CommandExecutor)new CommandForwarder());
            Modules.proceedBoot(BootSegment.STAGE_9);
            this.metrics = new Metrics(this, 6019);
            this.trustFactorService.setup();
            this.checkService.setup();
            this.fakePlayerEventService.setup();
            this.blackListService.setup();
            try {
                this.cloud.connectMasterShard();
            }
            catch (Exception exception) {
                this.logger.info("Unable to connect to cloud: " + exception.getMessage());
            }
        }
        catch (Exception exception) {
            this.logger.error("Unable to boot: " + exception.getMessage());
            exception.printStackTrace();
            this.invalidateCaches();
            this.bootFailure("Internal error occurred");
            this.performShutdown();
            return;
        }
        Modules.proceedBoot(BootSegment.STAGE_10);
        try {
            ViaVersionAdapter.patchConfiguration();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        GarbageCollector.setup();
        BackgroundExecutors.executeWhenever(this::clearIntegrityGarbage);
        BackgroundExecutors.executeWhenever(this::clearSaveFolderGarbage);
        this.logger.performCompression();
        LongTermViolationStorage.setup();
        if (JavaVersion.current() < 12) {
            this.logger.info(ChatColor.RED + "Your version of Java is seriously outdated, consider updating");
        }
        this.logger.info(ChatColor.YELLOW + "This version will dump netty threads when a player times out");
        Plugin viaBackwards = Bukkit.getPluginManager().getPlugin("ViaBackwards");
        if (viaBackwards != null && !viaBackwards.getConfig().getBoolean("handle-pings-as-inv-acknowledgements", false)) {
            this.logger.warn("ViaBackwards is misconfigured, causing false-positives and fault kicks");
            this.logger.warn("Go to plugins/ViaBackwards/config.yml and set \"handle-pings-as-inv-acknowledgements\" to TRUE");
        }
        Modules.linker().packetEvents().refreshLinkages();
        this.displayVersionInformation();
        successfullyBooted = true;
        this.randomExitMessages = Resources.localServiceCacheResource("exitmessages", "exitmessages", TimeUnit.DAYS.toMillis(7L)).readLines();
        this.logger.info("Intave booted successfully");
        Synchronizer.synchronize(() -> {
            Modules.proceedBoot(BootSegment.STAGE_11);
            StartupTasks.runAll();
        });
    }

    public void createDataFolder() {
        File dataFolder = this.dataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            this.logger.error("Failed to create Intave folder " + dataFolder.getAbsolutePath());
        }
    }

    public File dataFolder() {
        return new File(this.getServer().getUpdateFolderFile().getParentFile(), "Intave");
    }

    public void redirectPluginLogger() {
        try {
            Field loggerField = JavaPlugin.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set((Object)this, (Object)this.logger());
        }
        catch (Exception exception) {
            this.logger.error("[Intave] Failed to inject logger to bukkit");
        }
    }

    public void checkClassLoaderAvailability() {
        if (ClassLoader.usesNativeAccess() && !ClassLoader.loaded()) {
            try {
                ClassLoader.setupEnvironment(Files.createTempDirectory("intave-debug", new FileAttribute[0]).toFile());
            }
            catch (IOException exception) {
                this.logger.error("[Intave] Failed to create temporary directory for classloader");
                exception.printStackTrace();
            }
        }
    }

    public void displayVersionInformation() {
        IntaveVersion version = this.versions.versionInformation(IntavePlugin.fullVersion());
        if (version == null) {
            this.logger().info(ChatColor.YELLOW + "This version of Intave is not listed in the official version index");
        } else {
            long duration = System.currentTimeMillis() - version.release();
            String durationAsString = DurationTranslator.translateHours(duration);
            String infoMessage = "";
            switch (version.typeClassifier()) {
                case LATEST: {
                    infoMessage = "Running the latest version of Intave (" + durationAsString + " old)";
                    break;
                }
                case STABLE: {
                    infoMessage = "Running a stable version of Intave (" + durationAsString + " old)";
                    break;
                }
                case OUTDATED: {
                    infoMessage = "A newer version of Intave is available (this version is " + durationAsString + " old)";
                    break;
                }
                case TEST: {
                    infoMessage = "Running a test version of Intave";
                    break;
                }
                case DISABLED: 
                case INVALID: {
                    this.logger().error("Unable to boot: This version has been deactivated");
                    this.bootFailure("Version deactivated");
                    this.performShutdown();
                    throw new IntaveInternalException("Escape exception");
                }
            }
            this.logger().info(infoMessage);
        }
    }

    public void clearIntegrityGarbage() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        try {
            Files.walk(tempDir.toPath(), new FileVisitOption[0]).map(Path::toFile).filter(File::canRead).filter(File::canWrite).filter(file -> "deleteme".equalsIgnoreCase(file.getName()) && file.getParentFile().getName().toLowerCase(Locale.ROOT).contains("intave")).filter(file -> System.currentTimeMillis() - file.lastModified() > INTEGRITY_ERASE_BUFFER).map(File::getParentFile).filter(File::canRead).filter(File::canWrite).forEach(file -> {
                try {
                    this.clearDirectory((File)file);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            });
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void clearDirectory(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of " + directory);
        }
        for (File file : files) {
            try {
                this.forceDelete(file);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        directory.delete();
    }

    private void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            this.clearDirectory(file);
        } else {
            boolean exists = file.exists();
            if (!file.delete()) {
                if (!exists) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                throw new IOException("Unable to delete file: " + file);
            }
        }
    }

    public void clearSaveFolderGarbage() {
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String filePath = operatingSystem.contains("win") ? System.getenv("APPDATA") + "/Intave/" : System.getProperty("user.home") + "/.intave/";
        File workDirectory = new File(filePath);
        if (!workDirectory.exists()) {
            return;
        }
        try {
            Files.walk(workDirectory.toPath(), new FileVisitOption[0]).filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(Path::toFile).filter(File::canWrite).filter(File::canRead).filter(file -> System.currentTimeMillis() - file.lastModified() > FILE_EXPIRE).forEach(file -> {
                try {
                    file.delete();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
            Files.walk(workDirectory.toPath(), new FileVisitOption[0]).filter(x$0 -> Files.isDirectory(x$0, new LinkOption[0])).map(Path::toFile).filter(File::canWrite).filter(File::canRead).filter(file -> file.listFiles() == null).filter(file -> System.currentTimeMillis() - file.lastModified() > FILE_EXPIRE).forEach(file -> {
                try {
                    file.delete();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
        catch (NoSuchFileException noSuchFileException) {
        }
        catch (Error | Exception throwable) {
            // empty catch block
        }
    }

    public void invalidateCaches() {
        this.clearIntegrityGarbage();
    }

    public void bootFailure(String reason) {
        this.getCommand("intave").setExecutor((commandSender, command, s, strings) -> {
            commandSender.sendMessage(IntavePlugin.prefix() + ChatColor.RED + "Intave couldn't boot properly: " + reason);
            return false;
        });
    }

    public void onDisable() {
        this.performShutdown();
    }

    public void performShutdown() {
        this.logger.info("Stopping Intave");
        try {
            this.configService.shutdown();
        }
        catch (Exception exception) {
            // empty catch block
        }
        Bukkit.getScheduler().cancelTasks((Plugin)this);
        ShutdownTasks.runAll();
        BackgroundExecutors.stopAllBlocking();
        this.deleteIntegrityCache();
        if (successfullyBooted) {
            this.logger.info(this.randomExitMessage());
        }
        this.logger.info("Intave offline");
        this.logger.shutdown();
    }

    private String randomExitMessage() {
        return this.randomExitMessages.isEmpty() ? "No jokes? :(" : this.randomExitMessages.get(ThreadLocalRandom.current().nextInt(this.randomExitMessages.size()));
    }

    private void deleteIntegrityCache() {
        try {
            Class<?> relocator = Class.forName("de.jpx3.relocator.Relocator");
            relocator.getMethod("i", new Class[0]).invoke(null, new Object[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private File integrityFolder() {
        try {
            Class<?> relocator = Class.forName("de.jpx3.relocator.Relocator");
            File child = (File)relocator.getMethod("h", new Class[0]).invoke(null, "a", "b");
            return child.getParentFile();
        }
        catch (Exception relocator) {
            try {
                return Files.createTempDirectory("intave", new FileAttribute[0]).toFile();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public IntaveAccess access() {
        return this.access;
    }

    public void setAccess(IntaveAccess access) {
        this.access = access;
    }

    public IntaveAccessService accessService() {
        return this.accessService;
    }

    public TrustFactorService trustFactorService() {
        return this.trustFactorService;
    }

    public IntaveLogger logger() {
        return this.logger;
    }

    public Cloud cloud() {
        return this.cloud;
    }

    public LogTransmittor logTransmittor() {
        return this.transmittor;
    }

    public ProxyMessenger proxy() {
        return this.proxyMessenger;
    }

    public CheckService checks() {
        return this.checkService;
    }

    public YamlConfiguration settings() {
        return this.configService.configuration();
    }

    public FakePlayerEventService fakePlayerEventService() {
        return this.fakePlayerEventService;
    }

    @Deprecated
    public BukkitEventSubscriptionLinker eventLinker() {
        return Modules.linker().bukkitEvents();
    }

    public SibylIntegrationService sibyl() {
        return this.sibylIntegrationService;
    }

    public Analytics analytics() {
        return this.analytics;
    }

    public ScheduledUploadService uploader() {
        return this.uploadService;
    }

    public IntaveVersionList versions() {
        return this.versions;
    }

    public static String fullVersion() {
        return version;
    }

    public static String versionTag() {
        String version = IntavePlugin.fullVersion();
        int lastPlusIndex = version.lastIndexOf(45);
        if (lastPlusIndex != -1) {
            return version.substring(0, lastPlusIndex);
        }
        return version;
    }

    public static String commitHash() {
        String version = IntavePlugin.fullVersion();
        int lastPlusIndex = version.lastIndexOf(45);
        if (lastPlusIndex != -1 && lastPlusIndex < version.length() - 1) {
            return version.substring(lastPlusIndex + 1);
        }
        return "unknown";
    }

    public static UUID gameId() {
        return gameId;
    }

    public static String prefix() {
        return prefix;
    }

    public static String defaultColor() {
        return defaultColor;
    }

    public static IntavePlugin singletonInstance() {
        return singletonInstance;
    }

    static {
        version = "UNKNOWN";
        prefix = ChatColor.translateAlternateColorCodes((char)'&', (String)"&8[&c&lIntave&8]&7 ");
        defaultColor = ChatColor.getLastColors((String)prefix);
        gameId = UUID.randomUUID();
        offlineMode = false;
        successfullyBooted = false;
        INTEGRITY_ERASE_BUFFER = TimeUnit.MINUTES.toMillis(1L);
        FILE_EXPIRE = TimeUnit.DAYS.toMillis(90L);
    }
}

