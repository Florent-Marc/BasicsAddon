package fr.dynamx.addons.basics.common.modules;

import fr.dynamx.addons.basics.client.BasicsAddonController;
import fr.dynamx.addons.basics.client.FuelTankController;
import fr.dynamx.addons.basics.common.LightHolder;
import fr.dynamx.addons.basics.common.infos.FuelTankInfos;
import fr.dynamx.addons.basics.common.network.FuelSynchronizedVariable;
import fr.dynamx.api.entities.VehicleEntityProperties;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.entities.modules.IVehicleController;
import fr.dynamx.api.network.sync.SimulationHolder;
import fr.dynamx.common.contentpack.ModularVehicleInfo;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.EngineModule;
import fr.dynamx.common.entities.vehicles.CarEntity;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class FuelTankModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>>, IPhysicsModule.IEntityUpdateListener {

    private FuelTankController controller;
    private FuelTankInfos info;
    private float fuel = 100;
    private BaseVehicleEntity entity;

    public FuelTankModule(BaseVehicleEntity entity, FuelTankInfos info) {
        this.info = info;
        this.entity = entity;
        if (entity.world.isRemote)
            controller = new FuelTankController(entity, this);
    }

    public float getFuel() {
        return fuel;
    }

    public void setFuel(float fuel) {
        this.fuel = fuel;
    }

    public void setInfo(FuelTankInfos info) {
        this.info = info;
    }

    public FuelTankInfos getInfo() {
        return info;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IVehicleController createNewController() {
        return controller;
    }

    @Override
    public void addSynchronizedVariables(Side side, SimulationHolder simulationHolder, List variables) {
        if (side.isServer()) {
            variables.add(FuelSynchronizedVariable.NAME);
        }
    }

    @Override
    public void updateEntity() {
        IEntityUpdateListener.super.updateEntity();
        if(entity instanceof CarEntity)
        {
            CarEntity carEntity = (CarEntity) entity;
            FuelTankModule module = (FuelTankModule) carEntity.getModuleByType(FuelTankModule.class);
            if(module != null)
            {
                EngineModule engine = (EngineModule) carEntity.getEngine();
                if(engine != null)
                {
                    if(!engine.isReversing())
                    {
                        float RPM = engine.getEngineProperty(VehicleEntityProperties.EnumEngineProperties.ACTIVE_GEAR);
                        module.setFuel((float) (module.getFuel()-(RPM*(module.getInfo().getFuelConsumption()/2000)*((engine.isAccelerating() ? 1.1 : 1)))));
                    }
                    else
                    {
                        module.setFuel(module.getFuel()-0.01f);
                    }
                }
            }
        }
    }
}