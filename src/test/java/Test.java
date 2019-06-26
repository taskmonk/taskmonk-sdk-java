


import io.taskmonk.auth.OAuthClientCredentials;
import io.taskmonk.client.TaskMonkClient;
import io.taskmonk.entities.Notification;
import io.taskmonk.entities.Task;
import io.taskmonk.entities.TaskImportResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws Exception {
        String projectId = "154";
        /**
         * Initialize the taskmonk client witht he oauth credentials and projectId
         */
        TaskMonkClient client = new TaskMonkClient(projectId, "localhost:9000",
                new OAuthClientCredentials("Fxe2u1LAgT5as96s9U4ugxPMabvtAAfn", "7OXBVShSVenHVrLGYl7xMxAV3w42r6VfM1yYySsPM88i71Qtsw81J9WWAZdoPgVp"));

        /*
         * Upload the tasks csv to a new batch that will be created with name batchName
         */
//        String batchId = client.uploadTasks("dummy_batch_name", new File("/Users/sampath/tmp.csv")).batchId;
        String batchId = client.uploadTasksUrl("dummy_batch_name", "https://tmpupload.blob.core.windows.net/test/tmp.csv", "CSV").batchId;
        //TaskImportResponse resp = client.uploadTasksToBatch("3", new File("/home/aditya/Downloads/newfile.csv"));
        //System.out.println("got taskinpmportresponse "+ resp);



        /*String url = "http://download1474.mediafire.com/46usqtx2k3gg/5sw0pwol3662q7b/Primenow_Excel_50.xlsx";
        TaskImportResponse taskresp = client.uploadTasksUrl("new_test_batch", url);
        System.out.println(" got response for url uplaod of tasks "+ taskresp);
        */
        //client.createBatch("exception_testing_batch");
//        String url = "http://download1474.mediafire.com/46usqtx2k3gg/5sw0pwol3662q7b/Primenow_Excel_50.xlsx";
//        TaskImportResponse taskresp = client.uploadTasksUrl("new_test_batch", url);
//        System.out.println(" got response for url uplaod of tasks "+ taskresp);


//
//        Map<String, String> map = new HashMap<String, String>() ;
//        Notification nn = new Notification("Email", map);
//
//        List<Notification> n = new ArrayList<Notification>();
//        n.add(nn);
//        short a =2;
//        System.out.println("new batch created; batchId = " + batchId);
//        String updatedBatchId = client.updateBatch(batchId,"dummy_batch_name", a , " no comments", n);
//        System.out.println("updated batch id "+ updatedBatchId);
//        System.out.println("Downloaded to /tmp/output.csv");



        /*
         * check the returned batch id
         */
        //System.out.println("task batch id = " + batchId);

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
        while (!client.isProcessComplete(batchId)) {

            System.out.println("Processing not complete");
            Thread.sleep(1000);
        }
        System.out.println("Upload Completed");

        /*
         * Get the output in a local csv file
         */
        client.getBatchOutput(batchId, "CSV", "/tmp/output.csv");


        /*Task task = new Task("1","8","3", new HashMap<String, String>());
        String id = client.addTask(task);
        System.out.println(" added external task "+ id);
*/
        /*List<Notification> n = new ArrayList<Notification>();
        Map<String, String> map = new HashMap<String, String>() ;


        Notification nn = new Notification("Email", map);

        n.add(nn);
        short a =2;
        String updatedBatchId = client.updateBatch("3","dummy_batch_name", a , " no comments", n);
        System.out.println("updated batch id "+ updatedBatchId);
        System.out.println("Downloaded to /tmp/output.csv");*/
    }
}

