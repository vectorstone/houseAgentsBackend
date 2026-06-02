package com.house.agents.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.service.WechatDraftExtractionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WechatDraftExtractionServiceImpl implements WechatDraftExtractionService {

    private static final Pattern RENT_PATTERN = Pattern.compile("(?<!\\d)(\\d{3,6})(?:元|/月)?(?!\\d)");
    private static final Pattern ROOM_PATTERN = Pattern.compile("(\\d室\\d厅(?:\\d卫)?)");
    private static final Pattern ORIENTATION_PATTERN = Pattern.compile("(朝南|朝北|朝东|朝西|东南|西南|东北|西北|南北通透|南北|南向|北向|东向|西向|南|北|东|西)");
    private static final Pattern SUBWAY_PATTERN = Pattern.compile("([0-9]{1,2}号线)");
    private static final List<String> SCHEMA_FIELDS = Arrays.asList(
            "community",
            "subway",
            "roomNumber",
            "rent",
            "orientation",
            "keyOrPassword",
            "landlordName",
            "remark",
            "listingHighlights",
            "risksOrMissingInfo",
            "sourceEvidence"
    );
    private static final Set<String> ALLOWED_EVIDENCE_TYPES = new LinkedHashSet<>(Arrays.asList("text", "ocr", "link", "manual"));

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ExtractionOutcome enrich(WechatDraftIngestVo request) {
        ExtractionOutcome outcome = new ExtractionOutcome();
        outcome.setSchemaValid(true);
        outcome.setRecommendedDraftStatus("PENDING");
        if (request == null) {
            return outcome;
        }

        if (StringUtils.isNotBlank(request.getExtractedJson())) {
            validateProvidedExtraction(request, outcome);
            if (!outcome.isSchemaValid()) {
                outcome.setRecommendedDraftStatus("NEEDS_REVIEW");
            }
            return outcome;
        }

        String text = StringUtils.defaultString(request.getVisibleText()).replace('\n', ' ').trim();
        Map<String, Object> extracted = new LinkedHashMap<>();
        Map<String, Object> confidence = new LinkedHashMap<>();
        List<Map<String, Object>> evidence = new ArrayList<>();

        initializeSchema(extracted);
        fillRent(text, extracted, confidence, evidence);
        fillRoomNumber(text, extracted, confidence, evidence);
        fillOrientation(text, extracted, confidence, evidence);
        fillSubway(text, extracted, confidence, evidence);
        fillCommunity(text, extracted, confidence, evidence);
        fillRisks(extracted, evidence);
        ensureCollections(extracted, evidence);

        request.setExtractedJson(writeJson(extracted));
        request.setFieldConfidenceJson(writeJson(confidence));
        request.setOverallConfidence(calculateOverallConfidence(confidence));
        outcome.setExtracted(!confidence.isEmpty());
        outcome.setRecommendedDraftStatus(outcome.isExtracted() ? "EXTRACTED" : "PENDING");
        return outcome;
    }

    private void validateProvidedExtraction(WechatDraftIngestVo request, ExtractionOutcome outcome) {
        try {
            Map<String, Object> extracted = objectMapper.readValue(request.getExtractedJson(), new TypeReference<Map<String, Object>>() {});
            if (!hasSchemaShape(extracted)) {
                outcome.setSchemaValid(false);
                outcome.setFailureReason("extractedJson schema missing required fields");
                return;
            }
            Object sourceEvidence = extracted.get("sourceEvidence");
            if (!(sourceEvidence instanceof List)) {
                outcome.setSchemaValid(false);
                outcome.setFailureReason("sourceEvidence must be an array");
                return;
            }
            for (Object item : (List<?>) sourceEvidence) {
                if (!(item instanceof Map)) {
                    outcome.setSchemaValid(false);
                    outcome.setFailureReason("sourceEvidence item must be an object");
                    return;
                }
                Map<?, ?> evidence = (Map<?, ?>) item;
                Object field = evidence.get("field");
                Object evidenceType = evidence.get("evidenceType");
                Object span = evidence.get("span");
                Object confidence = evidence.get("confidence");
                if (!(field instanceof String) || !(span instanceof String) || !(confidence instanceof Number)
                        || !(evidenceType instanceof String) || !ALLOWED_EVIDENCE_TYPES.contains(evidenceType)) {
                    outcome.setSchemaValid(false);
                    outcome.setFailureReason("sourceEvidence item missing required typed fields");
                    return;
                }
            }
            if (StringUtils.isBlank(request.getFieldConfidenceJson())) {
                request.setFieldConfidenceJson(writeJson(buildConfidenceFromEvidence((List<?>) sourceEvidence)));
            }
            if (request.getOverallConfidence() == null) {
                request.setOverallConfidence(calculateOverallConfidenceFromEvidence((List<?>) sourceEvidence));
            }
            outcome.setExtracted(true);
            outcome.setRecommendedDraftStatus("EXTRACTED");
        } catch (Exception ex) {
            outcome.setSchemaValid(false);
            outcome.setFailureReason("invalid extractedJson schema: " + StringUtils.abbreviate(ex.getMessage(), 256));
        }
    }

    private void initializeSchema(Map<String, Object> extracted) {
        extracted.put("community", null);
        extracted.put("subway", null);
        extracted.put("roomNumber", null);
        extracted.put("rent", null);
        extracted.put("orientation", null);
        extracted.put("keyOrPassword", null);
        extracted.put("landlordName", null);
        extracted.put("remark", null);
        extracted.put("listingHighlights", new ArrayList<>());
        extracted.put("risksOrMissingInfo", new ArrayList<>());
        extracted.put("sourceEvidence", new ArrayList<>());
    }

    private void fillRent(String text, Map<String, Object> extracted, Map<String, Object> confidence, List<Map<String, Object>> evidence) {
        Matcher matcher = RENT_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group(1);
            if (StringUtils.length(candidate) >= 4) {
                extracted.put("rent", candidate);
                confidence.put("rent", 0.93D);
                addEvidence(extracted, evidence, "rent", "text", matcher.group(0), 0.93D);
                return;
            }
        }
    }

    private void fillRoomNumber(String text, Map<String, Object> extracted, Map<String, Object> confidence, List<Map<String, Object>> evidence) {
        Matcher matcher = ROOM_PATTERN.matcher(text);
        if (matcher.find()) {
            extracted.put("roomNumber", matcher.group(1));
            confidence.put("roomNumber", 0.95D);
            addEvidence(extracted, evidence, "roomNumber", "text", matcher.group(1), 0.95D);
        }
    }

    private void fillOrientation(String text, Map<String, Object> extracted, Map<String, Object> confidence, List<Map<String, Object>> evidence) {
        Matcher matcher = ORIENTATION_PATTERN.matcher(text);
        if (matcher.find()) {
            extracted.put("orientation", matcher.group(1));
            confidence.put("orientation", 0.84D);
            addEvidence(extracted, evidence, "orientation", "text", matcher.group(1), 0.84D);
        }
    }

    private void fillSubway(String text, Map<String, Object> extracted, Map<String, Object> confidence, List<Map<String, Object>> evidence) {
        Matcher matcher = SUBWAY_PATTERN.matcher(text);
        if (matcher.find()) {
            extracted.put("subway", matcher.group(1));
            confidence.put("subway", 0.81D);
            addEvidence(extracted, evidence, "subway", "text", matcher.group(1), 0.81D);
        }
    }

    private void fillCommunity(String text, Map<String, Object> extracted, Map<String, Object> confidence, List<Map<String, Object>> evidence) {
        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            if (token.contains("小区") || token.contains("花园") || token.contains("苑") || token.contains("城")) {
                if (!ROOM_PATTERN.matcher(token).find() && !RENT_PATTERN.matcher(token).matches()) {
                    extracted.put("community", token);
                    confidence.put("community", 0.72D);
                    addEvidence(extracted, evidence, "community", "text", token, 0.72D);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fillRisks(Map<String, Object> extracted, List<Map<String, Object>> evidence) {
        List<String> risks = (List<String>) extracted.get("risksOrMissingInfo");
        if (extracted.get("rent") == null) {
            risks.add("missing rent evidence");
        }
        if (extracted.get("roomNumber") == null) {
            risks.add("missing roomNumber evidence");
        }
        if (evidence.isEmpty()) {
            risks.add("no structured evidence extracted from visibleText");
        }
    }

    @SuppressWarnings("unchecked")
    private void ensureCollections(Map<String, Object> extracted, List<Map<String, Object>> evidence) {
        List<Map<String, Object>> sourceEvidence = (List<Map<String, Object>>) extracted.get("sourceEvidence");
        sourceEvidence.clear();
        sourceEvidence.addAll(evidence);
        List<String> highlights = (List<String>) extracted.get("listingHighlights");
        if (extracted.get("community") != null && extracted.get("roomNumber") != null) {
            highlights.add(extracted.get("community") + " " + extracted.get("roomNumber"));
        }
    }

    private void addEvidence(Map<String, Object> extracted, List<Map<String, Object>> evidence, String field, String evidenceType, String span, double value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("field", field);
        item.put("evidenceType", evidenceType);
        item.put("span", span);
        item.put("confidence", value);
        evidence.add(item);
        extracted.put(field, extracted.get(field));
    }

    private boolean hasSchemaShape(Map<String, Object> extracted) {
        if (extracted == null) {
            return false;
        }
        for (String field : SCHEMA_FIELDS) {
            if (!extracted.containsKey(field)) {
                return false;
            }
        }
        return extracted.get("listingHighlights") instanceof List
                && extracted.get("risksOrMissingInfo") instanceof List;
    }

    private Map<String, Object> buildConfidenceFromEvidence(List<?> evidenceList) {
        Map<String, Object> confidence = new LinkedHashMap<>();
        for (Object item : evidenceList) {
            if (item instanceof Map) {
                Map<?, ?> evidence = (Map<?, ?>) item;
                Object field = evidence.get("field");
                Object value = evidence.get("confidence");
                if (field instanceof String && value instanceof Number) {
                    confidence.put((String) field, value);
                }
            }
        }
        return confidence;
    }

    private Double calculateOverallConfidenceFromEvidence(List<?> evidenceList) {
        return calculateOverallConfidence(buildConfidenceFromEvidence(evidenceList));
    }

    private Double calculateOverallConfidence(Map<String, Object> confidence) {
        if (confidence == null || confidence.isEmpty()) {
            return 0.0D;
        }
        double sum = confidence.values().stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0.0D);
        return Math.round(sum * 100.0D) / 100.0D;
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize extracted draft json", e);
        }
    }
}
