
package CST438Project2.com.demo.controller;

import CST438Project2.com.demo.model.User;
import CST438Project2.com.demo.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void unauthenticatedRequestsCannotAddUsers() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Untrusted User")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedNonAdminRequestsCannotAddUsers() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("user", "user-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Regular User")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAddUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("New User")))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        matchesPattern("/api/v1/users/[0-9]+")
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.displayName").value("New User"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void adminCanViewAllUsers() throws Exception {
        User user = userRepository.save(new User(
                null,
                "Listed User",
                "listed@example.com",
                "USER"
        ));

        mockMvc.perform(get("/api/v1/users")
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath(
                        "$[?(@.id == " + user.getId() + ")]"
                ).isNotEmpty());
    }

    @Test
    void unauthenticatedRequestsCannotViewUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotViewAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(httpBasic("user", "user-password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanViewUserById() throws Exception {
        User user = userRepository.save(new User(
                null,
                "Find User",
                "find@example.com",
                "USER"
        ));

        mockMvc.perform(get("/api/v1/users/{userId}", user.getId())
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.displayName").value("Find User"))
                .andExpect(jsonPath("$.email").value("find@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999999999")
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanUpdateAllUserFields() throws Exception {
        User user = userRepository.save(new User(
                null,
                "Before",
                "before@example.com",
                "USER"
        ));

        Long id = user.getId();

        mockMvc.perform(patch("/api/v1/users/{userId}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "displayName": "After",
                              "email": "after@example.com",
                              "role": "ADMIN"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("After"))
                .andExpect(jsonPath("$.email").value("after@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        User updated = userRepository.findById(id).orElseThrow();

        assertEquals("After", updated.getDisplayName());
        assertEquals("after@example.com", updated.getEmail());
        assertEquals("ADMIN", updated.getRole());
    }

    @Test
    void adminCanPatchOnlyDisplayName() throws Exception {
        User user = userRepository.save(new User(
                null,
                "Original Name",
                "original@example.com",
                "USER"
        ));

        Long id = user.getId();

        mockMvc.perform(patch("/api/v1/users/{userId}", id)
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "displayName": "Updated Name"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("original@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        User updated = userRepository.findById(id).orElseThrow();

        assertEquals("Updated Name", updated.getDisplayName());
        assertEquals("original@example.com", updated.getEmail());
        assertEquals("USER", updated.getRole());
    }

    @Test
    void unauthenticatedRequestsCannotPatchUsers() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"No\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotPatchUsers() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1")
                        .with(httpBasic("user", "user-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"No\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingUserCannotBeUpdated() throws Exception {
        mockMvc.perform(patch("/api/v1/users/999999999")
                        .with(httpBasic("admin", "admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Missing\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanDeleteUser() throws Exception {
        User user = userRepository.save(new User(
                null,
                "Delete Me",
                "delete@example.com",
                "USER"
        ));

        Long id = user.getId();

        mockMvc.perform(delete("/api/v1/users/{userId}", id)
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(id).isEmpty());
    }

    @Test
    void unauthenticatedRequestsCannotDeleteUsers() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotDeleteUsers() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(httpBasic("user", "user-password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingUserCannotBeDeleted() throws Exception {
        mockMvc.perform(delete("/api/v1/users/999999999")
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isNotFound());
    }

    private static String userJson(String displayName) {
        return """
                {
                  "displayName": "%s",
                  "email": "newuser@example.com",
                  "role": "USER"
                }
                """.formatted(displayName);
    }
}
