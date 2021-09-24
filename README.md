---
page_type: sample
languages:
- java
products:
- azure
- azure-netapp-files
description: "This project demonstrates how to use Java with NetApp Files SDK for Microsoft.NetApp resource provider to enable cross-region replication on NFS 4.1 Volume."
---  

# Azure NetAppFiles Cross-Region Replication (CRR) SDK NFS 4.1 Sample Java

This project demonstrates how to deploy a volume based on NFSv4.1 protocol with cross-region replication enabled using Java and Azure NetApp Files SDK.

In this sample application we perform the following operations:

* Creation
  * NetApp accounts
  * Capacity pools
  * NFSv4.1 Volumes with data protection
* Replication
  * Authorize Data Replication
  * Break Data Replication
  * Remove Data Replication
* Deletions
  * Volumes (source and destination)
  * Capacity pool
  * NetApp accounts
  
If you don't already have a Microsoft Azure subscription, you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212).

## Prerequisites

1. This project is built upon Maven, which must be installed in order to run the sample. Instructions on installing Maven can be found on their website [here](https://maven.apache.org/install.html)
1. Azure subscription
1. Subscription needs to be whitelisted for Azure NetApp Files. For more information, see
   [Submit a waitlist request for accessing the service](https://docs.microsoft.com/azure/azure-netapp-files/azure-netapp-files-register#waitlist).
1. Resource Group created
1. Virtual Network with a delegated subnet to Microsoft.Netapp/volumes resource. For more information, see
   [Guidelines for Azure NetApp Files network planning](https://docs.microsoft.com/en-us/azure/azure-netapp-files/azure-netapp-files-network-topologies).
1. For this sample console application to work, authentication is needed. We will use Service Principal based authentication
    1. Within an [Azure Cloud Shell](https://docs.microsoft.com/en-us/azure/cloud-shell/quickstart) session, make sure
       you're logged in at the subscription where you want to be associated with the service principal by default:
        ```bash
        az account show
       ```
       If this is not the correct subscription, use:
         ```bash
        az account set -s <subscription name or id>  
        ```
    1. Create a service principal using Azure CLI:
        ```bash
        az ad sp create-for-rbac --sdk-auth
        ```

       >Note: This command will automatically assign RBAC contributor role to the service principal at subscription level.
       You can narrow down the scope to the specific resource group where your tests will create the resources.

    1. Set the following environment variables from the output of the creation:

       Powershell
        ```powershell
        $env:AZURE_SUBSCRIPTION_ID = <subscriptionId>
        $env:AZURE_CLIENT_ID = <clientId>
        $env:AZURE_CLIENT_SECRET = <clientSecret>
        $env:AZURE_TENANT_ID = <tenantId>
        ```
       Bash
        ```bash
        export AZURE_SUBSCRIPTION_ID=<subscriptionId>
        export AZURE_CLIENT_ID=<clientId>
        export AZURE_CLIENT_SECRET=<clientSecret>
        export AZURE_TENANT_ID=<tenantId>
        ```

# What is anf-java-crr-sdk-nfs4.1-sample.dll doing? 

This sample project is dedicated to demonstrate how to enable cross-region replication in Azure NetApp Files for an NFSv4.1 enabled volume.
We start this execution by reading a configuration file (appsettings.json). 
This file has two sections: the 'general' section has information about the subscription to be used and if cleanup should take place afterwards. 
The other section, 'accounts', is the place that defines the accounts, capacity pools, volumes and replication volumes to be created.
This process will create a configuration object that is used extensively throughout the code to reference the resources to be created, updated, and deleted.

> Note: This sample will create the resources in the same order as the resources defined in appsettings.json 
> so the source volume should always be defined before the destination volume. 
> The destination volume should then have 'sourceVolume' defined, see _sample_appsettings.json.

The SDK will then move forward to the authentication process, generating a TokenCredential (service principal) that
is accepted by the NetAppFilesManager to create the management client, which is used to make the CRUD requests
and is also used extensively throughout the code.

Then the sample will start creating the accounts, capacity pools, and volumes, in this exact sequence
\(see [Azure NetApp Files storage hierarchy](https://docs.microsoft.com/en-us/azure/azure-netapp-files/azure-netapp-files-understand-storage-hierarchy)\).
After all resources have been created, the sample will authorize all replications defined in the appsettings.json file.
Finally, the cleanup process starts. It begins to break all replication and removes/deletes them. 
After all replications have successfully been removed the sample removes all resources deployed by this application.

# How the project is structured

The following table describes all files within this solution:

| Folder         | FileName                    | Description                                                                                                                                                                                                                                                               |
|----------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Root              | _sample_appsettings.json    | This is the sample appsettings.json file. To use it, make a copy and rename to 'appsettings.json'. Sizes are all defined in bytes. By default the appsettings.json is included in the .gitignore file to avoid unwanted extra information being committed to a public Git repo
| Root\\^           | main.java                   | Reads configuration, authenticates, executes all operations
| Root\\^           | Creation.java               | Class that contains all resource creation loops, following the hierarchy logic in order to successfully deploy Azure NetApp Files resources
| Root\\^           | Replication.java            | Class used for replication operations, for now it only has authorize replication
| Root\\^           | Cleanup.java                | Class that performs cleanup of all artifacts that were created during this sample application. It's called when the shouldCleanUp property is set to true under 'general' in appsettings.json file
| Root\\^\nfs.sdk.sample.common    | CommonSdk.java              | Class dedicated to nfs.sdk.sample.common operations related to ANF's SDK
| Root\\^\common    | ProjectConfiguration.java   | Class used to create the configuration object based on appsettings.json file contents
| Root\\^\nfs.sdk.sample.common    | ResourceUriUtils.java       | Class that exposes a few methods that help parsing Uri's, building new Uri's, or getting a resource name from a Uri, etc
| Root\\^\nfs.sdk.sample.common    | Utils.java                  | Class that exposes methods that help with getting the configuration object, byte conversion, etc
| Root\\^\model     | AzureAuthInfo.java          | POJO class to hold authentication information
>\\^ == src/main/java/sdk/sample                                                               

# How to run the console application

1. Clone it locally
    ```powershell
    git clone https://github.com/Azure-Samples/netappfiles-java-nfs4.1-sdk-sample.git
    ``` 
3. Change folder to **.\anf-java-crr-sdk-sample**
4. Make a copy of **_sample_appsettings.json** file, rename it to **appsettings.json** and modify its contents accordingly
      (at minimum all values between **\<\>** must be replaced with real values).
5. Make sure you have the environment variables previously described defined.
6. Compile the console application
    ```powershell
    mvn clean compile
    ```
7. Run the console application
    ```powershell
    mvn exec:java -Dexec.mainClass="sdk.sample.main"

# References

* [Resource limits for Azure NetApp Files](https://docs.microsoft.com/azure/azure-netapp-files/azure-netapp-files-resource-limits)
* [Azure NetApp Files - Cross-Region Replication](https://docs.microsoft.com/en-us/azure/azure-netapp-files/cross-region-replication-introduction)
* [Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/quickstart)
* [Azure NetApp Files documentation](https://docs.microsoft.com/azure/azure-netapp-files/)
* [Download Azure SDKs](https://azure.microsoft.com/downloads/)    
  
