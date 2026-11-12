import { Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { CalendarioLunarPage } from './pages/CalendarioLunarPage'
import { InicioPage } from './pages/InicioPage'
import { NoEncontradaPage } from './pages/NoEncontradaPage'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/calendario-lunar" element={<CalendarioLunarPage />} />
        <Route path="*" element={<NoEncontradaPage />} />
      </Routes>
    </Layout>
  )
}
