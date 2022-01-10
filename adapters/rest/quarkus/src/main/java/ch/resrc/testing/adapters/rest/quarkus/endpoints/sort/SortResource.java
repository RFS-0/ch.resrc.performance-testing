package ch.resrc.testing.adapters.rest.quarkus.endpoints.sort;

import ch.resrc.testing.adapters.rest.endpoints.sort.dto.UnsortedListDto;
import ch.resrc.testing.adapters.rest.endpoints.sort.input.SortInput;
import ch.resrc.testing.adapters.rest.errorhandling.UseCaseTry;
import ch.resrc.testing.adapters.rest.quarkus.config.*;
import ch.resrc.testing.adapters.rest.quarkus.endpoints.sort.output.SortedResponse;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.ProblemCatalogue;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("sort")
@Produces(MediaType.APPLICATION_JSON)
public class SortResource {

    private final ProblemCatalogue problemCatalogue;
    private final Json json;

    private final Sort basicBubbleSort;
    private final Sort optimizedBubbleSort;
    private final Sort redBlackTreeSort;

    private final UseCaseTry useCaseTry;

    public SortResource(Json json,
                        ProblemCatalogue problemCatalogue,
                        @BasicBubbleSort Sort basicBubbleSort,
                        @OptimizedBubbleSort Sort optimizedBubbleSort,
                        @RedBlackTreeSort Sort redBlackTreeSort) {
        this.json = json;
        this.problemCatalogue = problemCatalogue;

        this.basicBubbleSort = basicBubbleSort;
        this.optimizedBubbleSort = optimizedBubbleSort;
        this.redBlackTreeSort = redBlackTreeSort;

        useCaseTry = new UseCaseTry(System.out::println);
    }

    /**
     * When using RESTEasy Reactive do the following:
     * <ul>
     *     <li>Return <pre>{@code Uni<Response>}</pre> instead of <pre>{@code Response}</pre></li>
     *     <li>Wrap use case execution in <pre>{@code Uni.createFrom().item(<useCaseTry...>}</pre> }</pre></li>
     * </ul>
     */
    @POST
    @Path("basic-bubble-sort")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response basicBubbleSort(@RequestBody JsonBody<UnsortedListDto> unsortedList) {
        return useCaseTry
                .withInput(new SortInput(Client.anonymous(), unsortedList))
                .withOutput(new SortedResponse(201, problemCatalogue, json))
                .invoke((input, output) -> basicBubbleSort.invoke(input.input(), output))
                .asResponseEntity();
    }

    @POST
    @Path("optimized-bubble-sort")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response optimizedBubbleSort(JsonBody<UnsortedListDto> unsortedList) {
        return useCaseTry
                .withInput(new SortInput(Client.anonymous(), unsortedList))
                .withOutput(new SortedResponse(201, problemCatalogue, json))
                .invoke((input, output) -> optimizedBubbleSort.invoke(input.input(), output))
                .asResponseEntity();
    }

    @POST
    @Path("red-black-tree")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response redBlackTreeSort(JsonBody<UnsortedListDto> unsortedList) {
        return useCaseTry
                .withInput(new SortInput(Client.anonymous(), unsortedList))
                .withOutput(new SortedResponse(201, problemCatalogue, json))
                .invoke((input, output) -> redBlackTreeSort.invoke(input.input(), output))
                .asResponseEntity();
    }
}
