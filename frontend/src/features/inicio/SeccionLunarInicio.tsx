import { Link } from 'react-router'
import { KintiGuia } from '../../components/KintiGuia'
import './SeccionLunarInicio.css'

export function SeccionLunarInicio() {
  return (
    <section className="lunar-inicio" aria-labelledby="lunar-inicio-titulo">
      <div className="lunar-inicio-contenedor">
        <div className="lunar-inicio-ilustracion">
          <KintiGuia variante="lunar" tamano="xl" alt="Kinti Lunar guiando el huerto" />
        </div>
        <div className="lunar-inicio-texto">
          <span className="lunar-inicio-eyebrow">Sabiduría ancestral y ciclos naturales</span>
          <h2 id="lunar-inicio-titulo" className="lunar-inicio-titulo">
            Calendario Lunar Agroecológico
          </h2>
          <p className="lunar-inicio-parrafo">
            Nuestros abuelos sabían que la luna guía la fuerza de la savia, las raíces y los
            aceites esenciales de cada planta medicinal. Con Kinti Lunar conoces en tiempo real la
            fase lunar actual —Luna Nueva, Cuarto Creciente, Luna Llena o Cuarto Menguante— y las
            prácticas recomendadas para cada día.
          </p>
          <p className="lunar-inicio-parrafo">
            Aprende cuándo sembrar, abonar, podar o cosechar respetando los ritmos de la tierra para
            obtener cultivos sanos, vigorosos y llenos de vida.
          </p>
          <div className="lunar-inicio-acciones">
            <Link to="/calendario-lunar" className="lunar-inicio-boton">
              Consultar calendario lunar
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}
