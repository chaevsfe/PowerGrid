/*
 * Copyright 2025 patryk3211
 * Modified 2026 by chaevsfe for the unofficial Fabric / Create Fly 26.2 port.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.patryk3211.powergrid.electricity.deviceconnector;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.base.ProxyElectricBehaviour;
import org.patryk3211.powergrid.electricity.febridge.IFEBridgeHandler;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;

import team.reborn.energy.api.EnergyStorage;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BridgeElectricBehaviour extends ProxyElectricBehaviour {
    private IFEBridgeHandler bridgeBehaviour;
    private long readEnergy;
    public long currentRate;
    private boolean fetched = false;
    private final Supplier<ElectricWire> converterWire;
    private boolean isProxy = false;
    protected int cooldownTicks = 0;

    public <T extends SmartBlockEntity & IElectricEntity> BridgeElectricBehaviour(T be, BlockPos behaviourPosition, Supplier<ElectricWire> converterWire) {
        super(be, true, () -> behaviourPosition);
        this.converterWire = converterWire;
    }

    protected void constructBehaviours() {
        var world = getLevel();
        if(!world.isLoaded(behaviourPosition.get()))
            return;
        fetched = true;
        var mainBehaviour = get(world, behaviourPosition.get(), TYPE);
        if(mainBehaviour != null) {
            isProxy = true;
            return;
        }
        bridgeBehaviour = makeFEHandler(blockEntity);
        if(bridgeBehaviour == null) {
            if(cooldownTicks >= 5) {
                world.destroyBlock(getPos(), true);
            } else {
                fetched = false;
            }
            return;
        }
        bridgeBehaviour.setAmount(readEnergy);
    }

    @Override
    public Optional<ElectricBehaviour> getMainBehaviour() {
        if(!fetched)
            constructBehaviours();
        return super.getMainBehaviour();
    }

    @Nullable
    public IFEBridgeHandler getBridgeBehaviour() {
        if(!fetched)
            constructBehaviours();
        return bridgeBehaviour;
    }

    protected boolean isProxy() {
        if(!fetched)
            constructBehaviours();
        return isProxy;
    }

    @Override
    public void initialize() {
        if(isProxy()) {
            super.initialize();
        } else {
            super.baseInitialize();
        }
    }

    @Override
    public void unload() {
        if(!isProxy())
            super.baseUnload();
    }

    @Override
    public void remove() {
        if (isProxy()) {
            // Node holder might not be removed.
            super.remove();
        } else {
            super.baseRemove();
        }
    }

    @Override
    public void read(ValueInput nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        if(bridgeBehaviour != null) {
            bridgeBehaviour.setAmount(nbt.getLongOr("Energy", 0L));
        } else {
            readEnergy = nbt.getLongOr("Energy", 0L);
        }
        if(clientPacket)
            currentRate = nbt.getLongOr("Rate", 0L);
    }

    @Override
    public void write(ValueOutput nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        if(bridgeBehaviour != null) {
            nbt.putLong("Energy", bridgeBehaviour.getAmount());
            if(clientPacket)
                nbt.putLong("Rate", currentRate);
        }
    }

    @Override
    public void tick() {
        super.tick();
        var energyStorage = getBridgeBehaviour();
        if(energyStorage == null) {
            if(!isProxy)
                ++cooldownTicks;
            return;
        }

        var wire = converterWire.get();
        var world = getLevel();

        energyStorage.charge(wire);
        var moved = energyStorage.moveEnergy();
        if(!world.isClientSide()) {
            if(moved != currentRate) {
                currentRate = moved;
                blockEntity.sendData();
            }
        } else if(moved == 0) {
            // Since some things might not sync this to client, this is necessary
            // to provide a valid, client-side simulation parameters.
            var amount = energyStorage.getAmount();
            energyStorage.setAmount(Math.max(amount - currentRate, 0));
        }
        energyStorage.manageWire(wire);
    }

    public static IFEBridgeHandler makeFEHandler(BlockEntity be) {
        var facing = be.getBlockState().getValue(DeviceConnectorBlock.FACING);
        var energyStorage = EnergyStorage.SIDED.find(be.getLevel(), be.getBlockPos().relative(facing), facing.getOpposite());
        if(energyStorage != null) {
            return new FEBridgeEnergyStorage(be);
        }
        return null;
    }

    public boolean isFE() {
        return bridgeBehaviour != null;
    }

    public long getBufferedAmount() {
        return bridgeBehaviour.getAmount();
    }
}
