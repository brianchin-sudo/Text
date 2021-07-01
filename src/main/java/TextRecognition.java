import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TextRecognition {
    public static void main(String[] args) throws IOException{
        String doc = "output.txt";

        FileWriter fstream = new FileWriter(doc);
        BufferedWriter outTitle = new BufferedWriter(fstream);
        outTitle.write("Report");
        outTitle.newLine();

        outTitle.close();

        FileWriter fstreamAppend = new FileWriter(doc, true);
        BufferedWriter out = new BufferedWriter(fstreamAppend);



        String[] myList;
        String photo = "";
        Random random = new Random();
        final String USAGE = "\n" +
                "To run this example, supply the name of a bucket to list!\n" +
                "\n" +
                "Ex: ListObjects <bucket-name>\n";

//        if (args.length < 1) {
//            System.out.println(USAGE);
//            System.exit(1);
//        }

//        String bucket_name = args[0];
//        String bucket_name = "testing-bucket-brian";
        String bucket_name = "njit-cs-643";

        String queueUrl = "https://sqs.us-east-1.amazonaws.com/323397123849/cs643pa1.fifo";




        boolean terminate = true;
        String body = "";
        while (terminate){

            System.out.format("Objects in S3 bucket %s:\n", bucket_name);
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
            ListObjectsV2Result result = s3.listObjectsV2(bucket_name);
            List<S3ObjectSummary> objects = result.getObjectSummaries();


            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-east-1").build();

            AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();

            List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
            for (Message m : messages) {
//            System.out.println(m.getMessageId());

                System.out.println(m.getBody());
                body = m.getBody();

                sqs.deleteMessage(queueUrl, m.getReceiptHandle());
                if (body.equals("done")) {
                    out.close();
                    terminate = false;
                    break;
                } else {
                    photo = body;
                    DetectTextRequest request = new DetectTextRequest()
                            .withImage(new Image()
                                    .withS3Object(new S3Object()
                                            .withName(photo)
                                            .withBucket(bucket_name)));


                    try {
                        DetectTextResult resultText = rekognitionClient.detectText(request);
                        List<TextDetection> textDetections = resultText.getTextDetections();
                        out.newLine();
                        System.out.println("Detected lines and words for " + photo);
                        out.write("File Index: " + photo);
                        out.newLine();
                        out.write("Type: Car" );
                        out.newLine();

                        for (TextDetection text : textDetections) {

                            System.out.println("Detected: " + text.getDetectedText());

                            out.write("Text: " + text.getDetectedText());
                            out.newLine();
                            System.out.println("Confidence: " + text.getConfidence().toString());
                            System.out.println("Id : " + text.getId());
                            System.out.println("Parent Id: " + text.getParentId());
                            System.out.println("Type: " + text.getType());
                            System.out.println();
                        }
                    } catch (AmazonRekognitionException e) {
                        e.printStackTrace();
                    }

                }


            }

    }
    }

}
