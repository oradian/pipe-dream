package com.oradian.pipedream

import java.util.Collections;

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.DomainRequirement

import hudson.util.Secret
import hudson.security.ACL

class Credentials implements Serializable {
    private String credentialsId

    Credentials(String credentialsId) {
        this.credentialsId = credentialsId
    }

    public StandardUsernameCredentials getCredentials() {
        def credentials = CredentialsMatchers.firstOrNull(
            CredentialsProvider
                .lookupCredentials(StandardUsernameCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
            CredentialsMatchers.withId(credentialsId)
        )
        if (credentials == null) {
            throw new IllegalArgumentException("Can't find credentials with id '${credentialsId}'")
        }
        return credentials
    }

    public String getUsername() {
        return getCredentials().getUsername();
    }

    public String getPassword() {
        return Secret.toString(StandardUsernamePasswordCredentials.class.cast(getCredentials()).getPassword());
    }
}
