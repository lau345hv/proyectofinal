import { Injectable } from '@angular/core';

/**
 * Servicio que gestiona el tema visual de la aplicación (claro / oscuro).
 *
 * Al inicializarse lee la preferencia guardada en `localStorage`. Si no
 * existe ninguna, usa la preferencia del sistema operativo mediante
 * `window.matchMedia`. El tema activo se aplica añadiendo o quitando la
 * clase CSS `dark-mode` del `<body>`.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {

    /** Clave usada para persistir la preferencia de tema en localStorage. */
    private readonly STORAGE_KEY = 'superconvert-theme';

    /** Estado interno del tema: `true` = oscuro, `false` = claro. */
    private oscuro: boolean = false;

    /**
     * Inicializa el servicio leyendo la preferencia guardada o detectando
     * la del sistema operativo, y aplica el tema correspondiente al DOM.
     */
    constructor() {
        const guardado = localStorage.getItem(this.STORAGE_KEY);
        if (guardado !== null) {
            this.oscuro = guardado === 'dark';
        } else {
            this.oscuro = window.matchMedia('(prefers-color-scheme: dark)').matches;
        }
        this.aplicarTema();
    }

    /**
     * Indica si el tema oscuro está activo.
     *
     * @returns `true` si el tema actual es oscuro, `false` si es claro.
     */
    isDark(): boolean {
        return this.oscuro;
    }

    /**
     * Alterna entre el tema claro y el oscuro, persiste la preferencia en
     * `localStorage` y actualiza la clase CSS del `<body>`.
     */
    toggle(): void {
        this.oscuro = !this.oscuro;
        localStorage.setItem(this.STORAGE_KEY, this.oscuro ? 'dark' : 'light');
        this.aplicarTema();
    }

    /**
     * Aplica el tema activo al `<body>` añadiendo o quitando la clase
     * `dark-mode`.
     */
    private aplicarTema(): void {
        if (this.oscuro) {
            document.body.classList.add('dark-mode');
        } else {
            document.body.classList.remove('dark-mode');
        }
    }
}