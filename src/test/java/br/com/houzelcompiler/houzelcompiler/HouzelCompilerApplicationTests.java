package br.com.houzelcompiler.houzelcompiler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "openai.api.key=${OPENAI_API_KEY}"
})
class HouzelCompilerApplicationTests {

    @Test
    void contextLoads() {
    }

}