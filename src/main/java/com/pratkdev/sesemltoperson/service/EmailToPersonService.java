package com.pratkdev.sesemltoperson.service;

import com.pratkdev.sesemltoperson.model.Person;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailToPersonService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?\\d{1,3}[\\s-]?)?(?:\\(\\d{2,4}\\)|\\d{2,4})[\\s-]?\\d{3,4}[\\s-]?\\d{3,4}");
    private static final Pattern AGE_PATTERN = Pattern.compile("\\bage\\s*[:\\-]?\\s*(\\d{1,3})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("(?i)name\\s*[:\\-]?\\s*([A-Z][a-zA-Z]+(?:\\s+[A-Z][a-zA-Z]+)*)");


    /**
     * Parse an .eml file, extract PDF attachments (if any), then extract text from PDFs and map to Person objects.
     */
    public List<Person> parseEmlFileToPersons(File emlFile) throws Exception {
        List<Person> persons = new ArrayList<>();
        try (InputStream is = new FileInputStream(emlFile)) {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session, is);
            Object content = message.getContent();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bp = multipart.getBodyPart(i);
                    if (bp.isMimeType("application/pdf") || bp.getFileName() != null && bp.getFileName().toLowerCase().endsWith(".pdf")) {
                        // Save attachment to temp file
                        InputStream ais = bp.getInputStream();
                        File pdf = File.createTempFile("attach-", ".pdf");
                        try (FileOutputStream fos = new FileOutputStream(pdf)) {
                            byte[] buf = new byte[4096];
                            int r;
                            while ((r = ais.read(buf)) != -1) fos.write(buf, 0, r);
                        }
                        // Extract text from PDF
                        String text = extractTextFromPdf(pdf);
                        // Parse text for person fields
                        Person p = parsePersonFromText(text);
                        if (p != null) persons.add(p);
                        pdf.delete();
                    } else if (bp.isMimeType("multipart/*")) {
                        // nested multipart (e.g., attachments inside)
                        Object nested = bp.getContent();
                        if (nested instanceof Multipart) {
                            Multipart nmp = (Multipart) nested;
                            for (int j = 0; j < nmp.getCount(); j++) {
                                BodyPart nbp = nmp.getBodyPart(j);
                                if (nbp.getFileName() != null && nbp.getFileName().toLowerCase().endsWith(".pdf")) {
                                    InputStream ais = nbp.getInputStream();
                                    File pdf = File.createTempFile("attach-", ".pdf");
                                    try (FileOutputStream fos = new FileOutputStream(pdf)) {
                                        byte[] buf = new byte[4096];
                                        int r;
                                        while ((r = ais.read(buf)) != -1) fos.write(buf, 0, r);
                                    }
                                    String text = extractTextFromPdf(pdf);
                                    Person p = parsePersonFromText(text);
                                    if (p != null) persons.add(p);
                                    pdf.delete();
                                }
                            }
                        }
                    }
                }
            } else {
                // No multipart — ignore
            }
        }
        return persons;
    }

    private String extractTextFromPdf(File pdf) throws IOException {
        try (PDDocument doc = PDDocument.load(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    /**
     * Very simple heuristics to extract name, email, phone, age from arbitrary text.
     * This will not be perfect — for better results, you would customize per-PDF format or use form fields.
     */
    public Person parsePersonFromText(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String name = null, email = null, phone = null;
        Integer age = null;

        // Email
        Matcher em = EMAIL_PATTERN.matcher(text);
        if (em.find()) email = em.group();

        // Phone - take first match
        Matcher pm = PHONE_PATTERN.matcher(text);
        if (pm.find()) phone = pm.group().trim();

        // Age
        Matcher am = AGE_PATTERN.matcher(text);
        if (am.find()) {
            try {
                age = Integer.parseInt(am.group(1));
            } catch (NumberFormatException ignored) {
            }
        } else {
            // Try to find standalone age like 'Age: 29'
            Pattern p = Pattern.compile("\\bAge\\b[:\\-]?\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);

            Matcher m = p.matcher(text);
            if (m.find()) {
                try {
                    age = Integer.parseInt(m.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Name - heuristic: look for "Name: ..." or first capitalized pair at top
        Matcher nm = NAME_PATTERN.matcher(text);
        if (nm.find()) {
            name = nm.group(1).trim();
        } else {
            // fallback: try first two capitalized words
            Pattern p2 = Pattern.compile("([A-Z][a-z]+)\\s+([A-Z][a-z]+)");
            Matcher m2 = p2.matcher(text);
            if (m2.find()) {
                name = (m2.group(1) + " " + m2.group(2)).trim();
            }
        }

        if (name == null && email == null && phone == null && age == null) return null;
        return new Person(name, email, phone, age);
    }
}
