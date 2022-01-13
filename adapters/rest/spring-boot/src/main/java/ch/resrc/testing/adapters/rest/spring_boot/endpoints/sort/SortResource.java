package ch.resrc.testing.adapters.rest.spring_boot.endpoints.sort;

import ch.resrc.testing.adapters.rest.endpoints.sort.dto.*;
import ch.resrc.testing.adapters.rest.endpoints.sort.input.SortInput;
import ch.resrc.testing.adapters.rest.errorhandling.UseCaseTry;
import ch.resrc.testing.adapters.rest.spring_boot.endpoints.sort.output.SortedResponse;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.ProblemCatalogue;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/sort", produces = "application/json")
public class SortResource {

    Logger logger = LoggerFactory.getLogger(SortResource.class);

    private final ProblemCatalogue problemCatalogue;
    private final Json json;

    private final Sort basicBubbleSort;
    private final Sort optimizedBubbleSort;
    private final Sort redBlackTreeSort;

    private final UseCaseTry useCaseTry;

    @Autowired
    public SortResource(Json json,
                        ProblemCatalogue problemCatalogue,
                        @Qualifier("basic-bubble-sort") Sort basicBubbleSort,
                        @Qualifier("optimized-bubble-sort") Sort optimizedBubbleSort,
                        @Qualifier("red-black-tree-sort") Sort redBlackTreeSort) {
        this.json = json;
        this.problemCatalogue = problemCatalogue;

        this.basicBubbleSort = basicBubbleSort;
        this.optimizedBubbleSort = optimizedBubbleSort;
        this.redBlackTreeSort = redBlackTreeSort;

        useCaseTry = new UseCaseTry(System.out::println);
    }

    @RequestMapping(method = RequestMethod.POST, path = "basic-bubble-sort", consumes = "application/json")
    public ResponseEntity<JsonBody<SortedListDto<? extends Comparable<?>>>> basicBubbleSort(@RequestBody JsonBody<UnsortedListDto> unsortedList) {
        return useCaseTry
                .withInput(new SortInput(Client.anonymous(), unsortedList))
                .withOutput(new SortedResponse(201, problemCatalogue, json))
                .invoke((input, output) -> basicBubbleSort.invoke(input.input(), output))
                .asResponseEntity();
    }

    @RequestMapping(method = RequestMethod.POST, path = "optimized-bubble-sort", consumes = "application/json")
    public ResponseEntity<JsonBody<SortedListDto<? extends Comparable<?>>>> optimizedBubbleSort(@RequestBody JsonBody<UnsortedListDto> unsortedList) {
        return useCaseTry
                .withInput(new SortInput(Client.anonymous(), unsortedList))
                .withOutput(new SortedResponse(201, problemCatalogue, json))
                .invoke((input, output) -> optimizedBubbleSort.invoke(input.input(), output))
                .asResponseEntity();
    }

    @RequestMapping(method = RequestMethod.POST, path = "red-black-tree-sort", consumes = "application/json")
    public ResponseEntity<JsonBody<SortedListDto<? extends Comparable<?>>>> redBlackTreeSort(@RequestBody JsonBody<UnsortedListDto> unsortedList) {
        return useCaseTry
                .withInput(new SortInput(Client.anonymous(), unsortedList))
                .withOutput(new SortedResponse(201, problemCatalogue, json))
                .invoke((input, output) -> redBlackTreeSort.invoke(input.input(), output))
                .asResponseEntity();
    }
}
