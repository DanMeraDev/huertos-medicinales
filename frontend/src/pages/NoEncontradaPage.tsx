import { Link } from 'react-router'
import { KintiGuia } from '../components/KintiGuia'
import './NoEncontradaPage.css'

export function NoEncontradaPage() {
  return (
    <section className="no-encontrada">
      <KintiGuia variante="vacio" tamano="lg" alt="" aria-hidden="true" />
      <h1 className="no-encontrada-titulo">Página no encontrada</h1>
      <p className="no-encontrada-texto">
        Kinti buscó por todo el huerto y no encontró esta página. Puede que el enlace esté mal escrito o
        que la página ya no exista.
      </p>
      <Link to="/" className="no-encontrada-enlace">
        Volver al inicio
      </Link>
    </section>
  )
}
