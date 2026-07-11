package fun.popka.api.storages.implement.helpertstorages.enumvar;


import fun.popka.visuals.modules.Module;

import java.util.List;

import fun.popka.visuals.modules.impl.render.Chams;

public class ModuleClass extends GlobalObject<Module> implements ModuleRewords {

    public static ModuleClass INSTANCE = new ModuleClass();
    public static Chams chams = Chams.INSTANCE;

    public void initialize() {
        this.add(
                arrows,
                autoAccept,
                autoForest,
                autoJoin,
                nameProtect,
                autoFish,
                autoSwap,
                autoTool,
                blockOverlay,
                clientSounds,
                clickPearl,
                cosmetics,
                cubes,
                deathCoord,
                ecopen,
                entityESP,
                fireworkESP,
                fastExp,
                fullBright,
                helpMessage,
                hitBubbles,
                hitMarker,
                interfaceModule,
                interpolateF5,
                itemScroller,
                jumpCircle,
                killEffect,
                leavetracker,
                lineGlyphes,
                lockSlot,
                lootTracker,
                noVignette,
                particle,
                projectile,
                scoreboardHP,
                removals,
                sattelite,
                seeInvisibles,
                shaderEsp,
                shaderHands,
                shaderSky,
                shulkerPreview,
                sonar,
                sprint,
                swingAnimations,
                targetESP,
                totemAngel,
                trails,
                trajectories,
                viewModel,
                worldTweaks
        );
    }

    private void add(final Module... mod) {
        this.getObject().addAll(List.of(mod));
    }
}
