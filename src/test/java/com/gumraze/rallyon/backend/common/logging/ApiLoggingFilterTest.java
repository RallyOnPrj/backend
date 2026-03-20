package com.gumraze.rallyon.backend.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiLoggingFilterTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(ApiLoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    @DisplayName("정상이고 빠른 요청은 로그를 남기지 않는다")
    void doFilter_doesNotLog_whenRequestIsSuccessfulAndFast() throws Exception {
        ApiLoggingFilter filter = new ApiLoggingFilter(1_000L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/free-games");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    @DisplayName("4xx 응답은 warn 로그를 남긴다")
    void doFilter_logsWarn_whenResponseIs4xx() throws Exception {
        ApiLoggingFilter filter = new ApiLoggingFilter(1_000L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/free-games/unknown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(404));

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getFormattedMessage()).contains("[API][CLIENT_ERROR]");
        assertThat(event.getFormattedMessage()).contains("status=404");
    }

    @Test
    @DisplayName("5xx 응답은 error 로그를 남긴다")
    void doFilter_logsError_whenResponseIs5xx() throws Exception {
        ApiLoggingFilter filter = new ApiLoggingFilter(1_000L);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/free-games");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(500));

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getFormattedMessage()).contains("[API][SERVER_ERROR]");
        assertThat(event.getFormattedMessage()).contains("status=500");
    }

    @Test
    @DisplayName("성공 응답이어도 느린 요청은 warn 로그를 남긴다")
    void doFilter_logsWarn_whenRequestIsSlow() throws Exception {
        ApiLoggingFilter filter = new ApiLoggingFilter(0L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/free-games");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getFormattedMessage()).contains("[API][SLOW]");
        assertThat(event.getFormattedMessage()).contains("status=200");
    }
}
