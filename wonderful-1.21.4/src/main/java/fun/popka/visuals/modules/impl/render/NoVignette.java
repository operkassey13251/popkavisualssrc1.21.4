package fun.popka.visuals.modules.impl.render;

import fun.popka.visuals.modules.Module;

public class NoVignette extends Module {

    public static NoVignette INSTANCE = new NoVignette();
    public NoVignette() {
        super("NoVignette", "Убирает затемнения на краях экрана", ModuleCategory.RENDER);
    }
}

