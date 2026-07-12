package fun.popka.visuals.modules.impl.combat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import fun.popka.api.events.EventLink;
import fun.popka.api.events.implement.EventBinding;
import fun.popka.api.events.implement.EventUpdate;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.BindSetting;
import fun.popka.visuals.modules.settings.implement.ModeSetting;

public class AutoSwap extends Module {

    public static AutoSwap INSTANCE = new AutoSwap();

    private final ModeSetting firstItem = new ModeSetting("Первый предмет", "Руна", "Руна", "Тотем", "Шар", "Гепл", "Щит");
    private final ModeSetting secondItem = new ModeSetting("Второй предмет", "Тотем", "Руна", "Тотем", "Шар", "Гепл", "Щит");
    private final BindSetting swapKey = new BindSetting("Кнопка свапа", -98);

    private static final int STAGE_SWITCH_TICKS = 3;
    private static final int STAGE_SWAP_HOLD_TICKS = 4;
    private static final int STAGE_STOP_AFTER_TICKS = 8;

    private int swapCooldown;
    private boolean needSwap = false;

    private int swapStage = 0;
    private int stageTicks = 0;
    private int targetHotbarIndex = -1;
    private int previousSelectedSlot = -1;

    public AutoSwap() {
        super("AutoSwap", "Быстрая смена предметов в офф-хенде", ModuleCategory.COMBAT);
        addSettings(firstItem, secondItem, swapKey);
    }

    @Override
    public void onEnable() {
        this.needSwap = false;
        this.swapCooldown = 0;
        this.swapStage = 0;
        this.stageTicks = 0;
        this.targetHotbarIndex = -1;
        this.previousSelectedSlot = -1;
        super.onEnable();
    }

    @EventLink
    public void onBinding(final EventBinding event) {
        if (mc.currentScreen != null) return;
        if (mc.player == null || mc.world == null) return;

        if (event.getKey() == swapKey.getKey()) {
            if (swapCooldown == 0 && swapStage == 0) {
                this.needSwap = true;
            }
        }
    }

    @EventLink
    public void onUpdate(final EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (swapStage > 0) {
            tickStage();
            return;
        }

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        if (!needSwap) return;
        needSwap = false;

        Item offhand = mc.player.getOffHandStack().getItem();
        Item first = getItem(firstItem.getCurrent());
        Item second = getItem(secondItem.getCurrent());

        boolean sameItem = (first == second);
        int firstSlot = findItemSlot(first);
        int secondSlot = sameItem ? firstSlot : findItemSlot(second);

        int slot;
        if (sameItem) {
            if (offhand != first || firstSlot == -1) return;
            slot = firstSlot;
        } else {
            if (firstSlot == -1 && secondSlot == -1) return;

            if (offhand == first && secondSlot != -1) {
                slot = secondSlot;
            } else if (firstSlot != -1) {
                slot = firstSlot;
            } else {
                slot = secondSlot;
            }
        }

        if (slot == -1) return;

        startVisualSwap(slot);
    }

    private void startVisualSwap(int screenSlot) {
        targetHotbarIndex = screenSlot - 36;
        previousSelectedSlot = mc.player.getInventory().selectedSlot;

        switchToSlot(targetHotbarIndex);

        swapStage = 2;
        stageTicks = STAGE_SWITCH_TICKS;
    }

    private void tickStage() {
        stopHorizontalMovement();

        if (stageTicks > 0) {
            stageTicks--;
            return;
        }

        if (swapStage == 2) {
            swapStage = 3;
            stageTicks = STAGE_SWAP_HOLD_TICKS;
        } else if (swapStage == 3) {
            mc.interactionManager.clickSlot(0, 45, targetHotbarIndex, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));

            swapStage = 4;
            stageTicks = STAGE_STOP_AFTER_TICKS;
        } else if (swapStage == 4) {
            switchToSlot(previousSelectedSlot);

            swapStage = 0;
            stageTicks = 0;
            targetHotbarIndex = -1;
            previousSelectedSlot = -1;
            swapCooldown = 2;
        }
    }

    private void switchToSlot(int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex > 8) return;
        if (mc.player.getInventory().selectedSlot != hotbarIndex) {
            mc.player.getInventory().selectedSlot = hotbarIndex;
        }
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarIndex));
    }

    private void stopHorizontalMovement() {
        mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
    }

    private int findItemSlot(Item item) {
        for (int i = 36; i <= 44; i++) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private Item getItem(String name) {
        return switch (name) {
            case "Руна" -> Items.FIREWORK_STAR;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Шар" -> Items.PLAYER_HEAD;
            case "Гепл" -> Items.GOLDEN_APPLE;
            case "Щит" -> Items.SHIELD;
            default -> Items.AIR;
        };
    }

    @Override
    public void onDisable() {
        swapCooldown = 0;
        needSwap = false;
        swapStage = 0;
        stageTicks = 0;
        targetHotbarIndex = -1;
        previousSelectedSlot = -1;
        super.onDisable();
    }
}
