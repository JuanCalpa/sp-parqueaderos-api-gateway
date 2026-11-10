package com.sp.parking.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

/**
 * Si el servicio de destino esta apagado, responde 503 con un mensaje que dice cual es
 * (mismo formato { message } que usan los servicios), en vez de un 500 generico.
 */
@Component
public class UnavailableServiceFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return chain.filter(exchange).onErrorResume(UnavailableServiceFilter::isUnreachable, error -> {
      ServerHttpResponse response = exchange.getResponse();
      if (response.isCommitted()) return Mono.error(error);
      response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
      response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
      String body = "{\"status\":503,\"message\":\"" + serviceName(exchange) + " no responde. Verifica que esté encendido.\"}";
      return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    });
  }

  private static boolean isUnreachable(Throwable error) {
    for (Throwable cause = error; cause != null; cause = cause.getCause()) {
      if (cause instanceof ConnectException || cause instanceof io.netty.channel.ConnectTimeoutException) return true;
    }
    return false;
  }

  private static String serviceName(ServerWebExchange exchange) {
    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    Object name = route == null ? null : route.getMetadata().get("name");
    return name == null ? "El servicio" : name.toString();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
