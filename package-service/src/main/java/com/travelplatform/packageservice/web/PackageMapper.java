package com.travelplatform.packageservice.web;

import com.travelplatform.packageservice.domain.ItemType;
import com.travelplatform.packageservice.domain.PackageItem;
import com.travelplatform.packageservice.domain.PaymentInfo;
import com.travelplatform.packageservice.domain.PaymentMethodType;
import com.travelplatform.packageservice.domain.TravelPackage;
import com.travelplatform.packageservice.web.dto.CreatePackageRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PackageMapper {

    public TravelPackage toDomain(CreatePackageRequest request) {
        List<PackageItem> items = new ArrayList<>();
        addIfPresent(items, ItemType.FLIGHT, request.flight());
        addIfPresent(items, ItemType.HOTEL, request.hotel());
        addIfPresent(items, ItemType.CAR, request.car());
        addIfPresent(items, ItemType.TOUR, request.tour());

        PaymentInfo payment = new PaymentInfo(
                PaymentMethodType.valueOf(request.paymentMethod().type()),
                request.paymentMethod().installments());

        return TravelPackage.create(request.customerId(), items, payment);
    }

    private void addIfPresent(List<PackageItem> items, ItemType type, CreatePackageRequest.ItemRequest req) {
        if (req != null) {
            items.add(new PackageItem(type, req.offerId(), req.quantity()));
        }
    }
}
