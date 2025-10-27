package com.pratkdev.sesemltoperson.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3EmailService {

    private final AmazonS3 s3Client;
    private final String bucket;

    public S3EmailService(@Value("${aws.accessKeyId}") String accessKey,
                          @Value("${aws.secretKey}") String secretKey,
                          @Value("${aws.region}") String region,
                          @Value("${aws.s3.bucket}") String bucket) {
        this.bucket = bucket;
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }

    /**
     * Download all .eml files from the configured bucket into a temporary directory and return list of files.
     */
    public List<File> downloadAllEmlFiles() throws IOException {
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
    }
}
