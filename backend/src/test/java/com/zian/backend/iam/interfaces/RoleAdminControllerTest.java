package com.zian.backend.iam.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zian.backend.security.SecurityTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 角色权限调整（B-12）：替换后立即生效，且 ADMIN 不能被移除角色管理权限。
 */
class RoleAdminControllerTest extends SecurityTestSupport {

    @Test
    void roleListAndPermissionCatalogAreExposed() throws Exception {
        String admin = newAccount("RA");
        createUser(admin, "ADMIN", "NORMAL", null);
        String token = login(admin);

        mockMvc.perform(get("/v1/admin/roles").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code=='ADMIN')].permissions").exists())
                .andExpect(jsonPath("$.data[?(@.code=='READER')].permissionCount").exists());

        mockMvc.perform(get("/v1/admin/permissions").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code=='role:manage')]").exists())
                .andExpect(jsonPath("$.data[?(@.code=='catalog:read')]").exists());
    }

    @Test
    void removingPermissionTakesEffectForExistingToken() throws Exception {
        String admin = newAccount("RB");
        createUser(admin, "ADMIN", "NORMAL", null);
        String reader = newAccount("RR");
        createUser(reader, "READER", "NORMAL", null);

        String adminToken = login(admin);
        String readerToken = login(reader);
        mockMvc.perform(get("/v1/books").header("Authorization", bearer(readerToken)))
                .andExpect(status().isOk());

        List<String> readerPermissions = permissionsOf(adminToken, "READER");
        assertThat(readerPermissions).contains("catalog:read");
        List<String> without = new ArrayList<>(readerPermissions);
        without.remove("catalog:read");

        mockMvc.perform(put("/v1/admin/roles/READER/permissions")
                        .header("Authorization", bearer(adminToken))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("permissions", without))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("READER"))
                .andExpect(jsonPath("$.data.permissions").isArray());

        // 旧 token 未过期，但权限按库重算 → 立即失效
        mockMvc.perform(get("/v1/books").header("Authorization", bearer(readerToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/me").header("Authorization", bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("catalog:read"))));
    }

    @Test
    void unknownRoleOrPermissionIsRejected() throws Exception {
        String admin = newAccount("RC");
        createUser(admin, "ADMIN", "NORMAL", null);
        String token = login(admin);

        mockMvc.perform(put("/v1/admin/roles/NOPE/permissions")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content("{\"permissions\":[]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("角色不存在：NOPE"));

        mockMvc.perform(put("/v1/admin/roles/READER/permissions")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content("{\"permissions\":[\"nope:read\"]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("权限不存在：nope:read"));
    }

    @Test
    void adminCannotDropRoleManagePermission() throws Exception {
        String admin = newAccount("RD");
        createUser(admin, "ADMIN", "NORMAL", null);
        String token = login(admin);

        List<String> permissions = permissionsOf(token, "ADMIN");
        permissions.remove("role:manage");
        mockMvc.perform(put("/v1/admin/roles/ADMIN/permissions")
                        .header("Authorization", bearer(token))
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(Map.of("permissions", permissions))))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("ADMIN 角色不能移除角色管理权限"));
    }

    private List<String> permissionsOf(String token, String role) throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/admin/roles").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        List<String> permissions = new ArrayList<>();
        objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").forEach(node -> {
                    if (role.equals(node.path("code").asText())) {
                        node.path("permissions").forEach(p -> permissions.add(p.asText()));
                    }
                });
        return permissions;
    }
}
