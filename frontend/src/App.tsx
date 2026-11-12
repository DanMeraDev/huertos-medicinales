import { Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { InicioPage } from './pages/InicioPage'
import { MiComunidadPage } from './pages/MiComunidadPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/mi-comunidad" element={<MiComunidadPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
