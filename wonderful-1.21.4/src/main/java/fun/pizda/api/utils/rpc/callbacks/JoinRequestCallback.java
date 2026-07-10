package fun.pizda.api.utils.rpc.callbacks;

import com.sun.jna.Callback;
import fun.pizda.api.utils.rpc.utils.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}
