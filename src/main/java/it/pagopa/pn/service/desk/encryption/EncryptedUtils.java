package it.pagopa.pn.service.desk.encryption;


import it.pagopa.pn.service.desk.encryption.model.EncryptionModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


@Getter
@Setter
public class EncryptedUtils {
    private static final Pattern ENCRYPTED_STRING = Pattern.compile("^(?>\\((?<context>.*)\\)|\\[(?<options>.*)]){0,2}(?<cipher>.*)$");
    private static final Function<String[], String> FIRST = arr -> arr[0];
    private static final Function<String[], String> SECOND = arr -> arr.length > 1 ? arr[1] : "";
    private static final String PAIR_SEPARATOR = ",";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private final ByteBuffer cipherBytes;
    private final Map<String, String> encryptionContext;
    private final EncryptionModel model;

    private EncryptedUtils(ByteBuffer cipherBytes, Map<String, String> encryptionContext, EncryptionModel model) {
        this.cipherBytes = cipherBytes;
        this.encryptionContext = encryptionContext;
        this.model = model;
    }

    public static EncryptedUtils parse(String s) {
        Assert.hasText(s, "Encrypted string must not be blank");

        final Matcher matcher = ENCRYPTED_STRING.matcher(s);
        Assert.isTrue(matcher.matches(), "Malformed encrypted string '" + s + "'");

        final String contextString = matcher.group("context");
        final String optionsString = matcher.group("options");
        final String cipherString = matcher.group("cipher");

        return new EncryptedUtils(parseCipher(cipherString), parseContext(contextString), parseOptions(optionsString));
    }

    private static ByteBuffer parseCipher(String valueString) {
        return ByteBuffer.wrap(Base64.getDecoder().decode(valueString.getBytes(StandardCharsets.UTF_8)));
    }

    private static EncryptionModel parseOptions(String optionsString) {
        final Map<String, String> optionsMap = parseKeyValueMap(optionsString);

        final String kmsKeyId = optionsMap.get("keyId");
        final String encryptionAlgorithm = optionsMap.get("algorithm");

        return new EncryptionModel(kmsKeyId, encryptionAlgorithm);
    }

    private static Map<String, String> parseContext(String contextString) {
        return parseKeyValueMap(contextString, v -> new String(Base64.getDecoder().decode(v.getBytes(StandardCharsets.UTF_8))));
    }

    private static Map<String, String> parseKeyValueMap(String kvString) {
        return parseKeyValueMap(kvString, identity());
    }

    private static Map<String, String> parseKeyValueMap(String kvString, Function<String, String> valueMapper) {
        return Stream.of(
                        Optional.ofNullable(kvString)
                                .map(StringUtils::trimAllWhitespace)
                                .filter(StringUtils::hasText)
                                .map(s -> s.split(PAIR_SEPARATOR))
                                .orElse(new String[0]))
                .map(StringUtils::trimAllWhitespace)
                .map(pair -> pair.split(KEY_VALUE_SEPARATOR, 2))
                .collect(toMap(
                        FIRST.andThen(StringUtils::trimAllWhitespace),
                        SECOND.andThen(StringUtils::trimAllWhitespace).andThen(valueMapper)));
    }
}