package fun.popka.api.events.implement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fun.popka.api.events.Event;

@Getter @Setter @AllArgsConstructor
public class EventRotation extends Event {
   private float yaw, pitch;
   private float partialTicks;
}