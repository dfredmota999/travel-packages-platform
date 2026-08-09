package com.travelplatform.packageservice.web;

import com.travelplatform.packageservice.domain.TravelPackage;
import com.travelplatform.packageservice.service.PackageService;
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

    public PackageController(PackageService packageService, PackageMapper packageMapper) {
        this.packageService = packageService;
        this.packageMapper = packageMapper;
    }

    /**
     * Cria um pacote. A resposta é 202 Accepted porque a reserva efetiva dos
     * itens (voo/hotel/carro/passeio) roda de forma assíncrona via saga —
     * o cliente consulta o status depois em GET /api/packages/{id}.
     */
    @PostMapping
    public ResponseEntity<PackageResponse> create(@Valid @RequestBody CreatePackageRequest request) {
        TravelPackage travelPackage = packageMapper.toDomain(request);
        TravelPackage saved = packageService.create(travelPackage);

        URI location = URI.create("/api/packages/" + saved.getId());
        return ResponseEntity.accepted().location(location).body(PackageResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> getById(@PathVariable UUID id) {
        TravelPackage travelPackage = packageService.findById(id);
        return ResponseEntity.ok(PackageResponse.from(travelPackage));
    }
}
