/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.langstream.tests;

import ai.langstream.tests.util.ConsumeGatewayMessage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(BaseEndToEndTest.class)
@Tag(BaseEndToEndTest.CATEGORY_NEEDS_CREDENTIALS)
public class ChatCompletionsIT extends BaseEndToEndTest {

    static Map<String, String> appEnv;

    @BeforeAll
    public static void checkCredentials() {
        appEnv =
                getAppEnvMapFromSystem(
                        List.of(
                                "OPEN_AI_ACCESS_KEY",
                                "OPEN_AI_URL",
                                "OPEN_AI_CHAT_COMPLETIONS_MODEL",
                                "OPEN_AI_PROVIDER"));
    }

    @Test
    public void test() throws Exception {
        installLangStreamCluster(false);
        final String tenant = "ten-" + System.currentTimeMillis();
        setupTenant(tenant);

        final String applicationId = "app";

        deployLocalApplication(applicationId, "chat-completions", appEnv);
        awaitApplicationReady(applicationId, 1);

        final String sessionId = UUID.randomUUID().toString();

        executeCommandOnClient(
                "bin/langstream gateway produce %s produce-input -v 'Who was the first president of the United States?' -p sessionId=%s"
                        .formatted(applicationId, sessionId)
                        .split(" "));

        final ConsumeGatewayMessage message =
                consumeOneMessageFromGateway(
                        applicationId,
                        "consume-history",
                        "-p sessionId=%s --position earliest --connect-timeout 30"
                                .formatted(sessionId)
                                .split(" "));
        log.info("Output: {}", message);
        Assertions.assertTrue(message.getAnswerFromChatCompletionsValue().contains("Washington"));
    }
}