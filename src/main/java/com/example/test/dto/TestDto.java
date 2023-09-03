package com.example.test.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TestDto {

  private String id;
  private String name;
  private MultipartFile file;
}
