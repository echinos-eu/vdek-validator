package eu.echinos.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import eu.echinos.validator.ValidationService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;

@WebServlet("/*")
public class RestServlet extends RestfulServer {

  public RestServlet() {
    super(FhirContext.forR4Cached());
  }

  @Override
  public void initialize() {
    try {
      final ValidationService validationService = new ValidationService(getFhirContext());
      BundleValidationProvider provider = new BundleValidationProvider(
          validationService);
      setResourceProviders(provider);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
