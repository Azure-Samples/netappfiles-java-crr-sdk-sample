package sdk.sample;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.netapp.NetAppFilesManager;
import sdk.sample.common.ProjectConfiguration;
import sdk.sample.common.Utils;
import sdk.sample.model.ModelNetAppAccount;

import java.util.List;

public class main
{
    public static void main(String[] args)
    {
        Utils.displayConsoleAppHeader();

        try
        {
            run();
            Utils.writeConsoleMessage("ANF CRR Java sample application successfully completed");
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage(e.getMessage());
        }

        // Note: this should not be here in a proper environment. I leave it here for a more compact sample that does what it needs to do and exits as soon as it finishes without waiting for other threads
        System.exit(0);
    }

    private static void run()
    {
        // Getting project configuration
        ProjectConfiguration config = Utils.getConfiguration("appsettings.json");
        if (config == null)
        {
            Utils.writeConsoleMessage("No appsettings.json file found. Exiting.");
            return;
        }

        List<ModelNetAppAccount> accounts = config.getAccounts();
        if (config.getAccounts() != null && config.getAccounts().isEmpty())
        {
            Utils.writeConsoleMessage("No ANF accounts defined within appsettings.json file. Exiting.");
            return;
        }

        // Instantiating a new ANF management client and authenticate
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        NetAppFilesManager manager = NetAppFilesManager
                .authenticate(credential, profile);

        //--------------------------------
        // Creating ANF Resources listed in the appsettings.json
        //--------------------------------
        Creation.createANFResources(accounts, manager.serviceClient());

        //--------------------------------
        // Authorize Data Replications from appsettings.json
        //--------------------------------
        Replication.authorizeReplications(accounts, manager.serviceClient());

        //--------------------------------
        // Run cleanup if set to true in appsettings.json
        //--------------------------------
        if (config.isShouldCleanUp())
            Cleanup.runCleanup(accounts, manager.serviceClient());
    }
}