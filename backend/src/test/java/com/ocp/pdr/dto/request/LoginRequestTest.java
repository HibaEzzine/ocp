package com.ocp.pdr.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class LoginRequestTest {

    @Test
    void shouldReadIdentifierFromJsonPayload() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        LoginRequest request = objectMapper.readValue(
                "{\"identifier\":\"admin@ocp.ma\",\"password\":\"password123\"}",
                LoginRequest.class
        );

        assertThat(request.getIdentifier()).isEqualTo("admin@ocp.ma");
        assertThat(request.getPassword()).isEqualTo("password123");
    }
}
