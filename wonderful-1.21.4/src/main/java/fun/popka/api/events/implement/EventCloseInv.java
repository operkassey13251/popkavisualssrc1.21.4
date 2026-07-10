package fun.popka.api.events.implement;

import lombok.AllArgsConstructor;
import fun.popka.api.events.Event;

@AllArgsConstructor
public class EventCloseInv extends Event {
    public int windowId;
}

