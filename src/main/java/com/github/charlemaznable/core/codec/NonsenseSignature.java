package com.github.charlemaznable.core.codec;

import com.github.charlemaznable.core.codec.nonsense.NonsenseOptions;
import com.github.charlemaznable.core.codec.signature.SignatureOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.Map;

import static com.github.charlemaznable.core.codec.nonsense.Nonsense.nonsense;
import static com.github.charlemaznable.core.codec.signature.Signature.signature;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true)
public final class NonsenseSignature {

    private NonsenseOptions nonsenseOptions = new NonsenseOptions();
    private SignatureOptions signatureOptions = new SignatureOptions();

    public Map<String, Object> process(Object source) {
        Map<String, Object> sourceMap = Json.desc(source);
        val nonsense = nonsense(nonsenseOptions);
        sourceMap.put(nonsense.getKey(), nonsense.getValue());
        val signature = signature(sourceMap, signatureOptions);
        sourceMap.put(signature.getKey(), signature.getValue());
        return sourceMap;
    }
}
