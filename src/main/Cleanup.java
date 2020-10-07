package main;

import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import rx.Observable;

import java.util.concurrent.CompletableFuture;

public class Cleanup {
    /**
     * Deletes destination replication connection
     * @param anfClient Azure NetApp Files Management Client
     * @param destinationResourceGroupName Resource Group name of the destination ANF
     * @param destinationAnfAccountName Destination ANF account name
     * @param destinationCapacityPoolName Destination ANF capacity pool name
     * @param destinationVolumeName Destination ANF volume name
     */
    public static CompletableFuture<Observable<Void>> deleteReplicationOnDestinationAsync(AzureNetAppFilesManagementClientImpl anfClient, String destinationResourceGroupName, String destinationAnfAccountName, String destinationCapacityPoolName, String destinationVolumeName)
    {
        anfClient.volumes().deleteReplicationAsync(destinationResourceGroupName,destinationAnfAccountName,destinationCapacityPoolName,destinationVolumeName);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Deletes ANF Volume
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     * @param capacityPoolName ANF capacity Pool name
     * @param volumeName ANF volume name
     */
    public static CompletableFuture<Observable<Void>> deleteAnfVolumeAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName, String capacityPoolName, String volumeName)
    {
        anfClient.volumes().deleteAsync(resourceGroupName,anfAccountName,capacityPoolName,volumeName);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Deletes ANF Capacity Pool
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     * @param capacityPoolName ANF capacity Pool name
     */
    public static CompletableFuture<Observable<Void>> deleteAnfCapacityPoolAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName, String capacityPoolName)
    {
        anfClient.pools().deleteAsync(resourceGroupName,anfAccountName,capacityPoolName);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Deletes ANF Volume
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     */
    public static CompletableFuture<Observable<Void>> deleteAnfAccountAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName)
    {
        anfClient.accounts().deleteAsync(resourceGroupName,anfAccountName);
        return CompletableFuture.completedFuture(null);
    }
}
