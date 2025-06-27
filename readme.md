

---

# AWS S3 Java Example Project (AWS SDK v2.x)

이 프로젝트는 Java 및 AWS SDK for Java 2.x를 활용하여 S3 버킷 생성, 파일 업로드/다운로드, presigned URL 생성, public bucket 설정, 멀티파일 업로드, access point 관리 등을 자동화한 예제 코드 모음입니다.
실제 AWS 환경과 연동된 테스트 및 운영 목적의 코드로 구성되어 있으며, 각 파일은 독립적 기능을 수행하도록 설계되어 있습니다.

---

## 📁 프로젝트 파일 및 기능 설명

### ✅ `PresignedUrl.java`

* **기능**: Presigned URL을 생성하여 외부에서 인증 없이 파일 업로드, 다운로드, 삭제 가능하도록 지원.
* **지원 메서드**:

  * `generatePutUrl()`: 파일 업로드용 presigned URL 생성
  * `generateGetUrl()`: 파일 다운로드용 presigned URL 생성
  * `generateDeleteUrl()`: 파일 삭제용 presigned URL 생성
* **특징**: `Content-Type` 자동 지정 및 presigned URL에 만료 시간 설정 포함

---

### ✅ `S3AP.java`

* **기능**: S3 Access Point 관련 설정 및 정책 적용 코드
* **주요 기능**:

  * Access Point 생성
  * Access Point 정책(Public Access 허용 등) 설정
* **이슈 경험**: `PutAccessPointPolicyRequest` 사용 시 `403 Access Denied` 오류 → IAM 권한 및 region 설정 고려 필요

---

### ✅ `S3Async.java`

* **기능**: `S3AsyncClient`를 사용하여 비동기 방식으로 S3 파일 업로드 수행
* **주요 기술**: `CompletableFuture`, `nio.Path`, Netty 기반
* **주의사항**: Credentials 설정 및 `source must not be null` 예외에 대한 사전 방어 코드 구현 필요

---

### ✅ `S3BucketDel.java`

* **기능**: 버킷 전체 삭제
* **기능 구성**:

  * 버킷 내 객체 전체 삭제 (자동 비우기)
  * 버킷 존재 여부 확인 후 삭제 수행
* **예외 처리**: 버킷이 없거나 접근 불가 시 예외 로깅 처리

---

### ✅ `S3BucketManager.java`

* **기능**: 버킷 생성 및 정책(Public Read 등) 자동 적용
* **지원 정책 예시**:

  ```json
  {
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::your-bucket-name/*"
  }
  ```
* **옵션**: region, cannedAcl, public access 설정 등

---

### ✅ `S3MultiFileUploader.java`

* **기능**: 여러 파일을 동시에 S3 버킷에 업로드 (멀티파일 업로드)
* **사용 기술**: `S3TransferManager`, `UploadFileRequest`
* **오류 해결 히스토리**:

  * `source must not be null` → 파일 경로 설정 수정
  * `CompletionException: unable to load credentials` → AWS CLI 설정 필요

---

### ✅ `S3PublicBucket.java`

* **기능**: 퍼블릭 접근 가능한 버킷 구성 및 객체 업로드
* **특징**:

  * Public Read 정책 적용
  * 객체 cannedAcl 설정 포함 (`CannedAccessControlList.PublicRead`)

---

### ✅ `S3test.java`

* **기능**: 기본적인 파일 업로드 및 다운로드 예제
* **구성**:

  * S3Client 생성
  * 업로드: `putObject()`
  * 다운로드: `getObject()`
* **기타**: 간단한 실습 목적의 통합 테스트 파일

---

## 🔧 사용된 기술 스택

* Java 17
* AWS SDK for Java v2.x
* Ubuntu CLI 기반 테스트 환경
* S3 Region: `eu-north-1`, `ap-northeast-2` 등 다양하게 테스트

---

## 💡 학습 및 실습 내용 요약

* S3 presigned URL 생성 및 파일 접근 제어
* AccessPoint를 활용한 S3 리소스 분리 및 정책 적용
* 버킷 정책 자동 적용(Public Policy)
* S3 TransferManager를 활용한 대용량 업로드
* IAM 정책 및 오류 대응 (`403`, `NoSuchKey`, `Credentials not found`)
* Region, Endpoint 관련 이슈 해결 경험
* 비동기 업로드 및 Netty 기반 오류 처리 경험

---

## ⚠️ 참고 사항

* 실제 S3 버킷 사용 시 민감한 정보(AWS credentials)는 환경 변수 또는 AWS CLI를 통해 설정 권장
* `PresignedUrl`은 보안상 유효시간 제한을 반드시 고려해야 하며, 외부 공유 시 주의 필요
* 오프라인 환경에서는 aws sdk jar 수동 다운로드 및 종속성 관리 필요 (pom.xml 내 jar만으로는 동작 안 됨)

---

필요 시 각 Java 파일별 주요 코드 블럭이나 실행 방법도 추가로 넣어드릴 수 있습니다. 원하면 예시 실행 명령어 및 `pom.xml` 예제도 붙여드릴게요.
