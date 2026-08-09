package com.travelplatform.packageservice.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * O cliente já escolheu as ofertas navegando em cada serviço de produto —
 * aqui só entra o offerId + a quantidade (passageiros/hóspedes/participantes).
 * Datas, preço, tipo de quarto etc. já estão na oferta, no serviço dono.
 */
public record CreatePackageRequest(
        @NotBlank String customerId,
        @Valid ItemRequest flight,
        @Valid ItemRequest hotel,
        @Valid ItemRequest car,
        @Valid ItemRequest tour,
        @NotNull @Valid PaymentRequest paymentMethod
) {

    public record ItemRequest(
            @NotBlank String offerId,
            @Min(1) int quantity
    ) {
    }

    public record PaymentRequest(
            @NotBlank String type,
            @Min(1) Integer installments
    ) {
    }
}
