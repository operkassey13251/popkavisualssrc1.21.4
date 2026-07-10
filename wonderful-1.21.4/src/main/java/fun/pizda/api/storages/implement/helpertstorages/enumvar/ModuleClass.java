package fun.pizda.api.storages.implement.helpertstorages.enumvar;


import fun.pizda.client.modules.Module;

import java.util.List;

import fun.pizda.client.modules.impl.render.Chams;

public class ModuleClass extends GlobalObject<Module> implements ModuleRewords {

    public static ModuleClass INSTANCE = new ModuleClass();
    public static Chams chams = Chams.INSTANCE;

    public void initialize() {
        this.add(
                airStuck,
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
                autoJump,
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
                elytraBoost,
                elytraSwap,
                entityESP,
                fireworkESP,
                fastBreak,
                fastExp,
                flight,
                freeCam,
                fullBright,
                grimGlide,
                grimNoFall,
                highJump,
                helpMessage,
                hitBubbles,
                hitMarker,
                interfaceModule,
                interpolateF5,
                inventoryWalk,
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
                noSlow,
                noVelocity,
                noVignette,
                noWeb,
                particle,
                playerFakeLags,
                projectile,
                scoreboardHP,
                removals,
                rPSpoofer,
                sattelite,
                seeInvisibles,
                shaderEsp,
                shaderHands,
                serverHelper,
                shulkerPreview,
                sonar,
                speed,
                spider,
                sprint,
                step,
                swingAnimations,
                targetESP,
                targetPearl,
                timer,
                totemAngel,
                tpBack,
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
