package io.thestencil.persistence.test;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.thestencil.client.api.StencilClient.SiteState;
import io.thestencil.client.api.beans.SitesBean;
import io.thestencil.persistence.test.config.MongoDbConfig;
import io.thestencil.persistence.test.config.PgProfile;
import io.thestencil.persistence.test.config.PgTestTemplate;
import io.thestencil.persistence.test.config.TestExporter;

@QuarkusTest
@TestProfile(PgProfile.class)
public class MigrationPgTest extends PgTestTemplate {

  @Test
  public void test1() throws JsonMappingException, JsonProcessingException {
    final var repo = getPersistence("migration-test");
 
    final var input = TestExporter.toString(getClass(), "migration-input.json");
    final SitesBean sites = MongoDbConfig.objectMapper.readValue(input, SitesBean.class);
    
    
    SiteState imported = repo.migration().importData(sites)
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
        .await().atMost(Duration.ofMinutes(1));

    
    String expected = TestExporter.toString(getClass(), "migration_state.txt");
    String actual = super.toRepoExport("migration-test");
    Assertions.assertEquals(expected, actual);
    
  }
}
