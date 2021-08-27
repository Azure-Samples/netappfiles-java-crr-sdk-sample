// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package sdk.sample.common;

import com.google.gson.Gson;
import sdk.sample.model.ModelNetAppAccount;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class ProjectConfiguration
{
    // List of Accounts to be created
    private List<ModelNetAppAccount> accounts;

    // Subscription Id where account(s) will be deployed
    private String subscriptionId;

    // Should resources be cleaned up afterwards
    private boolean shouldCleanUp;

    public static ProjectConfiguration readFromJsonFile(String path)
    {
        Gson gson = new Gson();
        AppSettings appSettings;

        try
        {
            appSettings = gson.fromJson(new FileReader(path), AppSettings.class);
        }
        catch (FileNotFoundException e)
        {
            Utils.writeWarningMessage("Could not find appsettings.json. Unable to load project configuration. Exiting.");
            return null;
        }

        ProjectConfiguration config = new ProjectConfiguration();
        config.setAccounts(appSettings.getAccounts());
        config.setSubscriptionId(appSettings.getGeneral().get("subscriptionId"));
        config.setShouldCleanUp(Boolean.parseBoolean(appSettings.getGeneral().get("shouldCleanUp")));

        return config;
    }

    public List<ModelNetAppAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<ModelNetAppAccount> accounts) {
        this.accounts = accounts;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public boolean isShouldCleanUp() {
        return shouldCleanUp;
    }

    public void setShouldCleanUp(boolean shouldCleanUp) {
        this.shouldCleanUp = shouldCleanUp;
    }

    private static class AppSettings
    {
        private List<ModelNetAppAccount> accounts;
        private Map<String, String> general;

        public List<ModelNetAppAccount> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<ModelNetAppAccount> accounts) {
            this.accounts = accounts;
        }

        public Map<String, String> getGeneral() {
            return general;
        }

        public void setGeneral(Map<String, String> general) {
            this.general = general;
        }
    }
}
