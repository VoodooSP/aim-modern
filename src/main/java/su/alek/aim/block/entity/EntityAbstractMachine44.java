package su.alek.aim.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import su.alek.aim.AimModMain;
import su.alek.aim.gui.menu.MenuMachine44;
import su.alek.aim.recipe.Recipe44;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public abstract class EntityAbstractMachine44 extends BlockEntity implements WorldlyContainer, MenuProvider {
    public int _test;
    public static ItemStack[] emptySlot = new ItemStack[4];
    //public NonNullList<ItemStack> items = NonNullList.withSize(8,ItemStack.EMPTY);
    public ItemStack[] input = new ItemStack[4];
    public ItemStack[] output = new ItemStack[4];
    public int recipeTime;
    public int workTime;
    public ItemStack[] recipeItems = new ItemStack[4];
    public boolean paused;
    public ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return _test;
        }

        @Override
        public void set(int pIndex, int pValue) {
            _test = pValue;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };
    static {
        for (int i=0;i<4;i++){
            emptySlot[i] = ItemStack.EMPTY;
        }
    }
    {
        for (int i=0;i<4;i++){
            input[i] = ItemStack.EMPTY;
            output[i] = ItemStack.EMPTY;
            recipeItems[i] = ItemStack.EMPTY;
        }
    }

    public EntityAbstractMachine44(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public abstract HashMap<HashSet<Item>, Recipe44> getRecipe();

    @Override
    public int getContainerSize() {
        return 8;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return pIndex <= 3;
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return pIndex >= 4;
    }
    @Override
    public int[] getSlotsForFace(Direction pSide) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    @Override
    public boolean isEmpty() {
        return Arrays.equals(input, emptySlot) && Arrays.equals(output, emptySlot);
    }

    @Override
    public ItemStack getItem(int pSlot) {
        if (pSlot <=3){
            return input[pSlot];
        }else {
            return output[pSlot -4];
        }
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        ItemStack stack;
        if (pSlot < 4){
            stack = input[pSlot];
        }else {
            stack = output[pSlot - 4];
        }
        int count = stack.getCount();
        stack.setCount(count>=pAmount ? count-pAmount : 0);
        ItemStack returnStack = stack.copy();
        returnStack.setCount(Math.min(count,pAmount));
        return returnStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        if (pSlot < 4){
            ItemStack stack = input[pSlot];
            input[pSlot] = ItemStack.EMPTY;
            return stack;
        }else {
            ItemStack stack = output[pSlot - 4];
            output[pSlot - 4] = ItemStack.EMPTY;
            return stack;
        }
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        if (pSlot < 4){
            input[pSlot] = pStack;
        }else {
            output[pSlot - 4] = pStack;
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0;i < 4;i++){
            input[i] = emptySlot[i].copy();
            output[i] = emptySlot[i].copy();
        }
    }

    public static void serverTick(Level pLevel, BlockPos pos, BlockState state, EntityAbstractMachine44 blockEntity){
        blockEntity._test++;
        if (!blockEntity.paused){
            if (Arrays.equals(blockEntity.recipeItems, emptySlot)){
                if (Recipe44.matches(blockEntity.input, blockEntity.getRecipe()) && Recipe44.canConsume(blockEntity.input,blockEntity.getRecipe())){
                    blockEntity.recipeTime = Recipe44.getTime(blockEntity.input, blockEntity.getRecipe());
                    blockEntity.workTime = 1;
                    blockEntity.recipeItems = Recipe44.getResult(blockEntity.input,blockEntity.getRecipe());
                    Recipe44.consumeByRecipe(blockEntity.input,blockEntity.getRecipe());
                }
            }else {
                ++blockEntity.workTime;
                if (blockEntity.workTime >= blockEntity.recipeTime){
                    blockEntity.output = blockEntity.recipeItems;
                    for (int i = 0;i < 4;i++){
                        blockEntity.recipeItems[i] = emptySlot[i].copy();
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        AimModMain.LOGGER.debug(String.format("%d", _test));
        return new MenuMachine44(pContainerId, pPlayerInventory, this, data);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        this.paused = pTag.getBoolean("paused");
        NonNullList<ItemStack> items = NonNullList.withSize(8, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(pTag, items, pRegistries);
        for (int i = 0; i < 8; i++) {
            this.setItem(i, items.get(i));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        NonNullList<ItemStack> items = NonNullList.withSize(8, ItemStack.EMPTY);
        pTag.putBoolean("paused", paused);
        if (!Arrays.equals(this.recipeItems, emptySlot)){
            this.output = this.recipeItems;
            for (int i = 0; i < 4; i++){
                this.recipeItems[i] = emptySlot[i].copy();
            }
        }
        for (int i = 0; i < 8; i++) {
            items.set(i, this.getItem(i));
        }
        ContainerHelper.saveAllItems(pTag, items, pRegistries);
    }
}
