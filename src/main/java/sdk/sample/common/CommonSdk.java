// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.*;
import com.azure.resourcemanager.netapp.models.*;
import sdk.sample.model.*;

import java.util.ArrayList;
import java.util.List;

// Contains public methods for SDK related operations
public class CommonSdk
{
    /**
     * Authorizes the replication and waits for the replication status to turn to Mirrored.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Name of the source resource group
     * @param account Name of the source account
     * @param pool Name of the source pool
     * @param volume Name of the source volume
     * @param destinationVolumeId Volume id of the destination volume.
     */
    public static void authorizeReplication(NetAppManagementClient anfClient, String resourceGroup, String account, String pool, String volume, String destinationVolumeId)
    {
        AuthorizeRequest authorizeRequest = new AuthorizeRequest();
        authorizeRequest.withRemoteVolumeResourceId(destinationVolumeId);
        anfClient.getVolumes().beginAuthorizeReplication(resourceGroup, account, pool, volume, authorizeRequest).getFinalResult();
    }

    /**
     * Creates or updates a volume. Note that if sourceVolume is defined in appsettings.json a data protection properties are added to the volume.
     * @param anfClient Azure NetApp Files Management Client
     * @param account ModelNetAppAccount object that describes the ANF Account, populated with data from appsettings.json
     * @param pool ModelCapacityPool object that describes the Capacity Pool, populated with data from appsettings.json
     * @param volume ModelVolume object that describes the Volume to be created, populated with data from appsettings.json
     * @return The newly created Volume
     */
    public static VolumeInner createOrUpdateVolume(NetAppManagementClient anfClient, ModelNetAppAccount account, ModelCapacityPool pool, ModelVolume volume, VolumeInner sourceVolume)
    {
        List<ExportPolicyRule> ruleList = new ArrayList<>();
        for (ModelExportPolicyRule rule : volume.getExportPolicies())
        {
            ruleList.add(new ExportPolicyRule()
                    .withAllowedClients(rule.getAllowedClients())
                    .withRuleIndex(rule.getRuleIndex())
                    .withUnixReadWrite(rule.isUnixReadWrite())
                    .withUnixReadOnly(rule.isUnixReadOnly())
                    .withCifs(rule.isCifs())
                    .withNfsv3(rule.isNfsv3())
                    .withNfsv41(rule.isNfsv4()));
        }

        VolumePropertiesExportPolicy exportPolicy = new VolumePropertiesExportPolicy().withRules(ruleList);

        List<String> protocol = new ArrayList<>();
        protocol.add(volume.getType());

        VolumeInner volumeInner = new VolumeInner();
        volumeInner.withCreationToken(volume.getCreationToken());
        volumeInner.withSubnetId(volume.getSubnetId());
        volumeInner.withUsageThreshold(volume.getUsageThreshold());
        volumeInner.withProtocolTypes(protocol);
        volumeInner.withExportPolicy(exportPolicy);
        volumeInner.withLocation(account.getLocation().toLowerCase());
        if (sourceVolume != null) {
            volumeInner.withVolumeType("DataProtection");
            volumeInner.withDataProtection(new VolumePropertiesDataProtection()
                    .withReplication(new ReplicationObject()
                            .withEndpointType(EndpointType.DST)
                            .withRemoteVolumeResourceId(sourceVolume.id())
                            .withReplicationSchedule(ReplicationSchedule.HOURLY)));
        }

        return anfClient.getVolumes().beginCreateOrUpdate(account.getResourceGroup(), account.getName(), pool.getName(), volume.getName(), volumeInner).getFinalResult();
    }

    /**
     * Creates or updates an Azure NetApp Files Account
     * @param anfClient Azure NetApp Files Management Client
     * @param account ModelNetAppAccount object that describes the ANF Account to be created, populated with data from appsettings.json
     * @return The newly created Account
     */
    public static NetAppAccountInner createOrUpdateAccount(NetAppManagementClient anfClient, ModelNetAppAccount account)
    {
        NetAppAccountInner netAppAccount = new NetAppAccountInner();
        netAppAccount.withLocation(account.getLocation());

        return anfClient.getAccounts().beginCreateOrUpdate(account.getResourceGroup(), account.getName(), netAppAccount).getFinalResult();
    }

    /**
     * Creates or updates a Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Resource Group name where the Capacity Pool will be created
     * @param accountName Name of the ANF Account this Capacity Pool will be associated with
     * @param pool ModelCapacityPool object that describes the Capacity Pool to be created, populated with data from appsettings.json
     * @return The newly created Capacity Pool
     */
    public static CapacityPoolInner createOrUpdateCapacityPool(NetAppManagementClient anfClient, String resourceGroup, String accountName, String location, ModelCapacityPool pool)
    {
        CapacityPoolInner capacityPool = new CapacityPoolInner();
        capacityPool.withServiceLevel(ServiceLevel.fromString(pool.getServiceLevel()));
        capacityPool.withSize(pool.getSize());
        capacityPool.withLocation(location);

        return anfClient.getPools().beginCreateOrUpdate(resourceGroup, accountName, pool.getName(), capacityPool).getFinalResult();
    }

