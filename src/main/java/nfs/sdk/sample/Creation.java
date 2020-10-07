package nfs.sdk.sample;

import com.microsoft.azure.management.netapp.v2019_11_01.*;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.NetAppAccountInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.CapacityPoolInner;
import com.microsoft.azure.management.netapp.v2019_11_01.implementation.VolumeInner;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Creation {
    /**
     * Creates or updates ANF Account resource
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     * @param location ANF resource location / region
     * @return ANF account inner object
     */
    public static CompletableFuture<Observable<NetAppAccountInner>> createOrUpdateAccountAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName, String location)
    {
        NetAppAccountInner netappAccount = new NetAppAccountInner();
        netappAccount.withLocation(location);
        Observable<NetAppAccountInner> netappAccountObs = anfClient.accounts().createOrUpdateAsync(resourceGroupName, anfAccountName, netappAccount);
        return CompletableFuture.completedFuture(netappAccountObs);
    }

    /**
     * Creates or updates ANF Capacity Pool resource
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     * @param capacityPoolName ANF capacity Pool name
     * @param serviceLevel ANF capacity pool service level {Premium or Standard}
     * @param location ANF resource location / region
     * @return ANF capacity pool inner object
     */
    public static CompletableFuture<Observable<CapacityPoolInner>> createOrUpdateCapacityPoolAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName, String capacityPoolName, String serviceLevel, long capacityPoolSize, String location)
    {
        CapacityPoolInner capacityPool = new CapacityPoolInner();
        capacityPool.withServiceLevel(ServiceLevel.fromString(serviceLevel));
        capacityPool.withSize(capacityPoolSize);
        capacityPool.withLocation(location);

        Observable<CapacityPoolInner> capacityPoolObservable = anfClient.pools().createOrUpdateAsync(resourceGroupName, anfAccountName, capacityPoolName, capacityPool);
        return CompletableFuture.completedFuture(capacityPoolObservable);
    }

    /**
     * Creates or updates ANF volume resource
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     * @param capacityPoolName ANF capacity Pool name
     * @param volumeName ANF volume name
     * @param volumeSize ANF volume size in Byte
     * @param serviceLevel ANF capacity pool service level {Premium or Standard}
     * @param subnetId Subnet Id
     * @param location ANF resource location / region
     * @return ANF volume inner object
     */
    public static CompletableFuture<Observable<VolumeInner>> createOrUpdateVolumeAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName,String capacityPoolName, String volumeName, long volumeSize, String serviceLevel, String subnetId, String location)
    {
        List<ExportPolicyRule> ruleList = new ArrayList<>();
        ruleList.add(new ExportPolicyRule()
                .withAllowedClients("0.0.0.0")
                .withRuleIndex(1)
                .withUnixReadWrite(true)
                .withUnixReadOnly(false)
                .withCifs(false)
                .withNfsv3(false)
                .withNfsv41(true));

        VolumePropertiesExportPolicy exportPolicy = new VolumePropertiesExportPolicy().withRules(ruleList);

        List<String> protocols = new ArrayList<>();
        protocols.add("NFSv4.1");

        VolumeInner volumeInner = (VolumeInner) new VolumeInner()
                .withCreationToken(volumeName)
                .withExportPolicy(exportPolicy)
                .withServiceLevel(ServiceLevel.fromString(serviceLevel))
                .withSubnetId(subnetId)
                .withUsageThreshold(volumeSize)
                .withProtocolTypes(protocols)
                .withLocation(location);

        Observable<VolumeInner> volumeInnerObservable = anfClient.volumes().createOrUpdateAsync(resourceGroupName, anfAccountName, capacityPoolName, volumeName, volumeInner);
        return CompletableFuture.completedFuture(volumeInnerObservable);
    }

    /**
     * Creates or updates ANF volume resource
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource Group of ANF resource
     * @param anfAccountName ANF account name
     * @param capacityPoolName ANF capacity Pool name
     * @param volumeName ANF volume name
     * @param volumeSize ANF volume size in Byte
     * @param serviceLevel ANF capacity pool service level {Premium or Standard}
     * @param subnetId Subnet Id
     * @param location ANF resource location / region
     * @return ANF volume inner object
     */
    public static CompletableFuture<Observable<VolumeInner>> createOrUpdateDataReplicationVolumeAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName,String capacityPoolName, String volumeName, long volumeSize, String serviceLevel, String subnetId, String location, String primaryVolumeId, String primaryVolumeLocation)
    {
        List<ExportPolicyRule> ruleList = new ArrayList<>();
        ruleList.add(new ExportPolicyRule()
                .withAllowedClients("0.0.0.0")
                .withRuleIndex(1)
                .withUnixReadWrite(true)
                .withUnixReadOnly(false)
                .withCifs(false)
                .withNfsv3(false)
                .withNfsv41(true));

        VolumePropertiesExportPolicy exportPolicy = new VolumePropertiesExportPolicy().withRules(ruleList);

        List<String> protocols = new ArrayList<>();
        protocols.add("NFSv4.1");

        VolumeInner volumeInner = new VolumeInner();
        volumeInner.withCreationToken(volumeName);
        volumeInner.withExportPolicy(exportPolicy);
        volumeInner.withServiceLevel(ServiceLevel.fromString(serviceLevel));
        volumeInner.withSubnetId(subnetId);
        volumeInner.withUsageThreshold(volumeSize);
        volumeInner.withProtocolTypes(protocols);
        volumeInner.withLocation(location);
        volumeInner.withDataProtection(new VolumePropertiesDataProtection()
                .withReplication(new ReplicationObject()
                .withEndpointType(EndpointType.DST)
                .withRemoteVolumeRegion(primaryVolumeLocation)
                .withRemoteVolumeResourceId(primaryVolumeId)
                .withReplicationSchedule(ReplicationSchedule.HOURLY)));

        Observable<VolumeInner> volumeInnerObservable = anfClient.volumes().createOrUpdateAsync(resourceGroupName, anfAccountName, capacityPoolName, volumeName, volumeInner);
        return CompletableFuture.completedFuture(volumeInnerObservable);
    }

    /**
     * Authorizes Data replication on the source volume
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroupName Resource group of the ANF resources
     * @param anfAccountName ANF account name
     * @param capacityPoolName ANF capacity pool name
     * @param primaryVolumeName ANF volume name
     * @param dataReplicationVolumeId Destination data replication volume Id
     * @return void
     */
    public static CompletableFuture<Observable<Void>> authorizeSourceReplicationAsync(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroupName, String anfAccountName, String capacityPoolName, String primaryVolumeName,String dataReplicationVolumeId)
    {
        anfClient.volumes().authorizeReplicationAsync(resourceGroupName,anfAccountName,capacityPoolName, primaryVolumeName, dataReplicationVolumeId);
        return CompletableFuture.completedFuture(null);
    }
}
