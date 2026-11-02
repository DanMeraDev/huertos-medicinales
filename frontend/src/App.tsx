import { Route, Routes } from 'react-router'
import { InicioPage } from './pages/InicioPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<InicioPage />} />
      <Route path="*" element={<NoEncontradaPage />} />
    </Routes>
  )
}
