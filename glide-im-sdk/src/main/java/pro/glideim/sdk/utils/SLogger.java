package pro.glideim.sdk.utils;

import pro.glideim.sdk.Logger;

public class SLogger {

    private static pro.glideim.sdk.Logger l = new pro.glideim.sdk.Logger() {
        @Override
        public void d(String tag, String log) {
            System.out.println("GlideIMSdk:" + tag + ": " + log);
        }

        @Override
        public void e(String tag, Throwable t) {
            System.err.println("GlideIMSdk:" + tag + ": " + t.getMessage());
            t.printStackTrace();
        }
    };

    public static void setLogger(Logger logger){
        l = logger;
    }

    private SLogger() {
    }

    public static pro.glideim.sdk.Logger getLogger() {
        return l;
    }

    public static void d(String tag, String log) {
        l.d(tag, log);
    }

    public static void e(String tag, Throwable t) {
        l.e(tag, t);
    }
}
