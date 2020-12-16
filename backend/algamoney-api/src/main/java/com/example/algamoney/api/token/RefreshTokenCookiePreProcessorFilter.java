package com.example.algamoney.api.token;

import java.io.IOException;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RefreshTokenCookiePreProcessorFilter implements Filter {

    private static final String REQ_OAUTH_TOKEN = "/oauth/token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // Maneira tradicional sem os recursos do Java 8
        //        if (REQ_OAUTH_TOKEN.equals(req.getRequestURI()) //
        //            && REFRESH_TOKEN.equals(req.getParameter("grant_type")) //
        //            && req.getCookies() != null) {
        //
        //            for (Cookie cookie : req.getCookies()) {
        //                if (cookie.getName().equals("refreshToken")) {
        //                    String refreshToken = cookie.getValue();
        //                    req = new MyServletRequestWrapper(req, refreshToken);
        //                }
        //            }
        //        }

        // Maneira mais moderna utilizando a API de stream disponibilizada a partir do Java 8
        if ("/oauth/token".equalsIgnoreCase(req.getRequestURI()) && "refresh_token".equals(req.getParameter("grant_type")) && req.getCookies() != null) {

            String refreshToken = Stream.of(req.getCookies()) // Transformar o array de cookies em um Strem, com o comando Stream.of(...)
                    .filter(cookie -> "refreshToken".equals(cookie.getName())) //Filtrar os dados do Stream para que retorne apenas o que tenha o nome refreshToken
                    .findFirst() // Obter o primeiro objeto do Stream (caso exista)
                    .map(cookie -> cookie.getValue()) // Transformá-lo de cookie em uma String com o seu valor.
                    .orElse(null); // Caso não tenha encontrado um cookie com o nome refreshToken, retorna null.

            req = new MyServletRequestWrapper(req, refreshToken);
        }

        chain.doFilter(req, response);
    }

}
