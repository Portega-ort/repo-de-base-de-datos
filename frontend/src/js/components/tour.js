const CLAVE_VISTO = 'habit-tracker:tour-visto';

export function tourVisto() {
  return localStorage.getItem(CLAVE_VISTO) === '1';
}

export function marcarTourVisto() {
  localStorage.setItem(CLAVE_VISTO, '1');
}

export function iniciarTour({ esAdmin, tieneHabitos, cambiarVista }) {
  const pasos = construirPasos(esAdmin, tieneHabitos).filter(p => !p.soloSi || p.soloSi());
  if (pasos.length === 0) return;

  const marco = document.createElement('div');
  marco.className = 'tour-ovl';
  marco.innerHTML = `
    <div class="tour-highlight" aria-hidden="true"></div>
    <div class="tour-card" role="dialog" aria-modal="true" aria-label="Guía de Habit Tracker">
      <p class="tour-metadata"></p>
      <h3 class="tour-titulo"></h3>
      <p class="tour-texto"></p>
      <div class="tour-acciones">
        <button type="button" data-tour="skip" class="quiet">Omitir</button>
        <button type="button" data-tour="prev" class="quiet">Atrás</button>
        <button type="button" data-tour="next" class="primary">Siguiente</button>
      </div>
    </div>`;
  document.body.appendChild(marco);

  let indice = 0;
  let activo = null;

  function esperarElemento(selector, tiempo) {
    return new Promise(resolve => {
      if (tiempo <= 0) {
        resolve(document.querySelector(selector));
        return;
      }
      const existente = document.querySelector(selector);
      if (existente) {
        resolve(existente);
        return;
      }
      const inicio = Date.now();
      const id = setInterval(() => {
        const nodo = document.querySelector(selector);
        if (nodo || Date.now() - inicio > tiempo) {
          clearInterval(id);
          resolve(nodo ?? null);
        }
      }, 120);
    });
  }

  async function mostrar(i) {
    indice = i;
    const paso = pasos[i];
    const card = marco.querySelector('.tour-card');
    const titulo = marco.querySelector('.tour-titulo');
    const texto = marco.querySelector('.tour-texto');
    const meta = marco.querySelector('.tour-metadata');
    const btnNext = marco.querySelector('[data-tour="next"]');
    const btnPrev = marco.querySelector('[data-tour="prev"]');

    if (paso.vista) cambiarVista(paso.vista);

    const objetivo = await esperarElemento(paso.selector, 2500);
    if (!objetivo) {
      if (indice + 1 < pasos.length) mostrar(indice + 1);
      else cerrar(false);
      return;
    }

    activo = objetivo;
    titulo.textContent = paso.titulo;
    texto.textContent = paso.texto;
    meta.textContent = `Paso ${indice + 1} de ${pasos.length}`;
    btnPrev.disabled = indice === 0;
    btnNext.textContent = indice === pasos.length - 1 ? 'Terminar' : 'Siguiente';
    posicionar(objetivo, card, marco.querySelector('.tour-highlight'));
  }

  function posicionar(objetivo, card, highlight) {
    objetivo.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    const rect = objetivo.getBoundingClientRect();
    const pad = 6;
    highlight.style.width = `${rect.width + pad * 2}px`;
    highlight.style.height = `${rect.height + pad * 2}px`;
    highlight.style.left = `${rect.left - pad}px`;
    highlight.style.top = `${rect.top - pad}px`;

    const anchoTarjeta = Math.min(340, window.innerWidth - 24);
    let izquierda = Math.max(12, Math.min(rect.left, window.innerWidth - anchoTarjeta - 12));
    let arriba = rect.bottom + 12;
    const altoEstimado = Math.min(250, card.offsetHeight || 210);
    if (arriba + altoEstimado > window.innerHeight - 12) {
      arriba = Math.max(12, rect.top - altoEstimado - 12);
    }
    card.style.position = 'fixed';
    card.style.width = `${anchoTarjeta}px`;
    card.style.left = `${izquierda}px`;
    card.style.top = `${arriba}px`;
  }

  function adelante() {
    if (indice < pasos.length - 1) mostrar(indice + 1);
    else cerrar(true);
  }

  function atras() {
    if (indice > 0) mostrar(indice - 1);
  }

  function cerrar(completado) {
    window.removeEventListener('resize', reencuadrar);
    document.removeEventListener('keydown', tecla);
    marco.remove();
    if (completado) marcarTourVisto();
  }

  const reencuadrar = () => {
    if (activo) posicionar(activo, marco.querySelector('.tour-card'), marco.querySelector('.tour-highlight'));
  };
  const tecla = evento => {
    if (evento.key === 'Escape') cerrar(false);
  };
  window.addEventListener('resize', reencuadrar);
  document.addEventListener('keydown', tecla);

  marco.querySelector('[data-tour="skip"]').addEventListener('click', () => cerrar(true));
  marco.querySelector('[data-tour="prev"]').addEventListener('click', atras);
  marco.querySelector('[data-tour="next"]').addEventListener('click', adelante);
  marco.addEventListener('click', evento => {
    if (evento.target === marco) cerrar(false);
  });

  mostrar(0);
}

function construirPasos(esAdmin, tieneHabitos) {
  return [
    {
      selector: '.brand',
      vista: 'habitos',
      titulo: 'Bienvenido a Habit Tracker',
      texto: 'Esta es la barra superior. Ahí está el logo, la navegación principal, tu nombre y el botón para salir.'
    },
    {
      selector: '[data-view="habitos"]',
      titulo: 'Hábitos',
      texto: 'En «Hábitos» ves y gestionas todos tus hábitos: crearlos, registrar avances y eliminarlos.'
    },
    {
      selector: '#nuevo-habito',
      titulo: 'Crear un hábito',
      texto: 'Presiona «Nuevo hábito» para crear uno. Elige una categoría para ver sugerencias y define una meta diaria (booleana o con cantidad y unidad).'
    },
    {
      selector: '.habit-card',
      vista: 'habitos',
      titulo: 'Tu tarjeta de hábito',
      soloSi: () => tieneHabitos,
      texto: 'Cada hábito tiene su card con la meta diaria. Usa «Registrar avance» para marcarlo como cumplido y «Eliminar» para quitarlo.'
    },
    {
      selector: '[data-view="progreso"]',
      vista: 'progreso',
      titulo: 'Progreso',
      texto: 'Aquí ves tus rachas: racha actual, mejor racha y el porcentaje de la semana. Cada hábito muestra los últimos 7 días de un vistazo.'
    },
    {
      selector: '#user-name',
      titulo: 'Tu perfil',
      vista: 'progreso',
      texto: 'Haz clic en tu nombre para abrir tu perfil y editar tus datos o cambiar tu contraseña.'
    },
    {
      selector: '#nav-admin',
      vista: 'admin',
      titulo: 'Panel de administración',
      soloSi: () => esAdmin,
      texto: 'Como administrador puedes ver el resumen global: usuarios, actividad de los últimos 14 días, hábitos por categoría y el cumplimiento por usuario.'
    }
  ];
}