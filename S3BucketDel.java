
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;

public class S3BucketDel {
    public static void main(String[] args) {
        String bucketName = "dansol-test3";
        Region region = Region.AP_NORTHEAST_2;

        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create()))//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                .build();

        try {
		emptyBucket(s3, bucketName);
		deleteBucket(s3, bucketName);

        } catch (S3Exception e) {
            System.err.println("S3 Exception: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            System.err.println("Common Java Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            s3.close();
        }
    }

    // Empty Bucket
    public static void emptyBucket(S3Client s3, String bucketName) {
        System.out.println("Start Empty Bucket: " + bucketName);
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder().bucket(bucketName).build();
            ListObjectsV2Response listRes;
            do {
                listRes = s3.listObjectsV2(listReq);
                List<ObjectIdentifier> toDelete = new ArrayList<>();
                for (S3Object s3Object : listRes.contents()) {
                    toDelete.add(ObjectIdentifier.builder().key(s3Object.key()).build());
                }
                if (!toDelete.isEmpty()) {
                    DeleteObjectsRequest delReq = DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .delete(Delete.builder().objects(toDelete).build())
                            .build();
                    s3.deleteObjects(delReq);
                    System.out.println( toDelete.size() + "개 삭제");
                }
                listReq = listReq.toBuilder()
                        .continuationToken(listRes.nextContinuationToken())
                        .build();
            } while (listRes.isTruncated());
            System.out.println("Empty Bucket Done: " + bucketName);
        } catch (S3Exception e) {
            if ("NoSuchBucket".equals(e.awsErrorDetails().errorCode())) {
                System.out.println("No longer Bucket Exists.");
            } else {
                throw e;
            }
        }
    }
    // Remove Bucket
    public static void deleteBucket(S3Client s3, String bucketName) {
        try {
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3.deleteBucket(deleteBucketRequest);
            System.out.println("Bucket Remove Done: " + bucketName);
        } catch (NoSuchBucketException e) {
            System.out.println("No Such a Bucket named :" + bucketName);
        } catch (S3Exception e) {
            if ("NoSuchBucket".equals(e.awsErrorDetails().errorCode())) {
                System.out.println("No Such a Bucket named : " + bucketName);
            } else if ("BucketNotEmpty".equals(e.awsErrorDetails().errorCode())) {
                System.out.println("Can not remove. Empty Bucket First.");
            } else {
                throw e;
            }
        }
    }
}
