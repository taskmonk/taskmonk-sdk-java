package io.taskmonk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.taskmonk.auth.OAuthClientCredentials;
import io.taskmonk.auth.TokenResponse;
import io.taskmonk.clientexceptions.*;
import io.taskmonk.entities.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.LaxRedirectStrategy;
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
    HttpHost proxyHost;
    TokenResponse tokenResponse;
    String projectId;
    OAuthClientCredentials credentials;

    private TokenResponse refreshToken() throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/oauth2/token").addParameter("grant_type", "client_credentials")
                .addParameter("client_id", credentials.getClientId())
                .addParameter("client_secret", credentials.getClientSecret());
        HttpPost post = new HttpPost(builder.build());
        return invoke(post, TokenResponse.class, false);

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
        return get;
    }


    /**
     * Create a new batch in an existing project
     * @param batchName . name of the batch to be created
     * @return {@link String} returns the id of the batch
     * @throws  io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws  io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws  io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws  io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     */
    public String createBatch(String batchName) throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch");
        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Content-type", "application/json");

        NewBatchData newBatchData = new NewBatchData(batchName);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newBatchData);
        logger.trace("batch create content = {}", body);
        StringEntity stringEntity = new StringEntity(body);
        post.setEntity(stringEntity);

        return invoke(post, Id.class).id;
    }

    private <T> T invoke(HttpUriRequest request, Class<T> clazz) throws Exception {
        return invoke(request, clazz, true);
    }
    private <T> T invoke(HttpUriRequest request, Class<T> clazz, Boolean addAuthorization) throws Exception {
        if (addAuthorization) {
            request.addHeader("Authorization", "Bearer " + getTokenResponse().getAccess_token());
        }
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setProxy(proxyHost)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        httpclient.start();
        Future<HttpResponse> httpResponse = httpclient.execute(request, null);
        logger.trace("Invoking : {} ", request);
        HttpResponse response = httpResponse.get();
        try {

            if (response.getStatusLine().getStatusCode() == StatusConstants.StatusCode.OK.getCode() ||
                    response.getStatusLine().getStatusCode() == StatusConstants.StatusCode.CREATED.getCode()) {
                String content = EntityUtils.toString(response.getEntity());
                ObjectMapper mapper = new ObjectMapper();
                T result = mapper.readValue(content, clazz);
                logger.trace("result {}", result);
                return result;
            } else {
                throw handleException(response.getStatusLine().getStatusCode());
            }
        } finally {
            httpclient.close();
        }

    }

    /**
     * update the details of an existing batch
     * @param batchId - id of an existing batch
     * @param batchName - name of new batch
     * @param priority - priority of batch
     * @param comments - comments in new batch
     * @param notifications - notifications of a new batch
     * @return {@link String} - returns the id of the updated batch
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws  io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws  io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws  io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     */
    public String updateBatch(String batchId, String batchName, Short priority, String comments, List<Notification> notifications) throws Exception{
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch/" + batchId);
        HttpPut put = new HttpPut(builder.build());
        put.addHeader("Content-type", "application/json");
        NewBatchData newBatchData = new NewBatchData(batchName);
        newBatchData.setComments(comments);
        newBatchData.setNotifications(notifications);
        newBatchData.setPriority(priority);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(newBatchData);
        logger.debug("update batch content {} ", body);
        StringEntity entity = new StringEntity(body);
        put.setEntity(entity);
        return invoke(put, Id.class).id;
    }

    /**
     * Create a new batch in an existing project and add tasks to it
     * @param batchName - name of the batch to be created
     * @param file - file of the tasks to be added
     * @return {@link TaskImportResponse} returns the task import response
     * @throws  io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws  io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws  io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws  io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     */
    public TaskImportResponse uploadTasks(String batchName, File file) throws Exception  {
        logger.debug("Uploading tasks to batch {}", batchName);

        String batchId = createBatch(batchName);
        String path = file.getAbsolutePath();
        String fileType = FilenameUtils.getExtension(path);

        byte[] bytes = Files.readAllBytes(file.toPath());
        ByteArrayOutputStream arrOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream zipOutputStream = new GZIPOutputStream(arrOutputStream);
        zipOutputStream.write(bytes);
        zipOutputStream.close();
        arrOutputStream.close();
        byte[] output = arrOutputStream.toByteArray();
        String encoded = Base64.getEncoder().encodeToString(output);
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/v2/" + projectId + "/batch/" + batchId + "/tasks/import");
        builder.addParameter("fileType", fileType);
        HttpPost post = new HttpPost(builder.build());

        StringEntity stringEntity = new StringEntity(encoded);
        post.setEntity(stringEntity);
        TaskImportResponse result = invoke(post, TaskImportResponse.class);
        logger.debug("Upload task id = {}", result.job_id);
        result.setBatch_id(batchId);
        return result;

    }


    /**
     * Create a new batch in an existing project and add tasks to it from an accessible url
     * @param batchName - name of the batch to be created
     * @param taskUrl - url of the file from which the tasks will be imported
     * @param  fileType - type of file from which the tasks would be fetched
     * @return {@link TaskImportResponse} - returns the task import response
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     */
    public TaskImportResponse uploadTasksUrl(String batchName, String taskUrl, String fileType) throws  Exception {

        String batchId = createBatch(batchName);

        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch/" + batchId + "/tasks/import/url");
        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Content-type", "application/json");

        ImportUrl importUrl = new ImportUrl(taskUrl, fileType);
        ObjectMapper mapper = new ObjectMapper();

        StringEntity entity = new StringEntity(mapper.writeValueAsString(importUrl));
        post.setEntity(entity);
        TaskImportResponse result = invoke(post, TaskImportResponse.class);
        result.setBatch_id(batchId);
        return result;
    }


    /**
     * Add tasks to an existing batch
     * @param batchId - batch id of an existing batch to which the tasks are to be added
     * @param file - file of the tasks to be added
     * @return {@link TaskImportResponse} - returns the task import response
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs

     */
    public TaskImportResponse uploadTasksToBatch(String batchId, File file) throws  Exception
    {
        String path = file.getAbsolutePath();
        String fileType = FilenameUtils.getExtension(path);
        byte[] bytes = Files.readAllBytes(file.toPath());
        ByteArrayOutputStream arrOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream zipOutputStream = new GZIPOutputStream(arrOutputStream);
        zipOutputStream.write(bytes);
        zipOutputStream.close();
        arrOutputStream.close();
        byte[] output = arrOutputStream.toByteArray();
        String encoded = Base64.getEncoder().encodeToString(output);

        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch/" + batchId + "/tasks/import");
        builder.addParameter("fileType", fileType);
        HttpPost post = new HttpPost(builder.build());
        StringEntity stringEntity = new StringEntity(encoded);
        post.setEntity(stringEntity);
        TaskImportResponse result = invoke(post, TaskImportResponse.class);
        logger.debug("Task upload job id = {}", result.job_id);
        return result;

    }


    /**
     * Add tasks to an existing batch from an accessible url
     * @param batchId - batch id of the batch to which the tasks are to be added
     * @param taskUrl - url of the file from which the tasks would be imported
     * @param  fileType - type of file from which the tasks would be fetched
     * @return {@link TaskImportResponse} - returns the task import response
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public TaskImportResponse uploadTasksUrlToBatch(String batchId, String taskUrl, String fileType) throws Exception
    {
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/batch/" + batchId + "/tasks/import/url");
        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Content-type", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        ImportUrl importUrl = new ImportUrl(taskUrl, fileType);

        StringEntity entity = new StringEntity(mapper.writeValueAsString(importUrl));
        post.setEntity(entity);

        return invoke(post, TaskImportResponse.class);
    }
    /**
     * Add an external task
     * @param task - the task to be added
     * @return task id of the newly created task
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public String addTask(Task task) throws Exception {
        URIBuilder builder = new URIBuilder(httpHost.toString() + "/api/project/" + projectId + "/task/external");

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(task);
        StringEntity entity = new StringEntity(body);

        HttpPost post = new HttpPost(builder.build());
        post.addHeader("Content-type", "application/json");

        post.setEntity(entity);
        return invoke(post, Id.class).id;

    }

    private void waitForCompletion(String jobId) throws Exception {
        JobProgressResponse jobProgressResponse = getJobProgress(jobId);
        while (!jobProgressResponse.isCompleted()) {
            Thread.sleep(5000);
            jobProgressResponse = getJobProgress(jobId);
        }
        return;

    }

    /**
     * Get the batch output in a local file path
     * @param batchId - batch id of an existing batch
     * @param outputFormat output format for the file - "CSV" or "Excel"
     * @param outputPath - path where the output file should be created
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public void getBatchOutput(String batchId, String outputFormat, String outputPath) throws Exception {
        String url = "/api/project/v2/" + projectId + "/batch/" + batchId + "/output";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("output_format", outputFormat));
        Map<String, List<String>> fieldNames = new HashMap<String, List<String>>();
        fieldNames.put("field_names", new ArrayList<String>());
        ObjectMapper mapper = new ObjectMapper();
        String content =  mapper.writeValueAsString(fieldNames);
        HttpPost post = getHttpPost(url, parameters, ContentType.APPLICATION_JSON);
        post.setEntity(new StringEntity(content));
        BatchOutput batchOutput = invoke(post, BatchOutput.class);

        waitForCompletion(batchOutput.getjob_id());
        downloadFile(batchOutput.getfile_url(), outputPath);

    }

    private  Exception handleException(int statusCode)   {

        logger.error("status code = {}", statusCode);
        if(StatusConstants.StatusCode.FORBIDDEN.getCode() == statusCode)
            return new ForbiddenException(StatusConstants.StatusCode.FORBIDDEN.getDisplay());
        else  if(StatusConstants.StatusCode.INTERNALSERVERERROR.getCode() == statusCode)
            return new InternalServerError(StatusConstants.StatusCode.INTERNALSERVERERROR.getDisplay());
        else  if(StatusConstants.StatusCode.NOTFOUND.getCode() == statusCode)
            return new NotFoundException(StatusConstants.StatusCode.NOTFOUND.getDisplay());
        else
            return new UnhandledException("Got status code : " + statusCode);

    }



    /**
     * Get the progress of a job
     * @param jobId - job id of the job
     * @return {@link JobProgressResponse} - returns the job progress response
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public JobProgressResponse getJobProgress(String jobId) throws Exception {
        String url = "/api/project/" + projectId + "/job/" + jobId + "/status";
        HttpGet httpGet = getHttpGet(url);
        JobProgressResponse getResponse = invoke(httpGet, JobProgressResponse.class);
        return getResponse;
    }

    /**
     * Get the progress of batch
     * @param batchId - id of the batch
     * @return {@link JobProgressResponse} - returns the job progress response
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public JobProgressResponse getJobProgressBatch(String batchId) throws Exception {
        String url = "/api/project/" + projectId + "/job/" + batchId + "/status";
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("input_type", "batch"));
        HttpGet httpGet = getHttpGet(url, parameters);
        JobProgressResponse getResponse = invoke(httpGet, JobProgressResponse.class);
        return getResponse;
    }

    /**
     * Get the status of the batch
     * @param batchId - id of the batch
     * @return {@link BatchStatus} - returns the batch status
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */

    public BatchStatus getBatchStatus(String batchId) throws Exception {
        String url  = "/api/project/v2/" + projectId + "/batch/" + batchId + "/status";
        HttpGet httpGet = getHttpGet(url);
        BatchStatus getResponse = invoke(httpGet, BatchStatus.class);
        return getResponse;
    }
    /**
     * To check if a process is complete
     * @param batchId - id of the batch
     * @return {@link Boolean} - returns true or false depending on completion of process
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an internal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public Boolean isProcessComplete(String batchId) throws Exception {
        BatchStatus batchStatus = getBatchStatus(batchId);
        return batchStatus.getCompleted().equals(batchStatus.getTotal());
    }


    /**
     * To check if the upload is complete or not
     * @param batchId - id of the batch
     * @return {@link Boolean} - returns true or false depending upon upload status
     * @throws io.taskmonk.clientexceptions.ForbiddenException if the access is unauthorized
     * @throws io.taskmonk.clientexceptions.NotFoundException if object not found
     * @throws io.taskmonk.clientexceptions.InternalServerError if an intfieldNamesernal server error occurs
     * @throws io.taskmonk.clientexceptions.UnhandledException if unhandled exception occurs
     *
     */
    public Boolean isUploadComplete(String batchId) throws Exception {
        JobProgressResponse jobResponse = getJobProgressBatch(batchId);
        return jobResponse.isCompleted();

    }
//    public TaskMonkClient(String projectId, String server, OAuthClientCredentials credentials) {
//        this(projectId, server, 80 , credentials);
//
//    }

    public TaskMonkClient(String projectId, String server, OAuthClientCredentials credentials) {
        this.projectId = projectId;
        this.credentials = credentials;
        this.httpHost = HttpHost.create(server);
    }

    public TaskMonkClient(String projectId, String server, String proxy, OAuthClientCredentials credentials) {
        this.projectId = projectId;
        this.credentials = credentials;
        this.httpHost = HttpHost.create(server);
        if (proxy != null) {
            this.proxyHost = HttpHost.create(proxy);
        }
    }

}
