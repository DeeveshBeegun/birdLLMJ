package com.acn.wildlifeextractor.support;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.acn.wildlifeextractor.domain.field.ExtractedBoolean;
import com.acn.wildlifeextractor.domain.field.ExtractedCode;
import com.acn.wildlifeextractor.domain.field.ExtractedCodeList;
import com.acn.wildlifeextractor.domain.field.ExtractedDate;
import com.acn.wildlifeextractor.domain.field.ExtractedDecimal;
import com.acn.wildlifeextractor.domain.field.ExtractedInteger;
import com.acn.wildlifeextractor.domain.field.ExtractedReference;
import com.acn.wildlifeextractor.domain.field.ExtractedString;
import com.acn.wildlifeextractor.domain.field.ExtractedTime;
import com.acn.wildlifeextractor.domain.field.ExtractionFieldStatus;
import com.acn.wildlifeextractor.domain.reference.ReferenceResolutionStatus;

/**
 * Compact factory helpers for building {@code Extracted*} values in tests.
 */
public final class ExtractedValues {

    private ExtractedValues() {
    }

    public static ExtractedString string(String raw, String normalized, String evidence) {
        return new ExtractedString(raw, normalized, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedString missingString() {
        return new ExtractedString(null, null, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedBoolean bool(String raw, Boolean value, String evidence) {
        return new ExtractedBoolean(raw, value, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedBoolean missingBoolean() {
        return new ExtractedBoolean(null, null, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedInteger integer(String raw, Integer value, String evidence) {
        return new ExtractedInteger(raw, value, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedInteger missingInteger() {
        return new ExtractedInteger(null, null, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedDecimal decimal(String raw, BigDecimal value, String evidence) {
        return new ExtractedDecimal(raw, value, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedDecimal missingDecimal() {
        return new ExtractedDecimal(null, null, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedDate date(String raw, LocalDate value, String evidence) {
        return new ExtractedDate(raw, value, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedDate missingDate() {
        return new ExtractedDate(null, null, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedTime time(String raw, LocalTime value, String evidence) {
        return new ExtractedTime(raw, value, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedTime missingTime() {
        return new ExtractedTime(null, null, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedCode code(String raw, String normalized, String vocabulary, String evidence) {
        return new ExtractedCode(raw, normalized, vocabulary, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedCode missingCode(String vocabulary) {
        return new ExtractedCode(null, null, vocabulary, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedCodeList codeList(List<String> raw, List<String> normalized, String vocabulary, String evidence) {
        return new ExtractedCodeList(raw, normalized, vocabulary, ExtractionFieldStatus.PRESENT, evidence, List.of());
    }

    public static ExtractedCodeList missingCodeList(String vocabulary) {
        return new ExtractedCodeList(List.of(), List.of(), vocabulary, ExtractionFieldStatus.MISSING, null, List.of());
    }

    public static ExtractedReference reference(String spoken, String type, String lookupKey, String evidence) {
        return new ExtractedReference(spoken, type, lookupKey, null, null,
                ReferenceResolutionStatus.NOT_REQUESTED, ExtractionFieldStatus.PRESENT, evidence, List.of(), List.of());
    }

    public static ExtractedReference missingReference(String type) {
        return new ExtractedReference(null, type, null, null, null,
                ReferenceResolutionStatus.NOT_REQUESTED, ExtractionFieldStatus.MISSING, null, List.of(), List.of());
    }
}
