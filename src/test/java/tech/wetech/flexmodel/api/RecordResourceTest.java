package tech.wetech.flexmodel.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.MySQLTestResource;

/**
 * @author cjbi
 */
@QuarkusTest
@QuarkusTestResource(MySQLTestResource.class)
class RecordResourceTest {

  @Test
  void testFindPagingRecords() {
  }

  @Test
  void testFindOneRecord() {
  }

  @Test
  void testCreateRecord() {
  }

  @Test
  void testUpdateRecord() {
  }

  @Test
  void testDeleteRecord() {
  }
}
