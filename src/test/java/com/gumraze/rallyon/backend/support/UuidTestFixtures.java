package com.gumraze.rallyon.backend.support;

import java.util.UUID;

public final class UuidTestFixtures {

    private UuidTestFixtures() {
    }

    public static UUID uuid(long value) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", value));
    }
}
