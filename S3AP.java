import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.model.*;

public class S3AP {
    public static void main(String[] args) {

        String accountId = "762454181866"; // 12자리 숫자
        String bucketName = "dansol-test3";
        String accessPointName = "dansol-test3-seoul4";
        String accessPointArn = "arn:aws:s3:ap-northeast-2:762454181866:accesspoint/dansol-test3-seoul4";
        Region region = Region.AP_NORTHEAST_2;
        S3ControlClient s3ControlClient = S3ControlClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create()))//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                .build();

        // 1. Access Point 생성
        try {
            //CreateAccessPointRequest createRequest = CreateAccessPointRequest.builder()
            //       .accountId(accountId)
            //        .bucket(bucketName)
            //        .name(accessPointName)
            //        .build();
            //CreateAccessPointResponse createResponse = s3ControlClient.createAccessPoint(createRequest);
            //System.out.println("Access Point ARN: " + createResponse.accessPointArn());

            PutPublicAccessBlockRequest pabRequest = PutPublicAccessBlockRequest.builder()
		    .accountId(accountId)
                    .publicAccessBlockConfiguration(PublicAccessBlockConfiguration.builder()
                            .blockPublicAcls(false)
                            .ignorePublicAcls(false)
                            .blockPublicPolicy(false)
                            .restrictPublicBuckets(false)
                            .build())
                    .build();

            s3ControlClient.putPublicAccessBlock(pabRequest);
            System.out.println("Public Access Block 해제 완료");
            // 2. 정책 JSON 작성 (예: 모든 사용자가 읽기 가능)
            String policy = "{\n" +
                    " \"Version\": \"2012-10-17\",\n" +
                    " \"Statement\": [\n" +
                    " {\n" +
                    " \"Effect\": \"Allow\",\n" +
                    " \"Principal\": \"*\",\n" +
                    " \"Action\": \"s3:*\",\n" +
		    " \"Resource\": \""+accessPointArn+"\""+
                    " }\n" +
                    " ]\n" +
                    "}";
	    System.out.println("Policy\n"+policy);
            // 3. Access Point에 정책 부여
            PutAccessPointPolicyRequest policyRequest = PutAccessPointPolicyRequest.builder()
                    .accountId(accountId)
                    .name(accessPointName)
                    .policy(policy)
                    .build();

            s3ControlClient.putAccessPointPolicy(policyRequest);
            System.out.println("정책 부여 성공");

        } catch (Exception e) {
            System.err.println("에러: " + e.getMessage());
        } finally {
            s3ControlClient.close();
        }
    }
}

