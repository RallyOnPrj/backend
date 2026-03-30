package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.user.entity.UserGradeHistory;

public interface SaveUserGradeHistoryPort {

    void save(UserGradeHistory userGradeHistory);
}
