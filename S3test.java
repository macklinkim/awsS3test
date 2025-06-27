import java.io.File;
import java.net.URI;
import java.util.Scanner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

public class S3test{

	public static void main(String[] args) {

        String region = "eu-north-1";
        String accessPointArn = "arn:aws:s3:eu-north-1:762454181866:accesspoint/dansol-test-eu";
        try {
            // 1. STS로 자격증명 확인 (가장 기본적인 테스트)
            System.out.println("1. STS auth check");
            StsClient stsClient = StsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create()))//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                    .build();
           
            
            GetCallerIdentityResponse  identity = stsClient.getCallerIdentity(GetCallerIdentityRequest.builder().build());
            System.out.println("result : STS ok!");
            System.out.println("   accoun ID: " + identity.account());
            System.out.println("   user ARN: " + identity.arn());
            System.out.println("   user ID: " + identity.userId());
           
            // 2. S3 기본 권한 테스트 (버킷 목록 조회)
            System.out.println("2. S3 auth check");
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
            
            ListBucketsResponse buckets = s3Client.listBuckets(ListBucketsRequest.builder().build());
	    List<Bucket> bucketList = buckets.buckets();
            bucketList.forEach(bucket -> {
                System.out.println("   Bucket Name: " + bucket.name());
            });
            System.out.println("3. S3 back auth checked ok!");
            System.out.println("   accessable bucket no: " + buckets.buckets().size());
           
            // 3. 시스템 시간 정보
            System.out.println("4. system time:");
            System.out.println("   now system time: " + java.time.Instant.now());
            System.out.println("   timezone: " + java.time.ZoneId.systemDefault());
           
            System.out.println("BASIC SETTING ALL OK!");
           
        } catch (S3Exception e) {
            System.err.println("S3 error:");
            analyzeS3Error(e);
        } catch (Exception e) {
            System.err.println("auth error:");
            System.err.println("   " + e.getMessage());
           
            if (e.getMessage().contains("SignatureDoesNotMatch") ||
                e.getMessage().contains("InvalidAccessKeyId") ||
                e.getMessage().contains("SignatureMismatch")) {
                System.err.println("ERROR solve:");
                System.err.println("   1. Access Key check");
                System.err.println("   2. Secret Key remake");
                System.err.println("   3. system time ");
            }
        }
	String directoryPath = "/home/ubuntu/aws/res/";
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    System.out.println("   - "+file.getName());
                }
            } else {
                System.out.println("can not find directory : "+directoryPath);
            }
        } else {
            System.out.println("invalid directory path.");
        }
	Scanner userIn = new Scanner(System.in);
	String filePath = directoryPath;
	System.out.print("File Name :");
	filePath = userIn.nextLine();
	String filePath2 = UUID.randomUUID()+"-"+filePath.trim();
	Path source = Paths.get(directoryPath+filePath);
	Path target = Paths.get(directoryPath+filePath2);
	byte[] fileBytes  = null;
	try{
		Files.copy(source, target);
		fileBytes = Files.readAllBytes(target);
	}catch(IOException e){
		System.err.println("file read error:"+e);
	}
	String customRegion = "";
	String accessPointName = "";
	System.out.print("type Region : ");
	customRegion = userIn.nextLine();
	System.out.print("type AccessPoint : ");
	accessPointName = userIn.nextLine();

	String accessPoint2 ="arn:aws:s3:"+customRegion+":762454181866:accesspoint/" + accessPointName;
	System.out.println("AccessPoint: "+accessPoint2);
        S3Client s3 = S3Client.builder()
                .region(Region.of(customRegion)) 
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
	System.out.println(filePath2);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(accessPoint2)
                .key(filePath2)
                .build();
        s3.putObject(request, RequestBody.fromBytes(fileBytes));
        System.out.println("file upload success");
    }
   
    private static void analyzeS3Error(S3Exception e) {
        System.err.println("   error code: " + e.awsErrorDetails().errorCode());
        System.err.println("   error msg: " + e.awsErrorDetails().errorMessage());
        System.err.println("   HTTP status: " + e.statusCode());
       
        switch (e.awsErrorDetails().errorCode()) {
            case "SignatureDoesNotMatch":
            case "SignatureMismatch":
                System.err.println("\n signature :");
                System.err.println("   • Secret Key major problem");
                System.err.println("   • system time ");
                System.err.println("   • network proxy");
                break;

            case "InvalidAccessKeyId":
                System.err.println("\n Access Key ID ");
                System.err.println("   • Access Key not exist");
                System.err.println("   • Access Key unactivated");
                break;
               
            case "AccessDenied":
                System.err.println("\n authentication :");
                System.err.println("   • IAM pro");
                System.err.println("   • Access Point check");
                break;
        }
    }
}
