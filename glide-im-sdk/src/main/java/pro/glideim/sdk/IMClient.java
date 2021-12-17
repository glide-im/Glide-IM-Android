package pro.glideim.sdk;

import io.reactivex.Single;
import pro.glideim.sdk.entity.IMMessage;

public interface IMClient {
    Single<IMMessage> sendChatMessage(long to, int type, String content);
}
