import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CrearUsuarioPage } from './pages/CrearUsuarioPage'
import { CalendarioLunarPage } from './pages/CalendarioLunarPage'
import { CatalogoPlantasPage } from './pages/CatalogoPlantasPage'
import { IngresoMaterialPage } from './pages/IngresoMaterialPage'
import { InicioPage } from './pages/InicioPage'
import { DirectorioComunidadesPage } from './pages/DirectorioComunidadesPage'
import { LoginPage } from './pages/LoginPage'
import { MiComunidadPage } from './pages/MiComunidadPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'
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
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="/usuarios/nuevo" element={<CrearUsuarioPage />} />
        <Route path="/ranking/comunidades" element={<RankingComunidadesPage />} />
        <Route path="/comunidad/miembros/nuevo" element={<RegistrarMiembroPage />} />
        <Route path="/calendario-lunar" element={<CalendarioLunarPage />} />
        <Route path="/bodega" element={<Navigate to="/bodega/ingreso-material" replace />} />
        <Route path="/bodega/ingreso-material" element={<IngresoMaterialPage />} />
        <Route path="/saberes-ancestrales" element={<SaberesAncestralesPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
