package com.pratkdev.sesemltoperson.controller;

import com.pratkdev.sesemltoperson.model.DsarModel;
import com.pratkdev.sesemltoperson.model.Person;
import com.pratkdev.sesemltoperson.service.EmailToPersonService;
import com.pratkdev.sesemltoperson.service.ModelProcessingService;
import com.pratkdev.sesemltoperson.service.S3EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PersonController {

    private final S3EmailService s3EmailService;
    private final EmailToPersonService emailToPersonService;
    private final ModelProcessingService modelProcessingService;

    public PersonController(S3EmailService s3EmailService, EmailToPersonService emailToPersonService, ModelProcessingService modelProcessingService) {
        this.s3EmailService = s3EmailService;
        this.emailToPersonService = emailToPersonService;
        this.modelProcessingService = modelProcessingService;
    }


    @GetMapping("/process-eml-files")
    public List<DsarModel> processEmlFiles() {
        return modelProcessingService.processAllEmlFiles();
    }

//    @GetMapping("/fetch-person/emails")
//    public ResponseEntity<List<Person>> fetchPersonsFromEmails() {
//        List<Person> all = new ArrayList<>();
//        try {
//            List<File> emls = s3EmailService.downloadAllEmlFiles();
//            for (File f : emls) {
//                try {
//                    List<Person> persons = emailToPersonService.parseEmlFileToPersons(f);
//                    if (persons != null) all.addAll(persons);
//                } catch (Exception e) {
//                    // log and continue
//                    e.printStackTrace();
//                } finally {
//                    // delete tmp eml file
//                    f.delete();
//                }
//            }
//            return ResponseEntity.ok(all);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body(all);
//        }
//    }
}
