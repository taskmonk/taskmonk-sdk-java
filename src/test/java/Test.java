


import io.taskmonk.auth.OAuthClientCredentials;
import io.taskmonk.client.TaskMonkClient;

import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {
        String projectId = "169";
        /**
         * Initialize the taskmonk client witht he oauth credentials and projectId
         */
        TaskMonkClient client = new TaskMonkClient(projectId, "preprod.taskmonk.io",
                new OAuthClientCredentials("Fxe2u1LAgT5as96s9U4ugxPMabvtAAfn", "7OXBVShSVenHVrLGYl7xMxAV3w42r6VfM1yYySsPM88i71Qtsw81J9WWAZdoPgVp"));

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
//        String batchId = "1049";
//        while (!client.isProcessComplete(batchId)) {
//
//            System.out.println("Processing not complete");
//            Thread.sleep(1000);
//        }
//        System.out.println("Upload Completed");

        /*
         * Get the output in a local csv file
         */
        client.getBatchOutput(batchId, "CSV", "/tmp/output.csv");
        System.out.println("Downloaded to /tmp/output.csv");
    }
}

