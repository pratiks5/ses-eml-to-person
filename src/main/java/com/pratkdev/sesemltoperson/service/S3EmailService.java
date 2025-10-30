package com.pratkdev.sesemltoperson.service;

import com.pratkdev.sesemltoperson.model.FileOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3EmailService {

    private final S3Client s3Client;
    private final String bucket;

    public S3EmailService(@Value("${aws.accessKeyId}") String accessKey,
                          @Value("${aws.secretKey}") String secretKey,
                          @Value("${aws.region}") String region,
                          @Value("${aws.s3.bucket}") String bucket) {
        this.bucket = bucket;

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }


    public List<FileOutput> downloadAllEmlFiles() throws IOException {
        List<FileOutput> downloaded = new ArrayList<>();

        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).build();
        ListObjectsV2Response listing = s3Client.listObjectsV2(request);

        return listing.contents().stream()
                .map(software.amazon.awssdk.services.s3.model.S3Object::key)
                .filter(key -> key != null && key.toLowerCase().endsWith(".eml"))
                .map(key -> {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();

                    ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getObjectRequest);
                    ;

                    return new FileOutput(responseStream, key);

                })
                .collect(Collectors.toList());
    }


    /**
     * Download all .eml files from the configured bucket into a temporary directory and return list of files.
     */
   /* public List<File> downloadAllEmlFiles() throws IOException {
        List<File> downloaded = new ArrayList<>();
        ObjectListing listing = s3Client.listObjects(bucket);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();
        for (S3ObjectSummary s : summaries) {
            String key = s.getKey();
            if (key == null) continue;
            if (!key.toLowerCase().endsWith(".eml")) continue;
            S3Object obj = s3Client.getObject(bucket, key);
            S3ObjectInputStream is = obj.getObjectContent();
            File tmp = File.createTempFile("email-", ".eml");
            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                byte[] buf = new byte[4096];
                int r;
                while ((r = is.read(buf)) != -1) {
                    fos.write(buf, 0, r);
                }
            } finally {
                is.close();
            }
            downloaded.add(tmp);
        }
        return downloaded;
    }*/
}
