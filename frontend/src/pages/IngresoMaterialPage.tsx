import { useNavigate } from 'react-router'
import { FormularioIngresoMaterial } from '../features/bodega/FormularioIngresoMaterial'

export function IngresoMaterialPage() {
  const navigate = useNavigate()

  return (
    <div className="pagina-ingreso-material">
      <FormularioIngresoMaterial onExito={() => navigate('/bodega/inventario')} />
    </div>
  )
}
