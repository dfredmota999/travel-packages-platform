package com.travelplatform.packageservice.service;

import com.travelplatform.packageservice.domain.ItemType;
import com.travelplatform.packageservice.domain.PackageItem;
import com.travelplatform.packageservice.domain.TravelPackage;
import com.travelplatform.packageservice.messaging.SagaCommandPublisher;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * O cérebro da saga orquestrada. Reage a eventos vindos dos serviços de
 * produto e decide: avançar o pacote, ou compensar o que já foi confirmado.
 *
 * Ainda só conhece o flight-service — quando hotel/car/tour entrarem, o
 * padrão de onXReserved/onXRejected se repete, e compensate() já está pronto
 * pra cancelar qualquer item CONFIRMED, não só voo.
 */
@Service
public class SagaOrchestrator {

    private final PackageService packageService;
    private final SagaCommandPublisher commandPublisher;

    public SagaOrchestrator(PackageService packageService, SagaCommandPublisher commandPublisher) {
        this.packageService = packageService;
        this.commandPublisher = commandPublisher;
    }

    /** Chamado pelo PackageController logo após criar o pacote — dispara os comandos de reserva. */
    public void startSaga(TravelPackage travelPackage) {
        packageService.update(travelPackage.getId(), pkg -> {
            pkg.startProcessing();
            PackageItem flight = firstItemOfType(pkg, ItemType.FLIGHT);
            if (flight != null) {
                commandPublisher.publishReserveFlight(pkg, flight);
            }
            // TODO: mesma ideia pra hotel/car/tour quando estiverem ligados.
        });
    }

    public void onFlightReserved(UUID packageId, String reservationId) {
        packageService.update(packageId, pkg -> {
            pkg.confirmItem(ItemType.FLIGHT, reservationId);
            advanceIfAllConfirmed(pkg);
        });
    }

    public void onFlightRejected(UUID packageId) {
        packageService.update(packageId, pkg -> {
            pkg.rejectItem(ItemType.FLIGHT);
            compensate(pkg, ItemType.FLIGHT);
        });
    }

    public void onFlightCancelled(UUID packageId) {
        packageService.update(packageId, pkg -> {
            pkg.cancelItem(ItemType.FLIGHT);
            finalizeCancellationIfComplete(pkg);
        });
    }

    private void advanceIfAllConfirmed(TravelPackage pkg) {
        if (pkg.allItemsConfirmed()) {
            pkg.markAwaitingPayment();
            // TODO: publicar cmd.process-payment pro payment-service. Por enquanto,
            // simulamos aprovação automática pra fechar o ciclo da saga de ponta a ponta.
            pkg.confirm();
        }
    }

    /** Rejeição de um item -> compensa (cancela) todos os outros itens já CONFIRMED. */
    private void compensate(TravelPackage pkg, ItemType rejectedType) {
        pkg.startCompensating();
        var confirmedItems = pkg.confirmedItemsExcept(rejectedType);
        if (confirmedItems.isEmpty()) {
            pkg.cancel();
            return;
        }
        for (PackageItem item : confirmedItems) {
            if (item.getItemType() == ItemType.FLIGHT) {
                commandPublisher.publishCancelFlight(pkg, item.getReservationId());
            }
            // TODO: mesma ideia pra hotel/car/tour.
        }
    }

    private void finalizeCancellationIfComplete(TravelPackage pkg) {
        boolean stillWaitingCompensation = pkg.getItems().stream()
                .anyMatch(i -> i.getStatus() == com.travelplatform.packageservice.domain.BookingItemStatus.CONFIRMED);
        if (!stillWaitingCompensation) {
            pkg.cancel();
        }
    }

    private PackageItem firstItemOfType(TravelPackage pkg, ItemType type) {
        return pkg.getItems().stream().filter(i -> i.getItemType() == type).findFirst().orElse(null);
    }
}
