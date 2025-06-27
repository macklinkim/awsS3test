import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class S3Async {

    public static void main(String[] args) {
        String bucket = "dansol-test";
        String key = "sample.txt";
        String filePath = "sample.txt";
        Scanner userIn = new Scanner(System.in);
        System.out.print("File Name :");
        filePath = userIn.nextLine();
        String filePath2 = UUID.randomUUID() + "-" + filePath.trim();
        Path source = Paths.get(filePath);
        Path target = Paths.get(filePath2);
        byte[] fileBytes  = null;

        try{
                Files.copy(source, target);
                fileBytes = Files.readAllBytes(target);
        }catch(Exception e) {
        	System.out.println(e);
        }
        
        S3AsyncClient s3Client = S3AsyncClient.builder()
                .region(Region.EU_NORTH_1) // 서울 등 본인 리전 지정
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create()) )//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                .build();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filePath2)
                .contentType("application/octet-stream")
                .build();

        CompletableFuture<?> future = s3Client.putObject(
                request,
                AsyncRequestBody.fromFile(target)
        );

        future.whenComplete((res, err) -> {
            if (err != null) {
                System.err.println("Upload failed: " + err.getMessage());
            } else {
                System.out.println("Upload complete!");
            }
            s3Client.close(); // 자원 해제
        });

        future.join();
    }
}
