package fun.popka.api.events.implement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;
import fun.popka.api.events.Event;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EventMove extends Event {
   private Vec3d movePos;
}