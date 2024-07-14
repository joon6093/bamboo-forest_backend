package org.jungppo.bambooforest.dto.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateRequest {

	@NotEmpty(message = "Battery item name cannot be empty")
	@Size(max = 15, message = "Battery item name must be less than or equal to 15 characters")
	private String batteryItemName;
}
