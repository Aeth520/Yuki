package cn.aetheris.yuki.core.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;

import java.util.UUID;

@Getter
@DatabaseTable(tableName = "violation")
public class Violation {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false, columnName = "vl")
    private int vl;
    @DatabaseField(canBeNull = false, columnName = "uuid")
    private String playerUuid;
    @DatabaseField(canBeNull = false, columnName = "check_name")
    private String checkName;
    @DatabaseField(columnName = "ip")
    private String ip;

    public Violation() {

    }

    public Violation(int vl, UUID playerUuid, String checkName, String ip) {
        this.vl = vl;
        this.playerUuid = playerUuid.toString();
        this.checkName = checkName;
        this.ip = ip;
    }
}
