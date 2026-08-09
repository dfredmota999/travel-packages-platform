package com.travelplatform.packageservice.exception;

import java.util.UUID;

public class PackageNotFoundException extends RuntimeException {

    public PackageNotFoundException(UUID id) {
        super("Pacote não encontrado: " + id);
    }
}
