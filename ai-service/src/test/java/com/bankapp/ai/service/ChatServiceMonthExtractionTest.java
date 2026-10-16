package com.bankapp.ai.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatServiceMonthExtractionTest {

    private List<String> extractMonths(String message) throws Exception {
        ChatService service = new ChatService(
                "http://localhost:11434",
                "test-model",
                "http://localhost:8089",
                "http://localhost:8088");

        Method method = ChatService.class.getDeclaredMethod(
                "extractRequestedMonths", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(service, message);

        return result;
    }

    @Test
    void extractsFrenchMonthsSharingOneYear() throws Exception {
        assertEquals(
                List.of("2026-09", "2026-10"),
                extractMonths("Compare mes paiements de septembre et octobre 2026"));
    }

    @Test
    void extractsEnglishMonthsSharingOneYear() throws Exception {
        assertEquals(
                List.of("2026-09", "2026-10"),
                extractMonths("Compare my payments in September and October 2026"));
    }

    @Test
    void preservesMonthsWithRepeatedYears() throws Exception {
        assertEquals(
                List.of("2026-09", "2026-10"),
                extractMonths("Compare septembre 2026 et octobre 2026"));
    }

    @Test
    void preservesIsoMonthOrder() throws Exception {
        assertEquals(
                List.of("2026-10", "2026-09"),
                extractMonths("Compare 2026-10 and 2026-09"));
    }

    @Test
    void supportsMixedIsoAndNamedMonths() throws Exception {
        assertEquals(
                List.of("2026-09", "2026-10"),
                extractMonths("Compare 2026-09 and October 2026"));
    }

    @Test
    void returnsNoMonthsForNullInput() throws Exception {
        assertEquals(List.of(), extractMonths(null));
    }
}
