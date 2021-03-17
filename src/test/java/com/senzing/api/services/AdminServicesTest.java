package com.senzing.api.services;

import com.senzing.api.model.*;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.*;
import com.senzing.gen.api.invoker.*;
import com.senzing.gen.api.services.AdminApi;

import static com.senzing.api.model.SzHttpMethod.*;
import static org.junit.jupiter.api.TestInstance.*;
import static com.senzing.api.services.ResponseValidators.*;

@TestInstance(Lifecycle.PER_CLASS)
public class AdminServicesTest extends AbstractServiceTest {
  private AdminServices adminServices;
  private AdminApi adminApi;

  @BeforeAll public void initializeEnvironment() {
    this.beginTests();
    this.initializeTestEnvironment();
    this.adminServices = new AdminServices();
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(this.formatServerUri(""));
    this.adminApi = new AdminApi(apiClient);
  }

  @AfterAll public void teardownEnvironment() {
    try {
      this.teardownTestEnvironment();
      this.conditionallyLogCounts(true);
    } finally {
      this.endTests();
    }
  }

  @Test public void heartbeatTest() {
    this.performTest(() -> {
      String uriText = this.formatServerUri("heartbeat");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzBasicResponse response = this.adminServices.heartbeat(uriInfo);
      response.concludeTimers();
      long after = System.nanoTime();

      validateBasics(response, uriText, after - before);
    });
  }

  @Test public void heartbeatViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("heartbeat");
      long    before  = System.nanoTime();
      SzBasicResponse response
          = this.invokeServerViaHttp(GET, uriText, SzBasicResponse.class);
      long after = System.nanoTime();

