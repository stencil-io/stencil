package io.thestencil.iam.spi.support;

import io.thestencil.iam.api.RemoteIntegration;
import io.vertx.core.http.RequestOptions;

public class RemoteIntegrationConverter {

  public static RequestOptions integrationToOptions(RemoteIntegration integration) {
    boolean ssl = "https".equals(integration.getProtocol());
    Integer port = integration.getPort();
    if (port == null) {
      port = ssl ? 443 : 80;
    }
    else if (ssl && port == 80) {
      // ssl set but port default http, change it to default https
      port = 443;
    }
    return new RequestOptions()
        .setURI(integration.getPath())
        .setHost(integration.getHost())
        .setSsl(ssl)
        .setPort(port);
  }
}
