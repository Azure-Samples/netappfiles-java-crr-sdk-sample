package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.CapacityPoolInner;
import com.azure.resourcemanager.netapp.fluent.models.NetAppAccountInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import sdk.sample.common.CommonSdk;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

public class Creation
{
    public static void createANFResources(ProjectConfiguration config, NetAppManagementClient anfClient)
    {
        /*
          Creating ANF Accounts
         */
        Utils.writeConsoleMessage("Creating Azure NetApp Files Account(s)...");
        if (!config.getAccounts().isEmpty())
        {
            config.getAccounts().forEach(account -> createAccount(anfClient, account));
        }
        else
        {
            Utils.writeConsoleMessage("No ANF accounts defined within appsettings.json file. Exiting.");
        }

        /*
          Creating Capacity Pools
         */
        Utils.writeConsoleMessage("Creating Capacity Pool(s)...");
        for (ModelNetAppAccount modelAccount : config.getAccounts())
        {
            if (!modelAccount.getCapacityPools().isEmpty())
            {
                modelAccount.getCapacityPools().forEach(pool -> createCapacityPool(anfClient, modelAccount, pool));
            }
            else
            {
                Utils.writeConsoleMessage("No capacity pool defined for account " + modelAccount.getName());
            }
        }

        /*
          Creating Volumes
         */
        Utils.writeConsoleMessage("Creating Volume(s)...");
        for (ModelNetAppAccount modelAccount : config.getAccounts())
        {
            if (!modelAccount.getCapacityPools().isEmpty())
            {
                for (ModelCapacityPool capacityPool : modelAccount.getCapacityPools())
                {
                    if (!capacityPool.getVolumes().isEmpty())
                    {
                        for (ModelVolume modelVolume : capacityPool.getVolumes())
                        {
                            try
                            {
                                createVolume(anfClient, modelAccount, capacityPool, modelVolume);
                            }
                            catch (Exception e)
                            {
                                Utils.writeErrorMessage("An error occurred while creating volume " + modelAccount.getName() + " " +
                                        capacityPool.getName() + " " + modelVolume.getName() + ".\nError message: " + e.getMessage());
                                throw e;
                            }
                        }
                    }
                    else
                    {
                        Utils.writeConsoleMessage("No volumes defined for Account: " + modelAccount.getName() + ", Capacity Pool: " + capacityPool.getName());
                    }
                }
            }
        }
    }

    /**
     * Creates volume
     * @param anfClient Azure NetApp Files Management Client
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool, populated with data from appsettings.json
     * @param volume ModelVolume object that describes the Volume to be created, populated with data from appsettings.json
     */
    private static void createVolume(NetAppManagementClient anfClient, ModelNetAppAccount account, ModelCapacityPool pool, ModelVolume volume)
    {
        String[] params = {account.getResourceGroup(), account.getName(), pool.getName(), volume.getName()};
        VolumeInner anfVolume = (VolumeInner) CommonSdk.getResource(anfClient, params, VolumeInner.class);
        if (anfVolume == null)
        {
            VolumeInner sourceVolume = null;
            if (volume.getSourceVolume() != null)
            {
                String[] sourceParams = {volume.getSourceVolume().getResourceGroup(), volume.getSourceVolume().getAccountName(), volume.getSourceVolume().getPoolName(), volume.getSourceVolume().getVolumeName()};
                sourceVolume = (VolumeInner) CommonSdk.getResource(anfClient, sourceParams, VolumeInner.class);
            }

            VolumeInner newVolume = CommonSdk.createOrUpdateVolume(anfClient, account, pool, volume, sourceVolume);
            Utils.writeSuccessMessage("Volume successfully created, resource id: " + newVolume.id());
        }
        else
        {
            Utils.writeConsoleMessage("Volume already exists, resource id: " + anfVolume.id());
        }
    }

    /**
     * Creates a Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool to be created, populated with data from appsettings.json
     */
    private static void createCapacityPool(NetAppManagementClient anfClient, ModelNetAppAccount account, ModelCapacityPool pool)
    {
        String[] params = {account.getResourceGroup(), account.getName(), pool.getName()};
        CapacityPoolInner capacityPool = (CapacityPoolInner) CommonSdk.getResource(anfClient, params, CapacityPoolInner.class);
        if (capacityPool == null)
        {
            CapacityPoolInner newCapacityPool = CommonSdk.createOrUpdateCapacityPool(anfClient, account.getResourceGroup(), account.getName(), account.getLocation(), pool);
            Utils.writeSuccessMessage("Capacity Pool successfully created, resource id: " + newCapacityPool.id());
        }
        else
        {
            Utils.writeConsoleMessage("Capacity Pool already exists, resource id: " + capacityPool.id());
        }
    }


    /**
     * Creates an Azure NetApp Files Account
     * @param anfClient Azure NetApp Files Management Client
     * @param account ModelNetAppAccount object that describes the ANF Account to be created, populated with data from appsettings.json
     */
    public static void createAccount(NetAppManagementClient anfClient, ModelNetAppAccount account)
    {
        String[] params = {account.getResourceGroup(), account.getName()};
        NetAppAccountInner anfAccount = (NetAppAccountInner) CommonSdk.getResource(anfClient, params, NetAppAccountInner.class);
        if (anfAccount == null)
        {
            NetAppAccountInner newAccount = CommonSdk.createOrUpdateAccount(anfClient, account);
            Utils.writeSuccessMessage("Account successfully created, resource id: " + newAccount.id());
        }
        else
        {
            Utils.writeConsoleMessage("Account already exists, resource id: " + anfAccount.id());
        }
    }
}
