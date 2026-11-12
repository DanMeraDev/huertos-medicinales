import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CambiarClavePage } from './pages/CambiarClavePage'
import { CrearUsuarioPage } from './pages/CrearUsuarioPage'
import { CalendarioLunarPage } from './pages/CalendarioLunarPage'
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
import { SaberesAncestralesPage } from './pages/SaberesAncestralesPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/comunidades" element={<DirectorioComunidadesPage />} />
        <Route path="/catalogo-plantas" element={<CatalogoPlantasPage />} />
        <Route path="/catalogo-plantas/:id" element={<DetallePlantaPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cambiar-clave" element={<CambiarClavePage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="/huerto/configuracion" element={<ConfiguracionHuertoPage />} />
        <Route path="/usuarios/nuevo" element={<CrearUsuarioPage />} />
        <Route path="/solicitar-comunidad" element={<SolicitarComunidadPage />} />
        <Route path="/ranking/comunidades" element={<RankingComunidadesPage />} />
        <Route path="/comunidad/miembros/nuevo" element={<RegistrarMiembroPage />} />
        <Route path="/calendario-lunar" element={<CalendarioLunarPage />} />
        <Route path="/bodega" element={<Navigate to="/bodega/ingreso-material" replace />} />
        <Route path="/bodega/ingreso-material" element={<IngresoMaterialPage />} />
        <Route path="/bodega/inventario" element={<InventarioBodegaPage />} />
        <Route path="/saberes-ancestrales" element={<SaberesAncestralesPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}