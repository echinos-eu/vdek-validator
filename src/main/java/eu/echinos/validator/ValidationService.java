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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

public class ValidationService extends FhirValidator {

  public static void main(String[] args) throws IOException {
    Locale.setDefault(Locale.ENGLISH);
    FhirContext ctx = FhirContext.forR4Cached();
    IParser iParser = ctx.newJsonParser().setPrettyPrint(true);
    ValidationService validationProvider = new ValidationService(ctx);

    Patient patient = newPatient();
    ValidationResult validationResult = validationProvider.validateWithResult(patient);
    System.out.println(iParser.encodeResourceToString(validationResult.toOperationOutcome()));
  }

  private static Patient newPatient() {
    Patient patient = new Patient();
    patient.addName().addGiven("Patrick").addGiven("Fritz")
        .setUse(NameUse.OFFICIAL);
    StringType familyElement = patient.getNameFirstRep().getFamilyElement();
    familyElement.setValue("Werner");
    familyElement.addExtension()
        .setUrl("http://hl7.org/fhir/StructureDefinition/humanname-own-name")
        .setValue(new StringType("Werner"));
    String identSystem = "http://echinos.eu/fhir/sid/PatientIdentifier";
    String identValue = "012598419642sfdf34";
    CodeableConcept mrcc = new CodeableConcept();
    mrcc.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
        .setCode("MR");
    patient.addIdentifier().setSystem(identSystem).setValue(identValue).setType(mrcc);
    patient.setGender(AdministrativeGender.MALE);
    patient.setActive(true);
    patient.setBirthDateElement(new DateType("1980-01-01"));

    patient.getMeta()
        .addProfile("https://gematik.de/fhir/isik/v3/Basismodul/StructureDefinition/ISiKPatient");
    return patient;
  }

  public ValidationService(FhirContext ctx) throws IOException {
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
