/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.gui.CrateContainer;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;

import javax.annotation.Nonnull;
import java.util.List;

public class WoodenCrateBlockEntity extends RandomizableContainerBlockEntity
		implements IIEInventory, IBlockEntityDrop, IComparatorOverride
{
	public static final int CONTAINER_SIZE = 27;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private ListTag enchantments;

	public WoodenCrateBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.WOODEN_CRATE.get(), pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		loadIEData(nbt);
	}

	private void loadIEData(CompoundTag nbt)
	{
		if(nbt.contains("enchantments", Tag.TAG_LIST))
			this.enchantments = nbt.getList("enchantments", Tag.TAG_COMPOUND);
		// Support for unopened legacy crates
		if(nbt.contains("lootTable", Tag.TAG_STRING)&&!nbt.contains("LootTable"))
			nbt.putString("LootTable", nbt.getString("lootTable"));
		if(!tryLoadLootTable(nbt))
			ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if(this.enchantments!=null&&this.enchantments.size() > 0)
			nbt.put("enchantments", this.enchantments);
		if(!trySaveLootTable(nbt))
			ContainerHelper.saveAllItems(nbt, inventory);
	}

	@Override
	protected Component getDefaultName()
	{
		Block b = getBlockState().getBlock();
		if(b==WoodenDevices.REINFORCED_CRATE.get())
			return new TranslatableComponent("block.immersiveengineering.reinforced_crate");
		else
			return new TranslatableComponent("block.immersiveengineering.crate");
	}

	@Override
	protected NonNullList<ItemStack> getItems()
	{
		return inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> pItemStacks)
	{
		this.inventory = pItemStacks;
	}

	@Override
	protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory)
	{
		return new CrateContainer(IEContainerTypes.WOODEN_CRATE.get(), pContainerId, pInventory, this);
	}

	@Override
	@Nonnull
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return IEApi.isAllowedInCrate(stack);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
	}

	@Override
	public List<ItemStack> getBlockEntityDrop(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, inventory, false);
		if(!tag.isEmpty())
			stack.setTag(tag);
		Component customName = getCustomName();
		if(customName!=null)
			stack.setHoverName(customName);
		if(enchantments!=null&&enchantments.size() > 0)
			stack.getOrCreateTag().put("ench", enchantments);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		if(stack.hasTag())
		{
			loadIEData(stack.getOrCreateTag());
			if(stack.hasCustomHoverName())
				setCustomName(stack.getHoverName());
			enchantments = stack.getEnchantmentTags();
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	@Override
	public int getContainerSize()
	{
		return CONTAINER_SIZE;
	}
}