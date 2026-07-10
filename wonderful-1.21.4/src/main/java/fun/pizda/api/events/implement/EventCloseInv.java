package fun.pizda.api.events.implement;

import lombok.AllArgsConstructor;
import fun.pizda.api.events.Event;

@AllArgsConstructor
public class EventCloseInv extends Event {
    public int windowId;
}

