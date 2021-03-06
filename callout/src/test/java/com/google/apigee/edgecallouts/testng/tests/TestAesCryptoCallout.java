// TestAesCryptoCallout.java
//
// Test code for the AES Crypto custom policy for Apigee Edge. Uses TestNG.
// For full details see the Readme accompanying this source file.
//
// Copyright (c) 2016 Apigee Corp, 2017 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// @author: Dino Chiesa
//
// Note:
// If you use the Oracle JDK to run tests, this test, which does
// 256-bit crypto, requires the Unlimited Strength JCE.
//
// Without it, you may get an exception while running this test:
//
// java.security.InvalidKeyException: Illegal key size
//         at javax.crypto.Cipher.checkCryptoPerm(Cipher.java:1039)
//         ....
//
// See http://stackoverflow.com/a/6481658/48082
//
// If you use OpenJDK to run the tests, then it's not an issue.
// In that JDK, there's no restriction on key strength.
//
// Saturday, 21 May 2016, 08:59
//

package com.google.apigee.edgecalluts.testng.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeMethod;

import mockit.Mock;
import mockit.MockUp;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionResult;

import com.google.apigee.edgecallouts.AesCryptoCallout;
import java.nio.charset.StandardCharsets;

public class TestAesCryptoCallout {

    MessageContext msgCtxt;
    ExecutionContext exeCtxt;

    @BeforeMethod()
    public void testSetup1() {

        msgCtxt = new MockUp<MessageContext>() {
                private Map<String,Object> variables;
                public void $init() {
                    getVariables();
                }
                private Map<String,Object> getVariables() {
                    if (variables == null) {
                        variables = new HashMap<String,Object>();
                    }
                    return variables;
                }
                @Mock()
                public Object getVariable(final String name){
                    return getVariables().get(name);
                }

                @Mock()
                public boolean setVariable(final String name, final Object value) {
                    getVariables().put(name, value);
                    return true;
                }

                @Mock()
                public boolean removeVariable(final String name) {
                    if (getVariables().containsKey(name)) {
                        variables.remove(name);
                    }
                    return true;
                }
            }.getMockInstance();

        exeCtxt = new MockUp<ExecutionContext>(){ }.getMockInstance();
        System.out.printf("=============================================\n");
    }

    private void reportThings(Map<String,String> props) {
        String test = props.get("testname");
        System.out.println("test  : " + test);
        String cipher = msgCtxt.getVariable("crypto_cipher");
        System.out.println("cipher: " + cipher);
        String action = msgCtxt.getVariable("crypto_action");
        System.out.println("action: " + action);
        String output = msgCtxt.getVariable("crypto_output");
        System.out.println("output: " + output);
        String keyHex = msgCtxt.getVariable("crypto_key_hex");
        System.out.println("key   : " + keyHex);
        String ivHex = msgCtxt.getVariable("crypto_iv_hex");
        System.out.println("iv    : " + ivHex);
        String saltHex = msgCtxt.getVariable("crypto_salt_hex");
        System.out.println("salt  : " + saltHex);
        Assert.assertNotNull(ivHex);
        Assert.assertNotNull(output);
    }


