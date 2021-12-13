package pro.glideim.sdk.entity;

public class User {
    public static class Contacts {
        public String title;
        public String avatar;
        public long id;
        public int type;

        @Override
        public String toString() {
            return "Contacts{" +
                    "title='" + title + '\'' +
                    ", avatar='" + avatar + '\'' +
                    ", id=" + id +
                    ", type=" + type +
                    '}';
        }
    }
}
