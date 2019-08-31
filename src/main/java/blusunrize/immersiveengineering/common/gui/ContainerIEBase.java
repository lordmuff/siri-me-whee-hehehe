/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerIEBase<T extends TileEntity> extends Container
{
	public T tile;
	@Nullable
	public IInventory inv;
	public int slotCount;

	public ContainerIEBase(PlayerInventory inventoryPlayer, T tile, int id)
	{
		super(GuiHandler.getContainerTypeFor(tile), id);
		this.tile = tile;
		if(tile instanceof IIEInventory)
			this.inv = new InventoryTile(tile);
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player)
	{
		return inv!=null&&inv.isUsableByPlayer(player);//Override for TE's that don't implement IIEInventory
	}

	@Nonnull
	@Override
	public ItemStack slotClick(int id, int button, ClickType clickType, PlayerEntity player)
	{
		Slot slot = id < 0?null: this.inventorySlots.get(id);
		if(!(slot instanceof IESlot.Ghost))
			return super.slotClick(id, button, clickType, player);
		//Spooky Ghost Slots!!!!
		ItemStack stack = ItemStack.EMPTY;
		ItemStack stackSlot = slot.getStack();
		if(!stackSlot.isEmpty())
			stack = stackSlot.copy();

		if(button==2)
			slot.putStack(ItemStack.EMPTY);
		else if(button==0||button==1)
		{
			PlayerInventory playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if(stackSlot.isEmpty())
			{
				if(!stackHeld.isEmpty()&&slot.isItemValid(stackHeld))
				{
					slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
				}
			}
			else if(stackHeld.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else if(slot.isItemValid(stackHeld))
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
		}
		else if(button==5)
		{
			PlayerInventory playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if(!slot.getHasStack())
			{
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
		}
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.inventorySlots.get(slot);
		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack itemstack1 = slotObject.getStack();
			itemstack = itemstack1.copy();
			if(slot < slotCount)
			{
				if(!this.mergeItemStack(itemstack1, slotCount, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.mergeItemStack(itemstack1, 0, slotCount, false))
			{
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty())
			{
				slotObject.putStack(ItemStack.EMPTY);
			}
			else
			{
				slotObject.onSlotChanged();
			}
		}

		return itemstack;
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
	{
		return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		if(inv!=null)
			this.inv.closeInventory(playerIn);
	}
}