    @Test()
    public void Encrypt_QuickBrownFox_Hex() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "Encrypt_QuickBrownFox_Hex");
        properties.put("action",        "encrypt");
        properties.put("passphrase",    "ABCDEFG-1234567");
        properties.put("debug",         "true");
        properties.put("padding",       "PKCS5PADDING");
        properties.put("encode-result", "hex");

        msgCtxt.setVariable("message.content", "The quick brown fox jumped over the lazy dog.");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        reportThings(properties);
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        Assert.assertNull(error);
    }

    @Test()
    public void Encrypt_QuickBrownFox_Base64() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "Encrypt_QuickBrownFox_Base64");
        properties.put("action",        "encrypt");
        properties.put("passphrase",    "ABCDEFG-1234567");
        properties.put("debug",         "true");
        properties.put("padding",       "PKCS5PADDING");
        properties.put("encode-result", "base64");

        msgCtxt.setVariable("message.content", "The quick brown fox jumped over the lazy dog.");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        Assert.assertNull(error);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "FutVqI4rZKuh08Yp4M4NtgIxn_XB_5dABt7LdinolokWgVRx-ILkJvKuR1IApkpD");
    }

    @Test()
    public void Decrypt_QuickBrownFox() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "Decrypt_QuickBrownFox");
        properties.put("action",             "decrypt");
        properties.put("passphrase",         "ABCDEFG-1234567");
        properties.put("iv",                 "fb14d470c24780cff5aa22836924c5af");
        properties.put("decode-iv",          "hex");
        properties.put("decode-source",      "base64");
        properties.put("utf8-decode-result", "true");
        properties.put("debug",              "true");

        msgCtxt.setVariable("message.content", "FutVqI4rZKuh08Yp4M4NtgIxn/XB/5dABt7LdinolokWgVRx+ILkJvKuR1IApkpD");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertNotNull(output);
        Assert.assertNull(error);
    }

    @Test()
    public void CBC_Decrypt_TestString1() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "CBC_Decrypt_TestString2");
        properties.put("action",        "decrypt");
        properties.put("key",           "2b7e151628aed2a6abf7158809cf4f3c");
        properties.put("decode-key",    "hex");
        properties.put("iv",            "000102030405060708090A0B0C0D0E0F");
        properties.put("decode-iv",     "hex");
        properties.put("decode-source", "hex");
        properties.put("mode",          "CBC");
        properties.put("padding",       "NoPadding");
        properties.put("encode-result", "hex");
        properties.put("debug",         "true");

        msgCtxt.setVariable("message.content", "7649abac8119b246cee98e9b12e9197d");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        // String cipher = msgCtxt.getVariable("crypto_cipher");
        // System.out.println("cipher: " + cipher);
        // System.out.println("output: " + output);
        reportThings(properties);
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "6bc1bee22e409f96e93d7e117393172a");
        Assert.assertNull(error);
    }

    @Test()
    public void CBC_Encrypt_TestString1() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "CBC_Encrypt_TestString1");
        properties.put("action",        "encrypt");
        properties.put("key",           "2b7e151628aed2a6abf7158809cf4f3c");
        properties.put("decode-key",    "hex");
        properties.put("iv",            "000102030405060708090A0B0C0D0E0F");
        properties.put("decode-iv",     "hex");
        properties.put("decode-source", "hex");
        properties.put("mode",          "CBC");
        properties.put("padding",       "NoPadding");
        properties.put("encode-result", "hex");
        properties.put("debug",         "true");

        msgCtxt.setVariable("message.content", "6bc1bee22e409f96e93d7e117393172a");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);

        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "7649abac8119b246cee98e9b12e9197d");
        Assert.assertNull(error);
    }

    @Test()
    public void CBC_Encrypt_TestString2() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "CBC_Encrypt_TestString2");
        properties.put("action",        "encrypt");
        properties.put("key",           "2b7e151628aed2a6abf7158809cf4f3c");
        properties.put("decode-key",    "hex");
        properties.put("iv",            "7649ABAC8119B246CEE98E9B12E9197D");
        properties.put("decode-iv",     "hex");
        properties.put("decode-source", "hex");
        properties.put("mode",          "CBC");
        properties.put("padding",       "NoPadding");
        properties.put("encode-result", "hex");
        properties.put("debug",         "true");

        msgCtxt.setVariable("message.content", "ae2d8a571e03ac9c9eb76fac45af8e51");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "5086cb9b507219ee95db113a917678b2");
        Assert.assertNull(error);
    }

    @Test()
    public void OFB_Encrypt_TestString2() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "OFB_Encrypt_TestString2");
        properties.put("action",        "encrypt");
        properties.put("key",           "2b7e151628aed2a6abf7158809cf4f3c");
        properties.put("decode-key",    "hex");
        properties.put("iv",            "50FE67CC996D32B6DA0937E99BAFEC60");
        properties.put("decode-iv",     "hex");
        properties.put("decode-source", "hex");
        properties.put("mode",          "OFB");
        properties.put("padding",       "NoPadding");
        properties.put("encode-result", "hex");
        properties.put("debug",         "true");

        msgCtxt.setVariable("message.content", "ae2d8a571e03ac9c9eb76fac45af8e51");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "7789508d16918f03f53c52dac54ed825");
        Assert.assertNull(error);
    }

    @Test()
    public void CFB_Encrypt_TestString2() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "CFB_Encrypt_TestString2");
        properties.put("action",         "encrypt");
        properties.put("key",            "2b7e151628aed2a6abf7158809cf4f3c");
        properties.put("decode-key",     "hex");
        properties.put("iv",             "3B3FD92EB72DAD20333449F8E83CFB4A");
        properties.put("decode-iv",      "hex");
        properties.put("decode-source",  "hex");
        properties.put("mode",           "CFB");
        properties.put("padding",        "NoPadding");
        properties.put("encode-result",  "hex");
        properties.put("debug",          "true");

        msgCtxt.setVariable("message.content", "ae2d8a571e03ac9c9eb76fac45af8e51");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "c8a64537a0b3a93fcde3cdad9f1ce58b");
        Assert.assertNull(error);
    }


    @Test()
    public void AES256_CBC_Encrypt_Hex() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "AES256_CBC_Encrypt_Hex");
        properties.put("action",        "encrypt");
        properties.put("passphrase",    "ABCDEFG-1234567-abcdefghijklmnopqrstuvwxyz");
        properties.put("key-strength",  "256");
        properties.put("debug",         "true");
        properties.put("mode",          "CBC");
        properties.put("padding",       "PKCS5PADDING");
        properties.put("encode-result", "hex");

        msgCtxt.setVariable("message.content", "The quick brown fox jumped over the lazy dog.");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        Assert.assertNull(error);
    }


    @Test()
    public void AES256_CBC_Encrypt_Hex_default_value_for_salt() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "AES256_CBC_Encrypt_Hex_default_value_for_salt");
        properties.put("action",        "encrypt");
        properties.put("passphrase",    "ABCDEFG-1234567-abcdefghijklmnopqrstuvwxyz");
        properties.put("key-strength",  "256");
        properties.put("debug",         "true");
        properties.put("salt",          "{unknown-variable:IloveAPIs2018}");
        properties.put("mode",          "CBC");
        properties.put("padding",       "PKCS5PADDING");
        properties.put("encode-result", "hex");

        msgCtxt.setVariable("message.content", "The quick brown fox jumped over the lazy dog.");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        Assert.assertNull(error);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "861e4a6805fb7390f60325657c5f56b3b5c453ff138ec65f5c0fef43abe40a5fc8a48a624ad5475e3123896902727093");
    }


    @Test()
    public void AES256_CBC_Decrypt_Hex_explicit_salt() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",           "AES256_CBC_Decrypt_Hex_explicit_salt");
        properties.put("action",             "decrypt");
        properties.put("passphrase",         "ABCDEFG-1234567-abcdefghijklmnopqrstuvwxyz");
        properties.put("key-strength",       "256");
        properties.put("debug",              "true");
        properties.put("salt",               "IloveAPIs2018");
        properties.put("mode",               "CBC");
        properties.put("source",             "ciphertext");
        properties.put("decode-source",      "hex");
        properties.put("padding",            "PKCS5PADDING");
        properties.put("encode-result",      "hex");
        properties.put("utf8-decode-result", "true");

        msgCtxt.setVariable("ciphertext", "861e4a6805fb7390f60325657c5f56b3b5c453ff138ec65f5c0fef43abe40a5fc8a48a624ad5475e3123896902727093");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        reportThings(properties);
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        Assert.assertNull(error);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "The quick brown fox jumped over the lazy dog.");
    }


    @Test()
    public void AES128_CBC_Encrypt_Hex_referenced_salt() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",      "AES128_CBC_Encrypt_Hex_referenced_salt");
        properties.put("action",        "encrypt");
        properties.put("passphrase",    "ABCDEFG-1234567-abcdefghijklmnopqrstuvwxyz");
        properties.put("key-strength",  "128");
        properties.put("debug",         "true");
        properties.put("salt",          "{known-variable:IloveAPIs2018}");
        properties.put("mode",          "CBC");
        properties.put("padding",       "PKCS5PADDING");
        properties.put("encode-result", "hex");

        msgCtxt.setVariable("message.content", "The quick brown fox jumped over the lazy dog.");
        msgCtxt.setVariable("known-variable", "This is the salt for the operation.");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        reportThings(properties);
        Assert.assertNull(error);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "b44a464626641cbffb088862adb36f64a6357e7fa38e9605b0034b0cc6e0d3ff32dade6635f713407fb5254e5df9c72f");
    }


    @Test()
    public void AES128_CBC_Decrypt_Hex_referenced_salt() {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put("testname",           "AES256_CBC_Decrypt_Hex_referenced_salt");
        properties.put("action",             "decrypt");
        properties.put("passphrase",         "ABCDEFG-1234567-abcdefghijklmnopqrstuvwxyz");
        //properties.put("key-strength",       "128"); // default = 128
        properties.put("debug",              "true");
        properties.put("salt",               "{known-variable}");
        properties.put("mode",               "CBC");
        properties.put("source",             "ciphertext");
        properties.put("decode-source",      "hex");
        properties.put("padding",            "PKCS5PADDING");
        properties.put("encode-result",      "hex");
        properties.put("utf8-decode-result", "true");

        msgCtxt.setVariable("ciphertext", "b44a464626641cbffb088862adb36f64a6357e7fa38e9605b0034b0cc6e0d3ff32dade6635f713407fb5254e5df9c72f");
        msgCtxt.setVariable("known-variable", "This is the salt for the operation.");

        AesCryptoCallout callout = new AesCryptoCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // retrieve output
        String error = msgCtxt.getVariable("crypto_error");
        if (error!=null)
            System.out.println("error: " + error);

        // check result and output
        reportThings(properties);
        Assert.assertEquals(result, ExecutionResult.SUCCESS);
        Assert.assertNull(error);
        String output = msgCtxt.getVariable("crypto_output");
        Assert.assertEquals(output, "The quick brown fox jumped over the lazy dog.");
    }

}
