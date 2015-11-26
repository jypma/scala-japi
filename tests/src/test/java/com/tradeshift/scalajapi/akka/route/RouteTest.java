package com.tradeshift.scalajapi.akka.route;

import static com.tradeshift.scalajapi.akka.route.Directives.*;
import static com.tradeshift.scalajapi.akka.route.StringUnmarshaller.*;

import com.tradeshift.scalajapi.akka.route.Marshaller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.junit.Test;

import scala.concurrent.ExecutionContext;

import com.tradeshift.scalajapi.collect.Seq;
import com.tradeshift.scalajapi.concurrent.Future;

import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaRanges;
import akka.http.javadsl.model.MediaType;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Accept;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.scaladsl.server.MissingQueryParamRejection;
import akka.http.scaladsl.server.MissingFormFieldRejection;
import akka.http.scaladsl.server.Rejection;
import akka.http.scaladsl.server.UnacceptedResponseContentTypeRejection;
import akka.japi.pf.PFBuilder;
import akka.util.ByteString;

public class RouteTest extends RouteTestKit {
    private final Route route = getRoute();
    private static final Unmarshaller<String,BigDecimal> BIG_DECIMAL_PARAM = Unmarshaller.sync(s -> new BigDecimal(s));
    
    private final Unmarshaller<HttpRequest,BigDecimal> BIG_DECIMAL_BODY =
        Unmarshaller.requestToEntity()
                    .flatMap(Unmarshaller.entityToString(materializer()))
                    .map(s -> new BigDecimal(s));
    
    private final Unmarshaller<HttpRequest,UUID> UUID_FROM_JSON_BODY =
        Unmarshaller.requestToEntity()
                    .flatMap(
                        Unmarshaller.forMediaType(MediaTypes.APPLICATION_JSON,
                        Unmarshaller.entityToString(materializer())))
                    .map(s -> {
                        // just a fake JSON parser, assuming it's {"id":"..."}
                        Pattern regex = Pattern.compile("\"id\":\"(.+)\"");
                        Matcher matcher = regex.matcher(s);
                        matcher.find();
                        return UUID.fromString(matcher.group(1));
                    });
    
    private final Unmarshaller<HttpRequest,UUID> UUID_FROM_XML_BODY =
        Unmarshaller.requestToEntity()
                    .flatMap(
                        Unmarshaller.forMediaTypes(Seq.of(MediaTypes.TEXT_XML, MediaTypes.APPLICATION_XML),
                        Unmarshaller.entityToString(materializer())))
                    .map(s -> {
                        // just a fake XML parser, assuming it's <id>...</id>
                        Pattern regex = Pattern.compile("<id>(.+)</id>");
                        Matcher matcher = regex.matcher(s);
                        matcher.find();
                        return UUID.fromString(matcher.group(1));
                    });
    
    private final Unmarshaller<HttpRequest,UUID> UUID_FROM_BODY = 
        Unmarshaller.firstOf(UUID_FROM_JSON_BODY, UUID_FROM_XML_BODY);
    
    private final Marshaller<UUID, RequestEntity> UUID_TO_RQ = Marshaller.wrapEntity(
        Marshaller.stringToEntity(), 
        ContentType.create(MediaTypes.APPLICATION_JSON), 
        (UUID u) -> "{\"id\":\"" + u + "\"}");
    
    private final Marshaller<UUID, HttpResponse> UUID_TO_JSON_BODY =
        Marshaller.entityToResponse(
        Marshaller.wrapEntity(Marshaller.stringToEntity(), ContentType.create(MediaTypes.APPLICATION_JSON), 
            (UUID u) -> "{\"id\":\"" + u + "\"}"));
    
    private final Marshaller<UUID, HttpResponse> UUID_TO_XML_BODY(MediaType xmlType) { return
        Marshaller.entityToResponse(
        Marshaller.wrapEntity(Marshaller.byteStringToEntity(), ContentType.create(xmlType), 
            (UUID u) -> ByteString.fromString("<id>" + u + "</id>")));
    }
    
    private final Marshaller<UUID, HttpResponse> UUID_TO_BODY = Marshaller.oneOf(
        UUID_TO_JSON_BODY,
        UUID_TO_XML_BODY(MediaTypes.APPLICATION_XML),
        UUID_TO_XML_BODY(MediaTypes.TEXT_XML));
    
    @Test
    public void path_can_match_uuid() {
        on(HttpRequest.GET("/documents/359e4920-a6a2-4614-9355-113165d600fb"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("document 359e4920-a6a2-4614-9355-113165d600fb");            
        });
    }
    
    @Test
    public void path_can_match_element() {
        on(HttpRequest.GET("/people/john"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("person john");            
        });
    }
    
    @Test
    public void param_is_extracted() {
        on(HttpRequest.GET("/cookies?amount=5"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("cookies 5");            
        });
    }
    
    @Test
    public void required_param_causes_rejection_when_missing() {
        on(HttpRequest.GET("/cookies"), route, () -> {
            assertThat(rejections()).containsExactly(MissingQueryParamRejection.apply("amount"));            
        });
    }
    
    @Test
    public void wrong_param_type_causes_next_route_to_be_evaluated() {
        on(HttpRequest.GET("/cookies?amount=one"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("cookies (string) one");            
        });
    }
    
    @Test
    public void required_param_causes_404_on_sealed_route() {
        onSealed(HttpRequest.GET("/cookies"), route, () -> {
            assertThat(status()).isEqualTo(StatusCodes.NOT_FOUND);
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("Request is missing required query parameter 'amount'");
        });        
    }
    
