package com.medical.integration;

import com.medical.dto.CreateUserRequest;
import com.medical.dto.UpdateUserRequest;
import com.medical.enums.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for users-service.
 * Uses unique suffixes per run to avoid data conflicts.
 * Run with: mvn test -Dtest=UsersServiceIntegrationTest
 *
 * @author Henry Fernando Mulato Llanten <henrymulato@unicauca.edu.co>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersServiceIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String suffix;

  @BeforeEach
  void setUp() {
    suffix = UUID.randomUUID().toString().substring(0, 8);
    // Ensure RestTemplate supports PATCH (JDK HttpURLConnection doesn't by default)
    restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  private Long extractId(ResponseEntity<String> response) {
    String body = response.getBody();
    int idStart = body.indexOf("\"id\":") + 5;
    int idEnd = body.indexOf(",", idStart);
    if (idEnd == -1)
      idEnd = body.indexOf("}", idStart);
    return Long.parseLong(body.substring(idStart, idEnd).trim());
  }

  /**
   * Shortcut: create a user with minimal fields. Uses SCHEDULER to avoid
   * PATIENT validation and last-admin protection.
   */
  private Long createUser(String label) {
    CreateUserRequest req = CreateUserRequest.builder()
        .username(label + "." + suffix)
        .password("Password1!")
        .email(label + "." + suffix + "@test.com")
        .role(UserRole.SCHEDULER)
        .build();
    ResponseEntity<String> resp = restTemplate.postForEntity(url("/api/users"), req, String.class);
    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    return extractId(resp);
  }

  @Test
  void shouldCreateUserAndPatient() {
    String s = suffix;
    CreateUserRequest request = CreateUserRequest.builder()
        .username("create." + s)
        .password("Password1!")
        .email("create." + s + "@test.com")
        .role(UserRole.PATIENT)
        .firstName("Create")
        .lastName("Test")
        .documentType("CC")
        .documentNumber("DOC-" + s)
        .phone("3001111111")
        .build();

    ResponseEntity<String> response = restTemplate.postForEntity(
        url("/api/users"), request, String.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertTrue(response.getBody().contains("create." + s));
  }

  @Test
  void shouldValidatePatientByDocument() {
    String s = suffix;
    CreateUserRequest request = CreateUserRequest.builder()
        .username("validate." + s)
        .password("Password1!")
        .email("validate." + s + "@test.com")
        .role(UserRole.PATIENT)
        .firstName("Validate")
        .lastName("Test")
        .documentType("CC")
        .documentNumber("DOC-" + s)
        .build();

    ResponseEntity<String> createResp = restTemplate.postForEntity(
        url("/api/users"), request, String.class);
    assertEquals(HttpStatus.CREATED, createResp.getStatusCode());

    ResponseEntity<Boolean> validationResponse = restTemplate.getForEntity(
        url("/api/users/patients/validate/DOC-" + s), Boolean.class);

    assertEquals(HttpStatus.OK, validationResponse.getStatusCode());
    assertTrue(validationResponse.getBody());
  }

  @Test
  void shouldRejectDuplicateUsername() {
    String s = suffix;
    CreateUserRequest request = CreateUserRequest.builder()
        .username("dup." + s)
        .password("Password1!")
        .email("dup." + s + "@test.com")
        .role(UserRole.SCHEDULER) // no extra fields needed
        .build();

    ResponseEntity<String> first = restTemplate.postForEntity(
        url("/api/users"), request, String.class);
    assertEquals(HttpStatus.CREATED, first.getStatusCode());

    ResponseEntity<String> second = restTemplate.postForEntity(
        url("/api/users"), request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
    assertTrue(second.getBody().contains("username already exists"));
  }

  @Test
  void shouldUpdateUser() {
    Long userId = createUser("upd");

    UpdateUserRequest updateReq = UpdateUserRequest.builder()
        .username("updated." + suffix)
        .email("updated." + suffix + "@test.com")
        .role(UserRole.SCHEDULER)
        .build();

    ResponseEntity<String> updateResp = restTemplate.exchange(
        url("/api/users/" + userId),
        HttpMethod.PUT,
        new HttpEntity<>(updateReq),
        String.class);

    assertEquals(HttpStatus.OK, updateResp.getStatusCode());
    assertTrue(updateResp.getBody().contains("updated." + suffix));
    assertTrue(updateResp.getBody().contains("SCHEDULER"));
  }

  @Test
  void shouldRejectUpdateWithDuplicateUsername() {
    createUser("dupa");
    Long userBId = createUser("dupb");

    // Try to update user B with user A's username
    String userAName = "dupa." + suffix;
    UpdateUserRequest updateReq = UpdateUserRequest.builder()
        .username(userAName)
        .build();

    ResponseEntity<String> updateResp = restTemplate.exchange(
        url("/api/users/" + userBId),
        HttpMethod.PUT,
        new HttpEntity<>(updateReq),
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, updateResp.getStatusCode());
    assertTrue(updateResp.getBody().contains("already exists"));
  }

  @Test
  void shouldReturnAllUsers() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().startsWith("["));
    assertTrue(response.getBody().contains("username"));
  }

  @Test
  void shouldReturnUserById() {
    Long userId = createUser("getid");

    ResponseEntity<String> getResp = restTemplate.getForEntity(
        url("/api/users/" + userId), String.class);

    assertEquals(HttpStatus.OK, getResp.getStatusCode());
    assertTrue(getResp.getBody().contains("getid." + suffix));
  }

  @Test
  void shouldDeactivateUser() {
    Long userId = createUser("deact");

    ResponseEntity<Void> deactResp = restTemplate.exchange(
        url("/api/users/" + userId + "/deactivate"),
        HttpMethod.PATCH,
        null,
        Void.class);

    assertEquals(HttpStatus.NO_CONTENT, deactResp.getStatusCode());
  }

  @Test
  void shouldRejectDeactivateAlreadyInactive() {
    Long userId = createUser("deact2");

    // First deactivation should succeed
    ResponseEntity<Void> firstDeact = restTemplate.exchange(
        url("/api/users/" + userId + "/deactivate"),
        HttpMethod.PATCH,
        null,
        Void.class);
    assertEquals(HttpStatus.NO_CONTENT, firstDeact.getStatusCode());

    // Second deactivation should fail
    ResponseEntity<String> secondDeact = restTemplate.exchange(
        url("/api/users/" + userId + "/deactivate"),
        HttpMethod.PATCH,
        null,
        String.class);

    assertEquals(HttpStatus.BAD_REQUEST, secondDeact.getStatusCode());
    assertTrue(secondDeact.getBody().contains("already inactive"));
  }

  // ──────────────────────────────────────────────
  // Search integration tests
  // ──────────────────────────────────────────────

  @Test
  void shouldSearchByUsername() {
    createUser("srch");

    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/username/srch." + suffix), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("srch." + suffix));
  }

  @Test
  void shouldReturnEmpty_whenSearchingNonExistentUsername() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/username/nonexistent_xyz"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("[]", response.getBody());
  }

  @Test
  void shouldSearchByEmail() {
    createUser("srchemail");

    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/email/srchemail." + suffix + "@test.com"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("srchemail." + suffix));
  }

  @Test
  void shouldSearchByRole() {
    createUser("role1");

    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/role/SCHEDULER"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("SCHEDULER"));
  }

  @Test
  void shouldReturn400_whenSearchingByInvalidRole() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/role/INVALID"), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldSearchActiveUsers() {
    createUser("act1");

    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/status?active=true"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("act1." + suffix));
  }

  @Test
  void shouldSearchByStatusInactive() {
    Long userId = createUser("inact1");
    // Deactivate the user first
    restTemplate.exchange(url("/api/users/" + userId + "/deactivate"), HttpMethod.PATCH, null, Void.class);

    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/status?active=false"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("inact1." + suffix));
  }

  @Test
  void shouldReturn400_whenStatusParamMissing() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/status"), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldSearchAdvancedWithMultipleParams() {
    createUser("adv1");

    // Search by role (SCHEDULER) which is what createUser uses
    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/advanced?role=SCHEDULER"), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("adv1." + suffix));
  }

  @Test
  void shouldReturn400_whenAdvancedSearchWithInvalidRole() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        url("/api/users/search/advanced?role=INVALID"), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
