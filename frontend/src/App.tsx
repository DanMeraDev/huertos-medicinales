import { Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CalendarioLunarPage } from './pages/CalendarioLunarPage'
import { CatalogoPlantasPage } from './pages/CatalogoPlantasPage'
import { ConfiguracionHuertoPage } from './pages/ConfiguracionHuertoPage'
import { InicioPage } from './pages/InicioPage'
import { LoginPage } from './pages/LoginPage'
import { MiComunidadPage } from './pages/MiComunidadPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/catalogo-plantas" element={<CatalogoPlantasPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="/huerto/configuracion" element={<ConfiguracionHuertoPage />} />
        <Route path="/calendario-lunar" element={<CalendarioLunarPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}