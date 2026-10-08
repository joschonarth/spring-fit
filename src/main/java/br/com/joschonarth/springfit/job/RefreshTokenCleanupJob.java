package br.com.joschonarth.springfit.job;

import br.com.joschonarth.springfit.database.repository.IRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);

    private final IRefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // every day at 03:00
    @Transactional
    public void deleteExpiredTokens() {
        int deleted = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.info("Refresh token cleanup: {} expired token(s) removed", deleted);
    }
}
