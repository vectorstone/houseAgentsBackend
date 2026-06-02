package com.house.agents.service;

import com.house.agents.entity.vo.WechatDraftIngestVo;

public interface WechatDraftExtractionService {

    ExtractionOutcome enrich(WechatDraftIngestVo request);

    class ExtractionOutcome {
        private boolean schemaValid;
        private boolean extracted;
        private String recommendedDraftStatus;
        private String failureReason;

        public boolean isSchemaValid() {
            return schemaValid;
        }

        public void setSchemaValid(boolean schemaValid) {
            this.schemaValid = schemaValid;
        }

        public boolean isExtracted() {
            return extracted;
        }

        public void setExtracted(boolean extracted) {
            this.extracted = extracted;
        }

        public String getRecommendedDraftStatus() {
            return recommendedDraftStatus;
        }

        public void setRecommendedDraftStatus(String recommendedDraftStatus) {
            this.recommendedDraftStatus = recommendedDraftStatus;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public void setFailureReason(String failureReason) {
            this.failureReason = failureReason;
        }
    }
}
