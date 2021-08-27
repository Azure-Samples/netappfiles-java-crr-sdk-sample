package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import sdk.sample.common.CommonSdk;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

public class Replication {
    /**
     * Authorizes Data Replication connection
     * @param config Project Configuration
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void authorizeReplications(ProjectConfiguration config, NetAppManagementClient anfClient)
    {
        if (config == null || config.getAccounts() == null)
            return;

        Utils.writeConsoleMessage("Authorizing Azure NetApp Files Replication(s)...");
        for (ModelNetAppAccount account : config.getAccounts())
        {
            if (!account.getCapacityPools().isEmpty())
            {
                for (ModelCapacityPool pool : account.getCapacityPools())
                {
                    if (!pool.getVolumes().isEmpty())
                    {
                        for (ModelVolume volume : pool.getVolumes())
                        {
                            if (volume.getSourceVolume() != null)
                            {
                                String[] destinationParams = {account.getResourceGroup(), account.getName(), pool.getName(), volume.getName()};
                                VolumeInner destinationVolume = (VolumeInner) CommonSdk.getResource(anfClient, destinationParams, VolumeInner.class);
                                if (destinationVolume == null)
                                {
                                    Utils.writeConsoleMessage("Destination volume not found to authorize replication.");
                                    return;
                                }
                                CommonSdk.authorizeReplication(anfClient, volume.getSourceVolume().getResourceGroup(), volume.getSourceVolume().getAccountName(), volume.getSourceVolume().getPoolName(), volume.getSourceVolume().getVolumeName(), destinationVolume.id());

                                // Wait for volumes to be in Successful state
                                CommonSdk.waitForVolumesSuccess(anfClient, account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), volume.getSourceVolume());

                                Utils.threadSleep(30000);

                                // Wait for replication status to be mirrored
                                CommonSdk.waitForReplicationStatus(anfClient, account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), "Mirrored");
                            }
                        }
                    }
                }
            }
        }
    }
}
