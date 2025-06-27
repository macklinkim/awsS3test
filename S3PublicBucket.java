import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.HashMap;
import java.util.Map;

public class S3PublicBucket {
    public static void main(String[] args) {


        String bucketName = "dansol-public-test-bucket-1"; // 고유 이름
        Region region = Region.AP_NORTHEAST_2;

        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create()))//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                .build();

        try {
            CreateBucketRequest createReq = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .locationConstraint(region.id())
                                    .build())
                    .build();
            s3.createBucket(createReq);
            System.out.println("Bucket Created: " + bucketName);

            PublicAccessBlockConfiguration pabConfig = PublicAccessBlockConfiguration.builder()
                    .blockPublicAcls(false)
                    .ignorePublicAcls(false)
                    .blockPublicPolicy(false)
                    .restrictPublicBuckets(false)
                    .build();
            PutPublicAccessBlockRequest pabRequest = PutPublicAccessBlockRequest.builder()
                    .bucket(bucketName)
                    .publicAccessBlockConfiguration(pabConfig)
                    .build();
            s3.putPublicAccessBlock(pabRequest);
            System.out.println("Public Policy ");

            String policy = "{\n" +
                    " \"Version\": \"2012-10-17\",\n" +
                    " \"Statement\": [\n" +
                    " {\n" +
                    " \"Effect\": \"Allow\",\n" +
                    " \"Principal\": \"*\",\n" +
                    " \"Action\": \"s3:GetObject\",\n" +
                    " \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                    " }\n" +
                    " ]\n" +
                    "}";

            PutBucketPolicyRequest policyRequest = PutBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .policy(policy)
                    .build();
            s3.putBucketPolicy(policyRequest);

            System.out.println("Bucket Public Policy applied");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            s3.close();
        }
    }
}
