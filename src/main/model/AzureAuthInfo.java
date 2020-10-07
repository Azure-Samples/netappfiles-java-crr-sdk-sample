package Model;
// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

/**
 Creates an instance of AzureAuthInfo. This is used to read the contents of azureauth.json to perform SP authentication
 */
public class AzureAuthInfo {

    private String clientId;
    private String clientSecret;
    private String subscriptionId;
    private String tenantId;
    private String activeDirectoryEndpointUrl;
    private String resourceManagerEndpointUrl;
    private String activeDirectoryGraphResourceId;
    private String sqlManagementEndpointUrl;
    private String galleryEndpointUrl;
    private String managementEndpointUrl;

    public String getClientId(){return clientId;}
    public String getClientSecret(){return clientSecret;}
    public String getSubscriptionId(){return subscriptionId;}
    public String getTenantId(){return tenantId;}
    public String getActiveDirectoryEndpointUrl(){return activeDirectoryEndpointUrl;}
    public String getResourceManagerEndpointUrl(){return resourceManagerEndpointUrl;}
    public String getActiveDirectoryGraphResourceId(){return activeDirectoryGraphResourceId;}
    public String getSqlManagementEndpointUrl(){return sqlManagementEndpointUrl;}
    public String getGalleryEndpointUrl(){return galleryEndpointUrl;}
    public String getManagementEndpointUrl(){return managementEndpointUrl;}

    public void setClientId(String clientId){this.clientId = clientId;}
    public void setClientSecret(String clientSecret){this.clientSecret = clientSecret;}
    public void setSubscriptionId(String subscriptionId){this.subscriptionId = subscriptionId;}
    public void setTenantId(String tenantId){this.tenantId = tenantId;}
    public void setActiveDirectoryEndpointUrl(String activeDirectoryEndpointUrl){this.activeDirectoryEndpointUrl = activeDirectoryEndpointUrl;}
    public void setResourceManagerEndpointUrl(String resourceManagerEndpointUrl){this.resourceManagerEndpointUrl = resourceManagerEndpointUrl;}
    public void setActiveDirectoryGraphResourceId(String activeDirectoryGraphResourceId){this.activeDirectoryGraphResourceId = activeDirectoryGraphResourceId;}
    public void setSqlManagementEndpointUrl(String sqlManagementEndpointUrl){this.sqlManagementEndpointUrl = sqlManagementEndpointUrl;}
    public void setGalleryEndpointUrl(String galleryEndpointUrl){this.galleryEndpointUrl = galleryEndpointUrl;}
    public void setManagementEndpointUrl(String managementEndpointUrl){this.managementEndpointUrl = managementEndpointUrl;}
}
