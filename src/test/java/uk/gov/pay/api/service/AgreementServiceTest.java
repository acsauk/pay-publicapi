package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.agreement.model.AgreementCreatedResponse;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CancelAgreementException;
import uk.gov.pay.api.model.CreateAgreementRequestBuilder;
import uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.it.CreateAgreementIT.agreementPayload;
import static uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams.CreateAgreementRequestParamsBuilder.aCreateAgreementRequestParams;

@ExtendWith(MockitoExtension.class)
class AgreementServiceTest {

    private static final String REFERENCE_ID = "test";
    private static final String AGREEMENT_ID = "12345678901234567890123456";
    private static final String AGREEMENTS_CONNECTOR_URI = "/v1/api/accounts/GATEWAY_ACCOUNT_ID/agreements"; // pragma: allowlist secret
    private static final String AGREEMENT_CONNECTOR_CANCEL_URI = AGREEMENTS_CONNECTOR_URI + '/' + AGREEMENT_ID + "/cancel";

    @Mock
    private Account mockAccount;
    @Mock
    private ConnectorUriGenerator mockConnectorUriGenerator;
    @Mock
    private Client mockClient;
    @Mock
    private WebTarget mockWebTarget;
    @Mock
    private Invocation.Builder mockInvocationBuilder;
    @Mock
    private Response mockConnectorResponse;

    private AgreementService service;

    @BeforeEach
    void setUp() {
        service = new AgreementService(mockClient, mockConnectorUriGenerator);
    }

    @Test
    void shouldCreateAgreement() {
        when(mockConnectorUriGenerator.getAgreementURI(mockAccount)).thenReturn(AGREEMENTS_CONNECTOR_URI);
        when(mockClient.target(AGREEMENTS_CONNECTOR_URI)).thenReturn(mockWebTarget);
        when(mockWebTarget.request()).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.accept(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE_ID)
                .build();
        when(mockInvocationBuilder.post(json(agreementPayload(createAgreementRequestParams)))).thenReturn(mockConnectorResponse);

        when(mockConnectorResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);

        var agreementResponse = new AgreementCreatedResponse(AGREEMENT_ID);
        when(mockConnectorResponse.readEntity(AgreementCreatedResponse.class)).thenReturn(agreementResponse);

        CreateAgreementRequest agreementCreateRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder
                .builder().reference(REFERENCE_ID));
        AgreementCreatedResponse agreementResponseFromService = service.create(mockAccount, agreementCreateRequest);

        assertThat(agreementResponseFromService.getAgreementId(), is(AGREEMENT_ID));
    }

    @Test
    void shouldCancelAgreement() {
        when(mockConnectorUriGenerator.cancelAgreementURI(mockAccount, AGREEMENT_ID)).thenReturn(AGREEMENT_CONNECTOR_CANCEL_URI);
        when(mockClient.target(AGREEMENT_CONNECTOR_CANCEL_URI)).thenReturn(mockWebTarget);
        when(mockWebTarget.request()).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.post(null)).thenReturn(mockConnectorResponse);
        when(mockConnectorResponse.getStatus()).thenReturn(HttpStatus.SC_NO_CONTENT);

        Response response = service.cancel(mockAccount, AGREEMENT_ID);

        assertThat(response.getStatus(), is(NO_CONTENT.getStatusCode()));

        verify(mockConnectorResponse).close();
    }

    @Test
    void shouldThrowExceptionIfConnectorReturnsUnexpectedStatusCodeWhenCancellingAgreement() {
        when(mockConnectorUriGenerator.cancelAgreementURI(mockAccount, AGREEMENT_ID)).thenReturn(AGREEMENT_CONNECTOR_CANCEL_URI);
        when(mockClient.target(AGREEMENT_CONNECTOR_CANCEL_URI)).thenReturn(mockWebTarget);
        when(mockWebTarget.request()).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.post(null)).thenReturn(mockConnectorResponse);
        when(mockConnectorResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        assertThrows(CancelAgreementException.class, () -> service.cancel(mockAccount, AGREEMENT_ID));

        verify(mockConnectorResponse).close();
    }
}
