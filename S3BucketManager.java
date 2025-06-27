
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class S3BucketManager {
    public static void main(String[] args) {
        String bucketName = "dansol-test3";
        Region region = Region.AP_NORTHEAST_2;

        S3Client s3 = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create()))//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                .build();
	String userIn = "";
	Scanner scannerIn = new Scanner(System.in);

        while(!userIn.equals("99")){
	    System.out.println("/n/nChoose Number(region : AP_NORTHEAST_1)");
            System.out.println("1.Create Bucket");
            System.out.println("2.List of Bucket");
            System.out.println("3.List Inside of Bucket");
            System.out.println("4.Delete Bucket");
            System.out.println("99.exit");
            userIn = scannerIn.nextLine();
            switch(userIn){
            case "1" : 
            	System.out.print("write bucket name:");
            	bucketName=scannerIn.nextLine(); 
            	createBucket(s3, bucketName, region);
            	break;
            case "2" :
		break;	
            case "3" :
            	System.out.print("write bucket name:");
            	bucketName=scannerIn.nextLine(); 
            	insideBucket(s3, bucketName);
		break;
            case "4" :
            	System.out.print("write bucket name:");
            	bucketName=scannerIn.nextLine();
            	emptyBucket(s3, bucketName);
            	deleteBucket(s3, bucketName);
		break;
            }
        }

        s3.close();
    }

    // 버킷 존재 확인
    public static boolean bucketExists(S3Client s3, String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) return false;
            throw e;
        }
    }
    // 버킷 생성
    public static void createBucket(S3Client s3, String bucketName, Region region) {
        try {
            System.out.println("Create Bucket start: " + bucketName);
            CreateBucketRequest.Builder builder = CreateBucketRequest.builder().bucket(bucketName);
            if (!region.equals(Region.US_EAST_1)) {
                builder.createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .locationConstraint(region.id())
                                .build()
                );
            }
            s3.createBucket(builder.build());
            System.out.println("Created: " + bucketName);
        } catch (S3Exception e) {
            if ("BucketAlreadyOwnedByYou".equals(e.awsErrorDetails().errorCode())) {
                System.out.println("Bucket is already Exist.");
            } else {
                throw e;
            }
        }
    }
    // 버킷에 담긴 파일 확인
    public static void insideBucket(S3Client s3, String bucketName) {
    	System.out.println("Bucket file check start: " + bucketName);
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder().bucket(bucketName).build();
            ListObjectsV2Response listRes;
            listRes = s3.listObjectsV2(listReq);
            for (S3Object s3Object : listRes.contents()) {
            	System.out.println(s3Object.key());
            }

        } catch (S3Exception e) {
            if ("NoSuchBucket".equals(e.awsErrorDetails().errorCode())) {
                System.out.println("No Such a Bucket: "+bucketName);
            } else {
                throw e;
            }
        }
    }

    // 버킷 비우기 
    public static void emptyBucket(S3Client s3, String bucketName) {
        System.out.println("Remove Bucket Files start: " + bucketName);
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
                    System.out.println(toDelete.size() + "개 삭제");
                }
                listReq = listReq.toBuilder()
                        .continuationToken(listRes.nextContinuationToken())
                        .build();
            } while (listRes.isTruncated());
            System.out.println("Remove Bucket finished: " + bucketName);
        } catch (S3Exception e) {
            if ("NoSuchBucket".equals(e.awsErrorDetails().errorCode())) {
                System.out.println("No Such a Bucket.");
            } else {
                throw e;
            }
        }
    }

    // 버킷 삭제
    public static void deleteBucket(S3Client s3, String bucketName) {
        try {
            System.out.println("Bucket Remove Start: " + bucketName);
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3.deleteBucket(deleteBucketRequest);
            System.out.println("Bucket Removed: " + bucketName);
        } catch (NoSuchBucketException e) {
	}
    }
}
