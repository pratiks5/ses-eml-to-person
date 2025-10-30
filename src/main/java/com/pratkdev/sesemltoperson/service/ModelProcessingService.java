package com.pratkdev.sesemltoperson.service;

import com.pratkdev.sesemltoperson.model.DsarModel;
import com.pratkdev.sesemltoperson.model.FileOutput;
import jakarta.mail.BodyPart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class ModelProcessingService {


    @Autowired
    private S3EmailService s3EmailService;

    @Autowired
    private PdfParserService pdfParserService;



    public List<DsarModel> processAllEmlFiles() {
        List<DsarModel> dsarModels = new ArrayList<>();

        try {
            List<FileOutput> emlFiles = s3EmailService.downloadAllEmlFiles();

            for (FileOutput emlFile : emlFiles) {
                try {
                    List<DsarModel> parsedModels = processSingleEmlFile(emlFile);
                    dsarModels.addAll(parsedModels);
                } catch (Exception e) {
                    // Log error but continue processing other files
                    System.err.println("Error processing EML file: " + emlFile.getName() + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download EML files from S3", e);
        }

        return dsarModels;
    }


    private List<DsarModel> processSingleEmlFile(FileOutput emlFile) throws Exception {
        List<DsarModel> dsarModels = new ArrayList<>();

        // Parse EML and extract PDF attachments
        MimeMessage mimeMessage = new MimeMessage(
                Session.getDefaultInstance(new Properties()),
                emlFile.getInputStream()
        );

        Object content = mimeMessage.getContent();

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                        StringUtils.hasText(bodyPart.getFileName())) {

                    String fileName = bodyPart.getFileName();
                    if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                        // Convert to MultipartFile
                        MultipartFile pdfFile = convertToMultipartFile(bodyPart, fileName);

                        // Parse PDF and convert to DSAR model
                        DsarModel dsarModel = pdfParserService.parsePdf(pdfFile);
                        dsarModels.add(dsarModel);
                    }
                }
            }
        }

        return dsarModels;
    }

    private MultipartFile convertToMultipartFile(BodyPart bodyPart, String fileName) throws Exception {
        InputStream inputStream = bodyPart.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }

            @Override
            public boolean isEmpty() {
                return bytes.length == 0;
            }

            @Override
            public long getSize() {
                return bytes.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return bytes;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), bytes);
            }
        };
    }

}
