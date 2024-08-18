package tech.wetech.flexmodel.domain.model.idp.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tech.wetech.flexmodel.util.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
@Getter
@Setter
@Slf4j
public class OIDC implements Provider {

  private String issuer;
  private String clientId;
  private String clientSecret;

  @Override
  public String getType() {
    return "oidc";
  }

  @Override
  public boolean checkToken(String token) {
    String paramString = Map.of(
        "token", token,
        "token_type_hint", "access_token",
        "client_id", clientId,
        "client_secret", clientSecret
      ).entrySet()
      .stream()
      .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
      .collect(Collectors.joining("&"));

    Map<String, Object> discovery = getDiscovery();

    String introspectionEndpoint = (String) discovery.get("introspection_endpoint");
    // todo 如果不存在introspection_endpoint，则通过userinfo_endpoint获取用户，来判断是否有权限

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(introspectionEndpoint))
      .headers("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(paramString))
      .build();

    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String bodyString = response.body();
      log.debug("Oidc token introspect res: {}", bodyString);
      Map<?, ?> body = JsonUtils.getInstance().parseToObject(bodyString, Map.class);
      Boolean active = (Boolean) body.get("active");
      return active != null && active;
    } catch (IOException | InterruptedException e) {
      log.error("Oidc token introspect error: {}", e.getMessage(), e);
      return false;
    }
  }

  @SuppressWarnings("all")
  private Map<String, Object> getDiscovery() {
    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(HttpRequest.newBuilder()
        .uri(URI.create(issuer + "/.well-known/openid-configuration"))
        .GET()
        .build(), HttpResponse.BodyHandlers.ofString());
      String bodyString = response.body();
      return JsonUtils.getInstance().parseToObject(bodyString, Map.class);
    } catch (IOException | InterruptedException e) {
      log.error("Oidc token introspect error: {}", e.getMessage(), e);
      return Map.of();
    }
  }

}
