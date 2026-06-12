package dev.ograh.dynamicforms.web.client;

import java.util.List;

public record RestPage<T>(
        List<T> content,
        int totalPages,
        int number,
        long totalElements,
        boolean first,
        boolean last
) {}
