import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { IngresoMaterialPage } from './pages/IngresoMaterialPage'
import { InicioPage } from './pages/InicioPage'
import { InventarioBodegaPage } from './pages/InventarioBodegaPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/bodega" element={<Navigate to="/bodega/inventario" replace />} />
        <Route path="/bodega/ingreso-material" element={<IngresoMaterialPage />} />
        <Route path="/bodega/inventario" element={<InventarioBodegaPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
