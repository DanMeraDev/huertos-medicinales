import { KintiGuia, type KintiVariante } from '../../components/KintiGuia'
import './NivelesKinti.css'

interface NivelKinti {
  codigo: 'APRENDIZ' | 'OFICIAL' | 'MAESTRO'
  etiqueta: string
  rango: string
  descripcion: string
  variante: KintiVariante
  clase: string
}

const NIVELES_KINTI: NivelKinti[] = [
  {
    codigo: 'APRENDIZ',
    etiqueta: 'Nivel 1 · Semilla inicial',
    rango: '0 a 49 puntos',
    descripcion:
      'Das tus primeros pasos en el huerto comunitario: aprendes a preparar camas, sembrar plantas medicinales y reconocer sus propiedades curativas básicas.',
    variante: 'sabiduria',
    clase: 'nivel-kinti--aprendiz',
  },
  {
    codigo: 'OFICIAL',
    etiqueta: 'Nivel 2 · Brote constante',
    rango: '50 a 149 puntos',
    descripcion:
      'Participas activamente en el cuidado, cosecha y empaque solidario de plantas gratuitas para el MSP, fortaleciendo la salud de tu comunidad.',
    variante: 'celebracion',
    clase: 'nivel-kinti--oficial',
  },
  {
    codigo: 'MAESTRO',
    etiqueta: 'Nivel 3 · Guardián del saber',
    rango: '150 puntos o más',
    descripcion:
      'Lideras jornadas territoriales, transmites la sabiduría ancestral a nuevas generaciones e impulsas a tu comunidad a lo más alto del ranking.',
    variante: 'dorado',
    clase: 'nivel-kinti--maestro',
  },
]

export function NivelesKinti() {
  return (
    <section className="niveles-kinti" aria-labelledby="niveles-kinti-titulo">
      <div className="niveles-kinti-cabecera">
        <span className="niveles-kinti-eyebrow">Aprende jugando y creciendo en comunidad</span>
        <h2 id="niveles-kinti-titulo" className="niveles-kinti-titulo">
          Cada labor en la tierra te hace subir de nivel con Kinti
        </h2>
        <p className="niveles-kinti-intro">
          Al registrar actividades en el huerto, empaques solidarios y saberes ancestrales, cada
          voluntario suma puntos y avanza en su camino. No es un sistema de trámites ni papeles:
          participa, mantén activo tu huerto y crece junto a tu comunidad.
        </p>
      </div>

      <div className="niveles-kinti-lista">
        {NIVELES_KINTI.map((nivel) => (
          <article key={nivel.codigo} className={`nivel-kinti-tarjeta ${nivel.clase}`}>
            <span className="nivel-kinti-etiqueta">{nivel.etiqueta}</span>
            <div className="nivel-kinti-imagen">
              <KintiGuia
                variante={nivel.variante}
                tamano="lg"
                alt={`Kinti en nivel ${nivel.codigo}`}
              />
            </div>
            <h3 className="nivel-kinti-nombre">{nivel.codigo}</h3>
            <p className="nivel-kinti-rango">{nivel.rango}</p>
            <p className="nivel-kinti-descripcion">{nivel.descripcion}</p>
          </article>
        ))}
      </div>

      <p className="niveles-kinti-cierre">
        ¡Suma puntos con tu comunidad, desbloquea insignias por talentos y demuestra cómo tu
        territorio cuida la salud natural!
      </p>
    </section>
  )
}
