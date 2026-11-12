import { Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
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
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
