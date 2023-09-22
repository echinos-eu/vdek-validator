package eu.echinos.validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Patient;

public class ValidationProvider extends FhirValidator {

  public static void main(String[] args) {
    FhirContext ctx = FhirContext.forR4Cached();
    IParser iParser = ctx.newJsonParser().setPrettyPrint(true);
    ValidationProvider validationProvider = new ValidationProvider(ctx);
    Patient patient = new Patient();
    patient.setActive(true);
    patient.getMeta()
        .addProfile("https://gematik.de/fhir/isik/v3/Basismodul/StructureDefinition/ISiKPatient");

    ValidationResult validationResult = validationProvider.validateWithResult(patient);
    System.out.println(iParser.encodeResourceToString(validationResult.toOperationOutcome()));
  }

  public ValidationProvider(FhirContext ctx) {
    super(ctx);

    ValidationSupportChain supportChain = new ValidationSupportChain();

    DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport(
        ctx);
    supportChain.addValidationSupport(defaultProfileValidationSupport);
    supportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(ctx));
    supportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(ctx));
    supportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(ctx));

    NpmPackageValidationSupport npmPackageValidationSupport = new NpmPackageValidationSupport(ctx);


    //cache validations
    CachingValidationSupport cache = new CachingValidationSupport(supportChain);

    FhirInstanceValidator validator = new FhirInstanceValidator(cache);

    validator.setAnyExtensionsAllowed(false);
    validator.setErrorForUnknownProfiles(true);
    validator.setNoExtensibleWarnings(true);

    registerValidatorModule(validator);
  }
}
