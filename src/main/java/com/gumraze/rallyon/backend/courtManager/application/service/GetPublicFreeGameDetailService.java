package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetPublicFreeGameDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetPublicFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPublicFreeGameDetailService implements GetPublicFreeGameDetailUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadFreeGameSettingPort loadFreeGameSettingPort;

    @Override
    public FreeGameDetailResponse get(GetPublicFreeGameDetailQuery query) {
        FreeGame freeGame = loadFreeGamePort.loadGameByShareCode(query.shareCode())
                .orElseThrow(() ->
                        new NotFoundException("존재하지 않는 공유 링크입니다. shareCode: " + query.shareCode()));

        FreeGameSetting setting = loadFreeGameSettingPort.loadSettingByGameId(freeGame.getId())
                .orElseThrow(() ->
                        new NotFoundException("존재하지 않는 게임 세팅입니다. gameId: " + freeGame.getId()));

        return FreeGameDetailResponse.from(freeGame, setting);
    }
}
