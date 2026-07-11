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
                autoArmor,
                autoBuy,
                autoDuel,
                autoEat,
                autoLeave,
                autoForest,
                autoJoin,
                autoPvp,
                nameProtect,
                autoFish,
                autoBuy,
                autoSwap,
                autoTool,
                blockesp,
                blockOverlay,
                chestStealer,
                clientSounds,
                clickPearl,
                cosmetics,
                cubes,
                deathCoord,
                ecopen,
                elytraSwap,
                entityESP,
                fireworkESP,
                fastBreak,
                fastExp,
                fullBright,
                helpMessage,
                hitBubbles,
                hitMarker,
                interfaceModule,
                interpolateF5,
                itemAim,
                itemScroller,
                jumpCircle,
                killEffect,
                kTLeave,
                leavetracker,
                lineGlyphes,
                lockSlot,
                lootTracker,
                noClip,
                noJumpDelay,
                nuker,
                noPush,
                noVelocity,
                noVignette,
                particle,
                projectile,
                scoreboardHP,
                removals,
                rPSpoofer,
                sattelite,
                seeInvisibles,
                shaderEsp,
                shaderHands,
                shaderSky,
                serverHelper,
                shulkerPreview,
                sonar,
                sprint,
                swingAnimations,
                targetESP,
                targetPearl,
                totemAngel,
                tpLoot,
                trails,
                trajectories,
                viewModel,
                worldTweaks,
                xCarry
        );
    }

    private void add(final Module... mod) {
        this.getObject().addAll(List.of(mod));
    }
}
