package com.oradian.pipedream

import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.io.File;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils
import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.binary.Base64;

import hudson.FilePath;

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class Nexus implements Serializable {
    private static Logger logger = Logger.getLogger(Nexus.class.name)

    private def step
    private FilePath cwd
    private String nexusUrl
    private String nexusCredentialsId

    def getNexusUrl() { nexusUrl }

    Nexus(CpsScript step, String nexusUrl = null, String nexusCredentialsId = null) {
        this.step = step
        this.cwd = step.getContext(FilePath)
        this.nexusUrl = nexusUrl ?: step.env.NEXUS_URL
        this.nexusCredentialsId = nexusCredentialsId ?: step.env.NEXUS_CREDENTIALS_ID ?: 'nexus'
    }

    private def handleHttpError(HttpURLConnection http, int expectedStatus) {
        def errorStream = null;
        int responseCode = http.getResponseCode()
        if (responseCode < 200 || responseCode > 299) {
            errorStream = http.getErrorStream()
        } else {
            errorStream = http.getInputStream()
        }

        if (responseCode != expectedStatus) {
            logger.severe("Error parsing response with status $responseCode, expected $expectedStatus: $errorStream")
        }

        return responseCode
    }

    private def getAuthorization() {
        def credentials = new Credentials(nexusCredentialsId)

        return "Basic " + Base64.encodeBase64String("${credentials.username}:${credentials.password}".getBytes(StandardCharsets.UTF_8))
    }

    def uploadFileToRaw(String repository, String destination, String filepath = null) {
        def url = new URL("${nexusUrl}/repository/${repository}/${destination}")
        def http = (HttpURLConnection) url.openConnection()
        http.setRequestMethod('PUT')
        http.setRequestProperty("Authorization", getAuthorization())
        http.setChunkedStreamingMode(32 * 1024 * 1024)
        http.setDoOutput(true)

        if (filepath != null) {
            cwd.child(filepath).copyTo(http.outputStream)
        }
        http.outputStream.close()

        logger.finest("Uploading ${filepath} to ${url}")
        if (handleHttpError(http, 201) != 201) {
            def fileDescription = filepath ? "file ${filepath}" : "empty file"
            throw new Exception("Error uploading ${fileDescription} to ${url}")
        }

        http.disconnect()
    }

    InputStream getFromRaw(String repository, String path) {
        def url = new URL("${nexusUrl}/repository/${repository}/${path}")
        def http = (HttpURLConnection) url.openConnection()
        http.setRequestProperty("Authorization", getAuthorization())

        logger.finest("Receiving $url")
        switch (handleHttpError(http, 200)) {
            case 200:
                break;
            case 404:
                throw new InvalidPathException(path, "No such file found on ${url}")
            default:
                throw new Exception("Couldn't fetch ${url}")
        }

        return http.getInputStream();
    }

    def getFromRaw(String repository, String path, String destination) {
        def inputStream = getFromRaw(repository, path)
        cwd.child(destination).copyFrom(inputStream)
    }
}
