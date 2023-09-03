package com.example.test.controller;

import com.example.test.dto.TestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class TestController {

  @GetMapping(value = "/get")
  public ResponseEntity<?> test0(@RequestParam String id, @RequestParam Long age) {
    log.info("id={}, age={}", id, age);
    return ResponseEntity.ok("test0");
  }

  @PostMapping(value = "/form_urlencoded", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> test1(TestDto testDto) {
    log.info("testDto={}", testDto);
    return ResponseEntity.ok("test1");
  }

  @PostMapping(value = "/multipart_form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> test2(TestDto testDto) {
    log.info("testDto={}", testDto);
    return ResponseEntity.ok("test2");
  }

  @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> test3(@RequestBody TestDto testDto) {
    log.info("testDto={}", testDto);
    return ResponseEntity.ok("test3");
  }
}