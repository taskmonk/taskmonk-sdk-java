package io.taskmonk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.taskmonk.auth.OAuthClientCredentials;
import io.taskmonk.auth.TokenResponse;
import io.taskmonk.entities.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

public class TaskMonkClient {
    private static final Logger logger = LoggerFactory.getLogger(TaskMonkClient.class);


    HttpHost httpHost;
    TokenResponse tokenResponse;
    String projectId;
    OAuthClientCredentials credentials;

    private TokenResponse refreshToken() throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/oauth2/token").addParameter("grant_type", "client_credentials")
                .addParameter("client_id", credentials.getClientId())
                .addParameter("client_secret", credentials.getClientSecret());
        HttpPost post = new HttpPost(builder.build());
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .build();
        httpclient.start();

        Future<HttpResponse> future = httpclient.execute(post, null);
        HttpResponse response = future.get();
        String content = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper();
        TokenResponse tokenResponse = mapper.readValue(content, TokenResponse.class);
        httpclient.close();
        return tokenResponse;

    }
    private TokenResponse getTokenResponse() throws Exception {
        if (tokenResponse == null || tokenResponse.isExpired()) {
            tokenResponse = refreshToken();
        }
        return tokenResponse;
    }

    private void downloadFile(String url, String localPath) throws Exception {
        ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());
        FileOutputStream fos = new FileOutputStream(localPath);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

    }
    private HttpPost getHttpPost(String path, List<NameValuePair> parameters, ContentType contentType) throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + path);
        builder.addParameters(parameters);
        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Authorization", "Bearer " + getTokenResponse().getAccess_token());
        post.addHeader("Content-type", contentType.toString());
        return post;
    }
    private HttpGet getHttpGet(String path) throws Exception {
        return getHttpGet(path, new ArrayList<NameValuePair>());
    }
    private HttpGet getHttpGet(String path, List<NameValuePair> parameters) throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + path)
                .addParameters(parameters);
        HttpGet get = new HttpGet(builder.build());
        get.addHeader("Authorization", "Bearer " + getTokenResponse().getAccess_token());
        return get;
    }

    public String createBatch(String batchName) throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch");
        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Authorization", "Bearer " + getTokenResponse().getAccess_token());
        post.addHeader("Content-type", "application/json");

        NewBatchData newBatchData = new NewBatchData(batchName);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newBatchData);
        logger.debug("batch create content = {}", body);
        System.out.println("body = " + body);
        StringEntity stringEntity = new StringEntity(body);
        post.setEntity(stringEntity);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .build();
        httpclient.start();
        Future<HttpResponse> future = httpclient.execute(post, null);
        HttpResponse response = future.get();
        httpclient.close();
        String content = EntityUtils.toString(response.getEntity());

        Id batchId = mapper.readValue(content, Id.class);
        logger.debug("batch id = " + batchId);
        return batchId.id;

    }


    public TaskImportResponse uploadTasks(String batchName, File file) throws Exception  {
        logger.debug("Uploading tasks to batch {}", batchName);

        String batchId = createBatch(batchName);
        String path = file.getAbsolutePath();
        String fileType = FilenameUtils.getExtension(path);

        logger.debug("fileType = {}", fileType);
        byte[] bytes = Files.readAllBytes(file.toPath());
        ByteArrayOutputStream arrOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream zipOutputStream = new GZIPOutputStream(arrOutputStream);
        zipOutputStream.write(bytes);
        zipOutputStream.close();
        arrOutputStream.close();
        byte[] output = arrOutputStream.toByteArray();
        String encoded = Base64.getEncoder().encodeToString(output);

        logger.debug("Uploading {} bytes", encoded.length());
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch/" + batchId + "/tasks/import");
        builder.addParameter("fileType", fileType);
        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Authorization", "Bearer " + getTokenResponse().getAccess_token());

        StringEntity stringEntity = new StringEntity(encoded);
        post.setEntity(stringEntity);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .build();
        httpclient.start();
        Future<HttpResponse> future = httpclient.execute(post, null);
        HttpResponse response = future.get();
        String content = EntityUtils.toString(response.getEntity());

        httpclient.close();

        ObjectMapper mapper = new ObjectMapper();
        TaskImportResponse importResponse = mapper.readValue(content, TaskImportResponse.class);
        System.out.println("importResponse = " + importResponse);

        return importResponse;

    }


    private void waitForCompletion(String jobId) throws Exception {
        JobProgressResponse jobProgressResponse = getJobProgress(jobId);
        while (!jobProgressResponse.isCompleted()) {
            Thread.sleep(1000);
            jobProgressResponse = getJobProgress(jobId);
        }
        return;

    }

    /**
     * Get the batch output in a local file path
     * @param batchId
     * @param outputFormat output format for the file - "CSV" or "Excel"
     * @param outputPath - path where the output file should be created
     */
    public void getBatchOutput(String batchId, String outputFormat, String outputPath) throws Exception {
        String url = "/api/project/" + projectId + "/batch/" + batchId + "/output";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("output_format", outputFormat));
        Map<String, List<String>> fieldNames = new HashMap<String, List<String>>();
        fieldNames.put("fieldNames", new ArrayList<String>());
        ObjectMapper mapper = new ObjectMapper();
        String content =  mapper.writeValueAsString(fieldNames);
        HttpPost post = getHttpPost(url, parameters, ContentType.APPLICATION_JSON);
        post.setEntity(new StringEntity(content));
        BatchOutput batchOutput = getResponse(post, BatchOutput.class);
        logger.debug("batchOutput = {}", batchOutput);

        waitForCompletion(batchOutput.getJobId());
        downloadFile(batchOutput.getFileUrl(), outputPath);

    }

    private <T> T getResponse(HttpPost post, Class<T> clazz) throws ExecutionException, InterruptedException, IOException {
        logger.debug("Running post {}", post);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .build();
        httpclient.start();
        Future<HttpResponse> future = httpclient.execute(post, null);
        HttpResponse response = future.get();
        String content = EntityUtils.toString(response.getEntity());
        logger.debug("Got response {}", content);
        ObjectMapper mapper = new ObjectMapper();
        T result = mapper.readValue(content, clazz);
        httpclient.close();
        return result;


    }
   private <T> T getResponse(HttpGet get, Class<T> clazz) throws ExecutionException, InterruptedException, IOException {
        logger.debug("Running get {}", get);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .build();
        httpclient.start();
        Future<HttpResponse> future = httpclient.execute(get, null);
        HttpResponse response = future.get();
        String content = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper();
        T result = mapper.readValue(content, clazz);
        logger.debug("Got response {}", result);
        httpclient.close();
        return result;


    }

    public JobProgressResponse getJobProgress(String jobId) throws Exception {
        String url = "/api/project/" + projectId + "/job/" + jobId + "/status";
        HttpGet httpGet = getHttpGet(url);
        JobProgressResponse getResponse = getResponse(httpGet, JobProgressResponse.class);
        return getResponse;
    }
    public JobProgressResponse getJobProgressBatch(String batchId) throws Exception {
        String url = "/api/project/" + projectId + "/job/" + batchId + "/status";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("input_type", "batch"));
        HttpGet httpGet = getHttpGet(url, parameters);
        JobProgressResponse getResponse = getResponse(httpGet, JobProgressResponse.class);
        return getResponse;
    }
    public BatchStatus getBatchStatus(String batchId) throws Exception {
        String url  = "/api/project/v2/" + projectId + "/batch/" + batchId + "/status";
        HttpGet httpGet = getHttpGet(url);
        BatchStatus getResponse = getResponse(httpGet, BatchStatus.class);
        return getResponse;
    }

    public Boolean isProcessComplete(String batchId) throws Exception {
        BatchStatus batchStatus = getBatchStatus(batchId);
        return batchStatus.getCompleted() == batchStatus.getTotal();
    }

    public Boolean isUploadComplete(String batchId) throws Exception {
        JobProgressResponse jobResponse = getJobProgressBatch(batchId);
        return jobResponse.isCompleted();

    }
    public TaskMonkClient(String projectId, String server, OAuthClientCredentials credentials) {
        this.projectId = projectId;
        this.credentials = credentials;
        if (server.startsWith("http")) {
            this.httpHost = new HttpHost(server);
        } else {
            httpHost = new HttpHost(server, 80, "http");

        }
    }

}
