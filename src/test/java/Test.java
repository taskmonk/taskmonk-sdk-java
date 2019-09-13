

import io.taskmonk.auth.OAuthClientCredentials;
import io.taskmonk.client.TaskMonkClient;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {
        String projectId = "169";
        /**
         * Initialize the taskmonk client witht he oauth credentials and projectId
         */
//        HttpHost proxyHost = HttpHost.create("http://proxyaddress:port");
//        HttpClientBuilder builder = HttpClientBuilder.create()
//                .setProxy(proxyHost) // Use proxy e.g. HttpHost.create("http://proxyaddress:port"); or set it to null
//                .useSystemProperties() // If proxy is null, then check if the env variables "http.proxyHost", "http.proxyPort", "https.proxyHost", "https.proxyPort" are set
//                .setRedirectStrategy(new LaxRedirectStrategy()); // Follow redirects (https -> http)
//        HttpClient client = builder.build();

        TaskMonkClient client = new TaskMonkClient(projectId, "api.taskmonk.io",
                new OAuthClientCredentials("Fxe2u1LAgT5as96s9U4ugxPMabvtAAfn", "7OXBVShSVenHVrLGYl7xMxAV3w42r6VfM1yYySsPM88i71Qtsw81J9WWAZdoPgVp"));
//        TaskMonkClient client = new TaskMonkClient(projectId, "preprod.taskmonk.io",
//                new OAuthClientCredentials("uIUSPlDMnH8gLEIrnlkdIPRE6bZYhHpw", "zsYgKGLUnftFgkASD8pndMwn3viA0IPoGKAiw6S7aVukgMWI8hGJflFs0P2QYxTg"));

        /*
         * Upload the tasks csv to a new batch that will be created with name batchName
         */
        String batchId = client.uploadTasks("dummy_batch_name", new File("/Users/sampath/tmp.csv")).batchId;

        /*
         * check the returned batch id
         */
        System.out.println("task batch id = " + batchId);

        /*
         * Wait while the tasks are being uploaded to the database
         */
        while (!client.isUploadComplete(batchId)) {
            System.out.println("Upload Not Completed");
            Thread.sleep(1000);
        }
        System.out.println("Upload Completed");

        /*
         * Wait while the tasks are being processed by the analysts
         */
//        String batchId = "411";
        while (!client.isProcessComplete(batchId)) {

            System.out.println("Processing not complete");
            Thread.sleep(1000);
        }
        System.out.println("Upload Completed");

        /*
         * Get the output in a local csv file
         */
        try {
            client.getBatchOutput(batchId, "CSV", "/tmp/output.csv");
            System.out.println("Downloaded to /tmp/output.csv");
        } catch ( Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}
