package su.alek.aim.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;
import su.alek.aim.AimModMain;

import java.util.function.Supplier;

public class AimAllItems {
    // Create a Deferred Register to hold Items which will all be registered under the "aim" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AimModMain.MODID);
    public static final Supplier<Item> SCALE_RAY = ITEMS.registerItem(
            "scale_ray",
            ItemScaleRay::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );
}
