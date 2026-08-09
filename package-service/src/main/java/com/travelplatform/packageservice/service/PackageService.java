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

    @Transactional
    public TravelPackage create(TravelPackage travelPackage) {
        return repository.save(travelPackage);
    }

    @Transactional(readOnly = true)
    public TravelPackage findById(UUID id) {
        return repository.findWithItemsById(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
    }

    /** Usado pelo SagaOrchestrator: carrega, aplica a mudança de domínio (via callback) e salva na mesma transação. */
    @Transactional
    public TravelPackage update(UUID id, java.util.function.Consumer<TravelPackage> mutation) {
        TravelPackage travelPackage = repository.findWithItemsById(id)
                .orElseThrow(() -> new PackageNotFoundException(id));
        mutation.accept(travelPackage);
        return repository.save(travelPackage);
    }
}
