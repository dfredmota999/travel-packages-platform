package com.travelplatform.packageservice.domain;

/**
 * Estado do agregado TravelPackage durante o ciclo de vida da compra.
 * A transição entre esses estados é conduzida pela saga (orquestração),
 * que vamos implementar na próxima etapa via RabbitMQ.
 */
public enum PackageStatus {
    CREATED,        // pacote criado, saga ainda não iniciada
    PROCESSING,      // saga em andamento, reservando voo/hotel/carro/passeio
    AWAITING_PAYMENT, // todos os itens reservados, aguardando pagamento
    CONFIRMED,       // pagamento aprovado, pacote confirmado
    COMPENSATING,     // falha em algum item, executando compensação (cancelamentos)
    CANCELLED,        // pacote cancelado (falha ou pedido do cliente)
    FAILED            // saga não conseguiu completar nem compensar totalmente
}