    /**
     * Returns an ANF resource or null if it does not exist
     * @param anfClient Azure NetApp Files Management Client
     * @param parameters List of parameters required depending on the resource type:
     *                   Account        -> ResourceGroupName, AccountName
     *                   Capacity Pool  -> ResourceGroupName, AccountName, PoolName
     *                   Volume         -> ResourceGroupName, AccountName, PoolName, VolumeName
     *                   Snapshot       -> ResourceGroupName, AccountName, PoolName, VolumeName, SnapshotName
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     * @return Valid resource T
     */
    public static <T> Object getResource(NetAppManagementClient anfClient, String[] parameters, Class<T> clazz)
    {
        try
        {
            switch (clazz.getSimpleName())
            {
                case "NetAppAccountInner":
                    return anfClient.getAccounts().getByResourceGroup(
                            parameters[0],
                            parameters[1]);
                case "CapacityPoolInner":
                    return anfClient.getPools().get(
                            parameters[0],
                            parameters[1],
                            parameters[2]);
                case "VolumeInner":
                    return anfClient.getVolumes().get(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3]);
                case "SnapshotInner":
                    return anfClient.getSnapshots().get(
                            parameters[0],
                            parameters[1],
                            parameters[2],
                            parameters[3],
                            parameters[4]);
            }
        }
        catch (Exception e)
        {
            Utils.writeWarningMessage("Error finding resource - " + e.getMessage());
        }
        return null;
    }

    /**
     * Method to overload function waitForNoANFResource(client, string, int, int, clazz) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForNoANFResource(NetAppManagementClient anfClient, String resourceId, Class<T> clazz)
    {
        waitForNoANFResource(anfClient, resourceId, 10, 60, clazz);
    }

    /**
     * This function checks if a specific ANF resource that was recently deleted stops existing. It breaks the wait
     * if the resource is not found anymore or if polling reached its maximum retries.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries Number of times polling will be performed
     * @param clazz Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForNoANFResource(NetAppManagementClient anfClient, String resourceId, int intervalInSec, int retries, Class<T> clazz)
    {
        for (int i = 0; i < retries; i++)
        {
            Utils.threadSleep(intervalInSec*1000);

            try
            {
                switch (clazz.getSimpleName())
                {
                    case "NetAppAccountInner":
                        NetAppAccountInner account = anfClient.getAccounts().getByResourceGroup(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId));
                        if (account == null)
                            return;

                        continue;

                    case "CapacityPoolInner":
                        CapacityPoolInner pool = anfClient.getPools().get(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId));
                        if (pool == null)
                            return;

                        continue;

                    case "VolumeInner":
                        VolumeInner volume = anfClient.getVolumes().get(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId));
                        if (volume == null)
                            return;

                        continue;

                    case "SnapshotInner":
                        SnapshotInner snapshot = anfClient.getSnapshots().get(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId),
                                ResourceUriUtils.getAnfSnapshot(resourceId));
                        if (snapshot == null)
                            return;
                }
            }
            catch (Exception e)
            {
                Utils.writeWarningMessage(e.getMessage());
                break;
            }
        }
    }

    /**
     * Method to overload function waitForReplicationStatus(client, string, Sting, Sting, String, String, int, int) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     * @param status The desired replication status
     */
    public static void waitForReplicationStatus(NetAppManagementClient anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, String status)
    {
        waitForReplicationStatus(anfClient, resourceGroupName, accountName, poolName, volumeName, status, 10, 60);
    }

    /**
     * This function checks the replication status until given status is reached or polling reached its maximum retries.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     * @param status The desired replication status
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries The amount of retries
     */
    public static void waitForReplicationStatus(NetAppManagementClient anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, String status, int intervalInSec, int retries)
    {
        for (int i = 0; i < retries; i++)
        {
            try
            {
                Utils.threadSleep(intervalInSec * 1000);
                ReplicationStatusInner replicationStatus = anfClient.getVolumes().replicationStatus(resourceGroupName, accountName, poolName, volumeName);
                if (replicationStatus.mirrorState().toString().equalsIgnoreCase(status))
                    break;
            }
            catch(Exception ex)
            {
                Utils.writeWarningMessage(ex.getMessage());
                break;
            }
        }
    }

    public static void waitForNoReplication(NetAppManagementClient anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, int intervalInSec, int retries)
    {
        for (int i = 0; i < retries; i++)
        {
            Utils.threadSleep(intervalInSec * 1000);
            try
            {
                anfClient.getVolumes().replicationStatus(resourceGroupName, accountName, poolName, volumeName);
            }
            catch(Exception ex)
            {
                if (ex.getMessage().contains("not found"))
                    return; // a not found exception means the replication does not exist any more
            }
        }
    }

    /**
     * Method to overload function waitForVolumesSuccess(client, string, Sting, Sting, String, ModelSourceVolume, int, int) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     * @param modelSourceVolume The source volume
     */
    public static void waitForVolumesSuccess(NetAppManagementClient anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, ModelSourceVolume modelSourceVolume)
    {
        waitForVolumesSuccess(anfClient, resourceGroupName, accountName, poolName, volumeName, modelSourceVolume, 10, 60);
    }

    /**
     * This function checks the provisioning state of the replication until both are succeeded or polling reached its maximum retries.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     * @param modelSourceVolume The source volume
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries The amount of retries
     */
    public static void waitForVolumesSuccess(NetAppManagementClient anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, ModelSourceVolume modelSourceVolume, int intervalInSec, int retries)
    {
        for (int i = 0; i < retries; i++)
        {
            try
            {
                Utils.threadSleep(intervalInSec * 1000);
                VolumeInner sourceVolume = anfClient.getVolumes().get(modelSourceVolume.getResourceGroup(), modelSourceVolume.getAccountName(), modelSourceVolume.getPoolName(), modelSourceVolume.getVolumeName());
                VolumeInner destinationVolume = anfClient.getVolumes().get(resourceGroupName, accountName, poolName, volumeName);
                if (sourceVolume.provisioningState().equals("Succeeded") && destinationVolume.provisioningState().equals("Succeeded"))
                    break;
            }
            catch(Exception ex)
            {
                Utils.writeWarningMessage(ex.getMessage());
                break;
            }
        }
    }
}
