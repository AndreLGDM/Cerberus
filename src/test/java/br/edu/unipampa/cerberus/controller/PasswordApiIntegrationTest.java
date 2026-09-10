package br.edu.unipampa.cerberus.controller;

import br.edu.unipampa.cerberus.service.BreachCheckService;
import br.edu.unipampa.cerberus.service.PwnedPasswordsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Substitui o cliente HTTP real: os testes de vazamento não tocam a rede.
    @MockBean
    private PwnedPasswordsClient pwnedPasswordsClient;

    @Test
    void strengthEndpointDeveRetornarAnalise() throws Exception {
        mockMvc.perform(post("/api/v1/password/strength")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"9xQ!vTm2#Lp8zR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.rating").exists())
                .andExpect(jsonPath("$.entropyBits").isNumber());
    }

    @Test
    void deveRejeitarSenhaVaziaCom400() throws Exception {
        mockMvc.perform(post("/api/v1/password/strength")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void deveRejeitarJsonMalformadoCom400() throws Exception {
        mockMvc.perform(post("/api/v1/password/strength")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("isto nao e json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hashEVerifyDevemSerConsistentes() throws Exception {
        String hashJson = mockMvc.perform(post("/api/v1/hash/bcrypt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"MinhaSenh@1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("bcrypt"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(hashJson);
        String hash = node.get("hash").asText();
        assertThat(hash).startsWith("$2");

        mockMvc.perform(post("/api/v1/hash/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new java.util.HashMap<>() {{
                                    put("password", "MinhaSenh@1");
                                    put("hash", hash);
                                }})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matches").value(true));
    }

    @Test
    void breachEndpointDeveReportarSenhaVazada() throws Exception {
        // Monta um corpo de resposta que contém o sufixo real do SHA-1 de "password".
        String sha1 = BreachCheckService.sha1Hex("password").toUpperCase();
        String suffix = sha1.substring(5);
        when(pwnedPasswordsClient.fetchRange(anyString()))
                .thenReturn(suffix + ":42\r\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA:1");

        mockMvc.perform(post("/api/v1/password/breach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true))
                .andExpect(jsonPath("$.breached").value(true))
                .andExpect(jsonPath("$.count").value(42));
    }
}
