package tech.wetech.flexmodel.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SettingResource {


  @GET
  @Path("/hello")
  public Object test() throws IOException, InterruptedException {
    String paramString = Map.of(
        "token", "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJhRERlR2R0UWpZQmJIY1BqZS1OVDRZTjJ5Q2NwbXhnMmFRaE9GdjFiTWZ3In0.eyJleHAiOjE3MTg5NDM2NTgsImlhdCI6MTcxODk0MzM1OCwianRpIjoiMWVkZGEwNGItYjVmOC00YzgxLWE5MzMtMWJjN2JlNzQ0Mjc4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg0L2F1dGgvcmVhbG1zL215cmVhbG0iLCJhdWQiOlsibXlzZXJ2ZXIiLCJhY2NvdW50Il0sInN1YiI6IjQxODlhODA0LWUyODItNGJhZS1iMGJjLTNhODhiOWU2OGE0NSIsInR5cCI6IkJlYXJlciIsImF6cCI6Im15Y2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6IjIyOTdjZjAyLTJhNjQtNDMyZS04NDljLTFjMzM3ZmNiMmFhNSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1teXJlYWxtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7Im15Y2xpZW50Ijp7InJvbGVzIjpbIkFETUlOX0NMSSJdfSwibXlzZXJ2ZXIiOnsicm9sZXMiOlsiU0VSVkVSX1JPTEUiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjIyOTdjZjAyLTJhNjQtNDMyZS04NDljLTFjMzM3ZmNiMmFhNSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkppbmJhbyBDaGVuZyIsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIiwiZ2l2ZW5fbmFtZSI6IkppbmJhbyIsImZhbWlseV9uYW1lIjoiQ2hlbmcifQ.e5Gqe1AK1HqydHJbRhlVgfFiDlsZ8K1jyzKm4OgpCtLRpYxUcR-LQ34alXM0Gwv8yrLlK7lRpA39nDjk6cPzB2nLuZRWrGpPkeelk0Q5tQHkeQ_l7fxuEDR5WqmfJF06cjfD-xushe-wQciWYbOIkW41M2h-cqsHwEtPblMHELKUyJP-PvorbInJS2oPtsdg4NE1FLS4rDCelPtA9AOEmMrPXSqh0k2gPZeT7z2q5xG2ZkJ8ln8hhAq38tFdr5e0XE0RhoZX7Gl8nor8n5ljh0QYBZMesJ-1fLUdsXDML9WN0ZXov32M3oTOYyzuXCzlBlcNjvwGX08ACy_p3i6S4g",
        "token_type_hint", "access_token",
        "client_id", "myserver",
        "client_secret", "BTpxEohjeTXvK0JkuiULF2fTLOxgUuG4"
      ).entrySet()
      .stream()
      .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
      .collect(Collectors.joining("&"));

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("http://localhost:8084/auth/realms/myrealm/protocol/openid-connect/token/introspect"))
      .headers("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(paramString))
      .build();
    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

}
