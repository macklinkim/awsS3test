import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
public class PresignedUrl {

	public static void main(String[] args) {

        String bucketName = "dansol-test2";
        String objectKey = "sample.txt";
        Region region = Region.AP_NORTHEAST_2; // 서울 리전 예시

        S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create()))//AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
                .region(region)
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        URL presignedUrl = presigner.presignGetObject(presignRequest).url();
        System.out.println("Pre-Signed URL: "+presignedUrl);

        try {
            String outputPath = "s3.txt";
            // 연결 설정
            URL url = new URL(presignedUrl.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            String contentType = conn.getContentType();
            System.out.println("Content-Type: " + contentType);
            if (responseCode == 200) {
                // 다운로드 시작
                try (InputStream in = conn.getInputStream();
                     OutputStream out = new FileOutputStream(outputPath)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("다운로드 성공 → " + outputPath);
            } else {
                System.out.println("다운로드 실패: HTTP " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        presigner.close();
	}

}


