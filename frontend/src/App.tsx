import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CalendarioLunarPage } from './pages/CalendarioLunarPage'
import { CatalogoPlantasPage } from './pages/CatalogoPlantasPage'
import { IngresoMaterialPage } from './pages/IngresoMaterialPage'
import { InicioPage } from './pages/InicioPage'
import { DirectorioComunidadesPage } from './pages/DirectorioComunidadesPage'
import { LoginPage } from './pages/LoginPage'
import { MiComunidadPage } from './pages/MiComunidadPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/comunidades" element={<DirectorioComunidadesPage />} />
        <Route path="/catalogo-plantas" element={<CatalogoPlantasPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="/calendario-lunar" element={<CalendarioLunarPage />} />
        <Route path="/bodega" element={<Navigate to="/bodega/ingreso-material" replace />} />
        <Route path="/bodega/ingreso-material" element={<IngresoMaterialPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
