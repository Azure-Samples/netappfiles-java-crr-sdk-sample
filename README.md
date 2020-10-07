---
page_type: sample
languages:
- Java
products:
- azure
- azure-netapp-files
description: "This project demonstrates how to use Java with NetApp Files SDK for Microsoft.NetApp resource provider to enable cross-region replication on NFS 4.1 Volume."
---

# Azure NetAppFiles Cross-Region Replication (CRR) SDK NFS 4.1 Sample Java

This project demonstrates how to deploy a volume based on NFSv4.1 protocol with cross-region replication enabled using Java and Azure NetApp Files SDK.

In this sample application we perform the following operations:

* Creation
  * Primary ANF Account
	| Primary Capacity pool 
		| Primary NFS v4.1 Volume 
		
 * Secondary ANF Account
	| Secondary Capacity pool
		| Secondary NFS v.1 Data Replication Volume with referencing to the primary volume Resource ID
			
 * Authorize Source volume with Desitnation Volume Resource ID
  
If you don't already have a Microsoft Azure subscription, you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212).

## Prerequisites

1. Azure Subscription
1. Subscription needs to be enabled for Azure NetApp Files. For more information, please refer to [this](https://docs.microsoft.com/azure/azure-netapp-files/azure-netapp-files-register#waitlist) document.
1. Resource Group created
1. Virtual Network with a delegated subnet to Microsoft.Netapp/volumes resource. For more information, please refer to [Guidelines for Azure NetApp Files network planning](https://docs.microsoft.com/en-us/azure/azure-netapp-files/azure-netapp-files-network-topologies)
1. For this sample console appplication work, we are using service principal based  authenticate, follow these steps in order to setup authentication:
    1. Within an [Azure Cloud Shell](https://docs.microsoft.com/en-us/azure/cloud-shell/quickstart) session, make sure you're logged on at the subscription where you want to be associated with the service principal by default:
        ```bash
        az account show
        ```
        If this is not the correct subscription, use             
          ```bash
         az account set -s <subscription name or id>  
         ```
    1. Create a service principal using Azure CLI
        ```bash
        az ad sp create-for-rbac --sdk-auth
        ```

        >Note: this command will automatically assign RBAC contributor role to the service principal at subscription level, you can narrow down the scope to the specific resource group where your tests will create the resources.

    1. Copy the output content and paste it in a file called azureauth.json, secure it with file system permissions and save it outside the tree related of your 	local git repo folder so the file doesn't get commited. 
    1. Set an environment variable pointing to the file path you just created, here is an example with Powershell and bash:
        Powershell 
        ```powershell
       [Environment]::SetEnvironmentVariable("AZURE_AUTH_LOCATION", "C:\sdksample\azureauth.json", "User")
       ```
        Bash
        ```bash
        export AZURE_AUTH_LOCATION=/sdksamples/azureauth.json
        ``` 


# What is anf-java-crr-sdk-nfs4.1-sample.dll doing? 

This sample project is dedicated to demonstrate how to enable cross-region replication in Azure NetApp Files for an NFS v4.1 enabled volume, similar to other examples, the authentication method is based on a service principal, this project will create two ANF Accounts in different regions with capacity pool. A single volume using Premium service level tier in the Source ANF, and Data Replication Volume with Standard service level tier in the destination region. 

The cleanup process takes place (not enabled by default, please change the variable shouldCleanUp to true at main file if you want the clean up code to take a place),deleting all resources in the reverse order following the hierarchy otherwise we can't remove resources that have nested resources still live. You will also notice that the clean up process uses a function called WaitForNoANFResource, at this moment this is required so we can workaround a current ARM behavior of reporting that the object was deleted when in fact its deletion is still in progress.
# How the project is structured

The following table describes all files within this solution:

| Folder         | FileName                    | Description                                                                                                                                                                                                                                                               |
|----------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Root\\^           | main.java                   | Reads configuration, authenticates, executes all operations
| Root\\^           | Creation.java               | Class that contains all resource creation loops, following the hierarchy logic in order to successfully deploy Azure NetApp Files resources
| Root\\^           | Cleanup.java                | Class that performs cleanup of all artifacts that were created during this sample application. Its call is commented out by default in main.java
| Root\\^\nfs.sdk.sample.common    | CommonSdk.java              | Class dedicated to nfs.sdk.sample.common operations related to ANF's SDK
| Root\\^\nfs.sdk.sample.common    | ResourceUriUtils.java       | Class that exposes a few methods that help parsing Uri's, building new Uri's, or getting a resource name from a Uri, etc
| Root\\^\nfs.sdk.sample.common    | ServiceCredentialsAuth.java | A small support class for extracting and creating credentials from a File
| Root\\^\nfs.sdk.sample.common    | Utils.java                  | Class that exposes methods that help with getting the configuration object, byte conversion, etc
| Root\\^\model     | AzureAuthInfo.java          | POJO class to hold authentication information
>\\^ == src/main/java/sdk/sample                                                               |

# How to run the console application

1. Clone it locally
    ```powershell
    git clone https://github.com/Azure-Samples/netappfiles-java-nfs4.1-sdk-sample.git
    ```
1. Make sure you change the variables located at **.netappfiles-java-crr-sdk-sample\src\main\main.java at RunAsync method.**
1. Change folder to **.\anf-java-crr-sdk-sample**
1. Since we're using service principal authentication flow, make sure you have the **azureauth.json** and its environment variable with the path to it defined (as previously described)
1. Compile the console application
    ```powershell
    mvn clean compile
    ```
1. Run the console application
    ```powershell
    mvn exec:java -Dexec.mainClass="sdk.sample.main"

Sample output
![e2e execution](./media/e2e-execution.png)

# References

* [Resource limits for Azure NetApp Files](https://docs.microsoft.com/azure/azure-netapp-files/azure-netapp-files-resource-limits)
* [Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/quickstart)
* [Azure NetApp Files documentation](https://docs.microsoft.com/azure/azure-netapp-files/)
* [Download Azure SDKs](https://azure.microsoft.com/downloads/)
 
