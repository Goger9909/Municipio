document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('form-seguimiento');
    const codigoInput = document.getElementById('codigo-seguimiento');
    const estado = document.getElementById('estado-consulta');
    const resultado = document.getElementById('resultado-seguimiento');

    if (!form || !codigoInput || !estado || !resultado) return;

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const codigo = codigoInput.value.trim();
        if (!codigo) return;

        estado.textContent = 'Buscando el reclamo...';
        estado.classList.remove('text-danger', 'text-success');
        resultado.hidden = true;
        resultado.replaceChildren();

        try {
            const response = await fetch(resolverApiUrl(
                `/api/reclamos/seguimiento/${encodeURIComponent(codigo)}`
            ), {
                headers: { Accept: 'application/json' }
            });
            const body = await response.json();
            if (!response.ok) {
                throw new Error(body.detail || body.mensaje || `Error ${response.status}`);
            }

            mostrarResultado(body);
            estado.textContent = 'Se encontró el reclamo.';
            estado.classList.add('text-success');
        } catch (error) {
            console.error('No se pudo consultar el seguimiento del reclamo.', error);
            estado.textContent = error instanceof Error
                ? error.message
                : 'No se pudo consultar el reclamo. Intentá nuevamente.';
            estado.classList.add('text-danger');
        }
    });

    function mostrarResultado(reclamo) {
        const titulo = document.createElement('h2');
        titulo.className = 'h5';
        titulo.textContent = `Reclamo ${reclamo.codigoSeguimiento}`;

        const categoria = document.createElement('p');
        categoria.className = 'mb-1';
        categoria.textContent = `Categoría: ${reclamo.categoria}`;

        const estadoReclamo = document.createElement('p');
        estadoReclamo.className = 'mb-1';
        estadoReclamo.textContent = `Estado: ${etiquetaEstado(reclamo.estado)}`;

        const fecha = document.createElement('p');
        fecha.className = 'mb-0';
        fecha.textContent = `Fecha de registro: ${new Date(reclamo.fechaCreacion).toLocaleString('es-AR')}`;

        resultado.replaceChildren(titulo, categoria, estadoReclamo, fecha);
        resultado.hidden = false;
    }

    function etiquetaEstado(valor) {
        return ({
            PENDIENTE: 'Pendiente',
            EN_PROCESO: 'En proceso',
            RESUELTO: 'Resuelto'
        })[valor] || valor;
    }

    function resolverApiUrl(endpoint) {
        const configuredBaseUrl = document.querySelector('meta[name="municipio-api-base-url"]')
            ?.content.trim();
        const baseUrl = configuredBaseUrl || (
            ['localhost', '127.0.0.1'].includes(window.location.hostname)
                ? `${window.location.protocol}//${window.location.hostname}:8080`
                : window.location.origin
        );
        return new URL(endpoint, `${baseUrl.replace(/\/+$/, '')}/`).toString();
    }
});
