package tests;

import manager.PricelistManager;
import manager.RentalManager;
import manager.VehicleManager;
import model.*;
import model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RentalManagerTest {

    private FakeRentalRepository renRepo;
    private FakeReservationRepository rr;
    private FakeVehicleRepository vr;
    private RentalManager rentalMgr;

    private final LocalDate start = LocalDate.now();
    private final LocalDate end = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        renRepo = new FakeRentalRepository();
        rr = new FakeReservationRepository();
        vr = new FakeVehicleRepository();
        FakeVehicleModelRepository mr = new FakeVehicleModelRepository();
        FakeVehicleCategoryRepository cr = new FakeVehicleCategoryRepository();
        FakePricelistRepository plRepo = new FakePricelistRepository();

        VehicleManager vm = new VehicleManager(vr, mr, cr);
        rentalMgr = new RentalManager(renRepo, rr, vm, plRepo);

        new PricelistManager(plRepo).createPricelist(3, 6000, 500, 10, 15, 5, "adm");
        cr.save(new VehicleCategory("cat1", VehicleCategoryType.ECONOMY, 3000));
        mr.save(new VehicleModel("m1", "VW", "Golf", "cat1"));
        vr.save(new Vehicle("v1", "m1", "BG-1", VehicleStatus.AVAILABLE, 1000));
        rr.save(new Reservation("res1", "ok", "m1", start, end,
                ReservationStatus.CONFIRMED, 9000, "", LocalDateTime.now(), "ag1"));
    }

    @Test
    void izdavanjeMenjaStatusVozilaINeMozeSePonoviti() {
        rentalMgr.issueVehicle("res1", "v1", "ag1", 1000);
        assertEquals(VehicleStatus.RENTED, vr.findById("v1").get().getStatus());
        assertEquals(1, renRepo.findAll().size());
        assertThrows(IllegalStateException.class,
                () -> rentalMgr.issueVehicle("res1", "v1", "ag1", 1000));
    }

    @Test
    void izdavanjeSaDodatnimUslugamaPovecavaCenuIDodajeUsluge() {
        vr.save(new Vehicle("v2", "m1", "BG-2", VehicleStatus.AVAILABLE, 500));
        rr.save(new Reservation("res2", "ok", "m1", start, end,
                ReservationStatus.CONFIRMED, 9000, "svc-a", LocalDateTime.now(), "ag1"));
        rentalMgr.issueVehicle("res2", "v2", "ag1", 500, "svc-b,svc-c", 2500);
        Reservation after = rr.findById("res2").get();
        assertEquals(11500, after.getTotalPrice());
        assertEquals("svc-a,svc-b,svc-c", after.getAdditionalServiceIds());
    }

    @Test
    void vracanjeSaKasnjenjemRacunaKaznu() {
        Rental rental = rentalMgr.issueVehicle("res1", "v1", "ag1", 1000);
        Rental returned = rentalMgr.returnVehicle(rental.getId(), 1300, end.plusDays(2));
        // 2 dana * 500 RSD/dan = 1000
        assertEquals(1000.0, returned.getLateFee());
        assertEquals(VehicleStatus.AVAILABLE, vr.findById("v1").get().getStatus());
        assertEquals(1300, vr.findById("v1").get().getMileage());
    }

    @Test
    void dodatniDaniProduzavajuDatumVracanja() {
        LocalDate origEnd = end;
        Rental r = rentalMgr.issueVehicle("res1", "v1", "ag1", 1000, "", 0.0, 2);
        // datum vraćanja rezervacije pomeren za 2 dana
        assertEquals(origEnd.plusDays(2), rr.findById("res1").get().getEndDate());
        // izdavanje beleži produženi očekivani povratak
        assertEquals(origEnd.plusDays(2), r.getExpectedReturnDate());
    }

    @Test
    void vracanjeSaManjomKilometrazomBaca() {
        Rental rental = rentalMgr.issueVehicle("res1", "v1", "ag1", 1000);
        assertThrows(IllegalStateException.class,
                () -> rentalMgr.returnVehicle(rental.getId(), 500, end));
    }

    @Test
    void noShowOtkazujeNepreuzeteProtekleRezervacije() {
        rr.save(new Reservation("ns1", "ok", "m1",
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(1),
                ReservationStatus.CONFIRMED, 9000, "", LocalDateTime.now().minusDays(3), "ag1"));
        int cancelled = rentalMgr.expireNoShowReservations();
        assertTrue(cancelled >= 1);
        assertEquals(ReservationStatus.CANCELLED, rr.findById("ns1").get().getStatus());
        assertNotNull(rr.findById("ns1").get().getCancelledAt());
    }

    @Test
    void noShowNeDiraBuduceNiIzdateRezervacije() {
        rr.save(new Reservation("ns2", "ok", "m1",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(8),
                ReservationStatus.CONFIRMED, 9000, "", LocalDateTime.now(), "ag1"));
        rentalMgr.issueVehicle("res1", "v1", "ag1", 1000); // res1 je izdata
        rentalMgr.expireNoShowReservations();
        assertEquals(ReservationStatus.CONFIRMED, rr.findById("ns2").get().getStatus());
        assertEquals(ReservationStatus.CONFIRMED, rr.findById("res1").get().getStatus());
    }
}
