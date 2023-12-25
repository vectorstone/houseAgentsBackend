package com.house.agents.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangjinlu on 16/6/12.
 */
public class XMDLogFormat implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(XMDLogFormat.class);
    private static final long serialVersionUID = 1L;
    private static final String TRACEID_KEY = "traceID";
    private static final String SUBCATEGORY_KEY = "__subcategory__";
    private static final String MAGIC_WORD = "#XMDT#";
    private static final String MAGIC_WORD_PREFIX = MAGIC_WORD + "{";
    private static final String MAGIC_WORD_SUFFIX = "}" + MAGIC_WORD;
    private static final String JSON_WORD = "#XMDJ#";
    private static final int TAGS_MAP_MAX = 1024;
    private final Map<String, String> tagsMap = new HashMap<String, String>();
    private String jsonStr = "";


    private static final int MAGIC_WORD_PREFIX_LENGTH = MAGIC_WORD_PREFIX.length();
    private static final int MAGIC_WORD_SUFFIX_LENGTH = MAGIC_WORD_SUFFIX.length();
    private static final int JSON_WORD_LENGTH = JSON_WORD.length();
    /**
     * return new XMDLogFormat
     */
    public static XMDLogFormat build() {
        return new XMDLogFormat();
    }

    public XMDLogFormat putTag(String k, String v) {
        if (k == null) {
            return this;
        }
        String validKey = getValidTagKey(k);
        if (validKey.isEmpty()) {
            return this;
        }
        if (tagsMap.size() >= TAGS_MAP_MAX) {
            LOGGER.error("no space left in XMDLogFormat tags map for {}={}", validKey, getValidTagValue(v));
            return this;
        }
        tagsMap.put(validKey, getValidTagValue(v));
        return this;
    }

    public XMDLogFormat putTags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return this;
        }
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            this.putTag(entry.getKey(), entry.getValue());
        }
        return this;
    }

    // public XMDLogFormat putTraceID() {
    //     String traceID = ConfigUtil.getTraceID();
    //     if (traceID != ConfigUtil.DEFAULT_TRACEID) {
    //         tagsMap.put(TRACEID_KEY, traceID);
    //     }
    //     return this;
    // }

    @Deprecated
    public XMDLogFormat subCategory(String category) {
        if (category == null || category.isEmpty()) {
            return this;
        }
        tagsMap.put(SUBCATEGORY_KEY, getValidTagValue(category));
        return this;
    }

    public XMDLogFormat putJson(String jStr) {
        if (jStr == null || jStr.isEmpty()) {
            return this;
        }
        jsonStr = jStr;
        return this;
    }

    public XMDLogFormat clear() {
        tagsMap.clear();
        jsonStr = "";
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getToStringLength());

        if (!tagsMap.isEmpty()) {
            sb.append(MAGIC_WORD_PREFIX);
            for (Map.Entry<String, String> entry : tagsMap.entrySet()) {
                sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
            }
            sb.append(MAGIC_WORD_SUFFIX);
        }
        if (jsonStr != null && !jsonStr.isEmpty()) {
            sb.append(" ").append(JSON_WORD).append(jsonStr).append(JSON_WORD);
        }
        return sb.toString();
    }

    private int getToStringLength() {
        int length = 0;

        if (!tagsMap.isEmpty()) {
            length += MAGIC_WORD_PREFIX_LENGTH;
            for (Map.Entry<String, String> entry : tagsMap.entrySet()) {
                length += entry.getKey().length() + entry.getValue().length() + 2;
            }
            length += MAGIC_WORD_SUFFIX_LENGTH;
        }
        if (jsonStr != null && !jsonStr.isEmpty()) {
            length += 1 + JSON_WORD_LENGTH + jsonStr.length() + JSON_WORD_LENGTH;
        }

        return length;
    }

    public String message(String msg) {
        if (msg == null || msg.isEmpty()) {
            return toString();
        }
        int length = getToStringLength() + msg.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        stringBuilder.append(toString()).append(msg);

        return stringBuilder.toString();
    }

    public String getTag(String k) {
        return tagsMap.get(getValidTagKey(k));
    }

    public static String getValidTagKey(String s) {
        if (s == null) {
            return "";
        }
        String temp = StringUtils.remove(s, '=');
        return StringUtils.remove(temp, ' ');
    }

    public static String getValidTagValue(String s) {
        if (s == null) {
            return "";
        }
        return StringUtils.remove(s, '=');
    }

    public static String putTagToSerializedString(final String k, final String v, final String message) {
        String ret = "";
        if (message != null && !message.isEmpty()) {
            ret = message;
        }
        if (k == null || getValidTagKey(k).isEmpty() || v == null || getValidTagValue(v).isEmpty()) {
            return ret;
        }
        if (ret.contains(MAGIC_WORD_SUFFIX)) {
            return ret.replace(MAGIC_WORD_SUFFIX, " " + getValidTagKey(k) + "=" + getValidTagValue(v) + MAGIC_WORD_SUFFIX);
        } else {
            return ret + MAGIC_WORD_PREFIX + " " + getValidTagKey(k) + "=" + getValidTagValue(v) + MAGIC_WORD_SUFFIX;
        }
    }

    /**
     * 把KV结构的tags序列化到message中
     *
     * @param tags    Key-Value String Map
     * @param message log message
     * @return serialize string
     */
    public static String putTagsToSerializedString(final Map<String, String> tags, final String message) {
        if (tags != null && !tags.isEmpty() && message != null && !message.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int splitIndex = message.indexOf(MAGIC_WORD_SUFFIX);
            // 前缀判断
            if (splitIndex == -1) {
                sb.append(message);
                sb.append(MAGIC_WORD_PREFIX);
            } else {
                sb.append(message.substring(0, splitIndex));
            }
            // tags 序列化
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                sb.append(" ");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
            }
            // 后缀判断
            if (splitIndex == -1) {
                sb.append(MAGIC_WORD_SUFFIX);
            } else {
                sb.append(message.substring(splitIndex, message.length()));
            }
            return sb.toString();
        }
        return message;
    }
}
