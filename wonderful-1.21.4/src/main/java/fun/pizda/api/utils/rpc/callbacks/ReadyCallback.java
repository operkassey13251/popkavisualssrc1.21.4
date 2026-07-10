package fun.pizda.api.utils.rpc.callbacks;

import com.sun.jna.Callback;
import fun.pizda.api.utils.rpc.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}
