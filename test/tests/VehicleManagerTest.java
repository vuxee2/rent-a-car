package tests;

import manager.VehicleManager;
import model.Vehicle;
import model.VehicleCategory;
import model.VehicleModel;
import model.enums.VehicleCategoryType;
import model.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.InMemoryRepositories.*;

import static org.junit.jupiter.api.Assertions.*;

class VehicleManagerTest {

    private VehicleManager vm;

    @BeforeEach
    void setUp() {
        vm = new VehicleManager(new FakeVehicleRepository(),
                new FakeVehicleModelRepository(),
                new FakeVehicleCategoryRepository());
        vm.addVehicleCategory(new VehicleCategory("cat1", VehicleCategoryType.ECONOMY, 3000));
        vm.addVehicleCategory(new VehicleCategory("cat2", VehicleCategoryType.LUXURY, 9000));
        vm.addVehicleModel(new VehicleModel("m1", "VW", "Golf 7", "cat1"));
        vm.addVehicleModel(new VehicleModel("m2", "BMW", "Serija 5", "cat2"));
    }

    @Test
    void pretragaModelaPoNazivu() {
        assertEquals(1, vm.searchModels("golf", null).size());
    }

    @Test
    void pretragaModelaPoProizvodjacu() {
        assertEquals(1, vm.searchModels("bmw", null).size());
    }

    @Test
    void pretragaModelaPoKategoriji() {
        assertEquals(1, vm.searchModels(null, "cat1").size());
    }

    @Test
    void praznaPretragaVracaSve() {
        assertEquals(2, vm.searchModels("", null).size());
    }

    @Test
    void dostupniPrimerciModela() {
        vm.addVehicle(new Vehicle("v1", "m1", "BG-1", VehicleStatus.AVAILABLE, 100));
        vm.addVehicle(new Vehicle("v2", "m1", "BG-2", VehicleStatus.RENTED, 200));
        assertEquals(1, vm.getAvailableVehiclesForModel("m1").size());
    }

    @Test
    void brisanjeKategorijeUUpotrebiBaca() {
        assertThrows(IllegalStateException.class, () -> vm.deleteVehicleCategory("cat1"));
    }

    @Test
    void brisanjeNekorisceneKategorije() {
        vm.addVehicleCategory(new VehicleCategory("cat3", VehicleCategoryType.STANDARD, 4000));
        vm.deleteVehicleCategory("cat3");
        assertEquals(2, vm.getAllCategories().size());
    }
}
