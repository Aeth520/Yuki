package cn.aetheris.yuki.core.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

import java.util.UUID;

@Getter
@DatabaseTable(tableName = "check_info")
public final class CheckInfo {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false)
    private String server;
    @DatabaseField(canBeNull = false, columnName = "uuid")
    private UUID playerUUID;
    @DatabaseField(canBeNull = false)
    private String playerName;
    @DatabaseField(canBeNull = false, columnName = "check_name")
    private String checkName;
    @DatabaseField(canBeNull = false, columnDefinition = "LONGTEXT")
    private String verbose;
    @DatabaseField(canBeNull = false, columnName = "vl")
    private int vl;
    @DatabaseField(canBeNull = false)
    private String description;
    @DatabaseField(canBeNull = false)
    private boolean exp;
    @DatabaseField(canBeNull = false, columnName = "created_at")
    private long createdAt;
    @DatabaseField(canBeNull = false)
    private String ping;
    @DatabaseField(canBeNull = false)
    private boolean lagging;
    @DatabaseField(canBeNull = false, columnName = "move_lagging")
    private boolean moveLagging;
    @DatabaseField(canBeNull = false)
    private String tps;
    @DatabaseField(canBeNull = false)
    private String brand;
    @DatabaseField(canBeNull = false)
    private String version;

    public CheckInfo() {
    }

    public CheckInfo(
            String server,
            UUID playerUUID,
            String playerName,
            String checkName,
            String verbose,
            int vl,
            String description,
            boolean exp,
            long createdAt,
            String ping,
            boolean lagging,
            boolean moveLagging,
            String tps,
            String brand,
            String version
    ) {
        this.server = server;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.checkName = checkName;
        this.verbose = verbose;
        this.vl = vl;
        this.description = description;
        this.exp = exp;
        this.createdAt = createdAt;
        this.ping = ping;
        this.lagging = lagging;
        this.moveLagging = moveLagging;
        this.tps = tps;
        this.brand = brand;
        this.version = version;
    }
}
