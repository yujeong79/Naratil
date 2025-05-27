package com.naratil.batch.controller;

import com.naratil.batch.runner.BidNoticeJobRunner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
@Tag(name = "입찰공고 관리", description = "입찰공고 데이터 수집 및 관리 API")
public class JobLauncherController {

    private final BidNoticeJobRunner bidNoticeJobRunner;

    /**
     * 입찰 공고 배치 작업을 수동으로 실행
     *
     * @param startDate 시작 날짜 (YYYYMMDDHHMM)
     * @param endDate   종료 날짜 (YYYYMMDDHHMM)
     * @return 작업 실행 결과
     */
    @PostMapping("/bids/fetch")
    @Operation(
        summary = "입찰공고 수집 작업 수동 실행",
        description = "공공데이터 포털에서 입찰공고 데이터를 수집하는 배치 작업을 실행"
    )
    public ResponseEntity<?> runBidNoticeJob(
        @Parameter(description = "조회 시작일 (YYYYMMDDHHMM 형식)")
        @RequestParam String startDate,
        @Parameter(description = "조회 종료일 (YYYYMMDDHHMM 형식)")
        @RequestParam String endDate) {

        try {
            // BidNoticeJobRunner를 사용하여 지정된 날짜 범위로 배치 작업 실행
            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDate, endDate);
            return ResponseEntity.ok()
                .body(String.format("입찰 공고 수동 조회 성공 : %s to %s", startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(String.format("입찰 공고 수동 조회 실패 : %s to %s - ", startDate, endDate) + e.getMessage());
        }
    }

    @PostMapping("/bids/update")
    @Operation(
        summary = "입찰공고 정보 업데이트 작업 수동 실행",
        description = "중복공고를 제거하고, 면허제한정보, 참가가능지역제한정보를 업데이트하는 배치 작업을 실행"
    )
    public ResponseEntity<?> runBidUpdateJob() {
        try {
            bidNoticeJobRunner.runBidUpdateJob();;
            return ResponseEntity.ok()
                .body(String.format("입찰공고 정보 업데이트 작업 성공"));
        }catch (Exception e) {
            return ResponseEntity.internalServerError().body(String.format("입찰공고 정보 업데이트 작업 실패 : {}") + e.getMessage());
        }
    }

    @PostMapping("/bids/init")
    @Operation(
            summary = "입찰공고 초기화 작업 수동 실행",
            description = "MongoDB의 bids 컬렉션을 초기화 하는 배치 작업을 실행"
    )
    public ResponseEntity<?> runBidInitJob() {
        try {
            bidNoticeJobRunner.runBidInitJob();;
            return ResponseEntity.ok()
                    .body(String.format("입찰공고 초기화 작업 성공"));
        }catch (Exception e) {
            return ResponseEntity.internalServerError().body(String.format("입찰공고 초기화 작업 실패 : {}") + e.getMessage());
        }
    }

}
