package uk.gov.pay.api.validation;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CardLastFourDigitsValidatorTest {

    private static CreateTelephonePaymentRequest.TelephoneRequestBuilder telephoneRequestBuilder = new CreateTelephonePaymentRequest.TelephoneRequestBuilder();

    private static Validator validator;

    @BeforeClass
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        telephoneRequestBuilder
                .withAmount(1200)
                .withDescription("Some description")
                .withReference("Some reference")
                .withProcessorId("1PROC")
                .withProviderId("1PROV")
                .withCardExpiry("01/99")
                .withCardType("visa")
                .withFirstSixDigits("123456")
                .withPaymentOutcome(new PaymentOutcome("success"));
    }
    
    @Test
    public void failsValidationForThreeDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withLastFourDigits("123")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [last_four_digits] must be exactly 4 digits"));
    }

    @Test
    public void failsValidationForFiveDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withLastFourDigits("12345")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [last_four_digits] must be exactly 4 digits"));
    }

    @Test
    public void passesValidationForFourDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withLastFourDigits("1234")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);
        
        assertThat(constraintViolations.isEmpty(), is(true));
    }

    @Test
    public void failsValidationForNullDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withLastFourDigits(null)
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(),is("Field [last_four_digits] cannot be null"));
    }
}
