
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.CompletedUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.Upload;
import java.nio.file.Path;
import java.util.List;
import java.nio.file.Files;

public class S3MultiFileUploader {

    public static void main(String[] args) {
        String bucket = "dansol-test2";

        List<Path> files = List.of(
            Path.of("./big.jpg"),
            Path.of("./fimage.png"),
            Path.of("./ec2.txt"),
            Path.of("./s3.txt")
        );

        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
                      .region(Region.AP_NORTHEAST_2) 
                      .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create()) ) //AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                        .build();
        S3TransferManager transferManager = S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
	for(Path file : files){
		if(!Files.exists(file)){
			System.out.println("no file exist");
		}else{
			System.out.println(file.getFileName().toString());
		}

	}

        for (Path file : files) {
	    	System.out.println("bucket: " + bucket);
    		System.out.println("file: " + file);
    		System.out.println("file exists: " + Files.exists(file));
    		System.out.println("key: " + file.getFileName());

		if (bucket == null || file == null || !Files.exists(file) || file.getFileName() == null) {
			throw new IllegalArgumentException("something is null");
 		}
            UploadFileRequest uploadRequest = UploadFileRequest.builder()
                    .putObjectRequest(req -> req.bucket(bucket).key(file.getFileName().toString()))
		    .source(file)
                    .build();

	    System.out.println("passed UploadFileRequest");
            FileUpload upload = transferManager.uploadFile(uploadRequest);
            CompletedFileUpload result = upload.completionFuture().join();
           System.out.println("Upload Complete: " + result.response());
        }

        transferManager.close();
    }
}
