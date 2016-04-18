package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.SearchChargesException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.P0498;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class SearchChargesExceptionMapper implements ExceptionMapper<SearchChargesException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchChargesException.class);

    @Override
    public Response toResponse(SearchChargesException exception) {
        PaymentError paymentError = aPaymentError(P0498, "Downstream system error");
        LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), INTERNAL_SERVER_ERROR, paymentError);
        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(paymentError)
                .build();
    }
}
