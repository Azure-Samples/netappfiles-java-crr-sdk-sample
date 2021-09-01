package sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import sdk.sample.common.CommonSdk;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelCapacityPool;
import sdk.sample.model.ModelNetAppAccount;
import sdk.sample.model.ModelVolume;

import java.util.List;

public class Replication {
    /**
     * Authorizes Data Replication connection
     * @param accounts List of ModelNetAppAccount to process
     * @param anfClient Azure NetApp Files Management Client
     */
    public static void authorizeReplications(List<ModelNetAppAccount> accounts, NetAppManagementClient anfClient)
    {
        Utils.writeConsoleMessage("Authorizing Azure NetApp Files Replication(s)...");
        for (ModelNetAppAccount account : accounts)
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
                                try
                                {
                                    CommonSdk.authorizeReplication(anfClient, volume.getSourceVolume().getResourceGroup(), volume.getSourceVolume().getAccountName(), volume.getSourceVolume().getPoolName(), volume.getSourceVolume().getVolumeName(), destinationVolume.id());
                                    // Wait for replication status to be mirrored
                                    CommonSdk.waitForReplicationStatus(anfClient, account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), "Mirrored");
                                }
                                catch (Exception e)
                                {
                                    Utils.writeErrorMessage("An error occurred while authorizing data replication: " + destinationVolume.id());
                                    Utils.writeConsoleMessage("Error: " + e);
                                    throw e;
                                }
                                Utils.writeSuccessMessage("Replication successfully authorized, resource id: " + destinationVolume.id());
                            }
                        }
                    }
                }
            }
        }
    }
}
