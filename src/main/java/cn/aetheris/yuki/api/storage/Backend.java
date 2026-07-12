package cn.aetheris.yuki.api.storage;

public interface Backend {

    String getType();

    void start();

    void stop();

    Storage getStorage();

    boolean isAvailable();
}
