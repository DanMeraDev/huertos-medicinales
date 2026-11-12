import { Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CatalogoPlantasPage } from './pages/CatalogoPlantasPage'
import { InicioPage } from './pages/InicioPage'
import { LoginPage } from './pages/LoginPage'
import { MiComunidadPage } from './pages/MiComunidadPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'
import { RegistrarMiembroPage } from './pages/RegistrarMiembroPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/catalogo-plantas" element={<CatalogoPlantasPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="/comunidad/miembros/nuevo" element={<RegistrarMiembroPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
