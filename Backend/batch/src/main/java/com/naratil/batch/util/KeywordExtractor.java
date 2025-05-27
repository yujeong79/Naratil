package com.naratil.batch.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import scala.collection.Seq;

@Slf4j
@Service
// 불용어 제외 키워드 추출
public class KeywordExtractor {

    // 불용어
    private final Set<String> stopwords;

    // 초기화 시 자동 로드 
    public KeywordExtractor() {
        this.stopwords = loadStopwords();
    }

    /**
     * 불용어 파일에서 읽어 오기 - 캐싱
     * @return stopwords 불용어 집합
     */
    private Set<String> loadStopwords() {
        Set<String> stopwords = new HashSet<>();
        try {
            ClassPathResource resource = new ClassPathResource("data/stopwords.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String line;
            // 한줄씩 읽기
            while ((line = reader.readLine()) != null) {
                stopwords.add(line.trim());
            }
            reader.close();
            log.info("[KeywordExtractor]stopwords 로드 완료 총 {}개 단어", stopwords.size());

        } catch (Exception e) {
            log.error("stopwords 파일 로드 실패: {}", e.getMessage());
        }
        return stopwords;
    }

    /**
     * 텍스트에서 불용어 제거
     * @param text 원본 텍스트
     * @return 불용어 제거된 텍스트
     */
    private String removeStopwordsFromText(String text) {
        String cleanText = text;
        for (String stopword : stopwords) {
            cleanText = cleanText.replaceAll(stopword, " ");
        }
        return cleanText.replaceAll("\\s+", " ").trim();
    }



    /**
     * 키워드 추출 (두 글자 이상 명사 + 영어 단어)
     * @param text 변환할 텍스트
     * @return List<String> 키워드 목록
     */
    private List<String> extractKeywords(String text) {
        // 정규화
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

        // 형태소 분해/분석
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
        
        // 두글자 이상 명사, 영어 단어 필터링
        return scala.collection.JavaConverters.seqAsJavaList(tokens)
                .stream()
                .filter(token -> token.text().length() > 1 && (
	                		token.pos().toString().equals("Noun") ||  // 일반 명사
	                        token.pos().toString().equals("ProperNoun") ||  // 고유 명사 
	                        token.pos().toString().equals("Alpha")  // 영어 단어
	                        ))
                .map(KoreanToken::text)
                .toList();
    }
    
    /**
     * Stopword 제외한 유의미한 키워드 추출
     * @param text 입찰공고명
     * @return List<String> 최종 키워드 리스트
     */
    public List<String> extractCleanKeywords(String text) {
        String cleanedText = removeStopwordsFromText(text); // 불용어 제거
        List<String> keywords = extractKeywords(cleanedText); // 키워드 추출
        log.debug("[KeywordExtractor] 추출된 키워드: {}", keywords);
        return keywords.stream()
                .distinct() // 중복 제거
                .toList();
    }
    
}
