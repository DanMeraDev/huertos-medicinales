import { CatalogoPlantasGrid } from '../features/catalogo-plantas/CatalogoPlantasGrid'
import './CatalogoPlantasPage.css'

export function CatalogoPlantasPage() {
  return (
    <div className="catalogo-pagina">
      <header className="catalogo-pagina-cabecera">
        <h1>Catálogo de Plantas</h1>
      </header>
      <div className="catalogo-pagina-cuerpo">
        <div className="catalogo-pagina-contenido">
          <CatalogoPlantasGrid />
        </div>
      </div>
    </div>
  )
}
