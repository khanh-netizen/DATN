package com.foxstyle.api.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Chuẩn hóa mọi chuỗi nhập vào hệ thống về Unicode NFC và tự sửa các chuỗi
 * tiếng Việt bị đọc nhầm UTF-8 thành Windows-1252 (ví dụ: "GiÃ¡").
 */
public final class VietnameseTextNormalizer {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final Pattern MOJIBAKE_MARKERS = Pattern.compile(
            "Ã.|Â.|Ä.|Æ.|áº|á»|â€|â€“|â€”|ï¿½|\\uFFFD");

    private VietnameseTextNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;
        for (int attempt = 0; attempt < 2 && looksCorrupted(result); attempt++) {
            String repaired = decodeWindows1252AsUtf8(result);
            if (repaired.equals(result) || repaired.indexOf('\uFFFD') >= 0) {
                break;
            }
            result = repaired;
        }
        return Normalizer.normalize(result, Normalizer.Form.NFC);
    }

    private static boolean looksCorrupted(String value) {
        return MOJIBAKE_MARKERS.matcher(value).find();
    }

    private static String decodeWindows1252AsUtf8(String value) {
        try {
            return new String(value.getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
        } catch (RuntimeException exception) {
            return value;
        }
    }
}
