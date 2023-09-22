package eu.echinos.validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;

public class ValidationProvider extends FhirValidator {

  public ValidationProvider(FhirContext ctx) {
    super(ctx);

    ValidationSupportChain supportChain = new ValidationSupportChain();

    DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport(
        ctx);
    supportChain.addValidationSupport(defaultProfileValidationSupport);
    supportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(ctx));
    supportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(ctx));
    supportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(ctx));

    //cache validations
    CachingValidationSupport cache = new CachingValidationSupport(supportChain);

    FhirInstanceValidator validator = new FhirInstanceValidator(cache);

    validator.setAnyExtensionsAllowed(false);
    validator.setErrorForUnknownProfiles(true);
    validator.setNoExtensibleWarnings(true);

    registerValidatorModule(validator);
  }
}