    @Test
    public void custom_param_type_can_be_extracted() {
        on(HttpRequest.GET("/cakes?amount=5"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("cakes 5");            
        });
    }
    
    @Test
    public void custom_extractors_can_be_invoked() {
        on(HttpRequest.GET("/bar").addHeader(RawHeader.create("foo", "hello")), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("bar hello");            
        });        
    }
    
    @Test
    public void entity_can_be_unmarshalled() {
        on(HttpRequest.POST("/bigdecimal").withEntity("1234"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("body 1234");            
        });        
    }
    
    @Test
    public void entity_can_be_unmarshalled_when_picking_json_unmarshaller() {
        on(HttpRequest.PUT("/uuid").withEntity(ContentType.create(MediaTypes.APPLICATION_JSON, HttpCharsets.UTF_8), 
            "{\"id\":\"76b38659-1dec-4ee6-86d0-9ca787bf578c\"}"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("uuid 76b38659-1dec-4ee6-86d0-9ca787bf578c");            
        });        
    }
    
    @Test
    public void entity_can_be_unmarshalled_when_picking_xml_unmarshaller() {
        on(HttpRequest.PUT("/uuid").withEntity(ContentType.create(MediaTypes.APPLICATION_XML), 
            "<id>76b38659-1dec-4ee6-86d0-9ca787bf578c</id>"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("uuid 76b38659-1dec-4ee6-86d0-9ca787bf578c");            
        });        
    }
    
    @Test
    public void entity_can_be_marshalled_when_json_is_accepted() {
        on(HttpRequest.GET("/uuid").addHeader(Accept.create(MediaRanges.create(MediaTypes.APPLICATION_JSON))), route, () -> {
            assertThat(contentType().mediaType()).isEqualTo(MediaTypes.APPLICATION_JSON);
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("{\"id\":\"80a05eee-652e-4458-9bee-19b69dbe1dee\"}");            
        });                
    }
    
    @Test
    public void entity_can_be_marshalled_when_xml_is_accepted() {
        on(HttpRequest.GET("/uuid").addHeader(Accept.create(MediaRanges.create(MediaTypes.TEXT_XML))), route, () -> {
            assertThat(contentType().mediaType()).isEqualTo(MediaTypes.TEXT_XML);
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("<id>80a05eee-652e-4458-9bee-19b69dbe1dee</id>");            
        });                
    }
    
    @Test
    public void request_is_rejected_if_no_marshaller_fits_accepted_type() {
        on(HttpRequest.GET("/uuid").addHeader(Accept.create(MediaRanges.create(MediaTypes.TEXT_PLAIN))), route, () -> {
            assertThat(rejections()).hasSize(1);
            Rejection rejection = rejections().get(0);
            assertThat(rejection).isInstanceOf(UnacceptedResponseContentTypeRejection.class);
        });                
        
    }
    
    @Test
    public void exception_handlers_are_applied_even_if_the_route_throws_in_future() {
        on(HttpRequest.GET("/shouldnotfail"), route, () -> {
            assertThat(responseEntityStrict().data().utf8String()).isEqualTo("no problem!");            
        });        
    }
    
    private static ExceptionHandler xHandler = ExceptionHandler.of(
        new PFBuilder<Throwable, Route>()
        .match(IllegalArgumentException.class, x -> complete("no problem!"))
        .build());
    
    private static Function<RequestContext,String> CUSTOM_EXTRACT = ctx -> ctx.getRequest().getHeader("foo").get().value();
    
    private Future<Integer> throwExceptionInFuture() {
        return Future.<Integer>call(() -> { throw new IllegalArgumentException("always failing"); });
    }
    
    public Route getRoute() {
        return route(
            path("documents", () ->
                path(UUID(), id ->
                    complete("document " + id)
                )
            ),
            path("people", () ->
                path(name ->
                    complete("person " + name)
                )
            ),
            path("notreally", () ->
                rejectWith(Rejections.missingFormField("always failing"))
            ),
            path("shouldnotfail", () ->
                handleExceptions(xHandler, () ->
                    onSuccess(() -> throwExceptionInFuture(), value -> 
                        complete("never reaches here")
                    )
                )
            ),
            path("cookies", () ->
                param(INT(), "amount", amount -> 
                    complete("cookies " + amount)
                )
            ),
            path("cookies", () ->
                param(STRING(), "amount", amount -> 
                    complete("cookies (string) " + amount)
                )
            ),
            path("bar", () ->
                extract(CUSTOM_EXTRACT, value -> 
                    complete("bar " + value)
                )
            ),
            path("custom_response", () ->
                complete(HttpResponse.create().withStatus(StatusCodes.ACCEPTED))
            ),
            path("bigdecimal", () ->
                entityAs(BIG_DECIMAL_BODY, value -> 
                    complete("body " + value)
                )
            ),
            path("uuid", () -> route(
                put(() ->
                    entityAs(UUID_FROM_BODY, value -> 
                        complete("uuid " + value)
                    )
                ),
                get(() -> {
                    UUID id = UUID.fromString("80a05eee-652e-4458-9bee-19b69dbe1dee");
                    return complete(id, UUID_TO_BODY);
                })
            )),
            path("cakes", () ->
                param(BIG_DECIMAL_PARAM, "amount", amount -> 
                    complete("cakes " + amount)
                )
            )
        );
    }
}
