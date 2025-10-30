package com.pratkdev.sesemltoperson.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@AllArgsConstructor
@Getter
@Setter
public class FileOutput {
    InputStream inputStream;
    String name;
}
