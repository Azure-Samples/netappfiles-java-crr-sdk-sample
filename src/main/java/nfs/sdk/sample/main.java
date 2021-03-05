package nfs.sdk.sample;

import com.ea.async.Async;
import com.microsoft.azure.management.netapp.v2020_09_01.AuthorizeRequest;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.CapacityPoolInner;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.NetAppAccountInner;
import com.microsoft.azure.management.netapp.v2020_09_01.implementation.VolumeInner;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import nfs.sdk.sample.common.ResourceUriUtils;
import nfs.sdk.sample.common.ServiceCredentialsAuth;
import nfs.sdk.sample.common.Utils;
import rx.Observable;

import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;

public class main {
    // Subscription - Change SubId below
    private static final String subscriptionId = "[Subscription ID here]";

    // Primary ANF
    private static final String primaryResourceGroupName = "[Primary Resource Group Name]";
    private static final String primaryLocation = "[Primary Resources Location]";
    private static final String primaryVNETName = "[Primary VNET Name]";
    private static final String primarySubnetName = "[Primary SubNet Name]";
    private static final String primaryAnfAccountName = "[Primary ANF Account name]";
    private static final String primaryCapacityPoolName = "[Primary ANF Capacity Pool name]";
    private static final String primaryVolumeName = "[Primary ANF Volume name]";
    private static final String primaryServiceLevel = "PREMIUM";

    // Secondary ANF
    private static final String secondaryResourceGroupName = "[Secondary Resource Group Name]";
    private static final String secondaryLocation = "[Secondary Resources Location]";
    private static final String secondaryVNETName = "[Secondary VNET Name]";
    private static final String secondarySubnetName = "[Secondary SubNet Name]";
    private static final String secondaryAnfAccountName = "[Secondary ANF Account name]";
    private static final String secondaryCapacityPoolName = "[Secondary ANF Capacity Pool name]";
    private static final String secondaryVolumeName = "[Primary ANF Volume name]";
    private static final String secondaryServiceLevel = "STANDARD";

    // Shared ANF Properties
    private static final long capacityPoolSize = 4398046511104L;  // 4TiB which is minimum size
    private static final long volumeSize = 107374182400L;  // 100GiB - volume minimum size

    // If resources should be cleaned up
    private static final boolean shouldCleanUp = false;

    public static void main(String[] args) {
        Utils.displayConsoleAppHeader();
        try {
            Async.init();
            runAsync();
            Utils.writeConsoleMessage("ANF CRR Java sample application successfully completed");
        } catch (Exception e) {
            Utils.writeErrorMessage(e.getMessage());
        }
        System.exit(0);
    }

