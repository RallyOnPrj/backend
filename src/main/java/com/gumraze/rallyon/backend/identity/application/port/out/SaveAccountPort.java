package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.identity.entity.Account;

public interface SaveAccountPort {

    Account save(Account account);
}
