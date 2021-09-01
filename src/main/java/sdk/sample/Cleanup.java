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

public class Cleanup {
    /**
     * Breaks and removes Data Replication connection and then deletes all resources -> volumes, pools and accounts
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void runCleanup(ProjectConfiguration config, NetAppManagementClient anfClient)
    {
        if (config == null || config.getAccounts() == null)
            return;

        /*
          Break and remove data replications
         */
        Utils.writeConsoleMessage("Breaking and removing Data Replication(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (account.getCapacityPools() != null)
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    if (pool.getVolumes() != null)
                    {
                        for (ModelVolume volume : pool.getVolumes())
                        {
                            if (volume.getSourceVolume() != null)
                            {
                                String[] params = {account.getResourceGroup(), account.getName(), pool.getName(), volume.getName()};
                                VolumeInner destinationVolume = (VolumeInner) CommonSdk.getResource(anfClient, params, VolumeInner.class);
                                if (destinationVolume != null)
                                {
                                    try
                                    {
                                        // Break replication on destination volume
                                        anfClient.getVolumes().beginBreakReplication(account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), null).getFinalResult();
                                        // Wait for replication status to be Broken
                                        CommonSdk.waitForReplicationStatus(anfClient, account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), "Broken");
                                        Utils.writeSuccessMessage("Successfully broke Volume Replication: " + destinationVolume.id());
                                    }
                                    catch (Exception e)
                                    {
                                        Utils.writeErrorMessage("An error occurred while breaking data replication: " + destinationVolume.id());
                                        Utils.writeConsoleMessage("Error: " + e);
                                        throw e;
                                    }

                                    try
                                    {
                                        anfClient.getVolumes().beginDeleteReplication(account.getResourceGroup(), account.getName(), pool.getName(), volume.getName()).getFinalResult();
                                        CommonSdk.waitForNoReplication(anfClient, account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), 10, 60);
                                        Utils.writeSuccessMessage("Successfully deleted Volume Replication: " + destinationVolume.id());
                                    }
                                    catch (Exception e)
                                    {
                                        Utils.writeErrorMessage("An error occurred while removing data replication: " + destinationVolume.id());
                                        Utils.writeConsoleMessage("Error: " + e);
                                        throw e;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

         /*
          Clean up volumes
         */
        Utils.writeConsoleMessage("Cleaning up Volume(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (account.getCapacityPools() != null)
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    if (pool.getVolumes() != null)
                    {
                        for (ModelVolume volume : pool.getVolumes())
                        {
                            String[] parameters = {account.getResourceGroup(), account.getName(), pool.getName(), volume.getName()};
                            VolumeInner volumeInner = (VolumeInner) CommonSdk.getResource(anfClient, parameters, VolumeInner.class);
                            if (volumeInner != null)
                            {
                                try
                                {
                                    anfClient.getVolumes().beginDelete(account.getResourceGroup(), account.getName(), pool.getName(), volume.getName()).getFinalResult();
                                    CommonSdk.waitForNoANFResource(anfClient, volumeInner.id(), VolumeInner.class);
                                    Utils.writeSuccessMessage("Successfully deleted Volume: " + volumeInner.id());
                                }
                                catch (Exception e)
                                {
                                    Utils.writeErrorMessage("An error occurred while deleting Volume: " + volumeInner.id());
                                    Utils.writeConsoleMessage("Error: " + e);
                                    throw e;
                                }
                            }
                        }
                    }
                    else
                    {
                        Utils.writeConsoleMessage("No Volumes defined for Account: " + account.getName() + ", Capacity Pool: " + pool.getName());
                    }
                }
            }
        }

         /*
          Clean up capacity pools
         */
        Utils.writeConsoleMessage("Cleaning up Capacity Pool(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (account.getCapacityPools() != null)
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    String[] parameters = {account.getResourceGroup(), account.getName(), pool.getName()};
                    CapacityPoolInner capacityPool = (CapacityPoolInner) CommonSdk.getResource(anfClient, parameters, CapacityPoolInner.class);
                    if (capacityPool != null)
                    {
                        try
                        {
                            anfClient.getPools().beginDelete(account.getResourceGroup(), account.getName(), pool.getName()).getFinalResult();
                            CommonSdk.waitForNoANFResource(anfClient, capacityPool.id(), CapacityPoolInner.class);
                        }
                        catch (Exception e)
                        {
                            Utils.writeErrorMessage("An error occurred while deleting Capacity Pool: " + capacityPool.id());
                            Utils.writeConsoleMessage("Error: " + e);
                            throw e;
                        }
                        Utils.writeSuccessMessage("Successfully deleted Capacity Pool: " + capacityPool.id());
                    }
                }
            }
        }

        /*
          Clean up accounts
         */
        Utils.writeConsoleMessage("Cleaning up Account(s)...");
        if (config.getAccounts() != null)
        {
            for (ModelNetAppAccount account : config.getAccounts())
            {
                String[] parameters = {account.getResourceGroup(), account.getName()};
                NetAppAccountInner anfAccount = (NetAppAccountInner) CommonSdk.getResource(anfClient, parameters, NetAppAccountInner.class);
                if (anfAccount != null)
                {
                    try
                    {
                        anfClient.getAccounts().beginDelete(account.getResourceGroup(), account.getName()).getFinalResult();
                        CommonSdk.waitForNoANFResource(anfClient, anfAccount.id(), NetAppAccountInner.class);
                    }
                    catch (Exception e)
                    {
                        Utils.writeErrorMessage("An error occurred while deleting Account: " + anfAccount.id());
                        Utils.writeConsoleMessage("Error: " + e);
                        throw e;
                    }
                    Utils.writeSuccessMessage("Successfully deleted Account: " + anfAccount.id());
                }
            }
        }
    }
}