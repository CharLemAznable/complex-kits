package com.github.charlemaznable.core.codec;

import com.github.charlemaznable.core.codec.nonsense.NonsenseOptions;
import com.github.charlemaznable.core.codec.signature.SignatureOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NonsenseSignatureTest {

    @Test
    public void testNonsenseSignature() {
        val demoBean = new DemoBean();
        demoBean.setName("DEMO");
        val demoSubBean = new DemoSubBean();
        demoSubBean.setValue("Hello, NS!");
        demoBean.setSub(demoSubBean);

        val ns = new NonsenseSignature()
                .nonsenseOptions(new NonsenseOptions())
                .signatureOptions(new SignatureOptions());
        val result = ns.sign(demoBean);

        val nonsense = result.get("nonsense").toString();
        assertTrue(nonsense.matches("[A-Za-z0-9]{16}"));
        val signature = result.get("signature").toString();
        val plain = "name=DEMO&nonsense=" + nonsense + "&sub.value=Hello, NS!";
        assertEquals(Digest.SHA256.digestBase64(plain), signature);

        assertTrue(ns.verify(result));
    }

    @Getter
    @Setter
    public static class DemoBean {

        private String name;
        private DemoSubBean sub;
    }

    @Getter
    @Setter
    public static class DemoSubBean {

        private String value;
    }
}
