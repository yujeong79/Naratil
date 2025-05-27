package com.naratil.industry.controller;

import com.naratil.industry.service.IndustryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class IndustryController {

    private final IndustryService service;

    public IndustryController(IndustryService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        service.saveExcelData(file);
        return ResponseEntity.ok("✅ 엑셀 데이터 업로드 성공!");
    }
}