      validateBasics(response, uriText, after - before);
    });
  }

  @Test public void heartbeatViaJavaClientTest() {
    this.performTest(() -> {
      String uriText = this.formatServerUri("heartbeat");
      long    before  = System.nanoTime();
      com.senzing.gen.api.model.SzBaseResponse clientResponse
          = this.adminApi.heartbeat();
      long after = System.nanoTime();

      SzBasicResponse response
          = jsonCopy(clientResponse, SzBasicResponse.class);

      validateBasics(response, uriText, after - before);
    });
  }

  @Test public void licenseTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long              before    = System.nanoTime();
      SzLicenseResponse response  = this.adminServices.license(false, uriInfo);
      response.concludeTimers();
      long              after     = System.nanoTime();

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              null,
                              null,
                              null);
    });
  }

  @Test public void licenseViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long before = System.nanoTime();
      SzLicenseResponse response
          = this.invokeServerViaHttp(GET, uriText, SzLicenseResponse.class);
      long after = System.nanoTime();

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              null,
                              null,
                              null);
    });
  }

  @Test public void licenseViaJavaClientTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLicenseResponse clientResponse
          = this.adminApi.license(null);
      long after = System.nanoTime();

      SzLicenseResponse response
          = jsonCopy(clientResponse, SzLicenseResponse.class);

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              null,
                              null,
                              null);
    });
  }

  @Test public void licenseWithoutRawTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license?withRaw=false");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long              before    = System.nanoTime();
      SzLicenseResponse response  = this.adminServices.license(false, uriInfo);
      response.concludeTimers();
      long              after     = System.nanoTime();

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              false,
                              null,
                              null);

    });
  }

  @Test public void licenseWithoutRawViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license?withRaw=false");

      long before = System.nanoTime();
      SzLicenseResponse response
          = this.invokeServerViaHttp(GET, uriText, SzLicenseResponse.class);
      long after = System.nanoTime();

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              false,
                              null,
                              null);
    });
  }

  @Test public void licenseWithoutRawViaJavaClientTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license?withRaw=false");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLicenseResponse clientResponse
          = this.adminApi.license(false);
      long after = System.nanoTime();

      SzLicenseResponse response
          = jsonCopy(clientResponse, SzLicenseResponse.class);

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              null,
                              null,
                              null);
    });
  }

  @Test public void licenseWithRawTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("license?withRaw=true");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long              before    = System.nanoTime();
      SzLicenseResponse response  = this.adminServices.license(true, uriInfo);
      response.concludeTimers();
      long              after     = System.nanoTime();

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              true,
                              null,
                              null);
    });
  }

  @Test public void licenseWithRawViaHttpTest() {
    this.performTest(()-> {
      String uriText = this.formatServerUri("license?withRaw=true");

      long before = System.nanoTime();
      SzLicenseResponse response
          = this.invokeServerViaHttp(GET, uriText, SzLicenseResponse.class);
      long after = System.nanoTime();

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              true,
                              null,
                              null);
    });
  }

  @Test public void licenseWithRawViaJavaClientTest() {
    this.performTest(()-> {
      String uriText = this.formatServerUri("license?withRaw=true");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzLicenseResponse clientResponse
          = this.adminApi.license(true);
      long after = System.nanoTime();

      SzLicenseResponse response
          = jsonCopy(clientResponse, SzLicenseResponse.class);

      validateLicenseResponse(response,
                              uriText,
                              after - before,
                              true,
                              null,
                              null);
    });
  }

  @Test public void versionTest() {
    this.performTest(()-> {
      String  uriText = this.formatServerUri("version");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long              before    = System.nanoTime();
      SzVersionResponse response  = this.adminServices.version(false,
                                                               uriInfo);
      response.concludeTimers();
      long              after     = System.nanoTime();

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              null,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version");

      long before = System.nanoTime();
      SzVersionResponse response
          = this.invokeServerViaHttp(GET, uriText, SzVersionResponse.class);
      long after = System.nanoTime();

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              null,
                              this.readInitJsonFile());

    });
  }

  @Test public void versionViaJavaClientTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzVersionResponse clientResponse
          = this.adminApi.version(null);
      long after = System.nanoTime();

      SzVersionResponse response
          = jsonCopy(clientResponse, SzVersionResponse.class);

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              false,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionWithoutRawTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version?withRaw=false");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long              before    = System.nanoTime();
      SzVersionResponse response  = this.adminServices.version(false, uriInfo);
      response.concludeTimers();
      long              after     = System.nanoTime();

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              false,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionWithoutRawViaHttpTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version?withRaw=false");

      long before = System.nanoTime();
      SzVersionResponse response
          = this.invokeServerViaHttp(GET, uriText, SzVersionResponse.class);
      long after = System.nanoTime();

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              false,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionWithoutRawViaJavaClientTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version?withRaw=false");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzVersionResponse clientResponse
          = this.adminApi.version(false);
      long after = System.nanoTime();

      SzVersionResponse response
          = jsonCopy(clientResponse, SzVersionResponse.class);

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              false,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionWithRawTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version?withRaw=true");
      UriInfo uriInfo = this.newProxyUriInfo(uriText);

      long              before    = System.nanoTime();
      SzVersionResponse response  = this.adminServices.version(true, uriInfo);
      response.concludeTimers();
      long              after     = System.nanoTime();

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              true,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionWithRawViaHttpTest() {
    this.performTest(() -> {
      String uriText = this.formatServerUri("version?withRaw=true");

      long before = System.nanoTime();
      SzVersionResponse response
          = this.invokeServerViaHttp(GET, uriText, SzVersionResponse.class);
      long after = System.nanoTime();

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              true,
                              this.readInitJsonFile());
    });
  }

  @Test public void versionWithRawViaJavaClientTest() {
    this.performTest(() -> {
      String  uriText = this.formatServerUri("version?withRaw=true");

      long before = System.nanoTime();
      com.senzing.gen.api.model.SzVersionResponse clientResponse
          = this.adminApi.version(true);
      long after = System.nanoTime();

      SzVersionResponse response
          = jsonCopy(clientResponse, SzVersionResponse.class);

      validateVersionResponse(response,
                              uriText,
                              after - before,
                              true,
                              this.readInitJsonFile());
    });
  }
}
