package com.pratkdev.sesemltoperson.service;

import com.pratkdev.sesemltoperson.model.DsarModel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfParserService {

    public DsarModel parsePdf(MultipartFile pdfFile) {
        try {
            String pdfText = extractTextFromPdf(pdfFile.getInputStream());
            return mapTextToDsarModel(pdfText);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF file: " + pdfFile.getOriginalFilename(), e);
        }
    }

    private String extractTextFromPdf(InputStream pdfStream) throws IOException {
        PDDocument document = PDDocument.load(pdfStream);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        return text;
    }

    private DsarModel mapTextToDsarModel(String pdfText) {
        DsarModel dsarModel = new DsarModel();
        DsarModel.Request request = new DsarModel.Request();
        DsarModel.Data data = new DsarModel.Data();

        // Extract person information
        DsarModel.Person person = extractPersonInfo(pdfText);
        data.setPerson(person);

        // Extract representative information
        DsarModel.Representative representative = extractRepresentativeInfo(pdfText);
        data.setRepresentative(representative);

        // Set process control (fixed values for DE)
        DsarModel.ProcessControl processControl = createProcessControl();
        request.setProcessControl(processControl);

        request.setData(data);
        dsarModel.setRequest(request);

        return dsarModel;
    }

    private DsarModel.Person extractPersonInfo(String pdfText) {
        DsarModel.Person person = new DsarModel.Person();
        String normalizedText = pdfText.replace('\u00A0', ' ');
        Pattern testPattern = Pattern.compile("(Herr|Frau)\\s+([A-Za-zäöüßÄÖÜ\\s]+),");
        Matcher nameMatcher = testPattern.matcher(normalizedText);

        if (nameMatcher.find()) {
            System.out.println("in if");
            String genderPrefix = nameMatcher.group(1);
            String fullName = nameMatcher.group(2).trim();
            // Set gender
            person.setGender("Herr".equals(genderPrefix) ? "MALE" : "FEMALE");

            // Split name - assuming format "FirstName LastName"
            String[] nameParts = fullName.split("\\s+", 2);
            if (nameParts.length >= 1) {
                person.setFirstName(nameParts[0]);
            }
            if (nameParts.length >= 2) {
                person.setLastName(nameParts[1]);
            }
        }

        // Extract date of birth - pattern: "geboren am 21.03.2005"
        Pattern dobPattern = Pattern.compile("geboren am\\s*(\\d{2}\\.\\d{2}\\.\\d{4})");
        Matcher dobMatcher = dobPattern.matcher(pdfText);
        if (dobMatcher.find()) {
            String germanDate = dobMatcher.group(1);
            String isoDate = convertGermanDateToIso(germanDate);
            person.setDateOfBirth(isoDate);
        }

        // Extract address - pattern: "wohnhaft in Pilgerweg 19, 40625 Düsseldorf"
        Pattern addressPattern = Pattern.compile("wohnhaft in\\s*([^,]+),\\s*(\\d{5})\\s*([^\\n]+)");
        Matcher addressMatcher = addressPattern.matcher(pdfText);
        if (addressMatcher.find()) {
            DsarModel.Address address = getAddress(addressMatcher);

            person.setAddress(address);
        }

        return person;
    }

    private static DsarModel.Address getAddress(Matcher addressMatcher) {
        DsarModel.Address address = new DsarModel.Address();
        String streetFull = addressMatcher.group(1).trim();
        String postalCode = addressMatcher.group(2).trim();
        String city = addressMatcher.group(3).trim();

        // Split street and house number
        String[] streetParts = streetFull.split("(?<=\\D)(?=\\d)");
        if (streetParts.length >= 1) {
            address.setStreet(streetParts[0].trim());
        }
        if (streetParts.length >= 2) {
            address.setHouseNumber(streetParts[1].trim());
        }

        address.setPostalCode(postalCode);
        address.setCity(city);
        address.setCountry("DE");
        return address;
    }

    private DsarModel.Representative extractRepresentativeInfo(String pdfText) {
        DsarModel.Representative representative = new DsarModel.Representative();

        // Extract law firm information from header
        Pattern firmPattern = Pattern.compile("KRAUS GHENDLER Rechtsanwälte[\\s\\S]*?Aachener Straße 1[\\s\\S]*?50674 Köln");
        Matcher firmMatcher = firmPattern.matcher(pdfText);

        if (firmMatcher.find()) {
            DsarModel.Address address = new DsarModel.Address();
            address.setStreet("Aachener Straße");
            address.setHouseNumber("1");
            address.setPostalCode("50674");
            address.setCity("Köln");
            address.setCountry("DE");

            representative.setAddress(address);
            representative.setReference("KRAUS GHENDLER Rechtsanwälte");
        }

        return representative;
    }

    private DsarModel.ProcessControl createProcessControl() {
        DsarModel.ProcessControl processControl = new DsarModel.ProcessControl();
        processControl.setBureauEntity("DE");
        processControl.setReceivedDate(OffsetDateTime.now().toString());
        processControl.setScheduledDate(OffsetDateTime.now().plusDays(1).toString());
        processControl.setRequiresApproval(false);
        processControl.setSkipPrinting(true);
        processControl.setTestDsar(true);
        processControl.setCalculateScore(false);
        processControl.setDataOrigin("DOT");
        return processControl;
    }

    private String convertGermanDateToIso(String germanDate) {
        // Convert from "21.03.2005" to "2005-03-21"
        String[] parts = germanDate.split("\\.");
        if (parts.length == 3) {
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }
        return germanDate;
    }
}