    private static CompletableFuture<Void> runAsync() throws Exception {

        // Authenticate using service principal
        Utils.writeConsoleMessage("Authenticating with Azure using service principal...");
        ServiceClientCredentials credentials = await(ServiceCredentialsAuth.getServicePrincipalCredentials(System.getenv("AZURE_AUTH_LOCATION")));
        if(credentials ==null)
        {
            return CompletableFuture.completedFuture(null);
        }

        // Instantiate a new ANF client
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        AzureNetAppFilesManagementClientImpl anfClient = new AzureNetAppFilesManagementClientImpl(credentials);
        anfClient.withSubscriptionId(subscriptionId);

        //--------------------------------
        //Creating Primary ANF Resources
        //--------------------------------
        Utils.writeConsoleMessage("Creating Primary ANF Account...");
        Observable<NetAppAccountInner> primaryAccount = await(Creation.createOrUpdateAccountAsync(anfClient, primaryResourceGroupName, primaryAnfAccountName,primaryLocation));
        Utils.writeSuccessMessage(primaryAccount.toBlocking().first().id());
        Utils.writeSuccessMessage("Primary ANF account was created successfully");

        Utils.writeConsoleMessage("Creating Primary ANF CapacityPool...");
        Observable<CapacityPoolInner> primaryCapacityPool = await(Creation.createOrUpdateCapacityPoolAsync(anfClient, primaryResourceGroupName, primaryAnfAccountName, primaryCapacityPoolName, primaryServiceLevel,capacityPoolSize, primaryLocation));
        String primaryCapacityPoolId = primaryCapacityPool.toBlocking().first().id();
        Utils.writeSuccessMessage(primaryCapacityPoolId);
        Utils.writeSuccessMessage("Primary ANF CapacityPool was created successfully");

        Utils.writeConsoleMessage("Creating Primary ANF NFSv4.1 Volume...");
        String primarySubnetId = new StringBuilder().append("/subscriptions/").append(subscriptionId).append("/resourceGroups/").append(primaryResourceGroupName)
                .append("/providers/Microsoft.Network/virtualNetworks/").append(primaryVNETName).append("/subnets/").append(primarySubnetName).toString();
        Observable<VolumeInner> primaryVolume = await(Creation.createOrUpdateVolumeAsync(anfClient,primaryResourceGroupName, primaryAnfAccountName, primaryCapacityPoolName, primaryVolumeName, volumeSize,primaryServiceLevel, primarySubnetId,primaryLocation));
        String primaryVolumeId = primaryVolume.toBlocking().first().id();
        Utils.writeSuccessMessage(primaryVolumeId);
        Utils.writeConsoleMessage("Waiting for primary volume to be available...");
        ResourceUriUtils.waitForANFResource(anfClient,primaryVolumeId,VolumeInner.class);
        Utils.writeSuccessMessage("Primary ANF NFSv4.1 Volume was created successfully");

        //--------------------------------
        //Creating Secondary ANF Resources
        //--------------------------------
        Utils.writeConsoleMessage("Creating Secondary ANF account...");
        Observable<NetAppAccountInner> secondaryAccount = await(Creation.createOrUpdateAccountAsync(anfClient, secondaryResourceGroupName, secondaryAnfAccountName, secondaryLocation));
        Utils.writeSuccessMessage(secondaryAccount.toBlocking().first().id());
        Utils.writeSuccessMessage("Secondary ANF account was created successfully");

        Utils.writeConsoleMessage("Creating Secondary ANF CapacityPool...");
        Observable<CapacityPoolInner> secondaryCapacityPool = await(Creation.createOrUpdateCapacityPoolAsync(anfClient, secondaryResourceGroupName, secondaryAnfAccountName, secondaryCapacityPoolName, secondaryServiceLevel,capacityPoolSize, secondaryLocation));
        String secondaryCapacityPoolId = secondaryCapacityPool.toBlocking().first().id();
        Utils.writeSuccessMessage(secondaryCapacityPoolId);
        Utils.writeSuccessMessage("Secondary ANF CapacityPool was created successfully");

        Utils.writeConsoleMessage("Creating secondary ANF NFSv4.1 Data Replication Volume...");
        String secondarySubnetId = new StringBuilder().append("/subscriptions/").append(subscriptionId).append("/resourceGroups/").append(secondaryResourceGroupName)
                .append("/providers/Microsoft.Network/virtualNetworks/").append(secondaryVNETName).append("/subnets/").append(secondarySubnetName).toString();
        Observable<VolumeInner> secondaryVolume = await(Creation.createOrUpdateDataReplicationVolumeAsync(anfClient, secondaryResourceGroupName, secondaryAnfAccountName, secondaryCapacityPoolName, secondaryVolumeName, volumeSize, secondaryServiceLevel, secondarySubnetId, secondaryLocation, primaryVolume.toBlocking().first().id(), primaryLocation));
        String secondaryVolumeId = secondaryVolume.toBlocking().first().id();
        Utils.writeSuccessMessage(secondaryVolumeId);

        Utils.writeConsoleMessage("Waiting for data replication volume to be available...");
        ResourceUriUtils.waitForANFResource(anfClient,secondaryVolumeId,VolumeInner.class);
        Utils.writeSuccessMessage("Secondary ANF NFSv4.1 Data Replication Volume was created successfullyI ");

        //--------------------------------
        //Authorizing Data Replication on the Source (Primary) Volume
        //--------------------------------
        Utils.writeConsoleMessage("Authorizing replication in source region...");
        await(Creation.authorizeSourceReplicationAsync(anfClient, primaryResourceGroupName, primaryAnfAccountName, primaryCapacityPoolName, primaryVolumeName, secondaryVolumeId));
        ResourceUriUtils.WaitForCompleteReplicationStatus(anfClient, secondaryResourceGroupName, secondaryAnfAccountName, secondaryCapacityPoolName, secondaryVolumeName);
        Utils.writeSuccessMessage("Successfully authorized primary volume for data replication");

        if(shouldCleanUp)
        {
            //Break Connection between Source and destination volume
            await(Cleanup.breakDataReplicationAsync(anfClient,secondaryResourceGroupName,secondaryAnfAccountName, secondaryCapacityPoolName, secondaryVolumeName));

            //Wait for connection to become "Broken"
            ResourceUriUtils.WaitForBrokenReplicationStatus(anfClient,secondaryResourceGroupName,secondaryAnfAccountName, secondaryCapacityPoolName, secondaryVolumeName);

            //Delete replication connection
            await(Cleanup.deleteDataReplicationVolumeAsync(anfClient,secondaryResourceGroupName,secondaryAnfAccountName, secondaryCapacityPoolName, secondaryVolumeName));

            // Delete secondary ANF resources
            await(Cleanup.deleteAnfVolumeAsync(anfClient, secondaryResourceGroupName, secondaryAnfAccountName, secondaryCapacityPoolName, secondaryVolumeName));
            // Wait for Data replication volume to be fully deleted
            ResourceUriUtils.waitForNoANFResource(anfClient,secondaryVolumeId,VolumeInner.class);
            await(Cleanup.deleteAnfCapacityPoolAsync(anfClient, secondaryResourceGroupName, secondaryAnfAccountName, secondaryCapacityPoolName));
            // Wait for secondary capacity pool to be fully deleted
            ResourceUriUtils.waitForNoANFResource(anfClient,secondaryCapacityPoolId,VolumeInner.class);
            await(Cleanup.deleteAnfAccountAsync(anfClient, secondaryResourceGroupName, secondaryAnfAccountName));

            // Delete primary ANF resources
            await(Cleanup.deleteAnfVolumeAsync(anfClient, primaryResourceGroupName, primaryAnfAccountName, primaryCapacityPoolName, primaryVolumeName));
            // Wait for primary Volume to be fully deleted
            ResourceUriUtils.waitForNoANFResource(anfClient,primaryVolumeId,VolumeInner.class);
            await(Cleanup.deleteAnfCapacityPoolAsync(anfClient, primaryResourceGroupName, primaryAnfAccountName, primaryCapacityPoolName));
            // Wait for primary capacity pool to be fully deleted
            ResourceUriUtils.waitForNoANFResource(anfClient,primaryCapacityPoolId,VolumeInner.class);
            await(Cleanup.deleteAnfAccountAsync(anfClient, primaryResourceGroupName, primaryAnfAccountName));
        }

        return CompletableFuture.completedFuture(null);
    }
}