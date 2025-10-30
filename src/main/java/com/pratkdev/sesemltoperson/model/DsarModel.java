package com.pratkdev.sesemltoperson.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DsarModel {
    private Request request;
    private Complaint complaint;
    private List<Comment> comments;
    @Setter
    @Getter
    public static class Request {
        private ProcessControl processControl;
        private Data data;
    }
    @Setter
    @Getter
    public static class ProcessControl {
        private String bureauEntity;
        private String receivedDate;
        private String scheduledDate;
        private boolean requiresApproval;
        private boolean skipPrinting;
        private boolean testDsar;
        private boolean calculateScore;
        private String dataOrigin;
    }
    @Setter
    @Getter
    public static class Data {
        private Person person;
        private Representative representative;
        private List<AdditionalInformation> additionalInformations;
    }

    @Setter
    @Getter
    public static class Person {
        private String firstName;
        private String lastName;
        private String previousLastName;
        private String gender;
        private String dateOfBirth;
        private String email;
        private String phone;
        private Address address;
        private List<Address> knownAddresses;
    }
    @Setter
    @Getter
    public static class Address {
        private String street;
        private String streetId;
        private String houseNumber;
        private String houseNumberAdditional;
        private String postalCode;
        private String city;
        private String cityId;
        private String country;
    }
    @Setter
    @Getter
    public static class Representative {
        private String reference;
        private String dateOfInquiry;
        private String addressLine1;
        private String addressLine2;
        private String salutation;
        private Address address;
    }

    @Setter
    @Getter
    public static class AdditionalInformation {
        private String sectionName;
        private String sectionContent;
    }
    @Setter
    @Getter
    public static class Complaint {
        private String escalationLevel;
        private String errorSource;
        private String description;
        private String reviewResult;
    }
    @Setter
    @Getter
    public static class Comment {
        private String comment;
    }
}