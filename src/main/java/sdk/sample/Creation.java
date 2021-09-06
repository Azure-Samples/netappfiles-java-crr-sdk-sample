package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.CapacityPoolInner;
import com.azure.resourcemanager.netapp.fluent.models.NetAppAccountInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import sdk.sample.common.CommonSdk;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

import java.util.List;

public class Creation
{
    /**
     * Create accounts, pools and volumes
     * @param accounts List of ModelNetAppAccount to process
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void createANFResources(List<ModelNetAppAccount> accounts, NetAppManagementClient anfClient)
    {
        /*
          Creating ANF Accounts
         */
        Utils.writeConsoleMessage("Creating Azure NetApp Files Account(s)...");
        accounts.forEach(account -> createAccount(anfClient, account));

        /*
          Creating Capacity Pools
         */
        Utils.writeConsoleMessage("Creating Capacity Pool(s)...");
        for (ModelNetAppAccount modelAccount : accounts)
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
        for (ModelNetAppAccount modelAccount : accounts)
        {
            if (!modelAccount.getCapacityPools().isEmpty())
            {
                for (ModelCapacityPool capacityPool : modelAccount.getCapacityPools())
                {
                    if (!capacityPool.getVolumes().isEmpty())
                    {
                        for (ModelVolume modelVolume : capacityPool.getVolumes())
                        {
                            createVolume(anfClient, modelAccount, capacityPool, modelVolume);
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
            try
            {
                VolumeInner sourceVolume = null;
                if (volume.getSourceVolume() != null)
                {
                    String[] sourceParams = {volume.getSourceVolume().getResourceGroup(), volume.getSourceVolume().getAccountName(), volume.getSourceVolume().getPoolName(), volume.getSourceVolume().getVolumeName()};
                    sourceVolume = (VolumeInner) CommonSdk.getResource(anfClient, sourceParams, VolumeInner.class);
                }

                VolumeInner newVolume = CommonSdk.createOrUpdateVolume(anfClient, account, pool, volume, sourceVolume);
                if (newVolume == null)
                {
                    // if the createOrUpdateVolume returns null than the volume has been created but is in failed state, logs need to be checked to see the reason
                    // make sure appsettings is properly set up and that vnet and subnet is created
                    throw new RuntimeException("Volume ended up in failed state");
                }
                CommonSdk.waitForANFResource(anfClient, newVolume.id(), VolumeInner.class);
                Utils.writeSuccessMessage("Volume successfully created, resource id: " + newVolume.id());
            }
            catch (Exception e)
            {
                Utils.writeErrorMessage("An error occurred while creating volume " + account.getName() + " " +
                        pool.getName() + " " + volume.getName());
                Utils.writeConsoleMessage("Error: " + e);
                throw e;
            }
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
            try
            {
                CapacityPoolInner newCapacityPool = CommonSdk.createOrUpdateCapacityPool(anfClient, account.getResourceGroup(), account.getName(), account.getLocation(), pool);
                if (newCapacityPool == null)
                {
                    // if the createOrUpdateCapacityPool returns null than the pool has been created but is in failed state, logs need to be checked to see the reason
                    throw new RuntimeException("Pool ended up in failed state");
                }
                Utils.writeSuccessMessage("Capacity Pool successfully created, resource id: " + newCapacityPool.id());
            }
            catch (Exception e)
            {
                Utils.writeErrorMessage("An error occurred while creating capacity pool " + account.getName() + " " + pool.getName());
                Utils.writeConsoleMessage("Error: " + e);
                throw e;
            }
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
            try
            {
                NetAppAccountInner newAccount = CommonSdk.createOrUpdateAccount(anfClient, account);
                if (newAccount == null)
                {
                    // if the createOrUpdateAccount returns null than the account has been created but is in failed state, logs need to be checked to see the reason
                    throw new RuntimeException("Account ended up in failed state");
                }
                Utils.writeSuccessMessage("Account successfully created, resource id: " + newAccount.id());
            }
            catch (Exception e)
            {
                Utils.writeErrorMessage("An error occurred while creating account " + account.getName());
                Utils.writeConsoleMessage("Error: " + e);
                throw e;
            }
        }
        else
        {
            Utils.writeConsoleMessage("Account already exists, resource id: " + anfAccount.id());
        }
    }
}
