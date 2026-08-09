package com.travelplatform.packageservice.web;

import com.travelplatform.packageservice.domain.CarRentalBooking;
import com.travelplatform.packageservice.domain.FlightBooking;
import com.travelplatform.packageservice.domain.HotelBooking;
import com.travelplatform.packageservice.domain.PaymentInfo;
import com.travelplatform.packageservice.domain.PaymentMethodType;
import com.travelplatform.packageservice.domain.TourBooking;
import com.travelplatform.packageservice.domain.TravelPackage;
import com.travelplatform.packageservice.web.dto.CreatePackageRequest;
import org.springframework.stereotype.Component;

@Component
public class PackageMapper {

    public TravelPackage toDomain(CreatePackageRequest request) {
        FlightBooking flight = request.flight() == null ? null : new FlightBooking(
                request.flight().offerId(),
                request.flight().origin(),
                request.flight().destination(),
                request.flight().departureDate(),
                request.flight().returnDate(),
                request.flight().passengers()
        );

        HotelBooking hotel = request.hotel() == null ? null : new HotelBooking(
                request.hotel().offerId(),
                request.hotel().checkIn(),
                request.hotel().checkOut(),
                request.hotel().roomType(),
                request.hotel().guests()
        );

        CarRentalBooking carRental = request.carRental() == null ? null : new CarRentalBooking(
                request.carRental().offerId(),
                request.carRental().pickupDate(),
                request.carRental().returnDate(),
                request.carRental().category()
        );

        TourBooking tour = request.tour() == null ? null : new TourBooking(
                request.tour().offerId(),
                request.tour().date(),
                request.tour().participants()
        );

        PaymentInfo payment = new PaymentInfo(
                PaymentMethodType.valueOf(request.paymentMethod().type()),
                request.paymentMethod().installments()
        );

        return TravelPackage.create(request.customerId(), flight, hotel, carRental, tour, payment);
    }
}
