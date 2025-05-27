package com.naratil.industry.service;

import com.naratil.industry.entity.Industry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryService {

    @PersistenceContext
    private EntityManager entityManager; // 직접 EntityManager 사용하여 배치 처리

    private static final int BATCH_SIZE = 5000; // 배치 저장 크기 증가

    @Transactional
    public void saveExcelData(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트
            List<Industry> industries = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 첫 줄(헤더) 스킵

                Industry code = Industry.builder()
                        .majorCategoryCode(getLongCellValue(row.getCell(0)))    // 대분류 코드
                        .majorCategoryName(getStringCellValue(row.getCell(1)))  // 대분류 명
                        .categoryCode(getLongCellValue(row.getCell(2)))         // 분류 코드
                        .categoryName(getStringCellValue(row.getCell(3)))       // 분류 명
                        .industryCode(getLongCellValue(row.getCell(4)))         // 업종 코드
                        .industryName(getStringCellValue(row.getCell(5)))       // 업종 명
                        .build();
                industries.add(code);

                // 배치 크기만큼 모이면 저장 후 리스트 초기화
                if (industries.size() >= BATCH_SIZE) {
                    saveBatch(industries);
                    industries.clear();
                }
            }

            // 남은 데이터 저장
            if (!industries.isEmpty()) {
                saveBatch(industries);
            }
        } catch (Exception e) {
            log.error("엑셀 파일 처리 중 오류 발생!", e);
            throw new RuntimeException("엑셀 파일 처리 중 오류 발생!", e);
        }
    }

    @Transactional
    public void saveBatch(List<Industry> industries) {
        for (int i = 0; i < industries.size(); i++) {
            entityManager.persist(industries.get(i));

            // BATCH_SIZE마다 flush & clear 수행
            if (i > 0 && i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    // 셀이 null이거나 빈 경우를 고려하여 안전하게 값을 가져오는 메서드
    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private Long getLongCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }
        try {
            return Long.parseLong(cell.getStringCellValue().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
