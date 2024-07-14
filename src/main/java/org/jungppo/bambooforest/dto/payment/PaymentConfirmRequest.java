package org.jungppo.bambooforest.dto.payment;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmRequest {

	@NotBlank(message = "Payment key cannot be blank")
	@Size(max = 200, message = "Payment key must be less than or equal to 200 characters")
	private String paymentKey;
	
	@NotNull(message = "Order ID cannot be null")
	private UUID orderId;

	@NotNull(message = "Amount cannot be null")
	@Min(value = 1, message = "Amount must be greater than zero")
	private BigDecimal amount;
}
