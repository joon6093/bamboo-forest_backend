package org.jungppo.bambooforest.payment.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.jungppo.bambooforest.battery.domain.BatteryItem;
import org.jungppo.bambooforest.client.paymentgateway.PaymentGatewayClient;
import org.jungppo.bambooforest.dto.paymentgateway.PaymentResponse;
import org.jungppo.bambooforest.dto.paymentgateway.toss.TossPaymentRequest;
import org.jungppo.bambooforest.member.domain.MemberEntity;
import org.jungppo.bambooforest.member.dto.PaymentConfirmRequest;
import org.jungppo.bambooforest.member.dto.PaymentDto;
import org.jungppo.bambooforest.member.dto.PaymentSetupRequest;
import org.jungppo.bambooforest.member.dto.PaymentSetupResponse;
import org.jungppo.bambooforest.payment.domain.PaymentEntity;
import org.jungppo.bambooforest.payment.domain.PaymentStatusType;
import org.jungppo.bambooforest.repository.member.MemberRepository;
import org.jungppo.bambooforest.repository.payment.PaymentRepository;
import org.jungppo.bambooforest.response.exception.battery.BatteryNotFoundException;
import org.jungppo.bambooforest.response.exception.member.MemberNotFoundException;
import org.jungppo.bambooforest.response.exception.payment.PaymentFailureException;
import org.jungppo.bambooforest.response.exception.payment.PaymentNotFoundException;
import org.jungppo.bambooforest.security.oauth2.CustomOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    @Transactional
    public PaymentSetupResponse setupPayment(PaymentSetupRequest paymentSetupRequest,
                                             CustomOAuth2User customOAuth2User) {
        BatteryItem batteryItem = BatteryItem.findByName(paymentSetupRequest.getBatteryItemName())
                .orElseThrow(BatteryNotFoundException::new);
        MemberEntity memberEntity = memberRepository.findById(customOAuth2User.getId())
                .orElseThrow(MemberNotFoundException::new);

        PaymentEntity paymentEntity = paymentRepository.save(PaymentEntity.builder()
                .batteryItem(batteryItem)
                .status(PaymentStatusType.PENDING)
                .member(memberEntity)
                .build());

        return new PaymentSetupResponse(paymentEntity.getId(), paymentEntity.getBatteryItem().getPrice());
    }

    @Transactional
    public PaymentDto confirmPayment(PaymentConfirmRequest paymentConfirmRequest) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentConfirmRequest.getOrderId())
                .orElseThrow(PaymentNotFoundException::new);

        validatePaymentAmount(paymentConfirmRequest, paymentEntity);
        processPayment(paymentConfirmRequest, paymentEntity);

        return new PaymentDto(paymentEntity.getId(), paymentEntity.getStatus(), paymentEntity.getProvider(),
                paymentEntity.getAmount(), paymentEntity.getCreatedAt()
        );
    }

    private void validatePaymentAmount(PaymentConfirmRequest request, PaymentEntity paymentEntity) {
        BigDecimal requestedAmount = request.getAmount();
        BigDecimal itemPrice = paymentEntity.getBatteryItem().getPrice();

        if (requestedAmount.compareTo(itemPrice) != 0) {
            throw new PaymentFailureException();
        }
    }

    private void processPayment(PaymentConfirmRequest request, PaymentEntity paymentEntity) {
        TossPaymentRequest tossPaymentRequest = new TossPaymentRequest(
                request.getPaymentKey(), request.getOrderId(), request.getAmount());

        PaymentResponse paymentResponse = paymentGatewayClient.payment(tossPaymentRequest)
                .getData()
                .orElseThrow(PaymentFailureException::new);
        paymentEntity.updatePaymentDetails(paymentResponse.getKey(), paymentResponse.getProvider(),
                paymentResponse.getAmount());
        paymentEntity.updatePaymentStatus(PaymentStatusType.COMPLETED);
        paymentEntity.getMember().addBatteries(paymentEntity.getBatteryItem().getCount());
    }
}
