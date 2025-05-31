package com.jelly.zzirit.domain.order.util;

import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerRecoveryTrigger {

    private final TossPaymentClient tossPaymentClient;

    public void attemptRecovery() {
        Optional<String> optionalKey = CircuitBreakerMemoryStore.getStoredKey();

        if (optionalKey.isEmpty()) {
            log.debug("CircuitBreaker 복구 대상 없음 - 작업 중단");
            return;
        }

        String paymentKey = optionalKey.get();

        try {
            tossPaymentClient.fetchPaymentInfo(paymentKey);
            log.info("복구 요청 성공 - CircuitBreaker 가 CLOSE 상태로 전환될 수 있음: paymentKey={}", paymentKey);
            CircuitBreakerMemoryStore.remove();

        } catch (HttpServerErrorException e) {
            log.warn("Toss 서버 측 문제로 복구 실패 - 재시도 필요: paymentKey={}, reason={}",
                    paymentKey, e.getMessage());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("유효 하지 않은 paymentKey - 복구 시도 무의미, 제거 처리: paymentKey={}, status={}",
                        paymentKey, e.getStatusCode());

                CircuitBreakerMemoryStore.remove();
            } else {
                log.error("예상치 못한 클라이언트 오류 발생: paymentKey={}, status={}, message={}",
                        paymentKey, e.getStatusCode(), e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("알 수 없는 오류 발생 - 복구 실패, 키 유지: paymentKey={}", paymentKey, e);
        }
    }
}