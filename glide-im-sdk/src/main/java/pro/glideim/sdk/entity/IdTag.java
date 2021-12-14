package pro.glideim.sdk.entity;

import java.util.HashMap;
import java.util.Map;

public class IdTag {
    private static final Map<String, IdTag> temp = new HashMap<>();
    int type;
    long id;


    private IdTag(int type, long id) {
        this.type = type;
        this.id = id;
    }

    public static IdTag get(int type, long id) {
        String tag = type + "@" + id;
        if (temp.containsKey(tag)) {
            return temp.get(tag);
        }
        IdTag r = new IdTag(type, id);
        temp.put(tag, r);
        return r;
    }

    public int getType() {
        return type;
    }

    public long getId() {
        return id;
    }
}
