package eu.echinos.server;

import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.ValidationResult;
import eu.echinos.validator.ValidationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;

public class BundleValidationProvider implements IResourceProvider {

  private final ValidationService validationProvider;

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Bundle.class;
  }

  @Validate
  public MethodOutcome validateBundle(@ResourceParam Bundle bundle) {
    final MethodOutcome outcome = new MethodOutcome();

    ValidationResult validationResult = validationProvider.validateWithResult(bundle);
    OperationOutcome operationOutcome = (OperationOutcome) validationResult.toOperationOutcome();

    //TODO: filter Warnings etc....

    outcome.setOperationOutcome(operationOutcome);
    return outcome;
  }

  public BundleValidationProvider(final ValidationService validationProvider) {
    this.validationProvider = validationProvider;
  }
}
