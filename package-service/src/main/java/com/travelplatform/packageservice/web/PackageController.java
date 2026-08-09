package com.travelplatform.packageservice.web;

import com.travelplatform.packageservice.domain.TravelPackage;
import com.travelplatform.packageservice.service.PackageService;
import com.travelplatform.packageservice.service.SagaOrchestrator;
import com.travelplatform.packageservice.web.dto.CreatePackageRequest;
import com.travelplatform.packageservice.web.dto.PackageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;
    private final PackageMapper packageMapper;
    private final SagaOrchestrator sagaOrchestrator;

    public PackageController(PackageService packageService, PackageMapper packageMapper, SagaOrchestrator sagaOrchestrator) {
        this.packageService = packageService;
        this.packageMapper = packageMapper;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    /**
     * Cria o pacote (status CREATED) e imediatamente dispara a saga, que passa
     * a rodar em background via RabbitMQ. A resposta é 202 Accepted — o cliente
     * consulta o progresso em GET /api/packages/{id}.
     */
    @PostMapping
    public ResponseEntity<PackageResponse> create(@Valid @RequestBody CreatePackageRequest request) {
        TravelPackage travelPackage = packageMapper.toDomain(request);
        TravelPackage saved = packageService.create(travelPackage);
        sagaOrchestrator.startSaga(saved);

        URI location = URI.create("/api/packages/" + saved.getId());
        return ResponseEntity.accepted().location(location).body(PackageResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> getById(@PathVariable UUID id) {
        TravelPackage travelPackage = packageService.findById(id);
        return ResponseEntity.ok(PackageResponse.from(travelPackage));
    }
}
