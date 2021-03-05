// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

//package main.common;
package nfs.sdk.sample.common;

import com.microsoft.azure.management.netapp.v2020_09_01.CapacityPool;
import com.microsoft.azure.management.netapp.v2020_09_01.NetAppAccount;
import com.microsoft.azure.management.netapp.v2020_09_01.Snapshot;
import com.microsoft.azure.management.netapp.v2020_09_01.Volume;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.*;
import javafx.concurrent.Task;
import org.joda.time.Period;
import rx.Observable;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import com.ea.async.Async;
import static com.ea.async.Async.await;

public class ResourceUriUtils {
    /**
     * Gets ANF Account name from resource uri
     * @param resourceUri Value with which to fetch an ANF Account
     * @return Name of Account
     */
    public static String getAnfAccount(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isEmpty())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/netAppAccounts");
    }

    /**
     * Gets ANF Capacity pool name from resource uri
     * @param resourceUri Value with which to fetch a Capacity Pool
     * @return Name of Capacity Pool
     */
    public static String getAnfCapacityPool(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isEmpty())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/capacityPools");
    }

    /**
     * Gets ANF Volume name from resource uri
     * @param resourceUri Value with which to fetch a Volume
     * @return Name of Volume
     */
    public static String getAnfVolume(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isEmpty())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/volumes");
    }

    /**
     * Gets ANF Snapshot name from resource uri
     * @param resourceUri Value with which to fetch a Snapshot
     * @return Name of Snapshot
     */
    public static String getAnfSnapshot(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isEmpty())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/snapshots");
    }

    /**
     * Gets the resource group name based on a resource uri
     * @param resourceUri Value with which to fetch a Resource Group
     * @return Name of Resource Group
     */
    public static String getResourceGroup(String resourceUri)
    {
        if (resourceUri == null || resourceUri.isEmpty())
        {
            return null;
        }

        return getResourceValue(resourceUri, "/resourceGroups");
    }

    /**
     * Parse the resource value from a resourceUri
     * @param resourceUri Id or similar value of resource
     * @param resourceName Which resource to parse from
     * @return True name of resource
     */
    public static String getResourceValue(String resourceUri, String resourceName)
    {
        if (resourceUri == null || resourceUri.isEmpty())
        {
            return null;
        }

        if (!resourceName.startsWith("/"))
        {
            resourceName = "/" + resourceName;
        }

        if (!resourceUri.startsWith("/"))
        {
            resourceUri = "/" + resourceUri;
        }

        // Checks if the resourceName and resourceGroup is the same name, and if so handles it specially
        String rgResourceName = "/resourceGroups" + resourceName;
        int rgIndex = resourceUri.toLowerCase().indexOf(rgResourceName.toLowerCase());
        if (rgIndex != -1) // resourceGroup name and resourceName passed are the same. Example: resourceGroup is "Snapshot" and so is the resourceName
        {
            String[] removedSameRgName = resourceUri.substring(rgIndex+1).split("/");
            return removedSameRgName[1];
        }

        int index = resourceUri.toLowerCase().indexOf(resourceName.toLowerCase());
        if (index != -1)
        {
            String res = resourceUri.substring(index + resourceName.length()).split("/")[1];

            // to handle the partial resource uri that doesn't have real resource name
            if (res.length() > 1)
            {
                return res;
            }
        }

        return null;
    }

    /**
     * Method to overload function waitForANFResource(client, string, int, int, clazz) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param anfClass Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForANFResource(AzureNetAppFilesManagementClientImpl anfClient, String resourceId, Class<T> anfClass) throws Exception {
        waitForANFResource(anfClient, resourceId, 10, 60, anfClass);
    }

    /**
     * This function checks if a specific ANF resource exists
     * if the resource is not found or if polling reached its maximum retries, it raise an exception.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries Number of times polling will be performed
     * @param anfClass Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForANFResource(AzureNetAppFilesManagementClientImpl anfClient, String resourceId, int intervalInSec, int retries, Class<T> anfClass) throws Exception {
        boolean isFound = false;
        for (int i = 0;i<retries;i++)
        {
            try {
                Utils.threadSleep(intervalInSec*1000);
                switch (anfClass.getSimpleName())
                {
                    case "SnapshotInner":
                        Observable<SnapshotInner> snapshot = anfClient.snapshots().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId),
                                ResourceUriUtils.getAnfSnapshot(resourceId));
                        if (snapshot.toBlocking().first() != null){
                            isFound = true;
                            return;
                        }

                        continue;

                    case "VolumeInner":
                        Observable<VolumeInner> volume = anfClient.volumes().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId));
                        if (volume.toBlocking().first() != null) {
                            isFound = true;
                            return;
                        }
                        continue;

                    case "CapacityPoolInner":
                        Observable<CapacityPoolInner> pool = anfClient.pools().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId));
                        if (pool.toBlocking().first() != null) {
                            isFound = true;
                            return;
                        }
                        continue;

                    case "NetAppAccountInner":
                        Observable<NetAppAccountInner> account = anfClient.accounts().getByResourceGroupAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId));
                        if (account.toBlocking().first() != null) {
                            isFound = true;
                            return;
                        }
                        continue;
                }
            }
            catch(Exception ex)
            {
               continue;
            }
        }
        if(!isFound)
            throw new Exception(new StringBuilder().append("Resource ").append(resourceId).append(" is not found").toString());
    }


    /**
     * Method to overload function waitForNoANFResource(client, string, int, int, clazz) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param anfClass Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForNoANFResource(AzureNetAppFilesManagementClientImpl anfClient, String resourceId, Class<T> anfClass)
    {
        waitForNoANFResource (anfClient, resourceId, 10, 60, anfClass);
    }

    /**
     * This function checks if a specific ANF resource that was recently deleted stops existing. It breaks the wait
     * if the resource is not found anymore or if polling reached its maximum retries.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceId Resource id of the resource that was deleted
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries Number of times polling will be performed
     * @param anfClass Valid class types: NetAppAccountInner, CapacityPoolInner, VolumeInner, SnapshotInner
     */
    public static <T> void waitForNoANFResource(AzureNetAppFilesManagementClientImpl anfClient, String resourceId, int intervalInSec, int retries, Class<T> anfClass)
    {
        for (int i = 0; i < retries; i++) {
            Utils.threadSleep(intervalInSec * 1000);
            try {
                switch (anfClass.getSimpleName()) {
                    case "NetAppAccountInner":
                        Observable<NetAppAccountInner> account = anfClient.accounts().getByResourceGroupAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId));
                        if (account.toBlocking().first() == null)
                            return;

                        continue;

                    case "CapacityPoolInner":
                        Observable<CapacityPoolInner> pool = anfClient.pools().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId));
                        if (pool.toBlocking().first() == null)
                            return;

                        continue;

                    case "VolumeInner":
                        Observable<VolumeInner> volume = anfClient.volumes().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId));
                        if (volume.toBlocking().first() == null)
                            return;

                        continue;

                    case "SnapshotInner":
                        Observable<SnapshotInner> snapshot = anfClient.snapshots().getAsync(ResourceUriUtils.getResourceGroup(resourceId),
                                ResourceUriUtils.getAnfAccount(resourceId),
                                ResourceUriUtils.getAnfCapacityPool(resourceId),
                                ResourceUriUtils.getAnfVolume(resourceId),
                                ResourceUriUtils.getAnfSnapshot(resourceId));
                        if (snapshot.toBlocking().first() == null)
                            return;

                        continue;
                }
            } catch (Exception e) {
                Utils.writeWarningMessage(e.getMessage());
                break;
            }
        }
    }

    /**
     * Method to overload function WaitForCompleteReplicationStatus(client, string, int, int) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     */
    public static <T> void WaitForCompleteReplicationStatus(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String accountName, String poolName, String volumeName) throws Exception
    {
        WaitForCompleteReplicationStatus(anfClient, resourceGroupName, accountName, poolName, volumeName, 10, 60);
    }

    /**
     * This function checks when the current replication is complete
     * if the resource is not found or if polling reached its maximum retries, it raise an exception.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries Number of times polling will be performed
     */
    public static <T> void WaitForCompleteReplicationStatus(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, int intervalInSec, int retries) throws Exception
    {
        boolean isFound = false;
        for (int i = 0;i<retries;i++)
        {
            try
            {
                Utils.threadSleep(intervalInSec*1000);
                Observable<ReplicationStatusInner> replicationStatus = anfClient.volumes().replicationStatusMethodAsync(resourceGroupName, accountName, poolName, volumeName);
                if(replicationStatus.toBlocking().first().mirrorState().toString().equalsIgnoreCase("mirrored"))
                    break;
            }
            catch(Exception ex)
            {
                throw new Exception(ex.getMessage());
            }
        }
    }

    /**
     * Method to overload function WaitForBrokenReplicationStatus(client, string, int, int) with default values
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     */
    public static <T> void WaitForBrokenReplicationStatus(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String accountName, String poolName, String volumeName) throws Exception
    {
        WaitForBrokenReplicationStatus(anfClient, resourceGroupName, accountName, poolName, volumeName, 10, 60);
    }

    /**
     * This function checks when the replication is broken
     * if the resource is not found or if polling reached its maximum retries, it raise an exception.
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group Name
     * @param accountName Azure NetApp Files Account name
     * @param poolName Azure NetApp Files Capacity Pool name
     * @param volumeName Azure NetApp Files Volume name
     * @param intervalInSec Time in second that the function will poll to see if the resource has been deleted
     * @param retries Number of times polling will be performed
     */
    public static <T> void WaitForBrokenReplicationStatus(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String accountName, String poolName, String volumeName, int intervalInSec, int retries) throws Exception
    {
        boolean isFound = false;
        for (int i = 0;i<retries;i++)
        {
            try
            {
                Utils.threadSleep(intervalInSec*1000);
                Observable<ReplicationStatusInner> replicationStatus = anfClient.volumes().replicationStatusMethodAsync(resourceGroupName, accountName, poolName, volumeName);
                if(replicationStatus.toBlocking().first().mirrorState().toString().equalsIgnoreCase("broken"))
                    break;
            }
            catch(Exception ex)
            {
                throw new Exception(ex.getMessage());
            }
        }
    }
}
