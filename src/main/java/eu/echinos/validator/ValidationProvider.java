package eu.echinos.validator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import java.io.IOException;
import java.util.Locale;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Patient;

public class ValidationProvider extends FhirValidator {

  public static void main(String[] args) throws IOException {
    Locale.setDefault(Locale.ENGLISH);
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

  public ValidationProvider(FhirContext ctx) throws IOException {
    super(ctx);

    ValidationSupportChain supportChain = new ValidationSupportChain();

    DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport(
        ctx);
    supportChain.addValidationSupport(defaultProfileValidationSupport);
    supportChain.addValidationSupport(new CommonCodeSystemsTerminologyService(ctx));
    supportChain.addValidationSupport(new InMemoryTerminologyServerValidationSupport(ctx));
    supportChain.addValidationSupport(new SnapshotGeneratingValidationSupport(ctx));

    NpmPackageValidationSupport npmPackageValidationSupport = new NpmPackageValidationSupport(ctx);
    npmPackageValidationSupport.loadPackageFromClasspath(
        "classpath:packages/de.basisprofil.r4-1.4.0.tgz");
    npmPackageValidationSupport.loadPackageFromClasspath(
        "classpath:packages/de.gematik.isik-basismodul-3.0.0.tgz");
    supportChain.addValidationSupport(npmPackageValidationSupport);

    //cache validations
    CachingValidationSupport cache = new CachingValidationSupport(supportChain);

    FhirInstanceValidator validator = new FhirInstanceValidator(cache);

    validator.setAnyExtensionsAllowed(false);
    validator.setErrorForUnknownProfiles(true);
    validator.setNoExtensibleWarnings(true);

    registerValidatorModule(validator);
  }
}
