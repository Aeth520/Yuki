package cn.aetheris.yuki.util.file;

import cn.aetheris.yuki.Yuki;

import java.io.*;
import java.util.logging.Level;

public class LoggerUtil {

    
    public static void log(final String playerName, final String log) {
        String dataFolder = Yuki.getInstance().getDataFolder().getAbsolutePath();
        String path = "data";
        String fileName = dataFolder + File.separator + path + File.separator + playerName + ".txt";
        writeLog(playerName, log, fileName, path);
    }

    
    public static void log(final String playerName, final String log, final String path) {
        String dataFolder = Yuki.getInstance().getDataFolder().getAbsolutePath();
        String fileName = dataFolder + File.separator + path + File.separator + playerName + ".txt";
        writeLog(playerName, log, fileName, path);
    }

    
    private static void writeLog(String playerName, String log, String fileName, String folder) {
        File fileDir = new File(fileName).getParentFile();
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            Yuki.getInstance().getLogger().log(Level.SEVERE, "无法创建目录：" + fileDir.getAbsolutePath());
            return;
        }
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
            pw.println(log);
        } catch (IOException e) {
            Yuki.getInstance().getLogger().log(Level.SEVERE, "无法写入日志到 " + playerName + " 的文件中！");
            e.printStackTrace();
        }
    }
}
