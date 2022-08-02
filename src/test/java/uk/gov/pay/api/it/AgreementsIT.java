package uk.gov.pay.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.LedgerMockClient;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.mocks.AgreementFromLedgerFixture.AgreementFromLedgerFixtureBuilder.anAgreementFromLedgerFixture;

public class AgreementsIT extends PaymentResourceITestBase {
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    private LedgerMockClient ledgerMockClient = new LedgerMockClient(ledgerMock);

    private final String agreementId = "an-agreement-id";

    @Before
    public void setUp() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void getAgreementFromLedger() throws JsonProcessingException {
        var fixture = anAgreementFromLedgerFixture()
                .withExternalId(agreementId)
                .build();
        ledgerMockClient.respondWithAgreement(agreementId, fixture);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("agreement_id", is(agreementId))
                .body("$", not(hasKey("service_id")))
                .body("reference", is(fixture.getReference()))
                .body("description", is(fixture.getDescription()))
                .body("status", is(fixture.getStatus().toLowerCase()))
                .body("created_date", is(fixture.getCreatedDate()));
    }

    @Test
    public void getAgreement_MissingAgreementShouldMapException() {
        ledgerMockClient.respondAgreementNotFound(agreementId);
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("code", is("P2200"))
                .body("description", is("Not found"));
    }

    @Test
    public void searchAgreementsFromLedger() {
        var fixture1 = anAgreementFromLedgerFixture()
                .withExternalId(agreementId)
                .build();

        var fixture2 = anAgreementFromLedgerFixture()
                .withExternalId("another-agreement-id")
                .withServiceId("service-2")
                .withReference("ref-2")
                .withDescription("Description 2")
                .withStatus("CREATED")
                .withCreatedDate("2022-07-27T12:30:00Z")
                .build();

        ledgerMockClient.respondWithSearchAgreements(GATEWAY_ACCOUNT_ID, fixture1, fixture2);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .basePath(AGREEMENTS_PATH)
                .queryParam("status", "created")
                .queryParam("page", "3")
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", is(9))
                .body("count", is(2))
                .body("page", is(3))
                .body("results.size()", is(2))
                .body("results[0].agreement_id", is(fixture1.getExternalId()))
                .body("results[0].reference", is(fixture1.getReference()))
                .body("results[0].description", is(fixture1.getDescription()))
                .body("results[0].status", is(fixture1.getStatus().toLowerCase()))
                .body("results[0].created_date", is(fixture1.getCreatedDate()))
                .body("results[1].agreement_id", is(fixture2.getExternalId()))
                .body("results[1].reference", is(fixture2.getReference()))
                .body("results[1].description", is(fixture2.getDescription()))
                .body("results[1].status", is(fixture2.getStatus().toLowerCase()))
                .body("results[1].created_date", is(fixture2.getCreatedDate()));
    }

    @Test
    public void searchAgreementsReturnsErrorWhenValidationError() throws Exception {
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .basePath(AGREEMENTS_PATH)
                .queryParam("status", "ethereal")
                .get()
                .then()
                .statusCode(422)
                .contentType(ContentType.JSON)
                .body("code", is("P2401"))
                .body("description", is("Invalid parameters: status. See Public API documentation for the correct data formats"));
    }

}
