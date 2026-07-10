package fun.popka.api.utils.rpc.callbacks;

import com.sun.jna.Callback;
import fun.popka.api.utils.rpc.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}
