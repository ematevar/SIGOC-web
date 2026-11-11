package com.sigoc.sigoc_web.aplication.usecases;

import com.sigoc.sigoc_web.domain.exceptions.AccesoDenegadoException;
import com.sigoc.sigoc_web.domain.exceptions.ReglaNegocioException;
import com.sigoc.sigoc_web.domain.models.*;
import com.sigoc.sigoc_web.domain.ports.out.MaterialPort;
import com.sigoc.sigoc_web.domain.ports.out.ProveedorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MaterialesProveedoresTest {

    private MaterialesFake materialPort;
    private ProveedoresFake proveedorPort;

    private final UsuarioSesion admin = new UsuarioSesion(1, "admin", "Admin", RolSistema.ADMIN, false);
    private final UsuarioSesion residente = new UsuarioSesion(2, "res", "Residente", RolSistema.RESIDENTE, false);
    private final UsuarioSesion almacenero = new UsuarioSesion(3, "alm", "Almacenero", RolSistema.ALMACENERO, false);

    @BeforeEach
    void setUp() {
        materialPort = new MaterialesFake();
        proveedorPort = new ProveedoresFake();
    }

    // ---------- RF-06 Materiales ----------

    @Test
    void guardarMaterialSoloAdminOAlmaceneroYPreservaStockActual() {
        GuardarMaterial uc = new GuardarMaterial(materialPort);
        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(residente, null, "Cemento", "bolsa", new BigDecimal("20"), new BigDecimal("30")));
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(almacenero, null, "Cemento", "bolsa", new BigDecimal("-1"), new BigDecimal("30")));

        Material m = uc.ejecutar(almacenero, null, " Cemento ", "bolsa", new BigDecimal("20"), new BigDecimal("30.555"));
        assertEquals(new BigDecimal("0.00"), m.getStockActual());
        assertEquals(new BigDecimal("30.56"), m.getCostoUnitarioRef());
        assertTrue(m.isStockEnAlerta());

        // Simular compra que aumenta stockActual
        m.setStockActual(new BigDecimal("50.00"));
        Material editado = uc.ejecutar(admin, m.getIdMaterial(), "Cemento I", "bolsa",
                new BigDecimal("25"), new BigDecimal("31"));
        assertEquals(new BigDecimal("50.00"), editado.getStockActual());
        assertFalse(editado.isStockEnAlerta());
    }

    @Test
    void eliminarMaterialBloqueaSiTieneStockYListaAlertas() {
        Material m1 = new GuardarMaterial(materialPort).ejecutar(almacenero, null, "Cemento", "bolsa",
                new BigDecimal("20"), new BigDecimal("30"));
        m1.setStockActual(new BigDecimal("20.00"));
        Material m2 = new GuardarMaterial(materialPort).ejecutar(almacenero, null, "Arena", "m3",
                new BigDecimal("5"), new BigDecimal("55"));
        m2.setStockActual(new BigDecimal("10.00"));

        assertEquals(1, new ListarAlertasStock(materialPort).ejecutar(almacenero).size());

        EliminarMaterial eliminar = new EliminarMaterial(materialPort);
        assertThrows(ReglaNegocioException.class, () -> eliminar.ejecutar(almacenero, m1.getIdMaterial()));

        m1.setStockActual(BigDecimal.ZERO);
        eliminar.ejecutar(admin, m1.getIdMaterial());
        assertFalse(materialPort.findById(m1.getIdMaterial()).orElseThrow().isActivo());
    }

    // ---------- RF-09 Proveedores ----------

    @Test
    void guardarProveedorSoloAdminYValidaRucUnico() {
        GuardarProveedor uc = new GuardarProveedor(proveedorPort);
        assertThrows(AccesoDenegadoException.class,
                () -> uc.ejecutar(almacenero, null, "Aceros SAC", "20123456789", null, null));
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(admin, null, "Aceros SAC", "12345", null, null));
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(admin, null, "Aceros SAC", "2012345678A", null, null));

        Proveedor p = uc.ejecutar(admin, null, " Aceros SAC ", "20123456789", "999888777", "Av. Lima 100");
        assertEquals("Aceros SAC", p.getRazonSocial());
        assertThrows(ReglaNegocioException.class,
                () -> uc.ejecutar(admin, null, "Otro", "20123456789", null, null));

        // Editar el mismo proveedor manteniendo su RUC sí está permitido
        Proveedor editado = uc.ejecutar(admin, p.getIdProveedor(), "Aceros del Sur SAC", "20123456789", null, null);
        assertEquals("Aceros del Sur SAC", editado.getRazonSocial());

        new EliminarProveedor(proveedorPort).ejecutar(admin, p.getIdProveedor());
        assertFalse(proveedorPort.findById(p.getIdProveedor()).orElseThrow().isActivo());
    }

    private static final class MaterialesFake implements MaterialPort {
        final Map<Integer, Material> datos = new LinkedHashMap<>();
        int seq = 1;

        @Override
        public List<Material> findAllActivos() {
            return datos.values().stream().filter(Material::isActivo).toList();
        }

        @Override
        public List<Material> findAlertasStockMinimo() {
            return datos.values().stream().filter(m -> m.isActivo() && m.isStockEnAlerta()).toList();
        }

        @Override
        public Optional<Material> findById(Integer idMaterial) {
            return Optional.ofNullable(datos.get(idMaterial));
        }

        @Override
        public Material guardar(Material material) {
            if (material.getIdMaterial() == null) {
                material.setIdMaterial(seq++);
            }
            datos.put(material.getIdMaterial(), material);
            return material;
        }

        @Override
        public void desactivar(Integer idMaterial) {
            datos.get(idMaterial).setActivo(false);
        }
    }

    private static final class ProveedoresFake implements ProveedorPort {
        final Map<Integer, Proveedor> datos = new LinkedHashMap<>();
        int seq = 1;

        @Override
        public List<Proveedor> findAllActivos() {
            return datos.values().stream().filter(Proveedor::isActivo).toList();
        }

        @Override
        public Optional<Proveedor> findById(Integer idProveedor) {
            return Optional.ofNullable(datos.get(idProveedor));
        }

        @Override
        public boolean existsByRuc(String ruc, Integer idExcluir) {
            return datos.values().stream()
                    .anyMatch(p -> p.getRuc().equals(ruc)
                            && (idExcluir == null || !Objects.equals(p.getIdProveedor(), idExcluir)));
        }

        @Override
        public Proveedor guardar(Proveedor proveedor) {
            if (proveedor.getIdProveedor() == null) {
                proveedor.setIdProveedor(seq++);
            }
            datos.put(proveedor.getIdProveedor(), proveedor);
            return proveedor;
        }

        @Override
        public void desactivar(Integer idProveedor) {
            datos.get(idProveedor).setActivo(false);
        }
    }
}
