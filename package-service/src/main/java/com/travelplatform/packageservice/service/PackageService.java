package com.travelplatform.packageservice.service;

import com.travelplatform.packageservice.domain.TravelPackage;
import com.travelplatform.packageservice.exception.PackageNotFoundException;
import com.travelplatform.packageservice.repository.TravelPackageRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackageService {

    private final TravelPackageRepository repository;

    public PackageService(TravelPackageRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria o pacote em estado CREATED e persiste.
     * TODO (próxima etapa): publicar evento "PackageCreated" no RabbitMQ para
     * disparar a saga de reserva (voo/hotel/carro/passeio) de forma assíncrona.
     */
    @Transactional
    public TravelPackage create(TravelPackage travelPackage) {
        return repository.save(travelPackage);
    }

    @Transactional(readOnly = true)
    public TravelPackage findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
    }
}
