import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { IngresoMaterialPage } from './pages/IngresoMaterialPage'
import { InicioPage } from './pages/InicioPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/bodega" element={<Navigate to="/bodega/ingreso-material" replace />} />
        <Route path="/bodega/ingreso-material" element={<IngresoMaterialPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
