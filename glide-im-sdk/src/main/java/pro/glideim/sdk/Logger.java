package pro.glideim.sdk;

public interface Logger {
    void d(String tag, String log);

    void e(String tag, Throwable t);
}
