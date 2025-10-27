package com.pratkdev.sesemltoperson.model;

public class Person {
    private String name;
    private String email;
    private String phone;
    private Integer age;

    public Person() {}

    public Person(String name, String email, String phone, Integer age) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.age = age;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", age=" + age +
                '}';
    }
}
