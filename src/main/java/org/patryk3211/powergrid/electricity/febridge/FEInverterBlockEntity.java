package org.patryk3211.powergrid.electricity.febridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import team.reborn.energy.api.EnergyStorage;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.sim.node.ProvidedVoltageSourceCoupling;
import org.patryk3211.powergrid.electricity.sim.special.CRSeriesWire;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FEInverterBlockEntity extends ElectricBlockEntity {
    private final InverterEnergyStorage storage = new InverterEnergyStorage();
    private ProvidedVoltageSourceCoupling outputSource;
    private CRSeriesWire control;
    private float prevThrottling;

    public FEInverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private float rawThrottling() {
        return Mth.clamp((float) (control.capacitorVoltage() / ModdedConfigs.server().electricity.feInverterControlVoltage.get()), 0, 1);
    }

    protected float inputThrottling() {
        return Mth.clamp((rawThrottling() + prevThrottling) * 0.5f, 0, 1);
    }

    public static int energyBufferSize() {
        return ModdedConfigs.server().electricity.feInverterBufferSize.get();
    }

    @Override
    public void electricalTick() {
        super.electricalTick();
        prevThrottling = rawThrottling() * 0.5f + prevThrottling * 0.5f;
        double power = -outputSource.getCurrent() * outputSource.getVoltage();
        power -= outputSource.getCurrent() * outputSource.getCurrent() * outputSource.getResistance();
        setUnsaved();
        if(power < 0)
            return;
        int fe = (int) Math.ceil(ModdedConfigs.server().electricity.forgeEnergyPerWatt.getF() * power);
        useEnergy(fe);
    }

    protected float outputVoltage() {
        return (storedEnergy() / ModdedConfigs.server().electricity.forgeEnergyPerVolt.getF() * (1 - inputThrottling()));
    }

    protected float outputResistance() {
        float V = outputVoltage();
        float W = storedEnergy() / ModdedConfigs.server().electricity.forgeEnergyPerWatt.getF();
        float R = V * V / (2 * W);
        if(W <= 0 || R <= 0)
            return 1000;
        return R;
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putFloat("ControlVoltage", (float) control.capacitorVoltage());
        tag.putFloat("PrevThrottle", prevThrottling);
        tag.putInt("Energy", (int) storage.amount);
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        control.setVoltage(tag.getFloatOr("ControlVoltage", 0.0f));
        prevThrottling = tag.getFloatOr("PrevThrottle", 0.0f);
        storage.amount = tag.getIntOr("Energy", 0);
    }

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(3);
        outputSource = new ProvidedVoltageSourceCoupling(builder.terminalNode(0), builder.terminalNode(1), 1);
        outputSource.setVoltageProvider(this::outputVoltage);
        outputSource.setResistanceProvider(this::outputResistance);
        builder.add(outputSource);
        control = new CRSeriesWire(
                ModdedConfigs.server().electricity.feInverterControlCapacitance.get(),
                10000, builder.terminalNode(2), builder.terminalNode(1));
        builder.connect(100000, builder.terminalNode(2), builder.terminalNode(1));
        builder.add(control);
    }

    protected void useEnergy(int amount) {
        storage.amount = Math.max(0, storage.amount - amount);
        setChanged();
    }

    protected int storedEnergy() {
        return (int) storage.amount;
    }

    public EnergyStorage getEnergyStorage(Direction side) {
        if(side != null && side != getBlockState().getValue(FEInverterBlock.FACING))
            return null;
        return storage;
    }

    public class InverterEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
        public long amount;

        @Override
        protected Long createSnapshot() {
            return amount;
        }

        @Override
        protected void readSnapshot(Long snapshot) {
            amount = snapshot;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);
            long inserted = Math.min(maxAmount, getCapacity() - amount);
            if(inserted <= 0)
                return 0;
            updateSnapshots(transaction);
            amount += inserted;
            return inserted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public long getCapacity() {
            return energyBufferSize();
        }
    }
}
