import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CambiarClavePage } from './pages/CambiarClavePage'
import { CrearUsuarioPage } from './pages/CrearUsuarioPage'
import { CalendarioLunarPage } from './pages/CalendarioLunarPage'
import { AdminCrearPlantaPage } from './pages/AdminCrearPlantaPage'
import { AdminEditarPlantaPage } from './pages/AdminEditarPlantaPage'
import { CatalogoPlantasPage } from './pages/CatalogoPlantasPage'
import { ConfiguracionHuertoPage } from './pages/ConfiguracionHuertoPage'
import { DetallePlantaPage } from './pages/DetallePlantaPage'
import { IngresoMaterialPage } from './pages/IngresoMaterialPage'
import { InicioPage } from './pages/InicioPage'
import { InventarioBodegaPage } from './pages/InventarioBodegaPage'
import { DirectorioComunidadesPage } from './pages/DirectorioComunidadesPage'
import { LoginPage } from './pages/LoginPage'
import { MiComunidadPage } from './pages/MiComunidadPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'
import { SolicitarComunidadPage } from './pages/SolicitarComunidadPage'
import { RankingComunidadesPage } from './pages/RankingComunidadesPage'
import { RegistrarMiembroPage } from './pages/RegistrarMiembroPage'
import { MiembrosComunidadPage } from './pages/MiembrosComunidadPage'
import { SolicitudesPage } from './pages/SolicitudesPage'
import { SaberesAncestralesPage } from './pages/SaberesAncestralesPage'
import { SubhuertosPage } from './pages/SubhuertosPage'
import { CatalogoActividadesPage } from './pages/CatalogoActividadesPage'
import { RegistroSiembraPage } from './pages/RegistroSiembraPage'
import { NuevaActividadAncestralPage } from './pages/NuevaActividadAncestralPage'
import { RegistroLaborMiembroPage } from './pages/RegistroLaborMiembroPage'
import { SubsanarEvidenciaPage } from './pages/SubsanarEvidenciaPage'
export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/comunidades" element={<DirectorioComunidadesPage />} />
        <Route path="/catalogo-plantas" element={<CatalogoPlantasPage />} />
        <Route path="/catalogo-plantas/:id" element={<DetallePlantaPage />} />
        <Route path="/admin/plantas/nueva" element={<AdminCrearPlantaPage />} />
        <Route path="/admin/plantas/:id/editar" element={<AdminEditarPlantaPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cambiar-clave" element={<CambiarClavePage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="/huerto/configuracion" element={<ConfiguracionHuertoPage />} />
        <Route path="/usuarios/nuevo" element={<CrearUsuarioPage />} />
        <Route path="/solicitar-comunidad" element={<SolicitarComunidadPage />} />
        <Route path="/ranking/comunidades" element={<RankingComunidadesPage />} />
        <Route path="/comunidad/miembros" element={<MiembrosComunidadPage />} />
        <Route path="/comunidad/miembros/nuevo" element={<RegistrarMiembroPage />} />
        <Route path="/calendario-lunar" element={<CalendarioLunarPage />} />
        <Route path="/bodega" element={<Navigate to="/bodega/ingreso-material" replace />} />
        <Route path="/bodega/ingreso-material" element={<IngresoMaterialPage />} />
        <Route path="/bodega/inventario" element={<InventarioBodegaPage />} />
        <Route path="/saberes-ancestrales" element={<SaberesAncestralesPage />} />
        <Route path="/solicitudes" element={<SolicitudesPage />} />
        <Route path="/huerto/subhuertos" element={<SubhuertosPage />} />
        <Route path="/huerto/siembras/nueva" element={<RegistroSiembraPage />} />
        <Route path="/comunidad/actividades-catalogo" element={<CatalogoActividadesPage />} />
        <Route path="/comunidad/actividades-miembros/nueva" element={<RegistroLaborMiembroPage />} />
        <Route path="/actividades-ancestrales/nueva" element={<NuevaActividadAncestralPage />} />
        <Route path="/actividades-ancestrales/:id/subsanar" element={<SubsanarEvidenciaPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
