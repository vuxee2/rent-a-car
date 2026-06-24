package tests;

import model.*;
import repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public final class InMemoryRepositories {

    private InMemoryRepositories() {}

    static class Store<T> {
        final List<T> items = new ArrayList<>();
        final Function<T, String> id;
        Store(Function<T, String> id) { this.id = id; }
        List<T> all() { return new ArrayList<>(items); }
        void put(T item) { items.removeIf(x -> id.apply(x).equals(id.apply(item))); items.add(item); }
        void remove(String key) { items.removeIf(x -> id.apply(x).equals(key)); }
    }

    public static class FakeUserRepository extends UserRepository {
        private final Store<User> s = new Store<>(User::getId);
        @Override public List<User> findAll() { return s.all(); }
        @Override public void save(User u) { s.put(u); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakeVehicleRepository extends VehicleRepository {
        private final Store<Vehicle> s = new Store<>(Vehicle::getId);
        @Override public List<Vehicle> findAll() { return s.all(); }
        @Override public void save(Vehicle v) { s.put(v); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakeVehicleModelRepository extends VehicleModelRepository {
        private final Store<VehicleModel> s = new Store<>(VehicleModel::getId);
        @Override public List<VehicleModel> findAll() { return s.all(); }
        @Override public void save(VehicleModel m) { s.put(m); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakeVehicleCategoryRepository extends VehicleCategoryRepository {
        private final Store<VehicleCategory> s = new Store<>(VehicleCategory::getId);
        @Override public List<VehicleCategory> findAll() { return s.all(); }
        @Override public void save(VehicleCategory c) { s.put(c); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakeReservationRepository extends ReservationRepository {
        private final Store<Reservation> s = new Store<>(Reservation::getId);
        @Override public List<Reservation> findAll() { return s.all(); }
        @Override public void save(Reservation r) { s.put(r); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakeRentalRepository extends RentalRepository {
        private final Store<Rental> s = new Store<>(Rental::getId);
        @Override public List<Rental> findAll() { return s.all(); }
        @Override public void save(Rental r) { s.put(r); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakeSubscriptionRepository extends SubscriptionRepository {
        private final Store<Subscription> s = new Store<>(Subscription::getId);
        @Override public List<Subscription> findAll() { return s.all(); }
        @Override public void save(Subscription sub) { s.put(sub); }
        @Override public void delete(String id) { s.remove(id); }
    }

    public static class FakePricelistRepository extends PricelistRepository {
        private final Store<Pricelist> s = new Store<>(Pricelist::getId);
        @Override public List<Pricelist> findAll() { return s.all(); }
        @Override public void save(Pricelist p) { s.put(p); }
    }

    public static class FakeAdditionalServiceRepository extends AdditionalServiceRepository {
        private final Store<AdditionalService> s = new Store<>(AdditionalService::getId);
        @Override public List<AdditionalService> findAll() { return s.all(); }
        @Override public void save(AdditionalService a) { s.put(a); }
        @Override public void delete(String id) { s.remove(id); }
    }
}
