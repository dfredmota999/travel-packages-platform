package com.travelplatform.packageservice.domain;

/**
 * Status individual de cada item que compõe o pacote (voo, hotel, carro, passeio).
 * Cada item é reservado de forma independente pelos respectivos microsserviços
 * de produto, e o package-service acompanha esse status via mensageria.
 */
public enum BookingItemStatus {
    NOT_REQUESTED, // item ainda não incluído no pacote (ex: cliente não pediu carro)
    PENDING,       // comando de reserva enviado, aguardando confirmação
    CONFIRMED,     // reserva confirmada pelo serviço de produto
    REJECTED,      // serviço de produto recusou (ex: sem disponibilidade)
    CANCELLED      // reserva cancelada como parte de uma compensação da saga
}
