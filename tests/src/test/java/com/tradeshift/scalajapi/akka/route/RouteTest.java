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

import scala.PartialFunction;
import scala.concurrent.ExecutionContext;

import com.tradeshift.scalajapi.collect.Option;
import com.tradeshift.scalajapi.collect.Seq;
import com.tradeshift.scalajapi.concurrent.Future;

import akka.http.impl.util.Rendering;
import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaRanges;
import akka.http.javadsl.model.MediaType;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Accept;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.scaladsl.model.headers.CustomHeader;
import akka.http.scaladsl.server.MalformedHeaderRejection;
import akka.http.scaladsl.server.MissingHeaderRejection;
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
                    .flatMap(Unmarshaller.entityToString())
                    .map(s -> new BigDecimal(s));
    
    private final Unmarshaller<HttpRequest,UUID> UUID_FROM_JSON_BODY =
        Unmarshaller.requestToEntity()
                    .flatMap(
                        Unmarshaller.forMediaType(MediaTypes.APPLICATION_JSON,
                        Unmarshaller.entityToString()))
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
                        Unmarshaller.entityToString()))
                    .map(s -> {
                        // just a fake XML parser, assuming it's <id>...</id>
                        Pattern regex = Pattern.compile("<id>(.+)</id>");
                        Matcher matcher = regex.matcher(s);
                        matcher.find();
                        return UUID.fromString(matcher.group(1));
                    });
    
    private final Unmarshaller<HttpRequest,UUID> UUID_FROM_BODY = 
        Unmarshaller.firstOf(UUID_FROM_JSON_BODY, UUID_FROM_XML_BODY);
    
    private final Marshaller<UUID, RequestEntity> UUID_TO_JSON = Marshaller.wrapEntity(
        (UUID u) -> "{\"id\":\"" + u + "\"}",
        Marshaller.stringToEntity(), 
        MediaTypes.APPLICATION_JSON 
    );
            
    private final Marshaller<UUID, RequestEntity> UUID_TO_XML(ContentType xmlType) { 
        return Marshaller.byteStringMarshaller(xmlType).compose(
            (UUID u) -> ByteString.fromString("<id>" + u + "</id>")); 
    }
    
    private final Marshaller<UUID, RequestEntity> UUID_TO_ENTITY = 
        Marshaller.oneOf(
            UUID_TO_JSON,
            UUID_TO_XML(MediaTypes.APPLICATION_XML.toContentType(HttpCharsets.UTF_8)),
            UUID_TO_XML(MediaTypes.TEXT_XML.toContentType(HttpCharsets.UTF_8))
        );
    
    private static boolean isUUID(String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException x) {
            return false;
        }
    }
    
    private static class UUIDHeader extends CustomHeader {
        public static Route extractValue(Function<UUIDHeader, Route> inner) {
            return headerValueByName("UUID", value -> {
                return isUUID(value) ? inner.apply(new UUIDHeader(UUID.fromString(value))) 
                                     : rejectWith(Rejections.malformedHeader("UUID", "must be a valid UUID"));
            });
        }
        
        private final UUID value;
        
        public UUIDHeader(UUID value) {
            this.value = value;
        }

        @Override
        public String name() {
            return "UUID";
        }

        @Override
        public String value() {
            return value.toString();
        }
        
        public UUID uuid() {
            return value;
        }
        
        @Override
        public boolean renderInRequests() {
            return true;
        }
        
        @Override
        public boolean renderInResponses() {
            return true;
        }
    }
    
    @Test
    public void path_can_match_uuid() {
        on(HttpRequest.GET("/documents/359e4920-a6a2-4614-9355-113165d600fb"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("document 359e4920-a6a2-4614-9355-113165d600fb");            
        });
    }
    
    @Test
    public void path_can_match_element() {
        on(HttpRequest.GET("/people/john"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("person john");            
        });
    }
    
    @Test
    public void param_is_extracted() {
        on(HttpRequest.GET("/cookies?amount=5"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("cookies 5");            
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
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("cookies (string) one");            
        });
    }
    
    @Test
    public void required_param_causes_404_on_sealed_route() {
        onSealed(HttpRequest.GET("/cookies"), route, () -> {
            assertThat(status()).isEqualTo(StatusCodes.NOT_FOUND);
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("Request is missing required query parameter 'amount'");
        });        
    }
    
    @Test
    public void custom_param_type_can_be_extracted() {
        on(HttpRequest.GET("/cakes?amount=5"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("cakes 5");            
        });
    }
    
    @Test
    public void custom_extractors_can_be_invoked() {
        on(HttpRequest.GET("/bar").addHeader(RawHeader.create("foo", "hello")), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("bar hello");            
        });        
    }
    
    @Test
    public void entity_can_be_unmarshalled() {
        on(HttpRequest.POST("/bigdecimal").withEntity("1234"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("body 1234");            
        });        
    }
    
    @Test
    public void entity_can_be_unmarshalled_when_picking_json_unmarshaller() {
        on(HttpRequest.PUT("/uuid").withEntity(MediaTypes.APPLICATION_JSON.toContentType(), 
            "{\"id\":\"76b38659-1dec-4ee6-86d0-9ca787bf578c\"}"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("uuid 76b38659-1dec-4ee6-86d0-9ca787bf578c");            
        });        
    }
    
    @Test
    public void entity_can_be_unmarshalled_when_picking_xml_unmarshaller() {
        on(HttpRequest.PUT("/uuid").withEntity(MediaTypes.APPLICATION_XML.toContentType(HttpCharsets.UTF_8), 
            "<id>76b38659-1dec-4ee6-86d0-9ca787bf578c</id>"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("uuid 76b38659-1dec-4ee6-86d0-9ca787bf578c");            
        });        
    }
    
    @Test
    public void entity_can_be_marshalled_when_json_is_accepted() {
        on(HttpRequest.GET("/uuid").addHeader(Accept.create(MediaRanges.create(MediaTypes.APPLICATION_JSON))), route, () -> {
            assertThat(contentType().mediaType()).isEqualTo(MediaTypes.APPLICATION_JSON);
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("{\"id\":\"80a05eee-652e-4458-9bee-19b69dbe1dee\"}");            
        });                
    }
    
    @Test
    public void entity_can_be_marshalled_when_xml_is_accepted() {
        on(HttpRequest.GET("/uuid").addHeader(Accept.create(MediaRanges.create(MediaTypes.TEXT_XML))), route, () -> {
            assertThat(contentType().mediaType()).isEqualTo(MediaTypes.TEXT_XML);
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("<id>80a05eee-652e-4458-9bee-19b69dbe1dee</id>");            
        });                
    }
    
    @Test
    public void request_is_rejected_if_no_marshaller_fits_accepted_type() {
        on(HttpRequest.GET("/uuid").addHeader(Accept.create(MediaRanges.create(MediaTypes.TEXT_PLAIN))), route, () -> {
            assertThat(rejection(UnacceptedResponseContentTypeRejection.class)).isNotNull();
        });                
    }
    
    @Test
    public void first_marshaller_is_picked_and_status_code_applied_if_no_accept_header_present() {
        on(HttpRequest.GET("/uuid"), route, () -> {
            assertThat(contentType().mediaType()).isEqualTo(MediaTypes.APPLICATION_JSON);
            assertThat(status()).isEqualTo(StatusCodes.FOUND);            
        });        
    }
    
    @Test
    public void exception_handlers_are_applied_even_if_the_route_throws_in_future() {
        on(HttpRequest.GET("/shouldnotfail"), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("no problem!");            
        });        
    }
    
    @Test
    public void route_with_required_header_fails_when_header_is_absent() {
        on(HttpRequest.GET("/requiredheader"), route, () -> {
            assertThat(rejection(MissingHeaderRejection.class).headerName()).isEqualTo("UUID");            
        });                
    }
    
    @Test
    public void route_with_required_header_fails_when_header_is_malformed() {
        on(HttpRequest.GET("/requiredheader").addHeader(RawHeader.create("UUID", "monkeys")), route, () -> {
            assertThat(rejection(MalformedHeaderRejection.class).headerName()).isEqualTo("UUID");            
        });                        
    }
    
    @Test
    public void route_with_required_header_succeeds_when_header_is_present_as_RawHeader() {
        on(HttpRequest.GET("/requiredheader").addHeader(RawHeader.create("UUID", "98610fcb-7b19-4639-8dfa-08db8ac19320")), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("has header: 98610fcb-7b19-4639-8dfa-08db8ac19320");
        });                        
    }
    
    @Test
    public void route_with_required_header_succeeds_when_header_is_present_as_custom_type() {
        on(HttpRequest.GET("/requiredheader").addHeader(new UUIDHeader(UUID.fromString("98610fcb-7b19-4639-8dfa-08db8ac19320"))), route, () -> {
            assertThat(responseEntityStrict().getData().utf8String()).isEqualTo("has header: 98610fcb-7b19-4639-8dfa-08db8ac19320");
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
                    return complete(StatusCodes.FOUND, id, UUID_TO_ENTITY);
                })
            )),
            path("cakes", () ->
                param(BIG_DECIMAL_PARAM, "amount", amount -> 
                    complete("cakes " + amount)
                )
            ),
            path("requiredheader", () ->
                UUIDHeader.extractValue(h -> 
                    complete("has header: " + h.uuid())
                )
            )
        );
    }
}
