package com.github.charlemaznable.core.codec;

import com.github.charlemaznable.core.codec.nonsense.Nonsense;
import com.github.charlemaznable.core.codec.nonsense.NonsenseOptions;
import com.github.charlemaznable.core.codec.signature.Signature;
import com.github.charlemaznable.core.codec.signature.SignatureOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true)
public final class NonsenseSignature {

    private NonsenseOptions nonsenseOptions = new NonsenseOptions();
    private SignatureOptions signatureOptions = new SignatureOptions();

    public Map<String, Object> sign(Object source) {
        Map<String, Object> sourceMap = Json.desc(source);
        val nonsense = Nonsense.nonsense(nonsenseOptions);
        sourceMap.put(nonsense.getKey(), nonsense.getValue());

        val signature = Signature.signature(sourceMap, signatureOptions);
        sourceMap.put(signature.getKey(), signature.getValue());
        return sourceMap;
    }

    public boolean verify(Object source) {
        Map<String, Object> sourceMap = Json.desc(source);
        return Signature.verify(sourceMap, signatureOptions);
    }
}
