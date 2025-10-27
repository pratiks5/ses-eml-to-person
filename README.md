# SES EML to Person - Spring Boot App

This sample Spring Boot application downloads `.eml` files from an S3 bucket, extracts PDF attachments, uses Apache PDFBox to read text, and parses simple Person fields (name, email, phone, age). It exposes:

`GET /fetch-person/emails` -> returns JSON list of extracted Person objects.

Notes:
- AWS credentials are placed in `application.properties` as dummy values.
- Parsing uses simple regex heuristics and won't be perfect for every PDF layout. For more accurate extraction, customize the parsing logic.
