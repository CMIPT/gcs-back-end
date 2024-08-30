package edu.cmipt.gcs.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.cmipt.gcs.constant.ApiPathConstant;
import edu.cmipt.gcs.constant.ApplicationConstant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ApplicationConstant.TEST_PROFILE})
public class WebConfigTest {
    @Value("${front-end.url:http://localhost:3000}")
    private String frontEndUrl;

    @Autowired private MockMvc mockMvc;

    @Test
    public void testCorsFilter() throws Exception {
        mockMvc.perform(
                        options(ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH)
                                .header("Origin", frontEndUrl)
                                .header("Access-Control-Request-Method", "GET"))
                .andExpectAll(
                        status().isOk(),
                        header().string("Access-Control-Allow-Origin", frontEndUrl));

        mockMvc.perform(
                        options(ApiPathConstant.DEVELOPMENT_GET_API_MAP_API_PATH)
                                .header("Origin", frontEndUrl + "/INVALID"))
                .andExpectAll(status().isForbidden());
    }
}
