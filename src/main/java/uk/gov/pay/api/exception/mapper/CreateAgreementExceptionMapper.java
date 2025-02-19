package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.model.RequestError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static uk.gov.pay.api.model.RequestError.aRequestError;

public class CreateAgreementExceptionMapper implements ExceptionMapper<CreateAgreementException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAgreementExceptionMapper.class);
   
    @Override
    public Response toResponse(CreateAgreementException exception) {
        RequestError requestError = aRequestError(RequestError.Code.CREATE_AGREEMENT_CONNECTOR_ERROR);
        LOGGER.info("Connector invalid response was {}.\n Returning http status {} with error body {}",
                exception.getMessage(), INTERNAL_SERVER_ERROR, requestError);
        LOGGER.info(exception.getMessage());
        return Response.status(INTERNAL_SERVER_ERROR)
                .entity(requestError)
                .type(APPLICATION_JSON)
                .build();
    }
}
