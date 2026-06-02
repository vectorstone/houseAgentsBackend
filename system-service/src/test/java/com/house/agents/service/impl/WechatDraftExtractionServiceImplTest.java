package com.house.agents.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.service.WechatDraftExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WechatDraftExtractionServiceImplTest {

    private WechatDraftExtractionServiceImpl service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        service = new WechatDraftExtractionServiceImpl();
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    @Test
    public void enrichShouldProduceSchemaWithEvidenceAndNullUnknownFields() throws Exception {
        WechatDraftIngestVo request = new WechatDraftIngestVo();
        request.setVisibleText("未来城 朝南 1室1厅 4200 6号线");

        WechatDraftExtractionService.ExtractionOutcome outcome = service.enrich(request);

        assertTrue(outcome.isSchemaValid());
        assertTrue(outcome.isExtracted());
        assertEquals("EXTRACTED", outcome.getRecommendedDraftStatus());
        assertNotNull(request.getExtractedJson());
        assertNotNull(request.getFieldConfidenceJson());
        assertNotNull(request.getOverallConfidence());

        Map<String, Object> extracted = objectMapper.readValue(request.getExtractedJson(), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> confidence = objectMapper.readValue(request.getFieldConfidenceJson(), new TypeReference<Map<String, Object>>() {});
        assertEquals("未来城", extracted.get("community"));
        assertEquals("1室1厅", extracted.get("roomNumber"));
        assertEquals("4200", extracted.get("rent"));
        assertEquals("朝南", extracted.get("orientation"));
        assertEquals("6号线", extracted.get("subway"));
        assertNull(extracted.get("keyOrPassword"));
        assertNull(extracted.get("landlordName"));
        assertNull(extracted.get("remark"));
        assertTrue(((List<?>) extracted.get("listingHighlights")).size() >= 1);
        assertTrue(((Number) confidence.get("rent")).doubleValue() > 0.9D);

        List<Map<String, Object>> sourceEvidence = (List<Map<String, Object>>) extracted.get("sourceEvidence");
        assertFalse(sourceEvidence.isEmpty());
        assertEquals("rent", sourceEvidence.stream()
                .filter(item -> "rent".equals(item.get("field")))
                .findFirst()
                .orElseThrow()
                .get("field"));
    }

    @Test
    public void enrichShouldKeepProvidedSchemaAndBackfillConfidenceFromEvidence() throws Exception {
        WechatDraftIngestVo request = new WechatDraftIngestVo();
        request.setVisibleText("未来城 朝南 1室1厅 4200");
        request.setExtractedJson("{\"community\":\"人工结果\",\"subway\":null,\"roomNumber\":null,\"rent\":null,\"orientation\":null,\"keyOrPassword\":null,\"landlordName\":null,\"remark\":null,\"listingHighlights\":[],\"risksOrMissingInfo\":[],\"sourceEvidence\":[{\"field\":\"community\",\"evidenceType\":\"manual\",\"span\":\"人工结果\",\"confidence\":0.99}]}");

        WechatDraftExtractionService.ExtractionOutcome outcome = service.enrich(request);

        assertTrue(outcome.isSchemaValid());
        assertEquals("EXTRACTED", outcome.getRecommendedDraftStatus());
        assertNotNull(request.getFieldConfidenceJson());
        assertEquals(0.99D, request.getOverallConfidence());

        Map<String, Object> confidence = objectMapper.readValue(request.getFieldConfidenceJson(), new TypeReference<Map<String, Object>>() {});
        assertEquals(0.99D, ((Number) confidence.get("community")).doubleValue());
    }

    @Test
    public void enrichShouldRejectInvalidProvidedSchemaSafely() {
        WechatDraftIngestVo request = new WechatDraftIngestVo();
        request.setVisibleText("未来城 朝南 1室1厅 4200");
        request.setExtractedJson("{\"community\":\"人工结果\"}");

        WechatDraftExtractionService.ExtractionOutcome outcome = service.enrich(request);

        assertFalse(outcome.isSchemaValid());
        assertEquals("NEEDS_REVIEW", outcome.getRecommendedDraftStatus());
        assertTrue(outcome.getFailureReason().contains("schema"));
        assertNull(request.getFieldConfidenceJson());
    }
}
