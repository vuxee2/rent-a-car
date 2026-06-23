package tests;

import manager.PricelistManager;
import manager.ReservationManager;
import manager.SubscriptionManager;
import manager.VehicleManager;
import model.*;
import model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationManagerTest {

    private FakeReservationRepository rr;
    private FakeVehicleRepository vr;
    private SubscriptionManager sm;
    private ReservationManager rm;

    private final LocalDate start = LocalDate.now().plusDays(1);
    private final LocalDate end = LocalDate.now().plusDays(4);

    @BeforeEach
    void setUp() {
        rr = new FakeReservationRepository();
        vr = new FakeVehicleRepository();
        FakeVehicleModelRepository mr = new FakeVehicleModelRepository();
        FakeVehicleCategoryRepository cr = new FakeVehicleCategoryRepository();
        FakePricelistRepository plRepo = new FakePricelistRepository();

        VehicleManager vm = new VehicleManager(vr, mr, cr);
        PricelistManager pm = new PricelistManager(plRepo);
        sm = new SubscriptionManager(new FakeSubscriptionRepository(), pm);
        rm = new ReservationManager(rr, vm, sm);

        pm.createPricelist(3, 6000, 5000, 10, 15, 5, "adm");
        cr.save(new VehicleCategory("cat1", VehicleCategoryType.ECONOMY, 3000));
        mr.save(new VehicleModel("m1", "VW", "Golf", "cat1"));
        vr.save(new Vehicle("v1", "m1", "BG-1", VehicleStatus.AVAILABLE, 0));
    }

    private Client client(String id, ClientCategory cat, int licenseYears) {
        return new Client(id, "C", "D", Gender.FEMALE, LocalDate.of(1995, 1, 1), "1", "a",
                "mail" + id, "p", LocalDate.now().minusYears(licenseYears), cat);
    }

    private void daClientImaPretplatu(String clientId) {
        Subscription sub = sm.requestSubscription(clientId);
        sm.approveSubscription(sub.getId(), "ag1", 0);
    }

    @Test
    void dozvolaKracaOdDveGodineSprecavaRezervaciju() {
        Client young = client("young", null, 1);
        assertThrows(IllegalStateException.class,
                () -> rm.createReservation(young, "m1", start, end, "", 9000));
    }

    @Test
    void bezAktivnePretplateSprecavaRezervaciju() {
        Client noSub = client("nosub", null, 3);
        assertThrows(IllegalStateException.class,
                () -> rm.createReservation(noSub, "m1", start, end, "", 9000));
    }

    @Test
    void uspesnaRezervacijaJePending() {
        Client ok = client("ok", ClientCategory.STUDENT, 5);
        daClientImaPretplatu("ok");
        Reservation res = rm.createReservation(ok, "m1", start, end, "", 9000);
        assertEquals(ReservationStatus.PENDING, res.getStatus());
    }

    @Test
    void potvrdaBezDostupnihVozilaBaca() {
        Client ok = client("ok", ClientCategory.STUDENT, 5);
        daClientImaPretplatu("ok");
        Reservation res = rm.createReservation(ok, "m1", start, end, "", 9000);
        // jedino vozilo zauzeto drugom potvrdjenom preklapajucom rezervacijom
        rr.save(new Reservation("r-other", "ok", "m1", start, end,
                ReservationStatus.CONFIRMED, 9000, "", LocalDateTime.now(), "ag1"));
        assertThrows(IllegalStateException.class, () -> rm.confirmReservation(res.getId(), "ag1"));
    }

    @Test
    void potvrdaPostavljaAgentaIStatus() {
        Client ok = client("ok", ClientCategory.STUDENT, 5);
        daClientImaPretplatu("ok");
        Reservation res = rm.createReservation(ok, "m1", start, end, "", 9000);
        rm.confirmReservation(res.getId(), "ag2");
        Reservation confirmed = rr.findById(res.getId()).get();
        assertEquals(ReservationStatus.CONFIRMED, confirmed.getStatus());
        assertEquals("ag2", confirmed.getAgentId());
    }

    @Test
    void otkazivanjeBeleziVremeIPokrece24hZabranu() {
        Client ok = client("ok", ClientCategory.STUDENT, 5);
        daClientImaPretplatu("ok");
        Reservation res = rm.createReservation(ok, "m1", start, end, "", 9000);
        rm.cancelReservation(res.getId());
        assertNotNull(rr.findById(res.getId()).get().getCancelledAt());
        assertFalse(rm.canClientReserveNow("ok"));
    }

    @Test
    void posle24hKlijentMozePonovo() {
        Client ok = client("ok", ClientCategory.STUDENT, 5);
        daClientImaPretplatu("ok");
        Reservation res = rm.createReservation(ok, "m1", start, end, "", 9000);
        rm.cancelReservation(res.getId());
        Reservation cancelled = rr.findById(res.getId()).get();
        cancelled.setCancelledAt(LocalDateTime.now().minusHours(30));
        rr.save(cancelled);
        assertTrue(rm.canClientReserveNow("ok"));
    }

    @Test
    void istekleNeobradjenePendingRezervacijeSeOdbijaju() {
        rr.save(new Reservation("r-od", "ok", "m1",
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(1),
                ReservationStatus.PENDING, 9000, "", LocalDateTime.now().minusDays(3), null));
        rm.expireOverdueReservations();
        assertEquals(ReservationStatus.REJECTED, rr.findById("r-od").get().getStatus());
    }
}
